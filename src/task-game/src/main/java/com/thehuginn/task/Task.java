package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehuginn.category.Category;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.TokenResolver;
import com.thehuginn.token.resolved.LocaleText;
import com.thehuginn.token.unresolved.AbstractUnresolvedToken;
import com.thehuginn.token.unresolved.Token;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Task extends PanacheEntity {

    public enum Type {SINGLE, DUO, ALL}

    public enum Repeat {ALWAYS, PER_PLAYER, NEVER}

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH},
            targetEntity = AbstractUnresolvedToken.class
    )
    public List<Token> tokens;

    @JsonProperty
    public Type type = Type.SINGLE;

    @JsonProperty
    public Repeat repeat = Repeat.NEVER;

    @JsonProperty
    public Short frequency = 1;

    @JsonProperty
    public Price price = new Price();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "task")
    public LocaleText task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    public Category category = Category.getDefaultInstance();

    public List<Token> getTokens() {
        return tokens;
    }

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

    public Task() {}

    public static class Builder {

        private boolean setId = false;
        private long id;

        private String task;

        private String locale = "en";

        private Type type = Type.SINGLE;

        private Repeat repeat = Repeat.NEVER;

        private short frequency = 1;

        private Price price = new Price();

        public Builder() {}

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

        public  Builder price(Price price) {
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
            builtTask.task = new LocaleText(builtTask, locale, task);
            builtTask.tokens = TokenResolver.translateTask(task);
            builtTask.type = type;
            builtTask.repeat = repeat;
            builtTask.frequency = frequency;
            builtTask.price = price;
            return builtTask;
        }
    }

    @JsonIgnore
    public String getKey() {
        return "task_" + id;
    }

    public GameTask resolve(ResolutionContext context) {
        return null;
    }


    public static Uni<Set<Task>> findByIds(Set<Task> tasks) {
        List<Long> ids = tasks.stream()
                .map(task -> task.id)
                .toList();
        return Task.<Task>find("From Task t where t.id IN :ids", Parameters.with("ids", ids))
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
