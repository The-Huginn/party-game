package com.thehuginn.pub;

import com.thehuginn.AbstractTest;
import com.thehuginn.GameSession;
import com.thehuginn.task.PubTask;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
public class GameServiceTest extends AbstractTest {

    @Override
    protected void additionalSetup(UniAsserter asserter) {
        asserter.execute(() -> new GameSession(GAME, GameSession.GameType.PUB_MODE).persist());
    }

    @Test
    void testCreatingGame(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("/pub")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("gameId", is(GAME),
                        "type", is(GameSession.GameType.PUB_MODE.toString())));

        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }

    @Test
    void testGettingRulesAsFirstTaskWithCurrent(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((PubTask) asserter.getData("task")).getKey(), is("simple task")));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/pub/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((PubTask) asserter.getData("task")).getKey(), is("simple task")));

        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }

    @Test
    void testGettingRulesAsFirstTaskWithNext(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", "newGame").build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is(new GameSession("newGame", GameSession.GameType.PUB_MODE))));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/pub/task/next")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((PubTask) asserter.getData("task")).getKey(), is("simple task")));

        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }
}
