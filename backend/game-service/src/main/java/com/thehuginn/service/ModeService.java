package com.thehuginn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.thehuginn.entities.Game;
import com.thehuginn.external.GameRestClientTask;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestCookie;

@Path("/mode")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ModeService {

    @RestClient
    GameRestClientTask taskRestClient;

    @GET
    @Path("/exists")
    public Uni<JsonNode> getGame(@RestCookie String gameId) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game -> switch (game.type) {
                    case TASK -> taskRestClient.getGame(gameId);
                    case NONE -> Uni.createFrom().nullItem();
                });
    }

    @POST
    @Path("/create")
    public Uni<JsonNode> createGameMode(@RestCookie String gameId) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game1 -> switch (game1.type) {
                    case TASK -> taskRestClient.createGame(gameId);
                    case NONE -> Uni.createFrom().nullItem();
                });
    }

    @PUT
    @Path("/start")
    public Uni<Boolean> startGame(@RestCookie String gameId) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game1 -> switch (game1.type) {
                    case TASK -> taskRestClient.startGame(gameId, game1.gameContext());
                    case NONE -> Uni.createFrom().item(Boolean.FALSE);
                })
                .onItem().ifNull().continueWith(Boolean.FALSE);
    }

    @GET
    @Path("/current")
    public Uni<JsonNode> currentTask(@RestCookie String gameId, @RestCookie String locale) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game1 -> switch (game1.type) {
                    case TASK -> taskRestClient.currentTask(gameId, locale, game1.gameContext());
                    case NONE -> Uni.createFrom().nullItem();
                });
    }

    @PUT
    @Path("/next")
    public Uni<JsonNode> nextTask(@RestCookie String gameId, @RestCookie String locale) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game1 -> switch (game1.type) {
                    case TASK -> taskRestClient.nextTask(gameId, locale, game1.gameContext());
                    case NONE -> Uni.createFrom().nullItem();
                });
    }
}
