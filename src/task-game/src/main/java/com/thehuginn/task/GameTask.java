package com.thehuginn.task;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.Resolvable;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class GameTask extends PanacheEntity implements Resolvable<ResolvedTask>, Cloneable {

    public String game;

    @ManyToOne(fetch = FetchType.EAGER)
    public Task unresolvedTask;

    public String assignedPlayer;

    public GameTask() {
    }

    @Override
    public ResolvedTask resolve(ResolutionContext context) {
        return ResolvedTask.resolve(this, context);
    }

    @Override
    // TODO update for players etc...
    public boolean isResolvable(ResolutionContext context) {
        return assignedPlayer == null || context.getPlayer().equals(assignedPlayer);
    }

    @Override
    public GameTask clone() {
        try {
            return (GameTask) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
