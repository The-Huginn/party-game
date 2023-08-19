package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.Resolvable;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.token.resolved.AbstractResolvedToken;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ResolvedTask extends PanacheEntity implements Resolvable<UnresolvedResult> {

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Task unresolvedTask;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            targetEntity = AbstractResolvedToken.class,
            orphanRemoval = true,
            mappedBy = "resolvedTask"
    )
    public List<ResolvedToken> tokens = new ArrayList<>();


    public ResolvedTask() {}

    public static ResolvedTask resolve(GameTask gameTask, ResolutionContext resolutionContext) {
        ResolvedTask resolvedTask = new ResolvedTask();
        resolvedTask.unresolvedTask = gameTask.unresolvedTask;
        resolvedTask.tokens.addAll(gameTask.unresolvedTask.tokens.stream()
                .map(resolvedTokenResolvable -> {
                    ResolvedToken resolvedToken = resolvedTokenResolvable.resolve(resolutionContext);
                    ((AbstractResolvedToken) resolvedToken).resolvedTask = resolvedTask;
                    return resolvedToken;
                })
                .toList());
        return resolvedTask;
    }

    @Override
    // TODO add title, current player etc...
    public UnresolvedResult resolve(ResolutionContext context) {
        UnresolvedResult unresolvedResult = unresolvedTask.task.resolve(context);
        for (ResolvedToken token : tokens) {
            unresolvedResult.addResolvedResult(token.resolve(context));
        }
        return unresolvedResult;
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return tokens.stream().allMatch(resolvedToken -> resolvedToken.isResolvable(context));
    }
}
