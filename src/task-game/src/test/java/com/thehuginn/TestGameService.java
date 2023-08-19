package com.thehuginn;

import com.thehuginn.resolution.GameSession;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.services.GameTaskService;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
                        .post("/game")
                .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("gameId", is(GAME),
                                "categories.size()", is(0)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    void testGettingGameSession(UniAsserter asserter) {
        asserter.execute(() -> {
            GameSession gameSession = new GameSession();
            gameSession.gameId = GAME;
            return gameSession.persistAndFlush();
        });

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .when()
                        .get("/game")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("gameId", is(GAME),
                                "categories.size()", is(0)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(5)
    void testStartingEmptyGame(UniAsserter asserter) {
        asserter.execute(() -> {
            GameSession gameSession = new GameSession();
            gameSession.gameId = GAME;
            return gameSession.persistAndFlush();
        });

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .get("game/task/current")
                        .then()
                        .statusCode(RestResponse.StatusCode.NO_CONTENT));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(6)
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
                return gameTaskService.generateGameTasks(
                        GAME,
                        tasks,
                        resolutionContext);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        asserter.execute(() -> {
            GameSession gameSession = new GameSession();
            gameSession.gameId = GAME;
            return gameSession.persistAndFlush();
        });

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                .when()
                        .get("game/task/current")
                .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .body("data." + ((Task) asserter.getData("task")).getKey(), is("simple task"),
                                "data.locale", is("en")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
