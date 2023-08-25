package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.service.GameService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(GameService.class)
@RunOnVertxContext
public class GameServiceTest extends AbstractTest {

    private static final String ID = "foo";

    @BeforeEach
    void setup(UniAsserter asserter) {
        super.setup(asserter);
        asserter.execute(() -> new GameService().createGame(ID));
        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }

    @Test
    void testCreatingSameGame(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .contentType(ContentType.JSON)
                    .body(ID)
                    .when()
                    .post()
                    .then()
                    .cookie("gameId", is(ID))
                    .statusCode(RestResponse.StatusCode.CONFLICT);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testUpdateStatus(UniAsserter asserter) {
        asserter.assertThat(() -> Game.count("state = READY"), game -> Assertions.assertEquals(0L, (long) game));

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", ID).build())
                        .contentType(ContentType.JSON)
                        .body("\"READY\"")
                        .when()
                        .put("/status")
                        .then()
                        .statusCode(RestResponse.StatusCode.NO_CONTENT)
        );

        asserter.assertThat(() -> Game.count("state = READY"), game -> Assertions.assertNotEquals(0L, (long) game));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testMissingCookie(UniAsserter asserter) {
        asserter.assertThat(() -> Game.count("type = TASK"), game -> Assertions.assertEquals(0L, (long) game));

        asserter.execute(() ->
                given()
                        .cookie(new Cookie.Builder("gameId", ID).build())
                        .contentType(ContentType.JSON)

                        .body("\"TASK\"")
                        .when()
                        .put("/type")
                        .then()
                        .statusCode(RestResponse.StatusCode.NO_CONTENT)
        );

        asserter.assertThat(() -> Game.count("type = TASK"), game -> Assertions.assertNotEquals(0L, (long) game));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}