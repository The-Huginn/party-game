package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.resolution.UnresolvedResult;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;

import java.util.List;
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

    public static PlayerResolvedToken getCurrentPlayerInstance(ResolutionContext context) {
        return getInstance("{player_c}", context);
    }

    public static PlayerResolvedToken getInstance(String key, ResolutionContext context) {
        List<String> args = TokenResolver.resolveToken(key).getItem2();
        if (args.size() != 1) {
            throw new IllegalStateException(PlayerResolvedToken.class + "#getInstace requires one parameter");
        }
        String player = args.get(0);
        if (!player.equals("c") && !player.matches("\\d+")) {
            throw new IllegalArgumentException(PlayerResolvedToken.class + "#getInstace expects integer or 'c' argument");
        }
        Integer playerIndex = Integer.getInteger(player);
        return new PlayerResolvedToken(key, context.getRandomPlayer(playerIndex));
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
