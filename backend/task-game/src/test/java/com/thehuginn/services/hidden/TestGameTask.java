package com.thehuginn.services.hidden;

import com.thehuginn.AbstractTest;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RunOnVertxContext
public class TestGameTask extends AbstractTest {

    private static final String GAME = "game";
    private static final String PLAYER = "player1";
    private static final List<String> PLAYERS = List.of(PLAYER, "player2", "player3");
    private static final String LOCALE = "en";
    private static final ResolutionContext resolutionContext = ResolutionContext.builder(GAME)
            .player(PLAYER)
            .players(PLAYERS)
            .locale(LOCALE).build();

    @Inject
    GameTaskService gameTaskService;

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        super.setup(asserter);
    }

    @Test
    @Order(1)
    @RunOnVertxContext
    void testCreateFromEmptyList(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            try {
                return new GameTaskService().generateGameTasks(Collections.emptyList(), resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    void testCreatedSingleGameTask(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask("task1")
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task)));

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of(
                    (Task) asserter.getData("task1"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.assertThat(() -> GameTask.count("game.gameId = :game", Parameters.with("game", GAME)),
                aLong -> Assertions.assertEquals(aLong, 9L));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    void testCreatedDifferentGameTasks(UniAsserter asserter) {
        asserter.execute(() -> new Task.Builder("simple task")
                .repeat(Task.Repeat.NEVER)
                .type(Task.Type.ALL)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task)));
        asserter.execute(() -> new Task.Builder("simple task per player")
                .repeat(Task.Repeat.PER_PLAYER)
                .type(Task.Type.ALL)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task)));
        asserter.execute(() -> new Task.Builder("simple task")
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.DUO)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task3", task)));
        asserter.execute(() -> new Task.Builder("simple per player task with higher frequency.")
                .repeat(Task.Repeat.PER_PLAYER)
                .type(Task.Type.SINGLE)
                .frequency((short) 2)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task4", task)));

        List<Task> tasks = new ArrayList<>();
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            tasks.add((Task) asserter.getData("task1"));
            tasks.add((Task) asserter.getData("task2"));
            tasks.add((Task) asserter.getData("task3"));
            tasks.add((Task) asserter.getData("task4"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.assertThat(() -> GameTask.<GameTask> find("game.id = :game", Parameters.with("game", GAME))
                .list(), gameTasks -> {
                    Assertions.assertEquals(gameTasks.size(), 11);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(0)))
                            .count(), 1);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(1)))
                            .count(), 3);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(2)))
                            .count(), 1);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(3)))
                            .count(), 6);
                    List<GameTask> task4 = gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(3)))
                            .toList();
                    Assertions.assertEquals(task4.size(), 6);
                    Assertions.assertEquals(task4.stream()
                            .filter(gameTask -> gameTask.assignedPlayer.equals(PLAYERS.get(0)))
                            .count(), 2);
                    Assertions.assertEquals(task4.stream()
                            .filter(gameTask -> gameTask.assignedPlayer.equals(PLAYERS.get(1)))
                            .count(), 2);
                    Assertions.assertEquals(task4.stream()
                            .filter(gameTask -> gameTask.assignedPlayer.equals(PLAYERS.get(2)))
                            .count(), 2);
                });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    void testDeleteAndCreateNewGameTasks(UniAsserter asserter) {
        asserter.execute(() -> new Task.Builder("simple task")
                .repeat(Task.Repeat.NEVER)
                .type(Task.Type.ALL)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task)));
        asserter.execute(() -> new Task.Builder("simple task per player")
                .repeat(Task.Repeat.PER_PLAYER)
                .type(Task.Type.ALL)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task)));
        asserter.execute(() -> new Task.Builder("simple task")
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.DUO)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task3", task)));
        asserter.execute(() -> new Task.Builder("simple duplicate task")
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.DUO)
                .build()
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task4", task)));

        List<Task> tasks = new ArrayList<>();
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            tasks.add((Task) asserter.getData("task1"));
            tasks.add((Task) asserter.getData("task2"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> {
            tasks.clear();
            tasks.add((Task) asserter.getData("task3"));
            tasks.add((Task) asserter.getData("task4"));
            tasks.add((Task) asserter.getData("task2"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.assertThat(() -> GameTask.<GameTask> find("game.id = :game", Parameters.with("game", GAME))
                .list(), gameTasks -> {
                    Assertions.assertEquals(gameTasks.size(), 5);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(0)))
                            .count(), 1);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(1)))
                            .count(), 1);
                    Assertions.assertEquals(gameTasks.stream()
                            .filter(gameTask -> gameTask.unresolvedTask.equals(tasks.get(2)))
                            .count(), 3);
                });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
