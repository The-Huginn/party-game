package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.service.GameService;
import com.thehuginn.service.PlayerService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(PlayerService.class)
public class TestGamePlayers {

    private final static String ID = "foo";

    @BeforeEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheMock.mock(Game.class);
            Game game = new Game(ID);
            Mockito.when(Game.<Game>findById(ID)).thenReturn(Uni.createFrom().item(game));
            asserter.putData(ID, game);
            asserter.assertThat(() -> Game.<Game>findById(ID), p -> Assertions.assertSame(p, asserter.getData(ID)));
        });
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
