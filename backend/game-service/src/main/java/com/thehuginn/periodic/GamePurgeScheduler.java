package com.thehuginn.periodic;

import com.thehuginn.entities.Game;
import com.thehuginn.external.GameRestClientPub;
import com.thehuginn.external.GameRestClientTask;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApplicationScoped
public class GamePurgeScheduler {

    @RestClient
    GameRestClientTask restClientTask;

    @RestClient
    GameRestClientPub restClientPub;

    @WithTransaction
    @Scheduled(every = "1h")
    Uni<Void> deleteOldGames() {
        Parameters queryParameters = Parameters.with("nowThreshold", LocalDateTime.now().minusDays(1));
        String query = "lastAccess < :nowThreshold";
        Supplier<Uni<Void>> deleteGames = () -> Game
                .delete(query, queryParameters)
                .invoke(deletedGames -> {
                    Log.infof("Purged games not played over 24hours [%d]", deletedGames);
                }).replaceWithVoid();
        return Game.<Game> find(query, queryParameters)
                .list().onItem().transformToUni(games -> {
                    List<Uni<Boolean>> deletionCallbacks = games.stream().<Uni<Boolean>> map(game -> switch (game.type) {
                        case TASK -> restClientTask.deleteGame(game.gameId);
                        case PUB -> restClientPub.deleteGame(game.gameId);
                        case NONE -> {
                            Log.errorf("Unable to determine game type for game %s", game.gameId);
                            yield Uni.createFrom().item(Boolean.FALSE);
                        }
                    }).collect(Collectors.toList());

                    if (deletionCallbacks.isEmpty()) {
                        return Uni.createFrom().voidItem();
                    }
                    return Uni.combine().all().unis(deletionCallbacks).discardItems();
                }).call(deleteGames::get)
                .invoke(deletedEntries -> {
                    Log.infof("Deleted %s number of games");
                });
    }
}
