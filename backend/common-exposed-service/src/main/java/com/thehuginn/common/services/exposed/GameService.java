package com.thehuginn.common.services.exposed;

import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
public interface GameService {

    @GET
    Uni<?> getGame(@RestCookie String gameId);

    @POST
    Uni<?> createGame(@RestCookie String gameId);

    @DELETE
    Uni<Boolean> deleteGame(@RestCookie String gameId);

    @PUT
    @Path("/start")
    Uni<Boolean> startGame(@RestCookie String gameId, @RestQuery ResolutionContext.Builder resolutionContext);

    @GET
    @Path("/task/current")
    Uni<?> currentTask(@RestCookie String gameId, @RestCookie String locale,
            @RestQuery ResolutionContext.Builder resolutionContext);

    @PUT
    @Path("/task/next")
    Uni<?> nextTask(@RestCookie String gameId, @RestCookie String locale,
            @RestQuery ResolutionContext.Builder resolutionContext);
}