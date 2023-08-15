package com.thehuginn.category;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
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

@NamedQueries({
        @NamedQuery(name = "LocaleCategory.byCategory", query = "from LocaleCategory where category.id = :id and locale = :locale")
})
@Entity
@IdClass(LocaleCategory.LocaleCategoryPK.class)
public class LocaleCategory extends PanacheEntityBase {

    public static class LocaleCategoryPK {
        public long category;

        public String locale;

        public LocaleCategoryPK() {
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LocaleCategoryPK pk)) {
                return false;
            } else {
                return category == pk.category && locale.equals(pk.locale);
            }
        }

        @Override
        public int hashCode() {
            return Long.hashCode(category) + locale.hashCode();
        }
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "category_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Category category;

    @Id
    public String locale = "en";

    public String name_content;

    public String description_content;

    public static Uni<Map<String, String>> translation(Long categoryId, String locale) {
        Uni<LocaleCategory> localeCategoryUniEnglish = LocaleCategory.
                <LocaleCategory>find("#LocaleCategory.byCategory", Parameters.with("id", categoryId)
                .and("locale", "en"))
                .firstResult();
        Uni<LocaleCategory> localeCategoryUni = LocaleCategory.
                <LocaleCategory>find("#LocaleCategory.byCategory", Parameters.with("id", categoryId)
                .and("locale", locale))
                .firstResult()
                .onItem()
                .ifNull()
                .switchTo(() -> {
                    Log.debugf("Category %s has no locale %s", categoryId, locale);
                    return localeCategoryUniEnglish;
                });
        return Category.<Category>findById(categoryId)
                .onItem()
                .invoke(category1 -> {
                    if (category1 == null) {
                        throw new IllegalStateException("Category with id: " + categoryId + " does not exist");
                    }
                })
                .onItem()
                .transformToUni(category1 -> localeCategoryUni
                        .onItem()
                        .transform(localeCategory -> Map.of(category1.name, localeCategory.name_content,
                                category1.description, localeCategory.description_content)));
    }
}