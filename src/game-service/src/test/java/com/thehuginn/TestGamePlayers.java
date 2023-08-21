package com.thehuginn;

import com.thehuginn.service.GameService;
import com.thehuginn.service.PlayerService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(PlayerService.class)
@RunOnVertxContext
public class TestGamePlayers extends AbstractTest {

    private static final String ID = "foo";

    @BeforeEach
    public void setup(UniAsserter asserter) {
        super.setup(asserter);
        asserter.execute(() -> new GameService().createGame(ID)
                .onItem().invoke(gameRestResponse -> asserter.putData(ID, gameRestResponse.getEntity())));
        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }

    @Test
    @RunOnVertxContext
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
