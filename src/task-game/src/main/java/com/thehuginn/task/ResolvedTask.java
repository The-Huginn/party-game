package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.token.resolved.AbstractResolvedToken;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
public class ResolvedTask extends PanacheEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Task unresolvedTask;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            targetEntity = AbstractResolvedToken.class
    )
    public List<ResolvedToken> tokens;


    public ResolvedTask() {}

    public static ResolvedTask resolve(GameTask gameTask, ResolutionContext resolutionContext) {
        ResolvedTask resolvedTask = new ResolvedTask();
        resolvedTask.unresolvedTask = gameTask.unresolvedTask;
        resolvedTask.tokens = resolvedTask.unresolvedTask.tokens.stream()
                .map(resolvedTokenResolvable -> resolvedTokenResolvable.resolve(resolutionContext))
                .toList();
        return resolvedTask;
    }
}
