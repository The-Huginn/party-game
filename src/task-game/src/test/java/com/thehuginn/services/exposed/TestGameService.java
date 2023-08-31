package com.thehuginn.services.exposed;

import com.thehuginn.AbstractTest;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.services.hidden.GameTaskService;
import com.thehuginn.services.hidden.TaskService;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.hibernate.AssertionFailure;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestHTTPEndpoint(GameService.class)
public class TestGameService extends AbstractTest {

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

    @Inject
    TaskService taskService;

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        super.setup(asserter);
    }

    @Test
    @Order(1)
    void testCreatingGameSession(UniAsserter asserter) {
        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .when()
                        .post()
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("gameId", is(GAME),
                                "categories.size()", is(0)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    void testStartingEmptyGame(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.NO_CONTENT));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    void testStartingGameWithOneTask(UniAsserter asserter) {
        asserter.execute(() -> new Task.Builder("simple task")
                .repeat(Task.Repeat.NEVER)
                .type(Task.Type.ALL)
                .build()
                .<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task", task)));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(), is("simple task"),
                                "data.locale", is("en")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(4)
    void testStartingGameWithOneTaskWithRandomPlayer(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_1}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task", task)));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(),
                                anyOf(is("simple task for " + PLAYERS.get(1)), is("simple task for " + PLAYERS.get(2))),
                                "data.locale", is("en")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(5)
    void testStartingGameWithOneTaskWithOneTimer(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {timer_30}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task", task)));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(), is("simple task for 30s"),
                                "data.locale", is("en")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(6)
    void testStartingGameWithOneTaskWithCurrentPlayerRandomPlayerOneTimer(UniAsserter asserter) {
        String task = "%s has to laugh with %s for %s";
        asserter.execute(() -> taskService.createTask(new Task.Builder(task.formatted("{player_c}", "{player_1}", "{timer_42}"))
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task1 -> asserter.putData("task", task1)));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(),
                                anyOf(is(task.formatted(PLAYER, PLAYERS.get(1), "42s")), is(task.formatted(PLAYER, PLAYERS.get(2), "42s"))),
                                "data.locale", is("en")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(7)
    void testStartingGameWithOneTaskWithCurrentPlayerAllRandomPlayer(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("{player_c}{player_1}{player_2}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task1 -> asserter.putData("task", task1)));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(),
                                anyOf(is(PLAYER + PLAYERS.get(1) + PLAYERS.get(2)), is(PLAYER + PLAYERS.get(2) + PLAYERS.get(1))),
                                "data.locale", is("en")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(8)
    void testStartingGameWithOneTaskWithCurrentPlayerAllRandomPlayerTranslated(UniAsserter asserter) {
        String task = "%s sa musí s hráčom %s smiať %s";
        asserter.execute(() -> taskService.createTask(new Task.Builder("{player_c} has to laugh with {player_1} for {timer_42}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task1 -> asserter.putData("task", task1)));
        asserter.execute(() -> taskService.createLocale(((Task) asserter.getData("task")).id, "sk",
                task.formatted("{player_c}", "{player_1}", "{timer_42}")));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "sk").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(),
                                anyOf(is(task.formatted(PLAYER, PLAYERS.get(1), "42s")), is(task.formatted(PLAYER, PLAYERS.get(2), "42s"))),
                                "data.locale", is("sk")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(9)
    void testGetCurrentTaskTwice(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_1}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task", task)));
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() ->
                asserter.putData("resolvedTask", given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .asPrettyString()));

        asserter.execute(() -> Assertions.assertEquals(asserter.getData("resolvedTask"),
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .asPrettyString()));

        asserter.execute(() -> Assertions.assertEquals(asserter.getData("resolvedTask"),
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .asPrettyString()));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(10)
    void testDeleteCurrentTask(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_c}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task1", task)));
        asserter.execute(() -> taskService.createTask(new Task.Builder("{player_c} with a simple task")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task2", task)));
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            asserter.putData("tasks", tasks);
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> {
            String task = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .put("/task/next")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asString();
            //noinspection unchecked
            List<Task> tasks = (List<Task>) asserter.getData("tasks");
            if (task.contains("simple task for")) {
                tasks.remove((Task) asserter.getData("task1"));
                asserter.putData("other", "task_" + ((Task) asserter.getData("task2")).id);
            } else {
                tasks.remove((Task) asserter.getData("task2"));
                asserter.putData("other", "task_" + ((Task) asserter.getData("task1")).id);
            }
            asserter.putData("tasks", tasks);
        });

        asserter.execute(() -> {
            String task = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .put("/task/next")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asString();
            Assertions.assertTrue(task.contains((String) asserter.getData("other")));
        });

        asserter.assertThat(() -> GameTask.count("game = " + GAME), aLong -> Assertions.assertEquals(1, aLong));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(11)
    void testGetCurrentTask(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_c}")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task1", task)));
        asserter.execute(() -> taskService.createTask(new Task.Builder("{player_c} with a simple task")
                        .repeat(Task.Repeat.NEVER)
                        .type(Task.Type.ALL)
                        .build())
                .onItem()
                .invoke(task -> asserter.putData("task2", task)));
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> {
            String task = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .put("/task/next")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asString();
            asserter.putData("task", task);
        });

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .get("/task/current")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is(asserter.getData("task")));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(12)
    void testRepeatingTaskNotRemoved(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_1}")
                        .repeat(Task.Repeat.ALWAYS)
                        .type(Task.Type.SINGLE)
                        .build())
                .invoke(task -> asserter.putData("task1", task)));
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_1}")
                        .repeat(Task.Repeat.ALWAYS)
                        .type(Task.Type.SINGLE)
                        .build())
                .invoke(task -> asserter.putData("task2", task)));
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> {
            String task = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .put("/task/next")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asString();

            int count = 0;
            while (count++ < 10) {
                String nextTask = given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .put("/task/next")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .asString();
                if (!task.equals(nextTask)) {
                    return;
                }
            }
            throw new AssertionFailure("Expected different resolution at least once. Rerunning the test might help");
        });

        asserter.assertThat(() -> GameTask.count("game = " + GAME), aLong -> Assertions.assertEquals(2, aLong));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(13)
    void testCurrentPlayerGetsUpdatedAndCycles(UniAsserter asserter) {
        String task = "simple task for %s";
        asserter.execute(() -> taskService.createTask(new Task.Builder(task.formatted("{player_c}"))
                        .repeat(Task.Repeat.ALWAYS)
                        .type(Task.Type.SINGLE)
                        .build())
                .invoke(task1 -> asserter.putData("task1", task1)));
        asserter.execute(() -> taskService.createTask(new Task.Builder(task.formatted("{player_c}"))
                        .repeat(Task.Repeat.ALWAYS)
                        .type(Task.Type.SINGLE)
                        .build())
                .invoke(task1 -> asserter.putData("task2", task1)));
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> {
            for (int i = 0; i <= 2 * PLAYERS.size(); i++) {
                String result = given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .put("/task/next")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .asString();
                Assertions.assertTrue(result.contains(task.formatted(PLAYERS.get(i % PLAYERS.size()))));
            }
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}