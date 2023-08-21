package com.thehuginn.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.thehuginn.entities.GameContext;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/game")
@RegisterRestClient(configKey = "task-api")
public interface GameRestClientTask extends GameRestClient {

    @POST
    public Uni<JsonNode> createGame(@RestCookie String gameId);

    @PUT
    @Path("/start")
    Uni<Boolean> startGame(@RestCookie String gameId, @RestQuery GameContext game);

    @GET
    @Path("/task/current")
    Uni<JsonNode> currentTask(@RestCookie String gameId, @RestQuery GameContext game);

    @GET
    @Path("/task/next")
    public Uni<JsonNode> nextTask(@RestCookie String gameId, @RestQuery GameContext game);
}
