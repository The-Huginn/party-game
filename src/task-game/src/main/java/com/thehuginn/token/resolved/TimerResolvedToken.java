package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.ResolvedResult;
import com.thehuginn.resolution.TokenResolver;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

public class TimerResolvedToken extends AbstractResolvedToken {

    String timerTag;

    @Override
    public ResolvedResult resolve(ResolutionContext context) {
        List<String> args = TokenResolver.resolveToken(timerTag).getItem2();
        if (args.isEmpty()) {
            throw new IllegalStateException(TimerResolvedToken.class + "#resolve requires at least one parameter");
        }
        // TODO add delay for autostarted timer
        String duration = args.get(0);
        if (!duration.matches("\\d+")) {
            throw new IllegalArgumentException(TimerResolvedToken.class + "#resolve expects integer");
        }
        Integer durationValue = Integer.getInteger(duration);
        return new ResolvedResult().appendData(Map.entry(timerTag, Uni.createFrom().item(durationValue)));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
