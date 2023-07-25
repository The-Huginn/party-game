package com.thehuginn;

import com.thehuginn.entities.Category;
import com.thehuginn.entities.Task;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class TestCategoryService {

    @BeforeEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheMock.mock(Task.class);
            PanacheMock.mock(Category.class);
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void testCreateEmptyCategory(UniAsserter asserter) {
        asserter.execute(() -> {

            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "name": "test",
                                "description": "first test"
                            }
                            """)
                    .when().post("/category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", is("test"),
                            "description", is("first test"),
                            "tasks.size()", is(0));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @RunOnVertxContext
    public void testCreateCategory(UniAsserter asserter) {
        asserter.execute(() -> {
            List<Task> taskList = new ArrayList<>(Arrays.asList(EntityCreator.createTask(1L), EntityCreator.createTask(2L)));
            Mockito.when(Task.findByIds(Mockito.anyList())).thenReturn(Uni.createFrom().item(taskList));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

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
