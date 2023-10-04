package com.thehuginn.services.exposed;

import com.thehuginn.GameSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestCookie;

@Path("neverEver-mode")
@RequestScoped
public class NeverEverGameCreationService {

    @POST
    @WithTransaction
    public Uni<Boolean> setNeverEverMode(@RestCookie String gameId) {
        return GameSession.update("type = :type where gameId = :gameId ",
                Parameters.with("type", GameSession.GameType.NEVER_EVER_MODE).and("gameId", gameId))
                .map(changed -> changed == 1);
    }
}
