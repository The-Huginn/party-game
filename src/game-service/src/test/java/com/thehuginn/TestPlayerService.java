package com.thehuginn;

import com.thehuginn.entities.Player;
import com.thehuginn.service.GameService;
import com.thehuginn.service.PlayerService;
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
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
public class TestPlayerService extends AbstractTest {

    private static final String ID = "foo";

    @Inject
    GameService gameService;

    @Inject
    PlayerService playerService;

    @BeforeEach
    @AfterEach
    void setup(UniAsserter asserter) {
        super.setup(asserter);
    }

    @Test
    void addPlayer(UniAsserter asserter) {
        asserter.execute(() -> gameService.createGame(ID));

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

    @Test
    void addSamePlayer(UniAsserter asserter) {
        Player player = new Player();
        player.name = "Player";
        asserter.execute(() -> gameService.createGame(ID));
        asserter.execute(() -> playerService.addPlayer(ID, player)
                .onItem().invoke(player1 -> Assertions.assertEquals(player.name, player1.name)));

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", ID).build())
                    .contentType(ContentType.JSON)
                    .body("{\"name\": \"Player\"}")
                    .when()
                    .post("/player")
                    .then()
                    .statusCode(RestResponse.StatusCode.NO_CONTENT);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void removePlayer(UniAsserter asserter) {
        Player player = new Player();
        player.name = "Player";
        asserter.execute(() -> gameService.createGame(ID));
        asserter.execute(() -> playerService.addPlayer(ID, player)
                .onItem().invoke(player1 -> asserter.putData("id", player1.id)));
        asserter.assertThat(() -> playerService.getTeam(ID), players -> Assertions.assertFalse(players.isEmpty()));

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", ID).build())
                    .contentType(ContentType.JSON)
                    .body(asserter.getData("id"))
                    .when()
                    .delete("/player")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));
        });

        asserter.assertThat(() -> playerService.getTeam(ID), players -> Assertions.assertTrue(players.isEmpty()));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
