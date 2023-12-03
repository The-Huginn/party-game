package com.thehuginn.task;

import com.thehuginn.common.game.task.AbstractTask;
import com.thehuginn.common.game.translation.LocaleTaskText;
import com.thehuginn.common.game.translation.TaskText;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Entity
@DiscriminatorValue("1")
public class PubTask extends AbstractTask {

    public static Uni<PubTask> createPubTask(
            String task,
            Map<String, String> translations) {
        PubTask pubTask = new PubTask();
        pubTask.task = new TaskText(pubTask, "en", task);
        List<Uni<LocaleTaskText>> localeTaskTexts = new ArrayList<>();
        for (Map.Entry<String, String> translation : translations.entrySet()) {
            localeTaskTexts
                    .add(new LocaleTaskText(pubTask.task, translation.getKey(), translation.getValue()).persistAndFlush());
        }

        if (localeTaskTexts.isEmpty()) {
            return pubTask.persistAndFlush();
        }

        return pubTask.<PubTask> persistAndFlush()
                .call(() -> Uni.combine().all().unis(localeTaskTexts)
                        .usingConcurrencyOf(1)
                        .discardItems());
    }

    public static Uni<List<? extends AbstractTask>> generateTasks() {
        return PubTask.<PubTask> listAll(Sort.ascending("id"))
                .map(pubTasks -> {
                    if (pubTasks.size() < 13) {
                        throw new IllegalStateException("Not enough Pub Tasks in the database");
                    }
                    List<PubTask> rules = new ArrayList<>(List.of(pubTasks.get(0), pubTasks.get(1)));
                    pubTasks.removeAll(rules);

                    Collections.shuffle(pubTasks);
                    rules.addAll(pubTasks.subList(0, 11));
                    Collections.swap(rules, 1, 7);
                    return rules;
                });
    }
}
