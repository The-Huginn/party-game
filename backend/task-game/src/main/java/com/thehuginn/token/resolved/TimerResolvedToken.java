package com.thehuginn.token.resolved;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thehuginn.common.game.resolution.ResolutionContext;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.resolution.UnresolvedResult;
import io.quarkus.logging.Log;
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

    public TimerResolvedToken() {
    }

    public TimerResolvedToken(String key) {
        this.timerTag = key;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        List<String> args = TokenResolver.resolveToken(timerTag).getItem2();
        if (args.isEmpty()) {
            Log.error(TimerResolvedToken.class + "#resolve requires at least one parameter");
            throw new IllegalStateException(TimerResolvedToken.class + "#resolve requires at least one parameter");
        }
        String durationString = args.get(0);
        if (!durationString.matches("\\d+")) {
            Log.error(TimerResolvedToken.class + "#resolve expects integer");
            throw new IllegalArgumentException(TimerResolvedToken.class + "#resolve expects integer");
        }
        int duration = Integer.parseInt(durationString);
        int delay = -1;
        if (args.size() == 2) {
            String delayString = args.get(1);

            if (!delayString.matches("-?\\d+")) {
                Log.error(TimerResolvedToken.class + "#resolve expects integer");
                throw new IllegalArgumentException(TimerResolvedToken.class + "#resolve expects integer");
            }

            delay = Integer.parseInt(delayString);
        } else if (args.size() > 2) {
            Log.error("Timer with unexpected argument [%s].".formatted(timerTag));
            throw new IllegalArgumentException("Timer with unexpected argument [%s].".formatted(timerTag));
        }
        return new UnresolvedResult().appendData(Map.entry(timerTag, duration + "s"))
                .appendData(Map.entry(TIMER_KEY, new Timer(duration, delay)));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Timer {
        public int duration;
        public Integer delay;
        public boolean autostart = true;

        private Timer(int duration, int delay) {
            this.duration = duration;
            if (delay >= 0) {
                this.delay = delay;
            } else {
                autostart = false;
            }
        }
    }
}
