package com.thehuginn;

import com.thehuginn.entities.Task;
import com.thehuginn.services.TaskService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TaskService.class)
public class TestTaskService {

    @Test
    public void testCreatingTask() {
        Task task = new Task();
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(task)
        .when().post()
        .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body(is("""
                        {
                            "id": 0
                            """));
    }
}
