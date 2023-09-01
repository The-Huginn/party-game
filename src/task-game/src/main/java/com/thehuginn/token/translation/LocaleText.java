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

import java.util.Objects;

@Entity
@IdClass(LocaleText.LocaleTextPK.class)
public class LocaleText extends PanacheEntityBase implements Translatable {

    public static class LocaleTextPK {
        public TaskText taskText;
        public String locale;

        public LocaleTextPK() {}

        public LocaleTextPK(TaskText taskText, String locale) {
            this.taskText = taskText;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocaleTextPK that = (LocaleTextPK) o;
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

    public LocaleText() {}

    public LocaleText(String locale, String content) {
        this.locale = locale;
        this.content = content;
    }

    public LocaleText(TaskText taskText, String locale, String content) {
        this.taskText = taskText;
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
}
