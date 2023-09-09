package com.thehuginn;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
public class TestGamePlayers extends AbstractTest {

    private static final String ID = "foo";

    @Inject
    GameService gameService;

    @BeforeEach
    @AfterEach
    void setup(UniAsserter asserter) {
        super.setup(asserter);
        asserter.execute(() -> gameService.createGame(ID)
                .onItem().invoke(gameRestResponse -> asserter.putData(ID, gameRestResponse.getEntity())));
    }

    @Test
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

    @Test
    void addPlayer(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", ID).build())
                    .contentType(ContentType.JSON)
                    .body("{\"name\": \"Player\"}")
                    .when()
                    .post("/player")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", is("Player"));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
