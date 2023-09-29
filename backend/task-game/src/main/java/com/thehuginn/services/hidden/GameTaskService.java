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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequestScoped
public class GameTaskService {

    private Random random = new Random();

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
        Map<String, List<GameTask>> perPlayerTasks = new HashMap<>();
        List<String> players = resolutionContext.getPlayers();
        for (String player : players) {
            perPlayerTasks.put(player, new ArrayList<>());
        }
        Set<Task> tasks = new HashSet<>(allTasks);
        for (Task task : tasks) {
            if (task.repeat.equals(Task.Repeat.PER_PLAYER)) {
                List<GameTask> tasks1 = task.resolve(resolutionContext);
                tasks1.forEach(gameTask -> perPlayerTasks.get(gameTask.assignedPlayer).add(gameTask));
            } else {
                createdTasks.addAll(task.resolve(resolutionContext));
            }
        }

        Collections.shuffle(createdTasks);
        int perPlayerTasksSize = 0;
        for (Map.Entry<String, List<GameTask>> playerTasks : perPlayerTasks.entrySet()) {
            perPlayerTasksSize = playerTasks.getValue().size();
            Collections.shuffle(playerTasks.getValue());
        }

        // We want to cut the List into equal parts and in each part
        //  every player will have one random task assigned to him
        //  Example:
        //      Non PER_PLAYER tasks: 103   -- createdTasks.size()
        //      PER_PLAYER tasks: 5         -- perPlayerTasksSize
        //      players: 4                  -- resolutionContext.getPlayers().size()
        //      in each 20 tasks we should add 4 PER_PLAYER -- createdTasks.size() / perPlayerTasksSize
        //      tasks for each player one
        //  Similar mechanism is applied inside a single sublist but
        //  additionally we need to guarantee a player will have his turn
        if (perPlayerTasksSize != 0) {
            int sublistWithoutSize = createdTasks.size() / perPlayerTasksSize;
            for (int sublistIndex = 0; sublistIndex < perPlayerTasksSize; sublistIndex++) {
                // size of sublist with non PER_PLAYER tasks with to-be-added PER_PLAYER tasks
                int sublistWithSize = sublistWithoutSize + players.size();
                // start of the sublist with offset for player's turn
                int currentIndex = sublistIndex * sublistWithSize;
                int currentIndexOffset = (players.size() - currentIndex % players.size()) % players.size();

                // Each GameTask has some positions it can position itself to be applied
                //  for assigned player in coherence with current player
                Map<Integer, GameTask> indexedPerPlayerTasks = new TreeMap<>();
                for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
                    // how many possible positions a PER_PLAYER task with the assigned player
                    //  the task has to be in coherence with current player, Note the offset
                    int possiblePositions = sublistWithSize / players.size();
                    if (sublistWithSize % players.size() > (playerIndex + currentIndexOffset) % players.size()) {
                        possiblePositions++;
                    }
                    // index within the sublist accounting for the offset and overflowing (modulo sublistWithSize + 1)
                    int inSublistIndex = (currentIndexOffset + random.nextInt(possiblePositions) * players.size() + playerIndex)
                            % (sublistWithSize + 1);

                    String realPlayer = players.get(playerIndex);
                    GameTask realPerPlayerGameTask = perPlayerTasks.get(realPlayer).get(sublistIndex);
                    // and finally we add the task to corresponding index
                    indexedPerPlayerTasks.put(currentIndex + inSublistIndex, realPerPlayerGameTask);
                }
                indexedPerPlayerTasks.forEach(createdTasks::add);
            }
        }

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
