package com.thehuginn.services.hidden;

import com.thehuginn.AbstractTest;
import com.thehuginn.task.Task;
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
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TaskService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTaskService extends AbstractTest {

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        super.setup(asserter);
    }

    @Test
    @RunOnVertxContext
    @Order(1)
    public void testCreateTask(UniAsserter asserter) {
        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "task": {
                                "content": "test",
                                "locale": "en"
                            },
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
                .body("task.content", is("test"),
                        "task.locale", is("en"),
                        "type", is("DUO"),
                        "repeat", is("PER_PLAYER"),
                        "frequency", is(3),
                        "price.enabled", is(false),
                        "price.price", is(2))
                .extract().asString());

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    @RunOnVertxContext
    public void testGetTask(UniAsserter asserter) {
        asserter.execute(() -> createTask("drink responsibly with <player_1>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
            given()
                    .pathParam("id", id)
                    .when().get("/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("task.content", is("drink responsibly with <player_1>"),
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
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "task": {
                                    "content": "updated task"
                                },
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
                    .body("task.content", is("<drink_responsibly>"),
                            "task.locale", is("en"),
                            "type", is("DUO"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(false),
                            "price.price", is(2));
        });

        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("id", asserter.getData("id"))
                .when().get("/{id}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("task.content", is("<drink_responsibly>"),
                        "task.locale", is("en"),
                        "type", is("DUO"),
                        "repeat", is("PER_PLAYER"),
                        "frequency", is(3),
                        "price.enabled", is(false),
                        "price.price", is(2)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(4)
    @RunOnVertxContext
    public void testDeleteTask(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
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
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));
        asserter.assertThat(() -> Task.<Task> findById(asserter.getData("id")), task -> Assertions.assertTrue(
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
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Pi zodpovedne")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("locale", is("sk"),
                            "content", is("Pi zodpovedne"));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(7)
    @RunOnVertxContext
    public void updateTaskWithLocaleFail(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "task": {
                                    "locale": "sk"
                                },
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
                    .body("task.content", is("<drink_responsibly>"),
                            "task.locale", is("en"),
                            "type", is("DUO"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(false),
                            "price.price", is(2));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(8)
    @RunOnVertxContext
    public void testGettingDefaultLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().get("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("task.content", is("<drink_responsibly>"),
                            "type", is("ALL"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(true),
                            "price.price", is(1));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(9)
    @RunOnVertxContext
    public void testGettingNewLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Pi zodpovedne")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("locale", is("sk"),
                            "content", is("Pi zodpovedne"));
        });

        asserter.execute(() -> {
            given()
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().get("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("task.content", is("Pi zodpovedne"),
                            "type", is("ALL"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(true),
                            "price.price", is(1));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(9)
    @RunOnVertxContext
    public void testCreateTaskWithUnknownLocale(UniAsserter asserter) {
        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "task": {
                                "content": "test",
                                "locale": "foo"
                            },
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
                .statusCode(RestResponse.StatusCode.BAD_REQUEST));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(10)
    @RunOnVertxContext
    public void testCreateUnknownLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("foo")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "bar")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.BAD_REQUEST);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(11)
    @RunOnVertxContext
    public void testCreateWrongLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("{player_c} plays").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{player_1} hrá")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.BAD_REQUEST);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(12)
    @RunOnVertxContext
    public void testUpdateToWrongLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("{player_c} plays").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{player_c} hrá")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK);
        });

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{player_1} hrá")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.BAD_REQUEST);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(13)
    @RunOnVertxContext
    public void testUpdateToCorrectLocale(UniAsserter asserter) {
        asserter.execute(() -> createTask("{player_c} plays for {timer_42} with {player_1}")
                .<Task> persistAndFlush()
                .invoke(task -> asserter.putData("id", task.id)));

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{player_c} hrá na {timer_42} s hráčom {player_1}")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().post("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK);
        });

        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{player_c} hrá na {timer_42} s hráčom: {player_1}")
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .when().put("/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("locale", is("sk"),
                            "content", is("{player_c} hrá na {timer_42} s hráčom: {player_1}"));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    @Order(14)
    public void testCreateTaskWithDefaultValues(UniAsserter asserter) {
        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "task": {
                                "content": "test",
                                "locale": "en"
                            }
                        }
                        """)
                .when().post()
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("task.content", is("test"),
                        "task.locale", is("en"),
                        "type", is("SINGLE"),
                        "repeat", is("NEVER"),
                        "frequency", is(1),
                        "price.enabled", is(true),
                        "price.price", is(1))
                .extract().asString());

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    @Order(4)
    public void testFailOnDuplicateTaskContent(UniAsserter asserter) {
        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "task": {
                                "content": "test",
                                "locale": "en"
                            }
                        }
                        """)
                .when().post()
                .then()
                .statusCode(RestResponse.StatusCode.OK));

        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "task": {
                                "content": "test",
                                "locale": "en"
                            }
                        }
                        """)
                .when().post()
                .then()
                .statusCode(RestResponse.StatusCode.INTERNAL_SERVER_ERROR));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
