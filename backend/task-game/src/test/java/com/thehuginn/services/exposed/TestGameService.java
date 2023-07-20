package com.thehuginn.services.exposed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehuginn.AbstractResolutionTaskTest;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import com.thehuginn.util.EntityCreator;
import com.thehuginn.util.JsonAsserter;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;
import org.hibernate.AssertionFailure;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static com.thehuginn.util.Helper.UNDERLINED;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestHTTPEndpoint(GameService.class)
public class TestGameService extends AbstractResolutionTaskTest {

    @Test
    @Order(1)
    void testCreatingGameSession(UniAsserter asserter) {
        asserter.execute(() -> given()
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

        asserter.execute(() -> given()
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
                .<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task", task)));

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((Task) asserter.getData("task")).getKey(), is("simple task")));

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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((Task) asserter.getData("task")).getKey(),
                        anyOf(is("simple task for " + UNDERLINED.formatted(PLAYERS.get(1))),
                                is("simple task for " + UNDERLINED.formatted(PLAYERS.get(2))))));

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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((Task) asserter.getData("task")).getKey(), is("simple task for 30s")));

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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((Task) asserter.getData("task")).getKey(),
                        anyOf(is(task.formatted(UNDERLINED.formatted(PLAYER), UNDERLINED.formatted(PLAYERS.get(1)), "42s")),
                                is(task.formatted(UNDERLINED.formatted(PLAYER), UNDERLINED.formatted(PLAYERS.get(2)),
                                        "42s")))));

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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((Task) asserter.getData("task")).getKey(),
                        anyOf(is(UNDERLINED.formatted(PLAYER) + UNDERLINED.formatted(PLAYERS.get(1))
                                + UNDERLINED.formatted(PLAYERS.get(2))),
                                is(UNDERLINED.formatted(PLAYER) + UNDERLINED.formatted(PLAYERS.get(2))
                                        + UNDERLINED.formatted(PLAYERS.get(1))))));

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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "sk").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((Task) asserter.getData("task")).getKey(),
                        anyOf(is(task.formatted(UNDERLINED.formatted(PLAYER), UNDERLINED.formatted(PLAYERS.get(1)), "42s")),
                                is(task.formatted(UNDERLINED.formatted(PLAYER), UNDERLINED.formatted(PLAYERS.get(2)),
                                        "42s")))));

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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = List.of((Task) asserter.getData("task"));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> asserter.putData("resolvedTask", given()
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

        asserter.execute(() -> JsonAsserter.assertEquals(asserter.getData("resolvedTask"),
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

        asserter.execute(() -> JsonAsserter.assertEquals(asserter.getData("resolvedTask"),
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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
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

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
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
            asserter.putData("task", task);
        });

        asserter.execute(() -> {
            JsonAsserter.assertEquals(asserter.getData("task"), given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .get("/task/current")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asPrettyString());
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
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for {player_2}")
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.SINGLE)
                .build())
                .invoke(task -> asserter.putData("task2", task)));

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
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
        asserter.execute(() -> taskService.createTask(new Task.Builder(task.formatted("{player_c}."))
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.SINGLE)
                .build())
                .invoke(task1 -> asserter.putData("task2", task1)));

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

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
                Assertions.assertTrue(result.contains(task.formatted(UNDERLINED.formatted(PLAYERS.get(i % PLAYERS.size())))));
            }
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(14)
    void testCycleTasks(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("First task")
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.SINGLE)
                .build())
                .invoke(task1 -> asserter.putData("task1", task1)));
        asserter.execute(() -> taskService.createTask(new Task.Builder("Second task")
                .repeat(Task.Repeat.ALWAYS)
                .type(Task.Type.SINGLE)
                .build())
                .invoke(task1 -> asserter.putData("task2", task1)));

        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData("task1"),
                    (Task) asserter.getData("task2")));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });

        asserter.execute(() -> {
            ObjectMapper mapper = new ObjectMapper();
            String first = given()
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

            // second task
            String second = given()
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

            for (int i = 0; i < 4; i++) {
                String receivedTask = given()
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
                String expectedTask = i % 2 == 0 ? first : second;
                try {
                    String taskTag = mapper.readTree(expectedTask).get("data").get("task").asText();
                    Assertions.assertEquals(mapper.readTree(expectedTask).get("data").get(taskTag).asText(),
                            mapper.readTree(receivedTask).get("data").get(taskTag).asText());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
