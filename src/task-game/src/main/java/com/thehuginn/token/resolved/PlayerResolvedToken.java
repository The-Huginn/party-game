package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.ResolvedResult;
import com.thehuginn.resolution.TokenResolver;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

public class PlayerResolvedToken extends AbstractResolvedToken {

    String playerTag;
    String  player;

    public PlayerResolvedToken() {}

    private PlayerResolvedToken(String playerTag, String player) {
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
    public ResolvedResult resolve(ResolutionContext context, ResolvedResult result) {
        return result.appendMessage(Uni.createFrom().item(playerTag))
                .appendData(Map.entry(playerTag, Uni.createFrom().item(player)));
    }
}
