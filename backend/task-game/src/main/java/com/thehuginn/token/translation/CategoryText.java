package com.thehuginn.token.translation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.category.Category;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.Resolvable;
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
public class CategoryText extends PanacheEntityBase implements Resolvable<Uni<CategoryText.CategoryDto>>, TranslatableCategory {

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
    public Category category;

    @Id
    public String locale = "en";

    public String name;

    public String description;

    public CategoryText() {
    }

    public CategoryText(Category category, CategoryText categoryText) {
        this.category = category;
        this.locale = categoryText.locale;
        this.name = categoryText.name;
        this.description = categoryText.description;
    }

    @Override
    public Uni<CategoryDto> resolve(ResolutionContext context) {
        return LocaleCategoryText
                .findById(new LocaleCategoryText.LocaleCategoryTextPK(this, context.getLocale()))
                .replaceIfNullWith(this)
                .map(translatable -> (TranslatableCategory) translatable)
                .map(translatableCategory -> new CategoryDto(category.id, translatableCategory.getName(),
                        translatableCategory.getDescription()));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
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

        public CategoryDto(Category category) {
            this.id = category.id;
            this.name = category.categoryText.getName();
            this.description = category.categoryText.getDescription();
        }
    }
}
