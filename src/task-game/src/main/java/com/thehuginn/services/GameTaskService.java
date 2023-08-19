package com.thehuginn.services;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class GameTaskService {

    /**
     * Change with caution from Uni, such as internally we use shallow-copy,
     *  and we persist all objects right away
     */
    @WithTransaction
    public Uni<Void> generateGameTasks(String game, Collection<Task> allTasks, ResolutionContext resolutionContext) throws CloneNotSupportedException {
        List<GameTask> createdTasks = new ArrayList<>();
        Set<Task> tasks = new HashSet<>(allTasks);
        for (var task : tasks) {
            for (short amount = 0; amount < task.frequency; amount++) {
                GameTask gameTask = new GameTask();
                gameTask.game = game;
                gameTask.unresolvedTask = task;
                if (task.repeat.equals(Task.Repeat.PER_PLAYER)) {
                    for (String player: resolutionContext.getPlayers()) {
                        GameTask shallowCopy = gameTask.clone();
                        shallowCopy.assignedPlayer = player;
                        createdTasks.add(shallowCopy);
                    }
                } else {
                    createdTasks.add(gameTask);
                }
            }
        }

        Uni<Long> deletePrevious = GameTask.delete("game", game )
                .invoke(aLong -> {
                    if (aLong.compareTo(0L) > 0) {
                        Log.infof("Previous game [%s] was deleted with %d tasks remaining");
                    }
                });

        if (createdTasks.isEmpty()) {
            return deletePrevious
                    .replaceWithVoid();
        }

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
