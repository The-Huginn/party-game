package com.thehuginn;

import com.thehuginn.entities.Category;
import com.thehuginn.entities.Task;
import com.thehuginn.services.CategoryService;
import com.thehuginn.util.EntityCreator;
import io.quarkus.hibernate.reactive.panache.Panache;
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

import java.util.LinkedHashMap;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCategoryService {

    String categoryBody = """
            {
                "name": "%s",
                "description": "%s",
                "tasks": [%s]
            }
            """;

    @BeforeEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> Task.deleteAll());
        asserter.execute(() -> Category.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @AfterEach
    @RunOnVertxContext
    public void teardown(UniAsserter asserter) {
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
                        .body("id", is(1),
                                "name", is("test"),
                                "description", is("first test"),
                                "tasks.size()", is(0))
        );
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    @RunOnVertxContext
    public void testCreateCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> {
            String tasks = String.format("{ \"id\": %s }, { \"id\": %s }",
                    asserter.getData("task1"),
                    asserter.getData("task2"));


            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format(categoryBody,
                            "name", "description", tasks))
                    .when().post("category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("id", is(2),
                            "name", is("name"),
                            "description", is("description"),
                            "tasks.size()", is(2));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    @RunOnVertxContext
    public void testUpdatingCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> new CategoryService().createCategory(EntityCreator.createCategory((long) asserter.getData("task1")))
                .onItem()
                .invoke(category -> {
                    asserter.putData("id", category.id);
                    asserter.putData("category", category);
                }));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");

            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format(categoryBody,
                            "new name", "new description",
                            String.format("{ \"id\": %s }", asserter.getData("task2"))
                    ))
                    .pathParam("id", id)
                    .when().put("category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("id", is((int) id),
                            "name", is("new name"),
                            "description", is("new description"),
                            "tasks.size()", is(1),
                            "tasks[0].id", is((int)((long) asserter.getData("task2"))));
        });

        asserter.execute(() -> {
            Set<Object> categories = given()
                    .when()
                    .get("/category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .as(Set.class);

            Assertions.assertEquals(categories.size(), 1);

            LinkedHashMap<String, String> category = (LinkedHashMap<String, String>) categories.stream().findAny().orElseThrow(IllegalStateException::new);
            Assertions.assertEquals(category.get("name"), "new name");
            Assertions.assertEquals(category.get("description"), "new description");

            given()
                    .pathParam("id", (long) asserter.getData("id"))
                    .when()
                    .get("/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("$.size()", is(1),
                            "[0].id", is((int) ((long) asserter.getData("task2"))));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(4)
    @RunOnVertxContext
    public void testDeletingCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> EntityCreator.createCategory((long) asserter.getData("task1"), (long) asserter.getData("task2"))
                .<Category>persistAndFlush()
                .onItem()
                .invoke(category -> {
                    asserter.putData("id", category.id);
                    asserter.putData("category", category);
                }));

        asserter.execute(() -> {
            long id = (long) asserter.getData("id");
            Boolean response = given()
                    .pathParam("id", id)
                    .when()
                    .delete("/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .as(Boolean.class);

            Assertions.assertTrue(response);
        });

        asserter.assertThat(() -> Task.<Task>listAll(), tasks -> {
            Assertions.assertEquals(tasks.size(), 2);
            Assertions.assertTrue(tasks.stream().allMatch(task ->
                    task.id == (long) asserter.getData("task1") ||
                            task.id == (long) asserter.getData("task2")));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(5)
    @RunOnVertxContext
    public void testGetCategories(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category>persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category1", category.id)));
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category>persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category2", category.id)));

        asserter.execute(() -> {
            given()
                    .when()
                    .get("/category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("$.size()", is(2),
                            "[0].id",is((int) ((long) asserter.getData("category1"))),
                            "[1].id",is((int) ((long) asserter.getData("category2"))));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(6)
    @RunOnVertxContext
    public void testGetCategoryTasks(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask().<Task>persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> new CategoryService().createCategory(EntityCreator.createCategory((long) asserter.getData("task1"), (long) asserter.getData("task2")))
                .onItem()
                .invoke(category -> {
                    asserter.putData("id", category.id);
                    asserter.putData("category", category);
                }));

        asserter.execute(() -> {

            given()
                    .pathParam("id", asserter.getData("id"))
                    .when()
                    .get("/category/{id}?sort=id")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("$.size()", is(2),
                            "[0].id",is((int) ((long) asserter.getData("task1"))),
                            "[1].id",is((int) ((long) asserter.getData("task2"))));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
