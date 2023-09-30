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

@Entity
@IdClass(LocaleCategoryText.LocaleCategoryTextPK.class)
public class LocaleCategoryText extends PanacheEntityBase implements TranslatableCategory {

    public static class LocaleCategoryTextPK {
        public CategoryText categoryText;
        public String locale;

        public LocaleCategoryTextPK() {
        }

        public LocaleCategoryTextPK(CategoryText categoryText, String locale) {
            this.categoryText = categoryText;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            LocaleCategoryTextPK that = (LocaleCategoryTextPK) o;
            return Objects.equals(categoryText, that.categoryText) && Objects.equals(locale, that.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categoryText, locale);
        }
    }

    @Id
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public CategoryText categoryText;

    @Id
    @JsonProperty
    public String locale;

    public String name;

    public String description;

    public LocaleCategoryText() {
    }

    public LocaleCategoryText(CategoryText categoryText, String locale, String name, String description) {
        this.categoryText = categoryText;
        this.locale = locale;
        this.name = name;
        this.description = description;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of(NAME_TAG, name,
                DESCRIPTION_TAG, description);
    }

    @Override
    public String getLocale() {
        return locale;
    }
}
