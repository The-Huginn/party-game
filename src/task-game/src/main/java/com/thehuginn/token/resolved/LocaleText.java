package com.thehuginn.token.resolved;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
    @JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    public Task task;

    @Id
    public String locale = "en";

    public String content = "<missing_value>";

    public LocaleText() {}

    @Override
    public ResolvedResult resolve(ResolutionContext context, ResolvedResult result) {
        Uni<String> localeTextUni = LocaleText
                .<LocaleText>find("#LocaleText.byLocale",
                        Parameters.with("locale", context.getLocale()).and("id", task.id))
                .firstResult()
                .onItem()
                .ifNull()
                .continueWith(this)
                .onItem()
                .transform(localeText -> localeText.content);
        return result.appendData(Map.entry(task.task, localeTextUni));
    }
}
