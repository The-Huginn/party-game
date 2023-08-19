package com.thehuginn.resolution;

import io.smallrye.mutiny.Uni;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnresolvedResult {

    // This needs to be ordered map see #resolveUnis()
    private Map.Entry<String, Uni<String>> task;
    private final Map<String, Uni<?>> data = new LinkedHashMap<>();

    public Uni<ResolvedResult> resolve() {
        //noinspection unchecked
        return Uni.combine()
                .all()
                .unis(fetchTask(), fetchData())
                .usingConcurrencyOf(1)
                .combinedWith(tuple2 -> new ResolvedResult((Map.Entry<String, String>) tuple2.get(0), (Map<String, Object>) tuple2.get(1)));
    }

    private Uni<Map.Entry<String, String>> fetchTask() {
        if (task == null) {
            throw new RuntimeException("Missing task to resolve");
        }
        return task.getValue()
                .map(s -> Map.entry(task.getKey(), s));
    }

    private Uni<Map<String, Object>> fetchData() {
        // we rely on the order of the unis retrieved and then returned back
        //  as resolved objects
        List<? extends Uni<?>> dataUnis = data.values().stream()
                .map(o -> (Uni<?>) o)
                .toList();
        return Uni.createFrom()
                .item(this)
                .chain(unresolvedResult -> Uni.combine()
                        .all()
                        .unis(dataUnis)
                        .usingConcurrencyOf(1)
                        .combinedWith(objects -> {
                            assert objects.size() == data.size();
                            Map<String, Object> result = new LinkedHashMap<>(data);
                            Iterator<?> iterator = objects.iterator();
                            for (Map.Entry<String, Object> entry : result.entrySet()) {
                                entry.setValue(iterator.next());
                            }

                            return result;
                        })
                );
    }

    public void addResolvedResult(UnresolvedResult other) {
        data.putAll(other.data);
    }

    public UnresolvedResult task(Map.Entry<String, Uni<String>> task) {
        if (this.task != null) {
            throw new RuntimeException("Reassignment of existing task");
        }
        this.task = task;
        return this;
    }

    public UnresolvedResult appendData(Map.Entry<String, Uni<?>> entry) {
        data.put(entry.getKey(), entry.getValue());
        return this;
    }

    public static class ResolvedResult {

        private final Map<String, Object> data;

        private ResolvedResult(Map.Entry<String, String> task, Map<String, Object> data) {
            this.data = data;
            String resolvedTask = task.getValue();
            for (Map.Entry<String, Object> entry: data.entrySet()) {
                if (TokenResolver.isToken(entry.getKey())) {
                    resolvedTask = (resolvedTask.replace(entry.getKey(), entry.getValue().toString()));
                }
            }
            this.data.put(task.getKey(), resolvedTask);
        }

        public Map<String, Object> getData() {
            return data;
        }
    }
}
