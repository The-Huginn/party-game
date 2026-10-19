package com.thehuginn.tasks;

import com.thehuginn.AbstractResolutionTaskTest;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
@RunOnVertxContext
public class TestPlayersChanged extends AbstractResolutionTaskTest {

    private static final String NEW_PLAYER = "player4";
    private static final String PER_PLAYER_TASK = "per player task";

    private void createGame(UniAsserter asserter, short frequency) {
        asserter.execute(() -> new Task.Builder(PER_PLAYER_TASK)
                .repeat(Task.Repeat.PER_PLAYER)
                .frequency(frequency)
                .build()
                .<Task> persistAndFlush()
                .invoke(task -> asserter.putData(PER_PLAYER_TASK, task)));
        for (int i = 0; i < 6; i++) {
            String taskName = "simple task " + i;
            asserter.execute(() -> new Task.Builder(taskName)
                    .repeat(Task.Repeat.NEVER)
                    .build()
                    .<Task> persistAndFlush()
                    .invoke(task -> asserter.putData(taskName, task)));
        }

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>();
            tasks.add((Task) asserter.getData(PER_PLAYER_TASK));
            for (int i = 0; i < 6; i++) {
                tasks.add((Task) asserter.getData("simple task " + i));
            }
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void nextTask(ResolutionContext context) {
        given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", context)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("game/task/next")
                .then()
                .statusCode(RestResponse.StatusCode.OK);
    }

    private static ResolutionContext context(List<String> players, boolean playersChanged) {
        return ResolutionContext.builder(GAME)
                .player(players.get(0))
                .players(players)
                .playersChanged(playersChanged)
                .locale(LOCALE)
                .build();
    }

    private static io.smallrye.mutiny.Uni<Long> perPlayerTasksOf(String player) {
        return GameTask.count("game.id = :game AND assignedPlayer = :player",
                Parameters.with("game", GAME).and("player", player));
    }

    @Test
    void testNewPlayerReceivesPerPlayerTasks(UniAsserter asserter) {
        createGame(asserter, (short) 2);
        asserter.execute(() -> nextTask(resolutionContext));

        List<String> players = new ArrayList<>(PLAYERS);
        players.add(NEW_PLAYER);
        asserter.execute(() -> nextTask(context(players, true)));

        asserter.assertThat(() -> perPlayerTasksOf(NEW_PLAYER), count -> Assertions.assertEquals(2, count));
        // query the database directly, as the test session still holds the original game session
        asserter.assertThat(() -> Panache.getSession()
                .chain(session -> session.createQuery(
                        "SELECT p FROM GameSession g JOIN g.players p WHERE g.id = :id ORDER BY index(p)", String.class)
                        .setParameter("id", GAME)
                        .getResultList()),
                storedPlayers -> Assertions.assertEquals(players, storedPlayers));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testNewPlayerReceivesOnlyRemainingPerPlayerTasks(UniAsserter asserter) {
        createGame(asserter, (short) 2);
        // play one of the PER_PLAYER tasks of every player
        asserter.execute(() -> GameTask.<GameTask> list("game.id = :game AND assignedPlayer IS NOT NULL ORDER BY id",
                Parameters.with("game", GAME))
                .chain(gameTasks -> {
                    List<Long> ids = new ArrayList<>();
                    for (String player : PLAYERS) {
                        gameTasks.stream()
                                .filter(gameTask -> player.equals(gameTask.assignedPlayer))
                                .findFirst()
                                .ifPresent(gameTask -> ids.add(gameTask.id));
                    }
                    return GameTask.delete("id IN :ids", Parameters.with("ids", ids));
                }));

        List<String> players = new ArrayList<>(PLAYERS);
        players.add(NEW_PLAYER);
        asserter.execute(() -> nextTask(context(players, true)));

        asserter.assertThat(() -> perPlayerTasksOf(NEW_PLAYER), count -> Assertions.assertEquals(1, count));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testRemovedPlayerLosesPerPlayerTasks(UniAsserter asserter) {
        createGame(asserter, (short) 1);
        asserter.execute(() -> nextTask(resolutionContext));
        asserter.assertThat(() -> perPlayerTasksOf(PLAYERS.get(2)), count -> Assertions.assertEquals(1, count));

        List<String> players = new ArrayList<>(PLAYERS.subList(0, 2));
        asserter.execute(() -> nextTask(context(players, true)));

        asserter.assertThat(() -> perPlayerTasksOf(PLAYERS.get(2)), count -> Assertions.assertEquals(0, count));
        // non PER_PLAYER tasks are kept, apart from the 2 already played ones
        asserter.assertThat(() -> GameTask.count("game.id = :game AND assignedPlayer IS NULL", Parameters.with("game", GAME)),
                count -> Assertions.assertTrue(count >= 4, "Expected at least 4 remaining tasks, but was " + count));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testPlayersAreKeptWithoutFlag(UniAsserter asserter) {
        createGame(asserter, (short) 1);
        asserter.execute(() -> nextTask(resolutionContext));

        List<String> players = new ArrayList<>(PLAYERS);
        players.add(NEW_PLAYER);
        asserter.execute(() -> nextTask(context(players, false)));

        asserter.assertThat(() -> perPlayerTasksOf(NEW_PLAYER), count -> Assertions.assertEquals(0, count));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testGameContinuesAfterPlayersChanged(UniAsserter asserter) {
        createGame(asserter, (short) 1);
        asserter.execute(() -> nextTask(resolutionContext));

        List<String> players = new ArrayList<>(PLAYERS);
        players.add(NEW_PLAYER);
        asserter.execute(() -> {
            nextTask(context(players, true));
            for (int i = 0; i < 3; i++) {
                nextTask(context(players, false));
            }
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
