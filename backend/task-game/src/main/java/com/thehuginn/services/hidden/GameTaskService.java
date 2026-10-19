package com.thehuginn.services.hidden;

import com.thehuginn.GameSession;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequestScoped
public class GameTaskService {

    private final Random random = new Random();

    // TODO update this in correspondence with GameSession#start
    public static Uni<Void> gameTasks(Collection<Task> allTasks, ResolutionContext resolutionContext)
            throws CloneNotSupportedException {
        return new GameTaskService().generateGameTasks(allTasks, resolutionContext);
    }

    public static Uni<Void> updatePlayers(GameSession gameSession, long currentTaskId, ResolutionContext resolutionContext) {
        return new GameTaskService().regenerateGameTasks(gameSession, currentTaskId, resolutionContext);
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
            if (!task.isResolvable(resolutionContext)) {
                continue;
            }

            if (task.repeat.equals(Task.Repeat.PER_PLAYER)) {
                List<GameTask> tasks1 = task.resolve(resolutionContext);
                tasks1.forEach(gameTask -> perPlayerTasks.get(gameTask.assignedPlayer).add(gameTask));
            } else {
                createdTasks.addAll(task.resolve(resolutionContext));
            }
        }

        Collections.shuffle(createdTasks);
        addPerPlayerTasks(createdTasks, perPlayerTasks, resolutionContext.getPlayers());

