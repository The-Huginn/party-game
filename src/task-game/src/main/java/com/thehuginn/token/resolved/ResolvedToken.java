package com.thehuginn.token.resolved;

import com.thehuginn.task.ResolutionContext;

public interface ResolvedToken {

    ResolvedResult resolve(ResolutionContext context, ResolvedResult result);
}
