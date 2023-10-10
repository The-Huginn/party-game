package com.thehuginn.task;

import com.thehuginn.common.game.task.AbstractTask;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("1")
public class PubTask extends AbstractTask {

    public static Uni<List<? extends AbstractTask>> generateTasks() {
        return PubTask.<PubTask> listAll(Sort.ascending("id"))
                .map(pubTasks -> {
                    List<PubTask> rules = new ArrayList<>(List.of(pubTasks.get(0)));
                    Collections.shuffle(pubTasks);
                    rules.addAll(pubTasks.subList(0, 12));
                    return rules;
                });
    }
}
