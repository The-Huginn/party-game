package com.thehuginn.pub;

import com.thehuginn.AbstractTest;
import com.thehuginn.GameSession;
import com.thehuginn.task.PubTask;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTest extends AbstractTest {

    @Override
    protected void additionalSetup(UniAsserter asserter) {
//        asserter.execute(() -> new GameSession(GAME, GameSession.GameType.PUB_MODE).persist());
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

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testGettingRulesAsFirstTaskWithCurrent(UniAsserter asserter) {
        asserter.execute(() -> new GameSession(GAME, GameSession.GameType.PUB_MODE).persistAndFlush());
        asserter.execute(this::createPubTasks);
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
                .body(is("true")));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/pub/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(((PubTask) asserter.getData("task")).getKey(), is(((PubTask) asserter.getData("task")).task.content)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testGettingRulesAsFirstTaskWithNext(UniAsserter asserter) {
        asserter.execute(() -> new GameSession(GAME, GameSession.GameType.PUB_MODE).persistAndFlush());
        asserter.execute(this::createPubTasks);
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
                .body(is("true")));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/task/next")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(((PubTask) asserter.getData("task")).getKey(), is(((PubTask) asserter.getData("task")).task.content)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    private Uni<List<PubTask>> createPubTasks() {
        List<Uni<PubTask>> pubTasks = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            pubTasks.add(PubTask.createPubTask(String.valueOf(i), Map.of()));
        }
        //noinspection unchecked
        return Uni.combine().all().unis(pubTasks).usingConcurrencyOf(1)
                .combinedWith(objects -> (List<PubTask>) objects);
    }
}
