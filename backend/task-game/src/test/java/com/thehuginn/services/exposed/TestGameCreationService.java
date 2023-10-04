package com.thehuginn.services.exposed;

import com.thehuginn.AbstractTest;
import com.thehuginn.category.Category;
import com.thehuginn.common.game.translation.LocaleCategoryText;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.services.hidden.CategoryService;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestGameCreationService extends AbstractTest {

    private static final String GAME = "game";
    private static final String PLAYER = "player1";
    private static final List<String> PLAYERS = List.of(PLAYER, "player2", "player3");
    private static final String LOCALE = "en";
    private static final ResolutionContext resolutionContext = ResolutionContext.builder(GAME)
            .player(PLAYER)
            .players(PLAYERS)
            .locale(LOCALE).build();

    @Test
    @Order(1)
    void testGettingGameSession(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .when()
                .get("/game")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("gameId", is(GAME),
                        "categories.size()", is(0)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    void testAddingAndRemovingCategories(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("id1", category.id)));
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("id2", category.id)));

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", 0)
                    .when()
                    .put("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", asserter.getData("id1"))
                    .when()
                    .put("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", asserter.getData("id2"))
                    .when()
                    .put("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .when()
                    .get("/game")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("categories.size()", is(3));
        });

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", 0)
                    .when()
                    .delete("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", asserter.getData("id2"))
                    .when()
                    .delete("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .when()
                    .get("/game")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("categories.size()", is(1));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    void testAddingCategoriesWithTasksAndStartingGame(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask("<player_1>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly_please>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task3", task.id)));
        asserter.execute(() -> EntityCreator.createTask("{player_1}").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task4", task.id)));

        asserter.execute(() -> new CategoryService()
                .createCategory(
                        EntityCreator.createCategory((long) asserter.getData("task1"), (long) asserter.getData("task2")))
                .onItem()
                .invoke(category -> asserter.putData("id1", category.id)));
        asserter.execute(
                () -> new CategoryService().createCategory(EntityCreator.createCategory((long) asserter.getData("task3")))
                        .onItem()
                        .invoke(category -> asserter.putData("id2", category.id)));

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", 0)
                    .when()
                    .put("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));

            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .pathParam("id", asserter.getData("id2"))
                    .when()
                    .put("/task-mode/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body(is("true"));
        });

        asserter.execute(() -> given()
                .cookie(new Cookie.Builder("gameId", GAME).build())
                .cookie(new Cookie.Builder("locale", "en").build())
                .queryParam("resolutionContext", resolutionContext)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/game/start")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("true")));

        asserter.assertThat(() -> GameTask.<GameTask> find("game.id = :game", Parameters.with("game", GAME))
                .list(), gameTasks -> Assertions.assertEquals(gameTasks.size(), 18));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(4)
    void testGetDefaultLocalizedCategories(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category1", category)));
        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category1"), "sk")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("sk_category1", localeCategory)));

        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category2", category)));
        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category2"), "sk")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("sk_category2", localeCategory)));

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .when()
                    .get("/task-mode/category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("$.size()", is(3),
                            "[1].name", is("name"),
                            "[1].description", is("description"));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(5)
    void testGetLocalizedCategories(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createGameSession(GAME).persistAndFlush());
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category1", category)));
        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category1"), "sk")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("sk_category1", localeCategory)));

        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category2", category)));
        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category2"), "sk")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("sk_category2", localeCategory)));

        asserter.execute(() -> {
            given()
                    .cookie(new Cookie.Builder("gameId", GAME).build())
                    .cookie(new Cookie.Builder("locale", "sk").build())
                    .when()
                    .get("/task-mode/category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("$.size()", is(3),
                            "[1].name", anyOf(
                                    is(((LocaleCategoryText) asserter.getData("sk_category1")).getName()),
                                    is(((LocaleCategoryText) asserter.getData("sk_category2")).getName())),
                            "[1].description", anyOf(
                                    is(((LocaleCategoryText) asserter.getData("sk_category1")).getDescription()),
                                    is(((LocaleCategoryText) asserter.getData("sk_category2")).getDescription())));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
