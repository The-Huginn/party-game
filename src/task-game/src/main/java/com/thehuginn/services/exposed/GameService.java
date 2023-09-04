package com.thehuginn.services.exposed;

import com.thehuginn.GameSession;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.task.ResolvedTask;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.function.Function;

@Path("/game")
public class GameService {

    @GET
    public Uni<GameSession> getGame(@RestCookie String gameId) {
        return GameSession.findById(gameId);
    }

    @POST
    @WithTransaction
    public Uni<GameSession> createGame(@RestCookie String gameId) {
        GameSession gameSession = new GameSession();
        gameSession.gameId = gameId;
        return GameSession.deleteById(gameId)
                .chain(gameSession::persist);
    }

    @PUT
    @Path("/start")
    @WithTransaction
    public Uni<Boolean> startGame(@RestCookie String gameId, @RestQuery ResolutionContext.Builder resolutionContext) {
        return findGameSession(gameId)
                .chain(gameSession -> gameSession.start(resolutionContext));
    }

    @GET
    @WithTransaction
    @Path("/task/current")
    public Uni<UnresolvedResult.ResolvedResult> currentTask(@RestCookie String gameId, @RestQuery ResolutionContext.Builder resolutionContext) {
        return getTaskUni(resolutionContext, gameId, gameSession -> gameSession.currentTask(resolutionContext));
    }

    @PUT
    @WithTransaction
    @Path("/task/next")
    public Uni<UnresolvedResult.ResolvedResult> nextTask(@RestCookie String gameId, @RestQuery ResolutionContext.Builder resolutionContext) {
        return getTaskUni(resolutionContext, gameId, gameSession -> gameSession.nextTask(resolutionContext));
    }

    private Uni<UnresolvedResult.ResolvedResult> getTaskUni(ResolutionContext.Builder resolutionContext,
                                                            String gameId, Function<? super GameSession, Uni<? extends ResolvedTask>> taskUni) {
        return findGameSession(gameId)
                .onItem().ifNotNull().transformToUni(taskUni)
                .onItem().ifNotNull().transformToUni(resolvedTask ->
                        resolvedTask.resolve(resolutionContext.build()).resolve())
                .onItem().ifNull().fail()
                .onFailure().recoverWithNull();
    }

    private Uni<GameSession> findGameSession(String gameId) {
        return GameSession.<GameSession>findById(gameId)
                .onItem().ifNull().failWith(new WebApplicationException("Unable to find game session"));
    }
}
