package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
public class Task extends PanacheEntity {

    public enum Type {SINGLE, DUO, ALL}

    public enum Repeat {ALWAYS, PER_PLAYER, NEVER}

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "task_sequence", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "task", nullable = false)
    @NotEmpty(message = "task sequence can't be empty")
    @JsonProperty
    public List<String> task;

    @JsonProperty
    public Type type = Type.SINGLE;

    @JsonProperty
    public Repeat repeat = Repeat.NEVER;

    @JsonProperty
    public short frequency = 1;

    @JsonProperty
    public Price price = new Price();

    @JsonProperty
    public Timer timer = new Timer();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    public Category category;

    @Embeddable
    public static class Price {
        @Column(name = "price_enabled")
        public boolean enabled = true;
        public int price = 1;

        public Price() {}

        public Price(boolean enabled, int price) {
            this.enabled = enabled;
            this.price = price;
        }
    }

    @Embeddable
    public static class Timer {
        @Column(name = "timer_enabled")
        public boolean enabled = false;
        public int duration = 60;

        public Timer() {}

        public Timer(boolean enabled, int duration) {
            this.enabled = enabled;
            this.duration = duration;
        }
    }

    public Task() {}

    public static class Builder {

        private boolean setId = false;
        private long id;

        private List<String> task;

        private Type type = Type.SINGLE;

        private Repeat repeat = Repeat.NEVER;

        private short frequency = 1;

        private Price price = new Price();

        private Timer timer = new Timer();

        public Builder() {}

        public Builder(List<String> task) {
            this.task = task;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder repeat(Repeat repeat) {
            this.repeat = repeat;
            return this;
        }

        public Builder frequency(short frequency) {
            this.frequency = frequency;
            return this;
        }

        public  Builder price(Price price) {
            this.price = price;
            return this;
        }

        public Builder timer(Timer timer) {
            this.timer = timer;
            return this;
        }

        public Builder id(long id) {
            this.id = id;
            this.setId = true;
            return this;
        }

        public Task build() {
            Task builtTask = new Task();
            if (setId) {
                builtTask.id = this.id;
            }
            builtTask.task = task;
            builtTask.type = type;
            builtTask.repeat = repeat;
            builtTask.frequency = frequency;
            builtTask.price = price;
            builtTask.timer = timer;
            return builtTask;
        }
    }

    public static Uni<Set<Task>> findByIds(Set<Task> tasks) {
        List<Long> ids = tasks.stream()
                .map(task -> task.id)
                .toList();
        return Task.<Task>find("From Task t where t.id IN :ids", Parameters.with("ids", ids))
                .list()
                .map(tasks1 -> new HashSet<>(tasks1));
    }

    public static Uni<Integer> addToCategory(Long id, Set<Task> tasks) {
        return updateCategory(id, false, tasks);
    }

    public static Uni<Integer> deleteFromCategory(Long id, Set<Task> tasks) {
        return updateCategory(id, true, tasks);
    }

    private static Uni<Integer> updateCategory(Long id, boolean delete, Set<Task> tasks) {
        List<Long> taskIds = tasks.stream()
                .map(task -> task.id)
                .toList();

        Parameters parameters = Parameters.with("id", delete ? null : id)
                .and("ids", taskIds);

        return Task.update("update from Task set category.id = :id where id in :ids", parameters);
    }
}
