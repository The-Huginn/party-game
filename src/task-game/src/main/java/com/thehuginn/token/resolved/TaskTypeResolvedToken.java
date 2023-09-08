package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.task.Task;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class TaskTypeResolvedToken extends AbstractResolvedToken {

    private static final String tag = "task_type";
    Task.Type type;

    public TaskTypeResolvedToken() {
    }

    public TaskTypeResolvedToken(Task unresolvedTask) {
        type = unresolvedTask.type;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        return new UnresolvedResult().appendData(Map.entry(tag, Uni.createFrom().item(type.toString())));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
