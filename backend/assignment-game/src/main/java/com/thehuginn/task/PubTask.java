package com.thehuginn.task;

import com.thehuginn.common.game.task.AbstractTask;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
@DiscriminatorValue("1")
public class PubTask extends AbstractTask {

    public static List<PubTask> generateTasks() {
        return null;
    }
}
