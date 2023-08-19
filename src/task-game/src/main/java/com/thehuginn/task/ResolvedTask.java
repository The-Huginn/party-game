package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.Resolvable;
import com.thehuginn.resolution.ResolvedResult;
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
public class ResolvedTask extends PanacheEntity implements Resolvable<ResolvedResult> {

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
        resolvedTask.tokens = gameTask.unresolvedTask.tokens.stream()
                .map(resolvedTokenResolvable -> resolvedTokenResolvable.resolve(resolutionContext))
                .toList();
        return resolvedTask;
    }

    @Override
    // TODO add title, current player etc...
    public ResolvedResult resolve(ResolutionContext context) {
        ResolvedResult resolvedResult = unresolvedTask.task.resolve(context);
        for (ResolvedToken token : tokens) {
            resolvedResult.addResolvedResult(token.resolve(context));
        }
        return resolvedResult;
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return tokens.stream().allMatch(resolvedToken -> resolvedToken.isResolvable(context));
    }
}
