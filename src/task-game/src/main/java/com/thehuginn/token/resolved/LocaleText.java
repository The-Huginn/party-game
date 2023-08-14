package com.thehuginn.token.resolved;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.task.ResolutionContext;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = "LocaleText.byLocale", query = "from LocaleText where locale = :locale and task.id = :id")
})
@IdClass(LocaleText.LocaleTextPK.class)
public class LocaleText extends PanacheEntityBase implements ResolvedToken {

    static class LocaleTextPK {
        public Long task;
        public String locale;

        public LocaleTextPK() {}

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
    public ResolvedResult resolve(ResolutionContext context, ResolvedResult result) {
        Uni<LocaleText> localeTextUni = LocaleText
                .<LocaleText>find("#LocaleText.byLocale",
                        Parameters.with("locale", context.getLocale()).and("id", task.id))
                .firstResult()
                .onItem()
                .ifNull()
                .continueWith(this);
        return result.appendData(Map.entry(task.getKey(),
                        localeTextUni
                                .onItem()
                                .transform(localeText -> localeText.content)))
                .appendData(Map.entry("locale",
                        localeTextUni
                                .onItem()
                                .transform(localeText -> localeText.locale)));
    }
}
