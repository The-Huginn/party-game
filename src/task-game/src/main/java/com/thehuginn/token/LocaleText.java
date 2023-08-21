package com.thehuginn.token;

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
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;
import java.util.Objects;

@Entity
@IdClass(LocaleText.LocaleTextPK.class)
public class LocaleText extends PanacheEntityBase implements ResolvedToken {

    public static class LocaleTextPK {
        public Long task;
        public String locale;

        public LocaleTextPK() {}

        public LocaleTextPK(Long task, String locale) {
            this.task = task;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof LocaleTextPK pk)) {
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
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id")
    @JsonIgnore
    public Task task;

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

    public LocaleText(Task task,  String locale, String content) {
        this.task = task;
        this.locale = locale;
        this.content = content;
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        Uni<LocaleText> localeTextUni = LocaleText
                .<LocaleText>findById(new LocaleTextPK(task.id, context.getLocale()))
                .replaceIfNullWith(this);
        return new UnresolvedResult().task(Map.entry(task.getKey(),
                        localeTextUni
                                .map(localeText -> localeText.content)))
                .appendData(Map.entry("locale",
                        localeTextUni
                                .map(localeText -> localeText.locale)));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }
}