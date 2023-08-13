package com.thehuginn;

import com.thehuginn.services.TaskService;
import com.thehuginn.task.Category;
import com.thehuginn.task.Task;
import com.thehuginn.token.unresolved.AbstractUnresolvedToken;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Objects;

import static com.thehuginn.util.EntityCreator.createTask;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TaskService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTaskService {

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> Category.delete("id > 0"));
        asserter.execute(() -> Task.deleteAll());
        asserter.execute(() -> AbstractUnresolvedToken.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    @Order(1)
    public void testCreateTask(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "task": "test",
                                "type": "DUO",
                                "repeat": "PER_PLAYER",
                                "frequency": 3,
                                "price": {
                                    "enabled": false,
                                    "price": 2
                                }
                            }
                            """)
                    .when().post()
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("task", is("test"),
                            "type", is("DUO"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(false),
                            "price.price", is(2));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    @RunOnVertxContext
    public void testGetTask(UniAsserter asserter) {
        asserter.execute(() -> createTask("drink responsibly with <player_1>").<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
            given()
                    .pathParam("id", id)
            .when().get("/{id}")
            .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("task", is("drink responsibly with <player_1>"),
                            "type", is("ALL"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(true),
                            "price.price", is(1));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    @RunOnVertxContext
    public void updateTask(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "task": "updated task",
                                "type": "DUO",
                                "repeat": "PER_PLAYER",
                                "frequency": 3,
                                "price": {
                                    "enabled": false,
                                    "price": 2
                                }
                            }
                            """)
                    .pathParam("id", id)
            .when().put("/{id}")
            .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("task", is("updated task"),
                            "type", is("DUO"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(false),
                            "price.price", is(2));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(4)
    @RunOnVertxContext
    public void testDeleteTask(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));
        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
        Boolean response = given()
            .pathParam("id", id)
            .accept(MediaType.APPLICATION_JSON)
        .when().delete("/{id}")
        .then()
            .statusCode(RestResponse.StatusCode.OK)
            .extract()
            .as(Boolean.class);

        Assertions.assertTrue(response);
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(5)
    @RunOnVertxContext
    public void testEntityCreatorCreateTask(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));
        asserter.assertThat(() -> Task.<Task>findById(asserter.getData("id")), task -> Assertions.assertTrue(
//                task.tokens.stream().allMatch(token -> token.key.equals("drink_responsibly") || token.key.equals("<player_1>")) &&
            task.type == Task.Type.ALL &&
            task.repeat == Task.Repeat.PER_PLAYER &&
            Objects.equals(task.frequency, (short) 3) &&
            task.price.enabled &&
            task.price.price == 1 &&
            task.id == (long) asserter.getData("id")));
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(6)
    @RunOnVertxContext
    public void testCreateLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Pi zodpovedne")
                    .pathParam("key", "drink_responsibly")
                    .pathParam("locale", "sk")
                    .when().post("/{key}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("token", anyOf(is(1), is(2)),
                            "locale", is("sk"),
                            "content", is("Pi zodpovedne"));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
