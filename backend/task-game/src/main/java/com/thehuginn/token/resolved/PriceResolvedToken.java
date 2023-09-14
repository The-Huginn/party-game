package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.task.ResolvedToken;
import com.thehuginn.task.Task;

import java.util.Map;

public class PriceResolvedToken implements ResolvedToken {

    private static final String tag = "price";
    private final Task.Price price;

    public PriceResolvedToken(Task unresolvedTask) {
        this.price = unresolvedTask.price;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        return new UnresolvedResult().appendData(Map.entry(tag, price));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
