package com.thehuginn.token.unresolved;

import com.thehuginn.task.ResolutionContext;
import com.thehuginn.token.resolved.PlayerResolvedToken;
import com.thehuginn.token.resolved.ResolvedToken;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;

@Entity
public class PlayerUnresolvedToken extends AbstractUnresolvedToken {

    public PlayerUnresolvedToken() {}

    public PlayerUnresolvedToken(String key) {
        super(key);
    }

    @Override
    public Uni<? extends ResolvedToken> resolve(ResolutionContext context) {
        return PlayerResolvedToken.getInstance(getKey(), context);
    }
}
