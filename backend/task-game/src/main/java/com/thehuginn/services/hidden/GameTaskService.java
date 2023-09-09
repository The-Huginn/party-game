package com.thehuginn.services.hidden;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
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

        Uni<Long> deletePrevious = GameTask.delete("game", resolutionContext.getGameId())
                .invoke(aLong -> {
                    if (aLong.compareTo(0L) > 0) {
                        Log.infof("Previous game [%s] was deleted with %d tasks remaining",
                                resolutionContext.getGameId(), aLong);
                    }
                });

        if (createdTasks.isEmpty()) {
            return deletePrevious
                    .replaceWithVoid();
        }

        Collections.shuffle(createdTasks);
        Uni<Void> newGameTasks = Uni.combine()
                .all()
                .unis(createdTasks.stream()
                        .map(gameTask -> gameTask.persist())
                        .collect(Collectors.toList()))
                .usingConcurrencyOf(1)
                .discardItems();

        return deletePrevious
                .replaceWith(newGameTasks);
    }
}
