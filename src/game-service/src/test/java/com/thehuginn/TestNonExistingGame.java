package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.service.GameService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(GameService.class)
public class TestNonExistingGame {

    private static final String ID = "foo";

    @BeforeEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheMock.mock(Game.class);
        });
    }

    @Test
    @RunOnVertxContext
    public void testCreatingGame(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .contentType(ContentType.JSON)
                    .body(ID)
            .when()
                    .post()
            .then()
                    .cookie("gameId", is(ID))
                    .statusCode(201);

        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void testMissingGame(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .queryParam("gameId", ID)
            .when()
                    .get()
            .then()
                    .statusCode(RestResponse.StatusCode.NO_CONTENT);
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void testMissingCookie(UniAsserter asserter) {

        asserter.execute(() ->
            given()
                    .body(Game.State.READY)
                    .contentType(ContentType.JSON)
                    .cookie(new Cookie.Builder("gameId", ID).build())
            .when()
                    .put("/status")
            .then()
                    .statusCode(RestResponse.StatusCode.BAD_REQUEST)
        );
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }


}
