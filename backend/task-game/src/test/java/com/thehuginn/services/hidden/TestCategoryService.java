package com.thehuginn.services.hidden;

import com.thehuginn.AbstractTest;
import com.thehuginn.category.Category;
import com.thehuginn.task.Task;
import com.thehuginn.token.translation.LocaleCategoryText;
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
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCategoryService extends AbstractTest {

    String categoryBody = """
            {
                "name": "%s",
                "description": "%s",
                "tasks": [%s]
            }
            """;

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        super.setup(asserter);
    }

    @Test
    @Order(1)
    @RunOnVertxContext
    public void testCreateEmptyCategory(UniAsserter asserter) {

        asserter.execute(() -> given()
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
                        "tasks.size()", is(0)));
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(2)
    @RunOnVertxContext
    public void testCreateCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask("<player_1").<Task> persistAndFlush()
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
                    .body("name", is("name"),
                            "description", is("description"),
                            "tasks.size()", is(2));
        });
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(3)
    @RunOnVertxContext
    public void testUpdatingCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask("<player_1>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> EntityCreator.createCategory((long) asserter.getData("task1"))
                .<Category> persistAndFlush()
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
                            String.format("{ \"id\": %s }", asserter.getData("task2"))))
                    .pathParam("id", id)
                    .when().put("category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("id", is((int) id),
                            "tasks.size()", is(1),
                            "tasks[0].id", is((int) ((long) asserter.getData("task2"))));
        });

        asserter.execute(() -> {
            Set<?> categories = given()
                    .when()
                    .get("/task-mode/category")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .as(Set.class);

            Assertions.assertEquals(categories.size(), 2);

            //noinspection unchecked
            Assertions.assertTrue(categories.stream()
                    .anyMatch(o -> ((LinkedHashMap<String, String>) o).get("name").equals("new name") &&
                            ((LinkedHashMap<String, String>) o).get("description").equals("new description")));

            given()
                    .pathParam("id", asserter.getData("id"))
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
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask("<player_1>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> EntityCreator.createCategory((long) asserter.getData("task1"), (long) asserter.getData("task2"))
                .<Category> persistAndFlush()
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

        asserter.assertThat(() -> Task.<Task> listAll(), tasks -> {
            Assertions.assertEquals(tasks.size(), 2);
            Assertions.assertTrue(tasks.stream().allMatch(task -> task.id == (long) asserter.getData("task1") ||
                    task.id == (long) asserter.getData("task2")));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(5)
    @RunOnVertxContext
    public void testGetCategories(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category1", category.id)));
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category2", category.id)));

        asserter.execute(() -> given()
                .when()
                .get("/task-mode/category")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("$.size()", is(3),
                        "[0].id", is(0),
                        "[1].id", is((int) ((long) asserter.getData("category1"))),
                        "[2].id", is((int) ((long) asserter.getData("category2")))));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(6)
    @RunOnVertxContext
    public void testGetCategoryTasks(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));
        asserter.execute(() -> EntityCreator.createTask("<player_1>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));
        asserter.execute(() -> new CategoryService()
                .createCategory(
                        EntityCreator.createCategory((long) asserter.getData("task1"), (long) asserter.getData("task2")))
                .onItem()
                .invoke(category -> {
                    asserter.putData("id", category.id);
                    asserter.putData("category", category);
                }));

        asserter.execute(() -> {
            List<?> tasks = given()
                    .pathParam("id", asserter.getData("id"))
                    .when()
                    .get("/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .as(List.class);

            Assertions.assertEquals(tasks.size(), 2);
            //noinspection unchecked
            Assertions.assertTrue(tasks.stream()
                    .allMatch(o -> ((LinkedHashMap<String, Integer>) o).get("id").equals((int) (long) asserter.getData("task1"))
                            ||
                            ((LinkedHashMap<String, Integer>) o).get("id").equals((int) (long) asserter.getData("task2"))));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(7)
    @RunOnVertxContext
    public void testCreateCategoryTranslation(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> {
                    asserter.putData("id", category.id);
                    asserter.putData("category", category);
                }));

        asserter.execute(() -> {
            given()
                    .body("""
                            {
                            "name": "Default Category",
                            "description": "Default English Category Description"
                            }""")
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "en")
                    .post("/category/translation/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", startsWith("Default Category"),
                            "description", startsWith("Default English Category Description"));

            given()
                    .body("""
                            {
                            "name": "Východzia Kategória",
                            "description": "Popis Východzej Kategórie"
                            }""")
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .post("/category/translation/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", startsWith("Východzia Kategória"),
                            "description", startsWith("Popis Východzej Kategórie"));

            given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .when()
                    .pathParam("id", asserter.getData("id"))
                    .pathParam("locale", "sk")
                    .get("task-mode/category/translation/{id}/{locale}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("name", startsWith("Východzia Kategória"),
                            "description", startsWith("Popis Východzej Kategórie"));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(8)
    @RunOnVertxContext
    public void testGetCategoryTranslation(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category1", category)));

        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category2", category)));

        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category1"), "en")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("en_locale", localeCategory)));

        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category2"), "sk")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("sk_locale", localeCategory)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> given()
                .pathParam("id", ((Category) asserter.getData("category1")).id)
                .pathParam("locale", "en")
                .when()
                .get("/task-mode/category/translation/{id}/{locale}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("name", is(((LocaleCategoryText) asserter.getData("en_locale")).name),
                        "description", is(((LocaleCategoryText) asserter.getData("en_locale")).description)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> given()
                .pathParam("id", ((Category) asserter.getData("category2")).id)
                .pathParam("locale", "sk")
                .when()
                .get("/task-mode/category/translation/{id}/{locale}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("name", is(((LocaleCategoryText) asserter.getData("sk_locale")).name),
                        "description", is(((LocaleCategoryText) asserter.getData("sk_locale")).description)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(9)
    @RunOnVertxContext
    public void testUpdateCategoryTranslation(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category", category)));

        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category"), "sk")
                .<LocaleCategoryText> persistAndFlush());

        asserter.execute(() -> given()
                .body("""
                        {
                        "name": "Východzia Kategória",
                        "description": "Popis Východzej Kategórie"
                        }""")
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("id", ((Category) asserter.getData("category")).id)
                .pathParam("locale", "sk")
                .when()
                .put("/category/translation/{id}/{locale}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("name", is("Východzia Kategória"),
                        "description", is("Popis Východzej Kategórie")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> given()
                .pathParam("id", ((Category) asserter.getData("category")).id)
                .pathParam("locale", "sk")
                .when()
                .get("/task-mode/category/translation/{id}/{locale}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("name", is("Východzia Kategória"),
                        "description", is("Popis Východzej Kategórie")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(10)
    @RunOnVertxContext
    public void testDefaultCategoryTasks(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createTask("<drink_responsibly>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task1", task.id)));

        asserter.execute(() -> EntityCreator.createTask("<player_1>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task2", task.id)));

        asserter.execute(() -> EntityCreator.createTask("<task>").<Task> persistAndFlush()
                .onItem()
                .invoke(task -> asserter.putData("task3", task.id)));

        asserter.execute(() -> new CategoryService()
                .createCategory(
                        EntityCreator.createCategory((long) asserter.getData("task1"), (long) asserter.getData("task2")))
                .onItem()
                .invoke(category -> {
                    asserter.putData("id", category.id);
                    asserter.putData("category", category);
                }));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));

        asserter.execute(() -> {
            List<?> tasks = given()
                    .pathParam("id", 0)
                    .when()
                    .get("/category/{id}")
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .extract()
                    .as(List.class);

            Assertions.assertEquals(tasks.size(), 1);
            //noinspection unchecked
            Assertions.assertTrue(tasks.stream()
                    .allMatch(o -> ((LinkedHashMap<String, Integer>) o).get("id")
                            .equals((int) (long) asserter.getData("task3"))));
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(11)
    @RunOnVertxContext
    public void testDefaultCategoryTranslation(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category", category)));

        asserter.execute(() -> EntityCreator.createRandomLocaleCategory((Category) asserter.getData("category"), "cs")
                .<LocaleCategoryText> persistAndFlush()
                .onItem()
                .invoke(localeCategory -> asserter.putData("cs_locale", localeCategory)));

        asserter.execute(() -> given()
                .pathParam("id", ((Category) asserter.getData("category")).id)
                .pathParam("locale", "sk")
                .when()
                .get("/task-mode/category/translation/{id}/{locale}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("name", is("name"),
                        "description", is("description")));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    @Order(14)
    @RunOnVertxContext
    public void testCreateTaskInCategory(UniAsserter asserter) {
        asserter.execute(() -> EntityCreator.createCategory()
                .<Category> persistAndFlush()
                .onItem()
                .invoke(category -> asserter.putData("category", category)));

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
                .pathParam("id", ((Category) asserter.getData("category")).id)
                .when().post("task/category/{id}")
                .then()
                .statusCode(RestResponse.StatusCode.OK));

        asserter.execute(() -> given()
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("id", ((Category) asserter.getData("category")).id)
                .when().get("category/{id}")
                .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("$.size()", is(1)));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
