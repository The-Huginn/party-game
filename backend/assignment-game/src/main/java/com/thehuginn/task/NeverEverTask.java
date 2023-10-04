package com.thehuginn.task;

import com.thehuginn.common.game.task.AbstractTask;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
@DiscriminatorValue("2")
public class NeverEverTask extends AbstractTask {

    public static List<NeverEverTask> generateTasks() {
        return null;
    }
}
