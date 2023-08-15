package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.ResolvedResult;

public interface ResolvedToken {

    ResolvedResult resolve(ResolutionContext context, ResolvedResult result);
}
