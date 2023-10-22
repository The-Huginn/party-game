package com.thehuginn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thehuginn.entities.Game;
import com.thehuginn.external.GameRestClientPub;
import com.thehuginn.external.GameRestClientTask;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestPath;

import java.util.function.Function;

@Path("/mode")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ModeService {

    @RestClient
    GameRestClientTask taskRestClient;

    @RestClient
    GameRestClientPub pubRestClient;

    @GET
    @Path("/exists")
    public Uni<JsonNode> getGame(@RestCookie String gameId) {
        return callbackUni(gameId, game -> switch (game.type) {
            case TASK -> taskRestClient.getGame(gameId);
            case PUB -> pubRestClient.getGame(gameId);
            case NONE -> Uni.createFrom().nullItem();
        });
    }

    @DELETE
    @Path("/delete")
    public Uni<Boolean> deleteGameMode(@RestCookie String gameId) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game -> switch (game.type) {
                    case TASK -> taskRestClient.deleteGame(gameId);
                    case PUB -> pubRestClient.deleteGame(gameId);
                    case NONE -> Uni.createFrom().nullItem();
                });
    }

    @POST
    @Path("/create/{type}")
    @WithTransaction
    public Uni<JsonNode> createGameMode(@RestCookie String gameId, @RestPath Game.Type type) {
        return callbackUni(gameId, game -> {
            game.type = type;
            return game.<Game> persist()
                    .<JsonNode> chain(game1 -> switch (game1.type) {
                        case TASK -> taskRestClient.createGame(gameId);
                        case PUB -> pubRestClient.createGame(gameId);
                        case NONE -> Uni.createFrom().nullItem();
                    });
        });
    }

    @PUT
    @Path("/start")
    public Uni<Boolean> startGame(@RestCookie String gameId) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game1 -> switch (game1.type) {
                    case TASK -> taskRestClient.startGame(gameId, game1.gameContext());
                    case PUB -> pubRestClient.startGame(gameId, game1.gameContext());
                    case NONE -> Uni.createFrom().item(Boolean.FALSE);
                })
                .onItem().ifNull().continueWith(Boolean.FALSE);
    }

    @GET
    @Path("/current")
    public Uni<JsonNode> currentTask(@RestCookie String gameId, @RestCookie @DefaultValue("en") String locale) {
        return callbackUni(gameId, game1 -> switch (game1.type) {
            case TASK -> taskRestClient.currentTask(gameId, locale, game1.gameContext());
            case PUB -> pubRestClient.currentTask(gameId, locale, game1.gameContext());
            case NONE -> Uni.createFrom().nullItem();
        });
    }

    @PUT
    @Path("/next")
    public Uni<JsonNode> nextTask(@RestCookie String gameId, @RestCookie @DefaultValue("en") String locale) {
        return callbackUni(gameId, game1 -> switch (game1.type) {
            case TASK -> taskRestClient.nextTask(gameId, locale, game1.gameContext());
            case PUB -> pubRestClient.nextTask(gameId, locale, game1.gameContext());
            case NONE -> Uni.createFrom().nullItem();
        });
    }

    private Uni<JsonNode> callbackUni(String gameId, Function<Game, Uni<JsonNode>> callback) {
        return Game.<Game> findById(gameId)
                .onItem().ifNotNull().transformToUni(game -> callback.apply(game)
                        .invoke(jsonNode -> ((ObjectNode) jsonNode).put("type", game.type.toString())));
    }
}
