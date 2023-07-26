package com.thehuginn;

import com.thehuginn.entities.Category;
import com.thehuginn.entities.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class TestCategoryService {

    @AfterEach
    @RunOnVertxContext
    public void teardown(UniAsserter asserter) {
        asserter.execute(() -> Task.deleteAll());
        asserter.execute(() -> Category.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @BeforeEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> Task.deleteAll());
        asserter.execute(() -> Category.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }


    @Test
    @Order(1)
    @RunOnVertxContext
    public void testCreateEmptyCategory(UniAsserter asserter) {
        asserter.execute(() ->
            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "name": "test",
                                "description": "first test"
                            }
                            """)
                    .when().post("category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", is("test"),
                            "description", is("first test"),
                            "tasks.size()", is(0))
        );
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    @RunOnVertxContext
    public void testCreateCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask().persistAndFlush());
        asserter.execute(() -> EntityCreator.createTask().persistAndFlush());
        asserter.execute(() -> {
            String tasks = """
                                    {
                                        "id": 1,
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
                                    },
                                    {
                                        "id": 2,
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
                                    """;


            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("""
                            {
                                "name": "test",
                                "description": "first test",
                                "tasks": [%s]
                            }
                            """, tasks))
                    .when().post("category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", is("test"),
                            "description", is("first test"),
                            "tasks.size()", is(2));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
