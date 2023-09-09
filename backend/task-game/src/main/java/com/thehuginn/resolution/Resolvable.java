package com.thehuginn.resolution;

public interface Resolvable<T> {

    T resolve(ResolutionContext context);

    boolean isResolvable(ResolutionContext context);
}
