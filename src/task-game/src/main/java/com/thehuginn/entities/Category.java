package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.Set;
import java.util.HashSet;

/**
 * We have a special Category with id 0. All Tasks without being
 *  assigned to any Category fall under this `special` Category.
 */
@Entity
public class Category extends PanacheEntity {


    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category", cascade = CascadeType.MERGE)
    public Set<Task> tasks = new HashSet<>();

    public Category() {}

    public static Category getDefaultInstance() {
        Category category = new Category();
        category.id = 0L;
        return category;
    }

    @JsonIgnore
    public static Uni<Set<Task>> getTasks(Long id) {
        return Category.<Category>list("from Category i left join fetch i.tasks where i.id = :id", Parameters.with("id", id))
                .map(categories -> categories.stream()
                        .map(category -> category.tasks)
                        .findFirst()
                        .get());
    }

    public static Uni<Category> findByIdFetch(Object id) {
        return Category.<Category>find("from Category i left join fetch i.tasks where i.id = :id", Parameters.with("id",id)).firstResult();
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
    }
}
