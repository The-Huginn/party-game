package com.thehuginn.resolution;

import io.smallrye.mutiny.Uni;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnresolvedResult {

    private Map.Entry<String, Uni<String>> task;
    private final Map<String, ? super Object> data = new LinkedHashMap<>();

    public Uni<ResolvedResult> resolve() {
        return fetchTask()
                .map(taskEntry -> new ResolvedResult(taskEntry, data));
    }

    private Uni<Map.Entry<String, String>> fetchTask() {
        if (task == null) {
            throw new RuntimeException("Missing task to resolve");
        }
        return task.getValue()
                .map(s -> Map.entry(task.getKey(), s));
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

    public UnresolvedResult appendData(Map.Entry<String, ?> entry) {
        data.put(entry.getKey(), entry.getValue());
        return this;
    }

    public static class ResolvedResult {

        private final Map<String, Object> data;

        private ResolvedResult(Map.Entry<String, String> task, Map<String, Object> data) {
            this.data = data;
            String resolvedTask = task.getValue();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (TokenResolver.isToken(entry.getKey())) {
                    resolvedTask = (resolvedTask.replace(entry.getKey(), entry.getValue().toString()));
                }
            }
            this.data.put(task.getKey(), resolvedTask);
            this.data.put("task", task.getKey());
        }

        public Map<String, Object> getData() {
            return data;
        }
    }
}
