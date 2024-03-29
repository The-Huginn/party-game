package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.category.Category;
import com.thehuginn.common.game.task.AbstractTask;
import com.thehuginn.common.game.translation.TaskText;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.resolution.Resolvable;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.token.unresolved.AbstractUnresolvedToken;
import com.thehuginn.token.unresolved.UnresolvedToken;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue("1")
public class Task extends AbstractTask implements Resolvable<List<GameTask>> {

    public enum Repeat {
        ALWAYS,
        PER_PLAYER,
        NEVER
    }

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH,
            CascadeType.DETACH }, targetEntity = AbstractUnresolvedToken.class)
    public List<UnresolvedToken> tokens = new ArrayList<>();

    @JsonProperty
    public Type type = Type.SINGLE;

    @JsonProperty
    public Repeat repeat = Repeat.NEVER;

    @JsonProperty
    public Short frequency = 1;

    @JsonProperty
    public Price price = new Price();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    public Category category = Category.getDefaultInstance();

    @Embeddable
    public static class Price {
        @Column(name = "price_enabled")
        public boolean enabled = true;
        public int price = 1;

        public Price() {
        }

        public Price(boolean enabled, int price) {
            this.enabled = enabled;
            this.price = price;
        }
    }

    public static class Builder {

        private boolean setId = false;
        private long id;

        private final String task;

        private String locale = "en";

        private Type type = Type.SINGLE;

        private Repeat repeat = Repeat.NEVER;

        private short frequency = 1;

        private Price price = new Price();

        public Builder(String task) {
            this.task = task;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
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

        public Builder price(Price price) {
            this.price = price;
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
            builtTask.task = new TaskText(builtTask, locale, task);
            builtTask.tokens = TokenResolver.translateTask(task);
            builtTask.type = type;
            builtTask.repeat = repeat;
            builtTask.frequency = frequency;
            builtTask.price = price;
            return builtTask;
        }
    }

    public Task() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Task task1 = (Task) o;
        return Objects.equals(tokens, task1.tokens) && type == task1.type && repeat == task1.repeat
                && Objects.equals(frequency, task1.frequency) && Objects.equals(price, task1.price)
                && Objects.equals(task, task1.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokens, type, repeat, frequency, price, task);
    }

    public enum Type {
        SINGLE,
        DUO,
        ALL
    }

    @JsonIgnore
    public String getKey() {
        return "task_" + id;
    }

    @Override
    public List<GameTask> resolve(ResolutionContext context) {
        List<GameTask> tasks = new ArrayList<>();
        for (short amount = 0; amount < frequency; amount++) {
            GameTask gameTask = new GameTask();
            gameTask.unresolvedTask = this;
            if (repeat.equals(Task.Repeat.PER_PLAYER)) {
                for (String player : context.getPlayers()) {
                    GameTask shallowCopy = gameTask.clone();
                    shallowCopy.assignedPlayer = player;
                    tasks.add(shallowCopy);
                }
            } else {
                tasks.add(gameTask);
            }
        }

        return tasks;
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return tokens.stream().allMatch(resolvedTokenResolvable -> resolvedTokenResolvable.isResolvable(context));
    }

    @JsonIgnore
    public boolean isRemovable() {
        return repeat != Repeat.ALWAYS;
    }

    public static Uni<Set<Task>> findByIds(Set<Task> tasks) {
        List<Long> ids = tasks.stream()
                .map(task -> task.id)
                .toList();
        return Task.<Task> find("From Task t where t.id IN :ids", Parameters.with("ids", ids))
                .list()
                .map(HashSet::new);
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
