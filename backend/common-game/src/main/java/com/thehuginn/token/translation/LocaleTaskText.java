package com.thehuginn.token.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;
import java.util.Objects;

import static com.thehuginn.token.translation.TaskText.CONTENT_TAG;

@Entity
@IdClass(LocaleTaskText.LocaleTaskTextPK.class)
public class LocaleTaskText extends PanacheEntityBase implements Translatable {

    public static class LocaleTaskTextPK {
        public TaskText taskText;
        public String locale;

        public LocaleTaskTextPK() {
        }

        public LocaleTaskTextPK(TaskText taskText, String locale) {
            this.taskText = taskText;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            LocaleTaskTextPK that = (LocaleTaskTextPK) o;
            return Objects.equals(taskText, that.taskText) && Objects.equals(locale, that.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(taskText, locale);
        }
    }

    @Id
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public TaskText taskText;

    @Id
    @JsonProperty
    public String locale = "en";

    @JsonProperty
    public String content = "<missing_value>";

    public LocaleTaskText() {
    }

    public LocaleTaskText(String locale, String content) {
        this.locale = locale;
        this.content = content;
    }

    public LocaleTaskText(TaskText taskText, String locale, String content) {
        this.taskText = taskText;
        this.locale = locale;
        this.content = content;
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
