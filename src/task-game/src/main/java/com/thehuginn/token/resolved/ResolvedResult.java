package com.thehuginn.token.resolved;

import io.smallrye.mutiny.Uni;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResolvedResult {

    private boolean resolved = false;
    private String resolvedMessage;

    private Uni<StringBuilder> message = Uni.createFrom().item(StringBuilder::new);
    // This needs to be ordered map see #resolve()
    private Map<String, Object> data = new LinkedHashMap<>();
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
                .invoke(stringBuilder -> resolvedMessage = stringBuilder.toString())
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

    public String getMessage() {
        if (!resolved) {
            throw new IllegalStateException("First call " + ResolvedResult.class + "#resolve() method to resolve unis");
        }
        return resolvedMessage;
    }

    public Map<String, Object> getData() {
        if (!resolved) {
            throw new IllegalStateException("First call " + ResolvedResult.class + "#resolve() method to resolve unis");
        }
        return data;
    }

    public ResolvedResult appendMessage(Uni<String> message) {
        this.message = this.message
                .onItem()
                .call(stringBuilder -> message.onItem()
                        .invoke(stringBuilder::append));
        return this;
    }

    public ResolvedResult appendData(Map.Entry<String, Uni<?>> entry) {
        data.put(entry.getKey(), entry.getValue());
        return this;
    }
}
