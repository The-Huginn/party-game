package com.thehuginn.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.thehuginn.entities.GameContext;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/game")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface GameRestClient {

    @POST
    Uni<JsonNode> createGame(@RestCookie String gameId);

    @PUT
    @Path("/start")
    Uni<Boolean> startGame(@RestCookie String gameId, @RestQuery GameContext resolutionContext);

    @GET
    @Path("/task/current")
    Uni<JsonNode> currentTask(@RestCookie String gameId, @RestQuery GameContext resolutionContext);

    @PUT
    @Path("/task/next")
    Uni<JsonNode> nextTask(@RestCookie String gameId, @RestQuery GameContext resolutionContext);
}
