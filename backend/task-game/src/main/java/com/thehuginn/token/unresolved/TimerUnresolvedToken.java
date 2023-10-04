package com.thehuginn.token.unresolved;

import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.task.ResolvedToken;
import com.thehuginn.token.resolved.TimerResolvedToken;
import jakarta.persistence.Entity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class TimerUnresolvedToken extends AbstractUnresolvedToken {

    public TimerUnresolvedToken() {
    }

    public TimerUnresolvedToken(String key) {
        super(key);
    }

    @Override
    public ResolvedToken resolve(ResolutionContext context) {
        return new TimerResolvedToken(getKey());
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
