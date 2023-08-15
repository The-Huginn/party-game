package com.thehuginn.token.unresolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.token.resolved.PlayerResolvedToken;
import com.thehuginn.token.resolved.ResolvedToken;
import jakarta.persistence.Entity;

@Entity
public class PlayerUnresolvedToken extends AbstractUnresolvedToken {

    public PlayerUnresolvedToken() {}

    public PlayerUnresolvedToken(String key) {
        super(key);
    }

    @Override
    public ResolvedToken resolve(ResolutionContext context) {
        return PlayerResolvedToken.getInstance(getKey(), context);
    }
}
