package com.thehuginn;

import com.thehuginn.common.game.AbstractGameSession;
import com.thehuginn.common.game.category.AbstractCategory;
import com.thehuginn.common.game.task.AbstractTask;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
@RunOnVertxContext
public class AbstractTest {

    protected static final String GAME = "game";
    protected static final String PLAYER = "player1";
    protected static final List<String> PLAYERS = new ArrayList<>(List.of(PLAYER, "player2", "player3"));
    protected static final String LOCALE = "en";
    protected static final ResolutionContext resolutionContext = ResolutionContext.builder(GAME).player(PLAYER).players(PLAYERS)
            .locale(LOCALE).build();

    @BeforeEach
    void setup(UniAsserter asserter) {
        asserter.execute(() -> AbstractGameSession.deleteAll());
        additionalSetup(asserter);
        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }

    protected void additionalSetup(UniAsserter asserter) {}
}
