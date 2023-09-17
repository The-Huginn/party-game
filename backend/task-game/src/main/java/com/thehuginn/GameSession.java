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
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class GameSession extends PanacheEntityBase {

    @Id
    public String gameId;

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

    public GameSession() {
    }

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

    public Uni<Boolean> start(ResolutionContext.Builder resolutionContext) {
        if (this.categories.isEmpty()) {
            return Uni.createFrom().item(Boolean.FALSE);
        }

        this.currentPlayer = resolutionContext.getPlayers().get(0);
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

    public Uni<ResolvedTask> currentTask(ResolutionContext.Builder resolutionContextBuilder) {
        resolutionContextBuilder = resolutionContextBuilder.player(this.currentPlayer);
        return Uni.createFrom()
                .item(this.currentTask)
                .onItem().ifNull().switchTo(nextTask(resolutionContextBuilder));
    }

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
                    if (gameSession.currentTask != null) {
                        gameSession.currentTask.copy(resolvedTask);
                    } else {
                        gameSession.currentTask = resolvedTask;
                    }
                })
                .call(gameSession -> gameSession.persist());

        Uni<Void> deleteCurrentTask = Uni.createFrom().item(this.currentTask)
                .onItem().ifNotNull().transformToUni(ResolvedTask::remove);

        Uni<ResolvedTask> nextTask = GameTask.count("game = :game", Parameters.with("game", gameId))
                .chain(count -> {
                    if (count.equals(0L)) {
                        return Uni.createFrom().failure(new IllegalStateException("No more tasks remain for current game"));
                    }

                    return nextTaskUni(resolutionContext, count);
                })
                .call(updateResolvedTask)
                .onFailure().recoverWithNull();

        return deleteCurrentTask
                .chain(() -> nextTask);
    }

    private Uni<ResolvedTask> nextTaskUni(ResolutionContext resolutionContext, long count) {
        return GameTask.<GameTask> find("game = :game", Parameters.with("game", gameId))
                .page((int) ((new Random()).nextLong(count)), 1)
                .firstResult()
                .chain(gameTask -> {
                    if (!gameTask.isResolvable(resolutionContext) ||
                            (currentTask != null && currentTask.gameTask.equals(gameTask))) {
                        return nextTaskUni(resolutionContext, count);
                    }

                    return Uni.createFrom().item(gameTask.resolve(resolutionContext));
                });
    }
}
