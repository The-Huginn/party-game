package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.Resolvable;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.token.resolved.AbstractResolvedToken;
import com.thehuginn.token.resolved.PairsResolvedToken;
import com.thehuginn.token.resolved.PlayerResolvedToken;
import com.thehuginn.token.resolved.TaskTypeResolvedToken;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class ResolvedTask extends PanacheEntity implements Resolvable<UnresolvedResult> {

    @OneToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    public GameTask gameTask;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = AbstractResolvedToken.class, orphanRemoval = true, mappedBy = "resolvedTask")
    public List<ResolvedToken> tokens = new ArrayList<>();

    public ResolvedTask() {
    }

    public static ResolvedTask resolve(GameTask gameTask, ResolutionContext resolutionContext) {
        ResolvedTask resolvedTask = new ResolvedTask();
        resolvedTask.gameTask = gameTask;
        List<ResolvedToken> tokens = gameTask.unresolvedTask.tokens.stream()
                .map(unresolvedToken -> unresolvedToken.resolve(resolutionContext))
                .collect(Collectors.toList());
        tokens.add(PlayerResolvedToken.getPlayer(resolutionContext));

        if (gameTask.unresolvedTask.type == Task.Type.DUO) {
            tokens.add(new PairsResolvedToken(resolutionContext.getPlayers()));
        }

        resolvedTask.tokens.addAll(tokens.stream()
                .peek(resolvedToken -> ((AbstractResolvedToken) resolvedToken).resolvedTask = resolvedTask)
                .toList());
        return resolvedTask;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        UnresolvedResult unresolvedResult = gameTask.unresolvedTask.task.resolve(context);
        for (ResolvedToken token : tokens) {
            unresolvedResult.addResolvedResult(token.resolve(context));
        }
        unresolvedResult.addResolvedResult(new TaskTypeResolvedToken(gameTask.unresolvedTask).resolve(context));
        return unresolvedResult;
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return tokens.stream().allMatch(resolvedToken -> resolvedToken.isResolvable(context));
    }

    public Uni<Void> remove() {
        if (gameTask.unresolvedTask.isRemovable()) {
            return gameTask.delete();
        }
        return Uni.createFrom().voidItem();
    }
}
