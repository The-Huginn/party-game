package com.thehuginn.common.game;

import com.thehuginn.common.game.task.AbstractTask;
import com.thehuginn.common.game.translation.LocaleTaskText;
import com.thehuginn.common.game.translation.TaskText;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RunOnVertxContext
public class TaskTextTest {

    @BeforeEach
    @AfterEach
    @RunOnVertxContext
    public void setup(UniAsserter asserter) {
        asserter.execute(() -> TaskText.deleteAll());
        asserter.execute(() -> AbstractTask.deleteAll());
        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testPersistingTask(UniAsserter asserter) {
        AbstractTask task = new AbstractTask();
        TaskText taskText = new TaskText();
        taskText.task = task;
        taskText.content = "test";
        task.task = taskText;
        asserter.execute(() -> task.persistAndFlush());
        asserter.assertThat(() -> TaskText.<TaskText> findAll().firstResult(), TaskText1 -> {
            Assertions.assertEquals("test", TaskText1.content);
            Assertions.assertEquals("en", TaskText1.locale);
        });

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }

    @Test
    void testTranslatingTask(UniAsserter asserter) {
        AbstractTask task = new AbstractTask();
        TaskText taskText = new TaskText();
        taskText.task = task;
        taskText.content = "test";
        task.task = taskText;
        LocaleTaskText localeTaskText = new LocaleTaskText(taskText, "sk", "Slovensky test");
        asserter.execute(() -> task.persistAndFlush());
        asserter.execute(() -> localeTaskText.persistAndFlush());
        asserter.assertThat(() -> LocaleTaskText.<LocaleTaskText> findAll().firstResult(), localeTaskText1 -> {
            Assertions.assertEquals("Slovensky test", localeTaskText1.content);
            Assertions.assertEquals("sk", localeTaskText1.locale);
        });

        ResolutionContext resolutionContext = ResolutionContext.locale("sk");
        asserter.assertThat(
                () -> TaskText.<TaskText> findAll().firstResult()
                        .chain(taskText1 -> taskText1.translate(resolutionContext).getValue()),
                content -> Assertions.assertEquals("Slovensky test", content));

        asserter.assertThat(() -> TaskText.<TaskText> findAll().firstResult(),
                taskText1 -> Assertions.assertEquals(taskText1.task.getKey(), taskText1.translate(resolutionContext).getKey()));

        asserter.surroundWith(uni -> Panache.withSession(() -> uni));
    }
}
