package com.thehuginn.common.game.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.common.game.category.AbstractCategory;
import com.thehuginn.common.game.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
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
@IdClass(CategoryText.CategoryTextPK.class)
public class CategoryText extends PanacheEntityBase implements TranslatableCategory {

    public static class CategoryTextPK {
        public Long category;
        public String locale;

        public CategoryTextPK() {
        }

        public CategoryTextPK(Long category, String locale) {
            this.category = category;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CategoryTextPK that = (CategoryTextPK) o;
            return Objects.equals(category, that.category) && Objects.equals(locale, that.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(category, locale);
        }
    }

    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id")
    @JsonIgnore
    public AbstractCategory category;

    @Id
    public String locale = "en";

    public String name;

    public String description;

    public CategoryText() {
    }

    public CategoryText(AbstractCategory category, CategoryText categoryText) {
        this.category = category;
        this.locale = categoryText.locale;
        this.name = categoryText.name;
        this.description = categoryText.description;
    }

    public Uni<CategoryDto> translate(ResolutionContext context) {
        return LocaleCategoryText
                .findById(new LocaleCategoryText.LocaleCategoryTextPK(this, context.getLocale()))
                .replaceIfNullWith(this)
                .map(translatable -> (TranslatableCategory) translatable)
                .map(translatableCategory -> new CategoryDto(category.id, translatableCategory.getName(),
                        translatableCategory.getDescription()));
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

    @RegisterForReflection
    public static class CategoryDto {
        public Long id;
        public String name;
        public String description;

        public CategoryDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public CategoryDto(AbstractCategory category) {
            this.id = category.id;
            this.name = category.categoryText.getName();
            this.description = category.categoryText.getDescription();
        }
    }
}
