package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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

import java.util.Objects;

@Entity
@NamedQueries({
    @NamedQuery(name = "LocaleText.byLocale", query = "from LocaleText where locale = :locale and token.key = :token")
})
@IdClass(LocaleText.LocaleTextPK.class)
public class LocaleText extends PanacheEntityBase {

    public static class LocaleTextPK {
        public Long token;
        public String locale;

        public LocaleTextPK() {}

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof LocaleTextPK pk)) {
                return false;
            } else {
                return Objects.equals(token, pk.token) && locale.equals(pk.locale);
            }
        }

        @Override
        public int hashCode() {
            return token.hashCode() + locale.hashCode();
        }
    }

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id")
    @JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    public Task.Token token;

    @Id
    public String locale = "en";

    public String content = "<missing_value>";

    public LocaleText() {}

    public static Uni<LocaleText> byLocale(String tokenId, String locale) {
        return LocaleText.<LocaleText>find("#LocaleText.byLocale", Parameters.with("locale", locale).and("token", tokenId))
                .firstResult()
                .onItem()
                .ifNull()
                .switchTo(LocaleText
                        .<LocaleText>find("#LocaleText.byLocale", Parameters.with("locale", "en").and("token", tokenId))
                        .firstResult());
    }
}
