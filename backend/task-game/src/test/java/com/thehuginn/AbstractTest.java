package com.thehuginn;

import com.thehuginn.category.Category;
import com.thehuginn.category.LocaleCategory;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.Task;
import com.thehuginn.token.resolved.AbstractResolvedToken;
import com.thehuginn.token.unresolved.AbstractUnresolvedToken;
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
        asserter.execute(() -> GameTask.deleteAll());
        asserter.execute(() -> Task.deleteAll());
        asserter.execute(() -> Category.delete("id > 0"));
        asserter.execute(() -> LocaleCategory.deleteAll());
        asserter.execute(() -> AbstractUnresolvedToken.deleteAll());
        asserter.execute(() -> AbstractResolvedToken.deleteAll());
        asserter.execute(() -> GameSession.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
