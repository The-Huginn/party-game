package com.thehuginn.services.exposed.pub;

import com.thehuginn.GameSession;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

import java.util.Map;
import java.util.function.Function;

@Path("/pub")
@WithTransaction
public class GameService implements com.thehuginn.common.services.exposed.GameService {
    @Override
    public Uni<GameSession> getGame(String gameId) {
        return GameSession.<GameSession> findById(gameId);
    }

    @Override
    @WithTransaction
    public Uni<GameSession> createGame(String gameId) {
        GameSession gameSession = new GameSession(gameId, GameSession.GameType.PUB_MODE);
        return GameSession.deleteById(gameId)
                .chain(gameSession::persist);
    }

    @Override
    public Uni<Boolean> deleteGame(String gameId) {
        return GameSession.deleteById(gameId);
    }

    @Override
    public Uni<Boolean> startGame(String gameId, ResolutionContext.Builder resolutionContext) {
        return execute(gameId, gameSession -> gameSession.start(resolutionContext))
                .onFailure().recoverWithItem(Boolean.FALSE);
    }

    @Override
    public Uni<Map.Entry<String, String>> currentTask(String gameId, String locale,
            ResolutionContext.Builder resolutionContext) {
        resolutionContext = resolutionContext.player(resolutionContext.getPlayers().get(0));
        ResolutionContext.Builder finalResolutionContext = resolutionContext;
        return execute(gameId, gameSession -> gameSession.currentTask(finalResolutionContext))
                .onFailure().recoverWithNull();
    }

    @Override
    public Uni<?> nextTask(String gameId, String locale, ResolutionContext.Builder resolutionContext) {
        resolutionContext = resolutionContext.player(resolutionContext.getPlayers().get(0));
        ResolutionContext.Builder finalResolutionContext = resolutionContext;
        return execute(gameId, gameSession -> gameSession.nextTask(finalResolutionContext))
                .onFailure().recoverWithNull();
    }

    private <T> Uni<T> execute(String gameId, Function<GameSession, Uni<T>> callback) {
        return GameSession.<GameSession> findById(gameId)
                .onItem().ifNull().failWith(new WebApplicationException("Unable to find game session"))
                .chain(gameSession -> {
                    if (gameSession.type != GameSession.GameType.PUB_MODE) {
                        throw new WebApplicationException(
                                "Game session %s was started as a different type [%s] not as type of %s".formatted(
                                        gameSession, gameSession.type, GameSession.GameType.PUB_MODE));
                    }
                    return callback.apply(gameSession);
                });
    }
}
