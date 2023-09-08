package com.thehuginn.token;

import com.thehuginn.AbstractResolutionTaskTest;
import com.thehuginn.services.exposed.GameService;
import com.thehuginn.task.Task;
import com.thehuginn.token.resolved.PairsResolvedToken;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
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
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestHTTPEndpoint(GameService.class)
public class TestTaskResolution extends AbstractResolutionTaskTest {

    @Test
    @Order(1)
    void testPairsResolvedTaskWith3Players(UniAsserter asserter) {
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for pairs.")
                .repeat(Task.Repeat.NEVER)
                .type(Task.Type.DUO)
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

        asserter.execute(() -> {
            PairsResolvedToken.Pair pair = given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .get("/task/current")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("data.pairs.size()", is(1))
                    .extract()
                    .jsonPath()
                    .getObject("data.pairs[0]", PairsResolvedToken.Pair.class);
            List<String> players = new ArrayList<>(PLAYERS);
            Assertions.assertTrue(players.contains(pair.first));
            players.remove(pair.first);
            Assertions.assertEquals(2, players.size());
            Assertions.assertTrue(players.contains(pair.second));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(1)
    void testPairsResolvedTaskWith6Players(UniAsserter asserter) {
        PLAYERS.addAll(List.of("player4", "player5", "player6"));
        asserter.execute(() -> taskService.createTask(new Task.Builder("simple task for pairs.")
                .repeat(Task.Repeat.NEVER)
                .type(Task.Type.DUO)
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

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data.pairs.size()", is(3)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
