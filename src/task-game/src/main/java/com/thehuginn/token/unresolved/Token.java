package com.thehuginn.token.unresolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.token.resolved.ResolvedToken;

public interface Token {

//    Uni<? extends ResolvedToken> resolve(ResolutionContext context);
    ResolvedToken resolve(ResolutionContext context);
}
