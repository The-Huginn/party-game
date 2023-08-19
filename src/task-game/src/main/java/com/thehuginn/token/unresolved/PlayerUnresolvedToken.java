package com.thehuginn.token.unresolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.task.ResolvedToken;
import com.thehuginn.token.resolved.PlayerResolvedToken;
import jakarta.persistence.Entity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class PlayerUnresolvedToken extends AbstractUnresolvedToken {

    public PlayerUnresolvedToken() {}

    public PlayerUnresolvedToken(String key) {
        super(key);
    }

    @Override
    public ResolvedToken resolve(ResolutionContext context) {
        Integer playerIndex = getPlayerIndex();
        return new PlayerResolvedToken(key, context.getRandomPlayer(playerIndex));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        Integer index = getPlayerIndex();
        return index.compareTo(0) > 0 && index.compareTo(context.getPlayers().size()) < 0;
    }

    private Integer getPlayerIndex() throws IllegalStateException, IllegalArgumentException {
        List<String> args = TokenResolver.resolveToken(key).getItem2();
        if (args.size() != 1) {
            throw new IllegalStateException(PlayerUnresolvedToken.class + "#resolve requires one parameter");
        }
        String player = args.get(0);
        if (player.equals("c")) {
            return 0;
        }
        if (!player.matches("\\d+")) {
            throw new IllegalArgumentException(PlayerUnresolvedToken.class + "#resolve expects integer or 'c' argument");
        }

        return Integer.valueOf(player);
    }
}
