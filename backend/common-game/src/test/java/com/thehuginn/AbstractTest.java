package com.thehuginn;

import com.thehuginn.category.AbstractCategory;
import com.thehuginn.task.AbstractTask;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTest {

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> AbstractTask.deleteAll());
        asserter.execute(() -> AbstractCategory.delete("id > 0"));
        asserter.execute(() -> AbstractGameSession.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
