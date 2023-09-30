package com.thehuginn;

import com.thehuginn.resolution.ResolutionContext;
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

    public abstract Uni<Boolean> addCategory(Long categoryId);

    public abstract Uni<Boolean> removeCategory(Long categoryId);

    public abstract Uni<Boolean> start(ResolutionContext.Builder resolutionContext);

    public abstract <T> T currentTask(ResolutionContext.Builder resolutionContextBuilder);

    public abstract <T> T nextTask(ResolutionContext.Builder resolutionContextBuilder);
}
