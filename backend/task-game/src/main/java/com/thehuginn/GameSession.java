package com.thehuginn;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thehuginn.category.Category;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.services.hidden.GameTaskService;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.ResolvedTask;
import com.thehuginn.task.Task;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class GameSession extends AbstractGameSession {

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ResolvedTask currentTask;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "GameSession_Category", joinColumns = @JoinColumn(name = "gameSession_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Set<Category> categories = new HashSet<>();

    public String currentPlayer = null;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "game", orphanRemoval = true)
    @JsonIgnore
    public List<GameTask> tasks = new ArrayList<>();

    public GameSession() {
    }

    @Override
    public Uni<Boolean> addCategory(Long categoryId) {
        return Category.<Category> findById(categoryId)
                .chain(category -> {
                    if (category != null) {
                        this.categories.add(category);
                        return this.persist()
                                .replaceWith(Boolean.TRUE);
                    }
                    return Uni.createFrom().item(Boolean.FALSE);
                });
    }

    @Override
    public Uni<Boolean> removeCategory(Long categoryId) {
        return Category.<Category> findById(categoryId)
                .chain(category -> {
                    if (category != null) {
                        Boolean removed = this.categories.remove(category);
                        return this.persist()
                                .replaceWith(removed);
                    }
                    return Uni.createFrom().item(Boolean.FALSE);
                });
    }

    @Override
    public Uni<Boolean> start(ResolutionContext.Builder resolutionContext) {
        if (this.categories.isEmpty()) {
            return Uni.createFrom().item(Boolean.FALSE);
        }

        this.currentPlayer = resolutionContext.getPlayers().get(resolutionContext.getPlayers().size() - 1);
        resolutionContext = resolutionContext.player(this.currentPlayer);

        ResolutionContext context = resolutionContext.build();

        List<Uni<Set<Task>>> tasks = categories.stream()
                .map(category -> Category.getTasks(category.id))
                .toList();
        Uni<Set<Task>> tasksUni = Uni.combine().all()
                .<Set<Task>> unis(tasks)
                .usingConcurrencyOf(1)
                .combinedWith(objects -> objects.stream()
                        .flatMap(collectedTasks -> ((Set<Task>) collectedTasks).stream())
                        .collect(Collectors.toSet()));

        return tasksUni.call(allTasks -> {
            try {
                return GameTaskService.gameTasks(allTasks, context);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        })
                .chain(() -> {
                    this.currentTask = null;
                    return this.persist();
                })
                .replaceWith(Boolean.TRUE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<ResolvedTask> currentTask(ResolutionContext.Builder resolutionContextBuilder) {
        resolutionContextBuilder = resolutionContextBuilder.player(this.currentPlayer);
        ResolutionContext.Builder finalResolutionContextBuilder = resolutionContextBuilder;
        return Uni.createFrom()
                .item(this.currentTask)
                .chain(resolvedTask -> {
                    if (resolvedTask == null || resolvedTask.gameTask == null) {
                        return nextTask(finalResolutionContextBuilder);
                    }

                    return Uni.createFrom().item(resolvedTask);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<ResolvedTask> nextTask(ResolutionContext.Builder resolutionContextBuilder) {
        List<String> players = resolutionContextBuilder.getPlayers();
        if (this.currentPlayer == null) {
            this.currentPlayer = players.get(0);
        } else {
            Iterator<String> iterator = players.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(this.currentPlayer)) {
                    break;
                }
            }
            this.currentPlayer = iterator.hasNext() ? iterator.next() : players.get(0);
        }

        ResolutionContext resolutionContext = resolutionContextBuilder.player(this.currentPlayer).build();

        Function<ResolvedTask, Uni<?>> updateResolvedTask = resolvedTask -> Uni.createFrom().item(this)
                .invoke(gameSession -> {
                    Log.infof("New resolved task to be: %s", resolvedTask.gameTask.unresolvedTask.task.content);
                    if (gameSession.currentTask != null) {
                        gameSession.currentTask.copy(resolvedTask);
                    } else {
                        gameSession.currentTask = resolvedTask;
                    }
                })
                .call(gameSession -> gameSession.persist());

        Uni<Void> deleteCurrentTask = this.currentTask != null
                ? this.currentTask.remove()
                : Uni.createFrom().voidItem();

        Uni<ResolvedTask> nextTask = GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                        Parameters.with("id", gameId))
                .singleResult().chain(gameSession -> {
                    if (gameSession.tasks.isEmpty()) {
                        return Uni.createFrom().failure(new IllegalStateException("No more tasks remain for current game"));
                    }

                    long id = -1;
                    if (gameSession.currentTask != null && gameSession.currentTask.gameTask != null) {
                        id = gameSession.currentTask.gameTask.id;
                    }
                    return nextTaskUni(resolutionContext, id);
                })
                .call(updateResolvedTask)
                .onFailure().recoverWithNull();

        return deleteCurrentTask
                .chain(() -> nextTask);
    }

    private Uni<ResolvedTask> nextTaskUni(ResolutionContext resolutionContext, long id) {
        return GameTask
                .<GameTask> find("game.id = :game AND id > :id", Parameters.with("game", gameId).and("id", id))
                .page(0, 1)
                .firstResult()
                .onItem().ifNull()
                .switchTo(() -> {
                    Log.info("Unable to find next task, getting a \"random\" one");
                    return GameTask.find("game.id = :game AND assignedPlayer is NULL", Parameters.with("game", gameId))
                            .firstResult();
                })
                .chain(gameTask -> {
                    Log.infof("Chosen task to potentially play: %d %s", gameTask.id, gameTask.unresolvedTask.task.content);
                    if (!gameTask.isResolvable(resolutionContext)) {
                        Log.infof("New task is required");
                        return nextTaskUni(resolutionContext, id + 1);
                    }

                    return Uni.createFrom().item(gameTask.resolve(resolutionContext));
                });
    }
}
