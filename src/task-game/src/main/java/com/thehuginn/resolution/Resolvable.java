package com.thehuginn.resolution;

public interface Resolvable<T> {

    public T resolve(ResolutionContext context);

    public boolean isResolvable(ResolutionContext context);
}
