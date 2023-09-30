package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.token.translation.TaskText;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;

import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "unimportant", discriminatorType = DiscriminatorType.INTEGER)
public class AbstractTask extends PanacheEntity {

    @JsonProperty
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "task")
    public TaskText task;

    public AbstractTask() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractTask task1 = (AbstractTask) o;
        return Objects.equals(task, task1.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task);
    }

    @JsonIgnore
    public String getKey() {
        return "task_" + id;
    }
}
