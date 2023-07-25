package com.thehuginn;

import com.thehuginn.entities.Category;
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
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.thehuginn.util.EntityCreator.createTask;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TaskService.class)
public class TestTaskService {

    @BeforeEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> {
            Task.deleteAll();
            Category.deleteAll();
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

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
            Task task = createTask(1L);
            Mockito.when(Task.<Task>findById(1L)).thenReturn(Uni.createFrom().item(task));

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

            Task task = createTask(1L);
            Mockito.when(Task.<Task>findById(1L)).thenReturn(Uni.createFrom().item(task));

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
}
