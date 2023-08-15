package com.thehuginn.task;

import com.thehuginn.resolution.ResolutionContext;

public interface ResolvableTask {

    public ResolvedTask resolve(ResolutionContext context);
}
