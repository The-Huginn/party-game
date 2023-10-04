package com.thehuginn.common.game.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.common.game.task.AbstractTask;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;
import java.util.Objects;

@Entity
@IdClass(TaskText.TaskTextPK.class)
public class TaskText extends PanacheEntityBase implements Translatable {

    public static final String CONTENT_TAG = "content";

    public static class TaskTextPK {
        public Long task;
        public String locale;

        public TaskTextPK() {
        }

        public TaskTextPK(Long task, String locale) {
            this.task = task;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TaskTextPK pk)) {
                return false;
            } else {
                return Objects.equals(task, pk.task) && locale.equals(pk.locale);
            }
        }

        @Override
        public int hashCode() {
            return task.hashCode() + locale.hashCode();
        }
    }

    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id")
    @JsonIgnore
    public AbstractTask task;

    @Id
    @JsonProperty
    public String locale = "en";

    @JsonProperty
    @Column(unique = true)
    public String content = "<missing_value>";

    public TaskText() {
    }

    public TaskText(String locale, String content) {
        this.locale = locale;
        this.content = content;
    }

    public TaskText(AbstractTask task, String locale, String content) {
        this.task = task;
        this.locale = locale;
        this.content = content;
    }

    public Map.Entry<String, Uni<String>> translate(ResolutionContext context) {
        Uni<? extends Translatable> localeTextUni = LocaleTaskText
                .findById(new LocaleTaskText.LocaleTaskTextPK(this, context.getLocale()))
                .replaceIfNullWith(this)
                // we will receive either LocaleTaskText or a fallback of TaskText, both are Translatable
                .map(panacheEntityBase -> (Translatable) panacheEntityBase);
        return Map.entry(task.getKey(), localeTextUni.map(translatable -> translatable.getContent().get(CONTENT_TAG)));
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of(CONTENT_TAG, content);
    }

    @Override
    public String getLocale() {
        return this.locale;
    }
}
