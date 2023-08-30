package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.resolution.UnresolvedResult;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.Map;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class TimerResolvedToken extends AbstractResolvedToken {

    private static final String TIMER_KEY = "timer";

    String timerTag;

    public TimerResolvedToken() {}

    public TimerResolvedToken(String key) {
        this.timerTag = key;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        List<String> args = TokenResolver.resolveToken(timerTag).getItem2();
        if (args.isEmpty()) {
            throw new IllegalStateException(TimerResolvedToken.class + "#resolve requires at least one parameter");
        }
        String duration = args.get(0);
        if (!duration.matches("\\d+")) {
            throw new IllegalArgumentException(TimerResolvedToken.class + "#resolve expects integer");
        }
        int durationValue = Integer.parseInt(duration);
        return new UnresolvedResult().appendData(Map.entry(timerTag, Uni.createFrom().item(durationValue + "s")))
                .appendData(Map.entry(TIMER_KEY, Uni.createFrom().item(new Timer(durationValue, 0))));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }

    private static class Timer {
        public int duration;
        public int delay;

        private Timer(int duration, int delay) {
            this.duration = duration;
            this.delay = delay;
        }
    }
}
