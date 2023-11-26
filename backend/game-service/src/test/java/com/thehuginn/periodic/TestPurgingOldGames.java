package com.thehuginn.periodic;

import com.thehuginn.AbstractTest;
import com.thehuginn.entities.Game;
import com.thehuginn.external.GameRestClientPub;
import com.thehuginn.external.GameRestClientTask;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@QuarkusTest
@RunOnVertxContext
public class TestPurgingOldGames extends AbstractTest {

    @InjectMock
    @RestClient
    GameRestClientPub gameRestClientPub;

    @InjectMock
    @RestClient
    GameRestClientTask gameRestClientTask;

    @Inject
    GamePurgeScheduler gamePurgeScheduler;

    /**
     * Expecting 3 games to be purged
     */
    @Test
    void testDeletingOnlyOldGames(UniAsserter asserter) {
        String idTemplate = "Game%d";
        int totalGames = 5;

        asserter.execute(() -> {
            List<Uni<Game>> gamesToPersist = new ArrayList<>();
            for (int i = 0; i < totalGames; i++) {
                Game game = new Game(idTemplate.formatted(i));
                // Yes, we will go into future
                int daysToSubtract = 3 - i;
                game.lastAccess = LocalDateTime.now().minusDays(daysToSubtract);
                game.type = i % 2 == 0 ? Game.Type.TASK : Game.Type.PUB;
                gamesToPersist.add(game.persistAndFlush());
            }
            return Uni.combine().all().unis(gamesToPersist).usingConcurrencyOf(1).discardItems();
        });

        asserter.execute(() -> Uni.createFrom().item(gamePurgeScheduler)
                .call(gamePurgeScheduler -> gamePurgeScheduler.deleteOldGames()));

        asserter.assertThat(() -> Game.<Game> find("from Game").list(), games -> {
            Assertions.assertEquals(2, games.size());
            Set<String> expectedGameIds = Set.of(idTemplate.formatted((totalGames - 1)),
                    idTemplate.formatted((totalGames - 2)));
            Set<String> collectedGameIds = Set.of(games.get(0).gameId, games.get(1).gameId);
            Assertions.assertEquals(2, expectedGameIds.size());
            Assertions.assertEquals(expectedGameIds, collectedGameIds);
            Mockito.verify(gameRestClientPub).deleteGame(ArgumentMatchers.anyString());
            Mockito.verify(gameRestClientTask, Mockito.times(2)).deleteGame(ArgumentMatchers.anyString());
        });

        asserter.surroundWith(uni -> Panache.withTransaction(() -> uni));
    }
}
