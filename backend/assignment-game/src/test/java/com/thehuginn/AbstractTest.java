package com.thehuginn;

import com.thehuginn.common.game.AbstractGameSession;
import com.thehuginn.common.game.category.AbstractCategory;
import com.thehuginn.common.game.task.AbstractTask;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@RunOnVertxContext
public class AbstractTest {

    @BeforeEach
    void setup(UniAsserter asserter) {
        asserter.execute(() -> AbstractGameSession.deleteAll());
        asserter.execute(() -> AbstractCategory.deleteAll());
        asserter.execute(() -> AbstractTask.deleteAll());
        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }
}
