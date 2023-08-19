package com.thehuginn.services;

import com.thehuginn.resolution.GameSession;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

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
        return gameSession.persist();
    }

    @PUT
    @WithTransaction
    @Path("/category/{categoryId}")
    public Uni<Boolean> addCategory(@RestCookie String gameId, @RestPath Long categoryId) {
        return GameSession.<GameSession>findById(gameId)
                .chain(gameSession -> gameSession.addCategory(categoryId));
    }

    @DELETE
    @WithTransaction
    @Path("/category/{categoryId}")
    public Uni<Boolean> removeCategory(@RestCookie String gameId, @RestPath Long categoryId) {
        return GameSession.<GameSession>findById(gameId)
                .chain(gameSession -> gameSession.removeCategory(categoryId));
    }

    @GET
    @WithTransaction
    @Path("/task/current")
    public Uni<UnresolvedResult.ResolvedResult> currentTask(@RestCookie String gameId, @RestQuery ResolutionContext resolutionContext) {
        return GameSession.<GameSession>findById(gameId)
                .onItem()
                .ifNull()
                .failWith(new WebApplicationException("Unable to find game session"))
                .onItem()
                .ifNotNull()
                .transformToUni(gameSession -> Uni.createFrom()
                        .item(gameSession.currentTask)
                        .onItem()
                        .ifNull()
                        .switchTo(gameSession.nextTask(resolutionContext))
                )
                .onItem()
                .ifNotNull()
                .transformToUni(resolvedTask -> resolvedTask.resolve(resolutionContext)
                        .resolve())
                .onItem()
                .ifNull()
                .fail()
                .onFailure()
                .recoverWithNull();
    }
}
