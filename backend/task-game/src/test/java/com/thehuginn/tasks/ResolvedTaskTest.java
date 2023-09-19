package com.thehuginn.tasks;

import com.thehuginn.AbstractResolutionTaskTest;
import com.thehuginn.task.ResolvedTask;
import com.thehuginn.task.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResolvedTaskTest extends AbstractResolutionTaskTest {

    /**
     * This test verifies that the potential bug is not present and a work-around is in place.
     * The bug is documented especially in this <a href="https://github.com/The-Huginn/party-game/issues/39">issue</a>
     * We delete the gameTask and set it to null for currentTask (ResolvedTask). When
     * calling `/current` or `/next` we expect the game to progress and not become stale
     */
    @Test
    @Order(1)
    @RunOnVertxContext
    void testWithNullGameTaskCurrent(UniAsserter asserter) {
        String FIRST = "first";
        String SECOND = "second";
        asserter.execute(() -> (new Task.Builder(FIRST)).build().<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData(FIRST, task)));
        asserter.execute(() -> (new Task.Builder(SECOND)).build().<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData(SECOND, task)));

        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData(FIRST),
                    (Task) asserter.getData(SECOND)));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> {
            String currentTask = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .get("game/task/current")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asPrettyString();

            if (currentTask.contains(FIRST)) {
                asserter.putData("next", SECOND);
            } else {
                asserter.putData("next", FIRST);
            }
        });

        // It's ugly but otherwise if we execute the remove in separate Uni it will not be executed upon our database
        asserter.execute(() -> ResolvedTask.<ResolvedTask> listAll(Sort.by("id").descending()).chain(
                resolvedTasks -> resolvedTasks.get(0).remove())
                .invoke(() -> {
                    String currentTask = given()
                            .cookie(new Cookie.Builder("gameId", GAME).build())
                            .cookie(new Cookie.Builder("locale", "en").build())
                            .queryParam("resolutionContext", resolutionContext)
                            .contentType(MediaType.APPLICATION_JSON)
                            .when()
                            .get("game/task/current")
                            .then()
                            .statusCode(RestResponse.StatusCode.OK)
                            .extract()
                            .asPrettyString();

                    Assertions.assertTrue(currentTask.contains((String) asserter.getData("next")));
                })
                .subscribe().with(x -> {
                }));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    /**
     * This is spin-off of previous test testing route `/next`
     * This test verifies that the potential bug is not present and a work-around is in place.
     * The bug is documented especially in this <a href="https://github.com/The-Huginn/party-game/issues/39">issue</a>
     * We delete the gameTask and set it to null for currentTask (ResolvedTask). When
     * calling `/current` or `/next` we expect the game to progress and not become stale
     */
    @Test
    @Order(2)
    @RunOnVertxContext
    void testWithNullGameTaskNext(UniAsserter asserter) {
        String FIRST = "first";
        String SECOND = "second";
        asserter.execute(() -> (new Task.Builder(FIRST)).build().<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData(FIRST, task)));
        asserter.execute(() -> (new Task.Builder(SECOND)).build().<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData(SECOND, task)));

        asserter.execute(() -> {
            List<Task> tasks = new ArrayList<>(List.of((Task) asserter.getData(FIRST),
                    (Task) asserter.getData(SECOND)));
            try {
                return gameTaskService.generateGameTasks(tasks, resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> {
            String currentTask = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .get("game/task/current")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .asPrettyString();

            if (currentTask.contains(FIRST)) {
                asserter.putData("next", SECOND);
            } else {
                asserter.putData("next", FIRST);
            }
        });

        // It's ugly but otherwise if we execute the remove in separate Uni it will not be executed upon our database
        asserter.execute(() -> ResolvedTask.<ResolvedTask> listAll(Sort.by("id").descending()).chain(
                resolvedTasks -> resolvedTasks.get(0).remove())
                .invoke(() -> {
                    String currentTask = given()
                            .cookie(new Cookie.Builder("gameId", GAME).build())
                            .cookie(new Cookie.Builder("locale", "en").build())
                            .queryParam("resolutionContext", resolutionContext)
                            .contentType(MediaType.APPLICATION_JSON)
                            .when()
                            .get("game/task/next")
                            .then()
                            .statusCode(RestResponse.StatusCode.OK)
                            .extract()
                            .asPrettyString();

                    Assertions.assertTrue(currentTask.contains((String) asserter.getData("next")));
                })
                .subscribe().with(x -> {
                }));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
