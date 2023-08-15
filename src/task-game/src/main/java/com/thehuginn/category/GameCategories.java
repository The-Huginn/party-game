package com.thehuginn.category;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.Set;

@Entity
public class GameCategories extends PanacheEntityBase {

    @Id
    public String game;

    @ManyToMany(fetch = FetchType.EAGER)
    public Set<Category> selectedCategories;

    public GameCategories() {}

    public Uni<Boolean> addCategory(Long categoryId) {
        return Category.<Category>findById(categoryId)
                .onItem()
                .transformToUni(category1 -> {
                    if (category1 != null) {
                        this.selectedCategories.add(category1);
                        return this.persist()
                                .replaceWith(Boolean.TRUE);
                    }
                    return Uni.createFrom().item(Boolean.FALSE);
                });
    }

    public Uni<Boolean> removeCategory(Long categoryId) {
        return Category.<Category>findById(categoryId)
                .onItem()
                .transformToUni(category1 -> {
                    if (category1 != null) {
                        Boolean removed = this.selectedCategories.remove(category1);
                        return this.persist()
                                .replaceWith(removed);
                    }
                    return Uni.createFrom().item(Boolean.FALSE);
                });
    }
}
