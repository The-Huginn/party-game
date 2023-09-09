package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.service.GameService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTest extends AbstractTest {

    private static final String ID = "foo";

    @Inject
    GameService gameService;

    @BeforeEach
    @AfterEach
    void setup(UniAsserter asserter) {
        super.setup(asserter);
        asserter.execute(() -> gameService.createGame(ID));
    }

    @Test
    @Order(1)
    void testCreatingSameGame(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .contentType(ContentType.JSON)
                    .body(ID)
                    .when()
                    .post("/game")
                    .then()
                    .cookie("gameId", is(ID))
                    .statusCode(RestResponse.StatusCode.CONFLICT);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    void testUpdateStatus(UniAsserter asserter) {
        asserter.assertThat(() -> Game.count("state = READY"), game -> Assertions.assertEquals(0L, (long) game));

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", ID).build())
                .contentType(ContentType.JSON)
                .body("\"READY\"")
                .when()
                .put("/game/status")
                .then()
                .statusCode(RestResponse.StatusCode.NO_CONTENT));

        asserter.assertThat(() -> Game.count("state = READY"), game -> Assertions.assertNotEquals(0L, (long) game));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    void testMissingCookie(UniAsserter asserter) {
        asserter.assertThat(() -> Game.count("type = TASK"), game -> Assertions.assertEquals(1L, (long) game));

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", ID).build())
                .contentType(ContentType.JSON)
                .body("\"NONE\"")
                .when()
                .put("/game/type")
                .then()
                .statusCode(RestResponse.StatusCode.NO_CONTENT));

        asserter.assertThat(() -> Game.count("type = NONE"), game -> Assertions.assertEquals(1L, (long) game));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(4)
    public void testEmptyTeamGame(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", ID).build())
                    .accept(ContentType.JSON)
                    .when()
                    .get("/team")
                    .then()
                    .statusCode(200)
                    .body("size()", is(0));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}