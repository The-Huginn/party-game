package com.thehuginn.token.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import com.thehuginn.task.ResolvedToken;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
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
public class TaskText extends PanacheEntityBase implements ResolvedToken, Translatable {

    public static class TaskTextPK {
        public Long task;
        public String locale;

        public TaskTextPK() {}

        public TaskTextPK(Long task, String locale) {
            this.task = task;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof TaskTextPK pk)) {
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
    public Task task;

    @Id
    @JsonProperty
    public String locale = "en";

    @JsonProperty
    public String content = "<missing_value>";

    public TaskText() {}

    public TaskText(String locale, String content) {
        this.locale = locale;
        this.content = content;
    }

    public TaskText(Task task, String locale, String content) {
        this.task = task;
        this.locale = locale;
        this.content = content;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public String getLocale() {
        return this.locale;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        Uni<? extends Translatable> localeTextUni = LocaleText
                .findById(new LocaleText.LocaleTextPK(this, context.getLocale()))
                .replaceIfNullWith(this)
                // we will receive either LocaleText or a fallback of TaskText, both are Translatable
                .map(panacheEntityBase -> (Translatable) panacheEntityBase);
        return new UnresolvedResult().task(Map.entry(task.getKey(),
                        localeTextUni
                                .map(Translatable::getContent)))
                .appendData(Map.entry("locale",
                        localeTextUni
                                .map(Translatable::getLocale)));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}
