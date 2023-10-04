package com.thehuginn.services.exposed.pub;

import com.thehuginn.GameSession;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Path;

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
        return null;
    }

    @Override
    public Uni<?> currentTask(String gameId, String locale, ResolutionContext.Builder resolutionContext) {
        return null;
    }

    @Override
    public Uni<?> nextTask(String gameId, String locale, ResolutionContext.Builder resolutionContext) {
        return null;
    }
}
