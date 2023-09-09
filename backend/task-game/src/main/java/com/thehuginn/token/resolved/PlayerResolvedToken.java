package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class PlayerResolvedToken extends AbstractResolvedToken {

    String playerTag;
    String player;

    public PlayerResolvedToken() {
    }

    public PlayerResolvedToken(String playerTag, String player) {
        this.playerTag = playerTag;
        this.player = player;
    }

    public static PlayerResolvedToken getPlayer(ResolutionContext resolutionContext) {
        return new PlayerResolvedToken("player", resolutionContext.getPlayer());
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
