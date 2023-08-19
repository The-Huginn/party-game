package com.thehuginn.resolution;

import io.smallrye.mutiny.Uni;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResolvedResult {

    private boolean resolved = false;

    // This needs to be ordered map see #resolve()
    private final Map<String, Object> data = new LinkedHashMap<>();
    private String title = null;

    public Uni<ResolvedResult> resolve() {
        // we rely on the order of the unis retrieved and then returned back
        //  as resolved objects
        List<? extends Uni<?>> dataUnis = data.values().stream()
                .map(o -> (Uni<?>) o)
                .toList();
        return Uni.createFrom()
                .item(this)
                .onItem()
                .call(() -> Uni.combine()
                        .all()
                        .unis(dataUnis)
                        .usingConcurrencyOf(1)
                        .combinedWith(objects -> {
                            assert objects.size() == data.size();
                            Iterator<?> iterator = objects.iterator();
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                entry.setValue(iterator.next());
                            }

                            return data;
                        })
                )
                .onItem()
                .invoke(resolvedResult -> resolvedResult.resolved = true);
    }

    public ResolvedResult addResolvedResult(ResolvedResult other) {
        data.putAll(other.data);
        return this;
    }

    public Map<String, Object> getData() {
        if (!resolved) {
            throw new IllegalStateException("First call " + ResolvedResult.class + "#resolve() method to resolve unis");
        }
        return data;
    }

    public ResolvedResult appendData(Map.Entry<String, Uni<?>> entry) {
        data.put(entry.getKey(), entry.getValue());
        return this;
    }
}
