package com.thehuginn.service;

import com.thehuginn.entities.Game;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.concurrent.atomic.AtomicBoolean;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class GameService {

    @POST
    @WithTransaction
    public Uni<RestResponse<Game>> createGame(String gameId) {
        AtomicBoolean created = new AtomicBoolean(false);
        return Panache.withTransaction(() -> Game.<Game>findById(gameId).replaceIfNullWith(() -> {
                created.set(true);
                Game game = new Game(gameId);
                game.persist();
                return game;
                }))
                .onItem()
                .transform(game -> RestResponse.ResponseBuilder.ok(game)
                        .status(created.get() ? RestResponse.Status.CREATED : RestResponse.Status.NOT_MODIFIED)
                        .cookie(new NewCookie.Builder("gameId").value(gameId).build())
                        .build());
    }

    @GET
    @WithSession
    public Uni<Game> getGame(@RestQuery String gameId) {
        return Game.findById(gameId);
    }

    @PUT
    @WithTransaction
    @Path("/status")
    public Uni<Void> updateStatus(@RestCookie String gameId, Game.State newState) {
        return Game.<Game>findById(gameId)
                .onItem()
                .ifNotNull()
                .invoke(game -> game.state = newState)
                .replaceWithVoid();
    }
}
