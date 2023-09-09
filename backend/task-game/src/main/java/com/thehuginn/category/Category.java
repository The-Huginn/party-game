package com.thehuginn.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.GameSession;
import com.thehuginn.task.Task;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

/**
 * We have a special Category with id 0. All Tasks without being
 * assigned to any Category fall under this `special` Category.
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

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<GameSession> gameSessions;

    public Category() {
    }

    public static Category getDefaultInstance() {
        Category category = new Category();
        category.id = 0L;
        return category;
    }

    @JsonIgnore
    public static Uni<Set<Task>> getTasks(Long id) {
        return Category.<Category> list("from Category i left join fetch i.tasks where i.id = :id", Parameters.with("id", id))
                .map(categories -> categories.stream()
                        .map(category -> category.tasks)
                        .findFirst()
                        .get());
    }

    public static Uni<Category> findByIdFetch(Object id) {
        return Category.<Category> find("from Category i left join fetch i.tasks where i.id = :id", Parameters.with("id", id))
                .firstResult();
    }

    /**
     * Keep the order of constructors for hibernate DTO projection
     */
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
            this.name = category.name;
            this.description = category.description;
        }

    }
}
