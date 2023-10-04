package com.thehuginn;

import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.services.hidden.GameTaskService;
import com.thehuginn.services.hidden.TaskService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
@RunOnVertxContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AbstractResolutionTaskTest extends AbstractTest {
    protected static final String GAME = "game";
    protected static final String PLAYER = "player1";
    protected static final List<String> PLAYERS = new ArrayList<>(List.of(PLAYER, "player2", "player3"));
    protected static final String LOCALE = "en";
    protected static final ResolutionContext resolutionContext = ResolutionContext.builder(GAME).player(PLAYER).players(PLAYERS)
            .locale(LOCALE).build();

    @Inject
    protected GameTaskService gameTaskService;

    @Inject
    protected TaskService taskService;
}
