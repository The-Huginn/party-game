package com.thehuginn.common.game;

import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractGameSession extends PanacheEntityBase {

    @Id
    public String gameId;

    public AbstractGameSession() {
    }

    public AbstractGameSession(String gameId) {
        this.gameId = gameId;
    }

    public abstract Uni<Boolean> start(ResolutionContext.Builder resolutionContext);

    public abstract <T> T currentTask(ResolutionContext.Builder resolutionContextBuilder);

    public abstract <T> T nextTask(ResolutionContext.Builder resolutionContextBuilder);
}
