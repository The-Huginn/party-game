package com.thehuginn.token.resolved;

import com.thehuginn.common.game.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import jakarta.persistence.Entity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;

import static com.thehuginn.util.Helper.ITALIC;
import static com.thehuginn.util.Helper.UNDERLINED;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class PlayerResolvedToken extends AbstractResolvedToken {

    private static final String PLAYER_TAG = "player";
    String playerTag;
    String player;

    public PlayerResolvedToken() {
    }

    public PlayerResolvedToken(String playerTag, String player) {
        this.playerTag = playerTag;
        this.player = UNDERLINED.formatted(player);
    }

    public static PlayerResolvedToken getPlayer(ResolutionContext resolutionContext) {
        PlayerResolvedToken playerResolvedToken = new PlayerResolvedToken();
        playerResolvedToken.playerTag = PLAYER_TAG;
        playerResolvedToken.player = ITALIC.formatted(resolutionContext.getPlayer());
        return playerResolvedToken;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        return new UnresolvedResult().appendData(Map.entry(playerTag, player));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
