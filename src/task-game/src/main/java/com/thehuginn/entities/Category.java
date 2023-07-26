package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Category extends PanacheEntity {


    @JsonProperty
    public String name;

    @JsonProperty
    public String description;

    @JsonProperty
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    public List<Task> tasks = new ArrayList<>();

    public Category() {}

    @JsonIgnore
    public Uni<List<Task>> getTasks() {
        return Category.<Category>list("from Category i left join i.tasks where i.id = :id", Parameters.with("id", id))
                .map(categories -> categories.stream()
                        .map(category -> category.tasks)
                        .findFirst()
                        .get());
    }
}
