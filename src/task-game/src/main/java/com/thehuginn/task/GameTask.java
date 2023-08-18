package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.token.resolved.AbstractResolvedToken;
import com.thehuginn.token.resolved.ResolvedToken;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
public class GameTask extends PanacheEntity implements ResolvableTask, Cloneable {

    public String game;

    @ManyToOne(fetch = FetchType.EAGER)
    public Task unresolvedTask;

    public String assignedPlayer;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH},
            targetEntity = AbstractResolvedToken.class
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<ResolvedToken> tokens;
    public GameTask() {}

    @Override
    public ResolvedTask resolve(ResolutionContext context) {
        return new ResolvedTask();
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
