package com.thehuginn.token.resolved;

import com.thehuginn.common.game.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.task.ResolvedToken;
import com.thehuginn.task.Task;

import java.util.Map;

public class TaskTypeResolvedToken implements ResolvedToken {

    private static final String tag = "task_type";
    private final Task.Type type;

    public TaskTypeResolvedToken(Task unresolvedTask) {
        type = unresolvedTask.type;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        return new UnresolvedResult().appendData(Map.entry(tag, type.toString()));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
