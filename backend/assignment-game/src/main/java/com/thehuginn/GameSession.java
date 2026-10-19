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

    public static final String HEADER_TITLE = "header_title";

    public GameType type = GameType.NONE;

    /**
     * Order of the currently played task, starting from 1
     */
    public int taskNumber = 0;

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
                                gameSession.taskNumber = 1;
                                return gameSession.persist()
                                        .replaceWith(Boolean.TRUE);
                            });
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<Map.Entry<String, Map<String, Object>>> currentTask(ResolutionContext.Builder resolutionContextBuilder) {
        return GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                        Parameters.with("id", this.gameId))
                .page(0, 1)
                .firstResult()
                .chain(gameSession -> resolveTask((AbstractTask) gameSession.tasks.get(0), gameSession.taskNumber,
                        resolutionContextBuilder));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<Map.Entry<String, Map<String, Object>>> nextTask(ResolutionContext.Builder resolutionContextBuilder) {
        return GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                        Parameters.with("id", this.gameId))
                .page(0, 1)
                .firstResult().<Map.Entry<String, Map<String, Object>>> chain(gameSession -> {
                    gameSession.tasks.remove(0);
                    gameSession.taskNumber++;
                    Uni<GameSession> persisted = gameSession.persist();
                    if (gameSession.tasks.isEmpty()) {
                        return persisted.map(ignored -> null);
                    }
                    return persisted.chain(() -> resolveTask((AbstractTask) gameSession.tasks.get(0),
                            gameSession.taskNumber, resolutionContextBuilder));
                });
    }

    private static Uni<Map.Entry<String, Map<String, Object>>> resolveTask(AbstractTask abstractTask, int taskNumber,
            ResolutionContext.Builder resolutionContextBuilder) {
        return abstractTask.task.translate(resolutionContextBuilder.build())
                .getValue()
                .map(content -> Map.entry("data", Map.of(
                        "task", abstractTask.getKey(),
                        abstractTask.getKey(), content,
                        HEADER_TITLE, taskNumber)));
    }
}
