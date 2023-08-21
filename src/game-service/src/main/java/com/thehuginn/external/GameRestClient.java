package com.thehuginn.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.thehuginn.entities.GameContext;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;

public interface GameRestClient {

    Uni<Boolean> startGame(@RestCookie String gameId, @RestQuery GameContext game);

    Uni<JsonNode> currentTask(@RestCookie String gameId, @RestQuery GameContext game);

    public Uni<JsonNode> nextTask(@RestCookie String gameId, @RestQuery GameContext game);
}
