package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Task extends PanacheEntity {

    public enum Type {SINGLE, DUO, ALL}

    public enum Repeat {ALWAYS, PER_PLAYER, NEVER}

    @JsonProperty
    @NotEmpty(message = "task sequence can't be empty")
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    public List<Token> tokens;

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
    public Category category = Category.getDefaultInstance();

    @Entity
//    @Table(uniqueConstraints = @UniqueConstraint(columnNames = "value"))
    public static class Token extends PanacheEntity {
        public enum TokenType {TEXT, PLAYER, TIMER}

        @JsonProperty
        public TokenType type = TokenType.TEXT;

        @Column(name = "key")
        public String key;

        public Token() {}

        private Token(TokenType type,  String key) {
            this.type = type;
            this.key = key;
        }

        public static Token textToken(String text) {
            return new Token(TokenType.TEXT, text);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Token other)) {
                return false;
            }
            return key.equals(other.key) && type == other.type;
        }

        @Override
        public int hashCode() {
            return key.hashCode() + type.hashCode();
        }
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

        private List<Token> task;

        private Type type = Type.SINGLE;

        private Repeat repeat = Repeat.NEVER;

        private short frequency = 1;

        private Price price = new Price();

        private Timer timer = new Timer();

        public Builder() {}

        public Builder(List<Token> task) {
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
            builtTask.tokens = task;
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
