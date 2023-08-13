package com.thehuginn.token.unresolved;

import com.thehuginn.task.ResolutionContext;
import com.thehuginn.token.resolved.ResolvedToken;
import io.smallrye.mutiny.Uni;

public interface Token {

    Uni<? extends ResolvedToken> resolve(ResolutionContext context);
}
