package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.service.GameService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(GameService.class)
public class GameServiceTest {

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
    public void testCreatingSameGame(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .contentType(ContentType.JSON)
                    .body(ID)
            .when()
                    .post()
            .then()
                    .cookie("gameId", is(ID))
                    .statusCode(RestResponse.StatusCode.NOT_MODIFIED);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void testMissingCookie(UniAsserter asserter) {
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

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

//    @Test
//    @RunOnVertxContext
}