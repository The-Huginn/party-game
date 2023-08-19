package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;

import java.util.Map;

@Entity
public class PlayerResolvedToken extends AbstractResolvedToken {

    String playerTag;
    String  player;

    public PlayerResolvedToken() {}

    public PlayerResolvedToken(String playerTag, String player) {
        this.playerTag = playerTag;
        this.player = player;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        return new UnresolvedResult().appendData(Map.entry(playerTag, Uni.createFrom().item(player)));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