        return GameSession.<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                Parameters.with("id", resolutionContext.getGameId())).firstResult()
                .<GameSession> chain(gameSession -> {
                    gameSession.tasks.clear();
                    gameSession.tasks.addAll(createdTasks);
                    gameSession.players = new ArrayList<>(players);
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

    /**
     * Rebuilds the remaining tasks of a running game after its players have changed.
     * Non {@link Task.Repeat#PER_PLAYER} tasks keep their order, while {@link Task.Repeat#PER_PLAYER}
     * tasks of removed players are dropped and new players receive as many of each
     * {@link Task.Repeat#PER_PLAYER} task as the other players still have left to play.
     * Afterward, all {@link Task.Repeat#PER_PLAYER} tasks are again spread between the rest of the tasks.
     * All rebuilt tasks are persisted as new {@link GameTask}s so their ids follow the current task.
     *
     * @param gameSession game session with fetched tasks
     * @param currentTaskId id of the currently played {@link GameTask}, which is kept as is
     * @param resolutionContext context with the new players
     */
    public Uni<Void> regenerateGameTasks(GameSession gameSession, long currentTaskId, ResolutionContext resolutionContext) {
        List<String> players = resolutionContext.getPlayers();
        List<String> previousPlayers = gameSession.players.isEmpty() ? players : gameSession.players;
        if (previousPlayers.equals(players)) {
            return Uni.createFrom().voidItem();
        }
        Log.infof("Players of game %s changed from %s to %s", gameSession.gameId, previousPlayers, players);

        List<GameTask> remainingTasks = gameSession.tasks.stream()
                .filter(gameTask -> !Objects.equals(gameTask.id, currentTaskId))
                .sorted(Comparator.comparingLong(gameTask -> gameTask.id))
                .toList();

        List<GameTask> createdTasks = new ArrayList<>();
        // how many of each PER_PLAYER task (by id) does any previous player still have to play
        Map<Long, Integer> perPlayerTaskCount = new TreeMap<>();
        Map<Long, Task> perPlayerTaskById = new HashMap<>();
        Map<String, Map<Long, Integer>> previousPerPlayerTaskCount = new HashMap<>();
        Map<String, List<GameTask>> perPlayerTasks = new HashMap<>();
        for (String player : players) {
            perPlayerTasks.put(player, new ArrayList<>());
        }
        for (GameTask gameTask : remainingTasks) {
            if (gameTask.assignedPlayer == null) {
                createdTasks.add(copyOf(gameTask, null));
                continue;
            }

            Long taskId = gameTask.unresolvedTask.id;
            int count = previousPerPlayerTaskCount.computeIfAbsent(gameTask.assignedPlayer, player -> new HashMap<>())
                    .merge(taskId, 1, Integer::sum);
            perPlayerTaskCount.merge(taskId, count, Math::max);
            perPlayerTaskById.put(taskId, gameTask.unresolvedTask);
            // tasks of removed players are dropped
            if (perPlayerTasks.containsKey(gameTask.assignedPlayer)) {
                perPlayerTasks.get(gameTask.assignedPlayer).add(copyOf(gameTask, gameTask.assignedPlayer));
            }
        }

        for (String player : players) {
            if (previousPlayers.contains(player)) {
                continue;
            }
            perPlayerTaskCount.forEach((taskId, count) -> {
                for (int i = 0; i < count; i++) {
                    perPlayerTasks.get(player).add(copyOf(perPlayerTaskById.get(taskId), player));
                }
            });
        }

        // the first rebuilt task will be played by the current player
        List<String> orderedPlayers = new ArrayList<>(players);
        int currentPlayerIndex = players.indexOf(resolutionContext.getPlayer());
        Collections.rotate(orderedPlayers, -Math.max(currentPlayerIndex, 0));
        addPerPlayerTasks(createdTasks, perPlayerTasks, orderedPlayers);

        gameSession.tasks.removeAll(remainingTasks);
        gameSession.tasks.addAll(createdTasks);
        gameSession.players = new ArrayList<>(players);
        if (createdTasks.isEmpty()) {
            return gameSession.persist()
                    .chain(() -> Panache.flush());
        }
        return Uni.combine()
                .all()
                .unis(createdTasks.stream()
                        .peek(gameTask -> gameTask.game = gameSession)
                        .map(gameTask -> gameTask.persist())
                        .collect(Collectors.toList()))
                .usingConcurrencyOf(1)
                .discardItems()
                .chain(() -> gameSession.persist())
                .chain(() -> Panache.flush());
    }

    private static GameTask copyOf(GameTask gameTask, String assignedPlayer) {
        return copyOf(gameTask.unresolvedTask, assignedPlayer);
    }

    private static GameTask copyOf(Task task, String assignedPlayer) {
        GameTask copy = new GameTask();
        copy.unresolvedTask = task;
        copy.assignedPlayer = assignedPlayer;
        return copy;
    }

    /**
     * We cut the List into equal parts and in each part
     * every player will have one random task assigned to him
     * <p>
     * Furthermore, the added tasks might not have similar spread between them
     * resulting in more random order of tasks. However, the order
     * of players is critical and skipping some tasks might result in
     * unexpected behavior, i.e. if players change mid-game or one
     * of th tasks has to get skipped
     * Example:
     * Non PER_PLAYER tasks: 103
     * PER_PLAYER tasks: 5
     * players: 4
     * in each 20 tasks we should add 4 PER_PLAYER tasks, for each player one
     * Note we can receive all 4 PER_PLAYER tasks in the beginning
     * followed by 20 Non PER_PLAYER tasks.
     *
     * @param collectTo List to which we add PER_PLAYER tasks
     * @param perPlayerTasks Map of players and tasks for each player
     * @param players Ordered List of players
     * @implNote players might have different amount of tasks assigned, i.e. after players have changed mid-game,
     *           in such case a player without any task left is skipped in the remaining parts
     */
    private void addPerPlayerTasks(List<GameTask> collectTo, Map<String, List<GameTask>> perPlayerTasks, List<String> players) {
        int perPlayerTasksSize = 0;
        for (Map.Entry<String, List<GameTask>> playerTasks : perPlayerTasks.entrySet()) {
            perPlayerTasksSize = Math.max(perPlayerTasksSize, playerTasks.getValue().size());
            Collections.shuffle(playerTasks.getValue());
        }

        if (perPlayerTasksSize != 0) {
            int sublistWithoutSize = collectTo.size() / perPlayerTasksSize;
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
                    List<GameTask> realPlayerTasks = perPlayerTasks.get(realPlayer);
                    if (sublistIndex >= realPlayerTasks.size()) {
                        continue;
                    }
                    GameTask realPerPlayerGameTask = realPlayerTasks.get(sublistIndex);
                    // and finally we add the task to corresponding index
                    indexedPerPlayerTasks.put(currentIndex + inSublistIndex, realPerPlayerGameTask);
                }
                // skipped players might shift the remaining positions beyond the end of the List
                indexedPerPlayerTasks.forEach((index, gameTask) -> collectTo.add(Math.min(index, collectTo.size()), gameTask));
            }
        }
    }
}
