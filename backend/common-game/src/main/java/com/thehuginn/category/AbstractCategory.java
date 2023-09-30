package com.thehuginn.category;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.thehuginn.token.translation.CategoryText;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;

/**
 * We have a special AbstractCategory with id 0. All Tasks without being
 * assigned to any AbstractCategory fall under this `special` AbstractCategory.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "unimportant", discriminatorType = DiscriminatorType.INTEGER)
public class AbstractCategory extends PanacheEntity {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "category")
    @JsonUnwrapped
    public CategoryText categoryText;

    public AbstractCategory() {
    }
}
