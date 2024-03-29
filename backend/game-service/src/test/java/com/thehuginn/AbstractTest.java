package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.entities.Player;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTest {

    @BeforeEach
    @RunOnVertxContext
    void setup(UniAsserter asserter) {
        asserter.execute(() -> Game.deleteAll());
        asserter.execute(() -> Player.deleteAll());
        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }
}
