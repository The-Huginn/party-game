package com.thehuginn.service;

import com.thehuginn.entities.Game;
import com.thehuginn.entities.Player;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestCookie;

import java.util.List;

@Path("/")
public class PlayerService {

    @GET
    @Path("/team")
    public Uni<List<Player>> getTeam(@RestCookie String gameId) {
        if (gameId == null) {
            throw new IllegalStateException("Missing Cookie");
        }

        return Game.<Game>findById(gameId)
                .onItem()
                .transform(game -> game.team);
    }

    @POST
    @Path("/player")
    @WithTransaction
    public Uni<Player> addPlayer(@RestCookie String gameId, Player newPlayer) {
        return Game.<Game>findById(gameId)
                .onItem()
                .ifNotNull()
                .transform(game -> game.addPlayer(newPlayer));
    }

    @DELETE
    @Path("/player")
    @WithTransaction
    public Uni<Boolean> removePlayer(@RestCookie String gameId, Long playerId) {
        return Game.<Game>findById(gameId)
                .onItem()
                .transform(game -> game.removePlayer(playerId));

    }
}
