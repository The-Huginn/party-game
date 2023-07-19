package com.thehuginn.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class GameTask extends PanacheEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    public Task unresolvedTask;

    // List of players, who already answered this question in case of type PER_PLAYER
    public GameTask() {}

    public ResolvedTask resolve() {
        return new ResolvedTask();
    }
}
