package com.thehuginn.resolution;

import com.thehuginn.common.services.exposed.resolution.ResolutionContext;

public interface Resolvable<T> {

    T resolve(ResolutionContext context);

    boolean isResolvable(ResolutionContext context);
}
