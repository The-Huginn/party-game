package com.thehuginn.pub;

import com.thehuginn.AbstractTest;
import com.thehuginn.GameSession;
import com.thehuginn.task.PubTask;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTest extends AbstractTest {

    @Override
    protected void additionalSetup(UniAsserter asserter) {
        asserter.execute(() -> new GameSession(GAME, GameSession.GameType.PUB_MODE).persistAndFlush());
        asserter.execute(this::createPubTasks);
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
                .post("/pub/game")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("gameId", is(GAME),
                        "type", is(GameSession.GameType.PUB_MODE.toString())));

        asserter.assertThat(() -> GameSession.findById(GAME), Assertions::assertNotNull);

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testNoPubTasksInDatabase(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> PubTask.delete("id > 0"));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("false")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
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
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/pub/game/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((PubTask) asserter.getData("task")).getKey(),
                        is(((PubTask) asserter.getData("task")).task.content)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testGettingFirstTaskWithNext(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/task/next")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(containsString("EN ")));
        asserter.assertThat(() -> GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id", Parameters.with("id", GAME))
                .page(0, 1).firstResult(),
                gameSession -> Assertions.assertEquals(12, gameSession.tasks.size()));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testGettingAllTasks(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));
        asserter.execute(() -> {

            Set<String> tasks = new HashSet<>();
            for (int i = 0; i < 12; i++) {
                LinkedHashMap<String, String> response = given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .put("/pub/game/task/next")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .path("data");
                tasks.add(response.get(response.get("task")));
            }

            Assertions.assertEquals(12, tasks.size());

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "en").build())
                    .queryParam("resolutionContext", resolutionContext)
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .put("/pub/game/task/next")
                    .then()
                    .statusCode(RestResponse.StatusCode.NO_CONTENT);
        });
        asserter.assertThat(() -> GameSession
                .<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id", Parameters.with("id", GAME))
                .page(0, 1).firstResult(),
                gameSession -> Assertions.assertEquals(0, gameSession.tasks.size()));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testNotGettingRules(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));
        asserter.execute(() -> {

            Set<String> tasks = new HashSet<>();
            for (int i = 0; i < 12; i++) {
                LinkedHashMap<String, String> response = given()
                        .cookie(new Cookie.Builder("gameId", GAME).build())
                        .cookie(new Cookie.Builder("locale", "en").build())
                        .queryParam("resolutionContext", resolutionContext)
                        .contentType(MediaType.APPLICATION_JSON)
                        .when()
                        .put("/pub/game/task/next")
                        .then()
                        .statusCode(RestResponse.StatusCode.OK)
                        .extract()
                        .path("data");
                tasks.add(response.get(response.get("task")));
            }

            Assertions.assertFalse(tasks.contains(((PubTask) asserter.getData("task")).task.content));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testRecreatingNewGame(UniAsserter asserter) {
        asserter.execute(() -> PubTask.<PubTask> findById(0L).invoke(pubTask -> asserter.putData("task", pubTask)));
        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("/pub/game")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("gameId", is(GAME),
                        "type", is(GameSession.GameType.PUB_MODE.toString())));

        //        asserter.assertThat(() -> GameSession.<GameSession>find("from GameSession g left join fetch g.tasks where g.id = :id", Parameters.with("id", GAME)).firstResult(), gameSession -> Assertions.assertEquals(0, gameSession.tasks.size()));

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));

        //        asserter.assertThat(() -> GameSession.<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id", Parameters.with("id", GAME)).firstResult(), gameSession -> Assertions.assertEquals(13, gameSession.tasks.size()));

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/pub/game/task/current")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("data." + ((PubTask) asserter.getData("task")).getKey(),
                        is(((PubTask) asserter.getData("task")).task.content)));

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("/pub/game")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("gameId", is(GAME),
                        "type", is(GameSession.GameType.PUB_MODE.toString())));

        asserter.assertThat(
                () -> GameSession.<GameSession> find("from GameSession g left join fetch g.tasks where g.id = :id",
                        Parameters.with("id", GAME)).firstResult(),
                gameSession -> Assertions.assertEquals(0, gameSession.tasks.size()));

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/pub/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    private Uni<List<PubTask>> createPubTasks() {
        List<Uni<PubTask>> pubTasks = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            pubTasks.add(PubTask.createPubTask("EN %d".formatted(i), Map.of("sk", "SK %d".formatted(i))));
        }
        //noinspection unchecked
        return Uni.combine().all().unis(pubTasks).usingConcurrencyOf(1)
                .with(objects -> (List<PubTask>) objects);
    }
}
