package com.thehuginn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.common.game.AbstractGameSession;
import com.thehuginn.common.game.task.AbstractTask;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.task.PubTask;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Entity
public class GameSession extends AbstractGameSession {

    public GameType type = GameType.NONE;

    public enum GameType {
        NONE,
        PUB_MODE,
        NEVER_EVER_MODE
    }

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = AbstractTask.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH })
    @JoinTable(name = "gameSession_tasks", joinColumns = @JoinColumn(name = "gameSession_id", referencedColumnName = "gameId"), inverseJoinColumns = @JoinColumn(name = "task_id", referencedColumnName = "id"))
    public List<? super AbstractTask> tasks = new ArrayList<>();

    public GameSession() {
    }

    public GameSession(String gameId, GameType type) {
        super(gameId);
        this.type = type;
    }

    @Override
    public Uni<Boolean> start(ResolutionContext.Builder resolutionContext) {
        Function<GameSession, Uni<List<? extends AbstractTask>>> resolveTasks = gameSession -> {
            try {
                return switch (gameSession.type) {
                    case PUB_MODE -> PubTask.generateTasks();
                    case NEVER_EVER_MODE, NONE -> Uni.createFrom().item(List.of());
                };
            } catch (IllegalStateException ignored) {
            }
            return Uni.createFrom().item(List.of());
        };

        return GameSession.<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :gameId",
                Parameters.with("gameId", this.gameId))
                .firstResult().chain(gameSession -> {
                    gameSession.tasks.clear();
                    return resolveTasks.apply(gameSession)
                            .chain(abstractTasks -> {
                                if (abstractTasks.isEmpty()) {
                                    return Uni.createFrom().item(Boolean.FALSE);
                                }
                                gameSession.tasks.addAll(abstractTasks);
                                return gameSession.persist()
                                        .replaceWith(Boolean.TRUE);
                            });
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<Map.Entry<String, Map<String, String>>> currentTask(ResolutionContext.Builder resolutionContextBuilder) {
        return GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                        Parameters.with("id", this.gameId))
                .page(0, 1)
                .firstResult()
                .map(gameSession -> (AbstractTask) gameSession.tasks.get(0))
                .chain(abstractTask -> abstractTask.task.translate(resolutionContextBuilder.build())
                        .getValue()
                        .map(content -> Map.entry("data", Map.of(
                                "task", abstractTask.getKey(),
                                abstractTask.getKey(), content))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<Map.Entry<String, Map<String, String>>> nextTask(ResolutionContext.Builder resolutionContextBuilder) {
        return GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                        Parameters.with("id", this.gameId))
                .page(0, 1)
                .firstResult()
                .chain(gameSession -> {
                    gameSession.tasks.remove(0);
                    AbstractTask task = !gameSession.tasks.isEmpty() ? (AbstractTask) gameSession.tasks.get(0) : null;
                    return gameSession.persist()
                            .replaceWith(task);
                })
                .chain(abstractTask -> abstractTask.task.translate(resolutionContextBuilder.build())
                        .getValue()
                        .map(content -> Map.entry("data", Map.of(
                                "task", abstractTask.getKey(),
                                abstractTask.getKey(), content))));
    }
}
