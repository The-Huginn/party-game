package com.thehuginn.services.hidden;

import com.thehuginn.GameSession;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class GameTaskService {

    // TODO update this in correspondence with GameSession#start
    public static Uni<Void> gameTasks(Collection<Task> allTasks, ResolutionContext resolutionContext)
            throws CloneNotSupportedException {
        return new GameTaskService().generateGameTasks(allTasks, resolutionContext);
    }

    /**
     * Change with caution from Uni, such as internally we use shallow-copy,
     * and we persist all objects right away
     */
    @WithTransaction
    public Uni<Void> generateGameTasks(Collection<Task> allTasks, ResolutionContext resolutionContext)
            throws CloneNotSupportedException {
        List<GameTask> createdTasks = new ArrayList<>();
        Set<Task> tasks = new HashSet<>(allTasks);
        for (var task : tasks) {
            createdTasks.addAll(task.resolve(resolutionContext));
        }

        Collections.shuffle(createdTasks);
        return GameSession.<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                Parameters.with("id", resolutionContext.getGameId())).firstResult()
                .<GameSession> chain(gameSession -> {
                    gameSession.tasks.clear();
                    gameSession.tasks.addAll(createdTasks);
                    return gameSession.persist();
                })
                .chain(gameSession -> {
                    if (createdTasks.isEmpty()) {
                        return Uni.createFrom().voidItem();
                    }
                    return Uni.combine()
                            .all()
                            .unis(createdTasks.stream()
                                    .peek(gameTask -> gameTask.game = gameSession)
                                    .map(gameTask -> gameTask.persist())
                                    .collect(Collectors.toList()))
                            .usingConcurrencyOf(1)
                            .discardItems();
                });
    }
}
