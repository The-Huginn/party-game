package com.thehuginn;

import com.thehuginn.entities.Task;
import com.thehuginn.services.TaskService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TaskService.class)
public class TestTaskService {

    @Test
    @RunOnVertxContext
    public void testCreateTask(UniAsserter asserter) {
        asserter.execute(() -> {
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "task": ["test"],
                                "type": "DUO",
                                "repeat": "PER_PLAYER",
                                "frequency": 3,
                                "price": {
                                    "enabled": false,
                                    "price": 2
                                },
                                "timer": {
                                    "enabled": true,
                                    "duration": 11
                                }
                            }
                            """)
                    .when().post()
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("id", is(1),
                            "task.size()", is(1),
                            "task[0]", is("test"),
                            "type", is("DUO"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(false),
                            "price.price", is(2),
                            "timer.enabled", is(true),
                            "timer.duration", is(11));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void testGetTask(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheMock.mock(Task.class);
            createTask(1L);

            given()
                    .pathParam("id", 1L)
            .when().get("/{id}")
            .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("id", is(1),
                            "task.size()", is(2),
                            "task[0]", is("drink responsibly"),
                            "task[1]", is("<player_1>"),
                            "type", is("ALL"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(true),
                            "price.price", is(1),
                            "timer.enabled", is(true),
                            "timer.duration", is(15));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void updateTask(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheMock.mock(Task.class);
            createTask(1L);

            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "task": ["test"],
                                "type": "DUO",
                                "repeat": "PER_PLAYER",
                                "frequency": 3,
                                "price": {
                                    "enabled": false,
                                    "price": 2
                                },
                                "timer": {
                                    "enabled": true,
                                    "duration": 11
                                }
                            }
                            """)
                    .pathParam("id", 1L)
            .when().put("/{id}")
            .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("id", is(1),
                            "task.size()", is(1),
                            "task[0]", is("test"),
                            "type", is("DUO"),
                            "repeat", is("PER_PLAYER"),
                            "frequency", is(3),
                            "price.enabled", is(false),
                            "price.price", is(2),
                            "timer.enabled", is(true),
                            "timer.duration", is(11));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    // This test can't use mocking, otherwise @DELETE returns false
    @Test
    @RunOnVertxContext
    public void testDeleteTask(UniAsserter asserter) {
        asserter.execute(() -> {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                            {
                                "task": ["test"],
                                "type": "DUO",
                                "repeat": "PER_PLAYER",
                                "frequency": 3,
                                "price": {
                                    "enabled": false,
                                    "price": 2
                                },
                                "timer": {
                                    "enabled": true,
                                    "duration": 11
                                }
                            }
                            """)
                .when().post()
                .then()
                .statusCode(RestResponse.StatusCode.OK);

        Boolean response = given()
            .pathParam("id", 1L)
            .accept(ContentType.JSON)
        .when().delete("/{id}")
        .then()
            .statusCode(RestResponse.StatusCode.OK)
            .extract()
            .as(Boolean.class);

        Assertions.assertTrue(response);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    private void createTask(long id) {
        Task task = new Task.Builder(Arrays.asList("drink responsibly", "<player_1>"))
                .id(id)
                .type(Task.Type.ALL)
                .repeat(Task.Repeat.PER_PLAYER)
                .frequency((short) 3)
                .price(new Task.Price(true, 1))
                .timer(new Task.Timer(true, 15))
                .build();

        Mockito.when(Task.<Task>findById(id)).thenReturn(Uni.createFrom().item(task));
    }
}
