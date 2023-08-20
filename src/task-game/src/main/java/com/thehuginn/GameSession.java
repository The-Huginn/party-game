package com.thehuginn;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thehuginn.category.Category;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.task.GameTask;
import com.thehuginn.task.ResolvedTask;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

@Entity
public class GameSession extends PanacheEntityBase {

    @Id
    public String gameId;

    @JsonIgnore
    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ResolvedTask currentTask;

    @ManyToMany(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    public Set<Category> categories = new HashSet<>();

    public GameSession() {}

    public Uni<Boolean> addCategory(Long categoryId) {
        return Category.<Category>findById(categoryId)
                .chain(category -> {
                    if (category != null) {
                        this.categories.add(category);
                        return this.persist()
                                .replaceWith(Boolean.TRUE);
                    }
                    return Uni.createFrom().item(Boolean.FALSE);
                });
    }

    public Uni<Boolean> removeCategory(Long categoryId) {
        return Category.<Category>findById(categoryId)
                .chain(category -> {
                    if (category != null) {
                        Boolean removed = this.categories.remove(category);
                        return this.persist()
                                .replaceWith(removed);
                    }
                    return Uni.createFrom().item(Boolean.FALSE);
                });
    }

    public Uni<ResolvedTask> nextTask(ResolutionContext resolutionContext) {
        Function<ResolvedTask, Uni<?>> updateResolvedTask = resolvedTask -> Uni.createFrom().item(this)
                .invoke(gameSession -> gameSession.currentTask = resolvedTask)
                .call(gameSession -> gameSession.persist());
        return GameTask.count("game = :game", Parameters.with("game", gameId))
                .chain(count -> {
                    if (count.equals(0L)) {
                        return Uni.createFrom().failure(new IllegalStateException("No more tasks remain for current game"));
                    }

                    return nextTaskUni(resolutionContext, count);
                })
                .call(updateResolvedTask)
                .onFailure().recoverWithNull();
    }

    private Uni<ResolvedTask> nextTaskUni(ResolutionContext resolutionContext, long count) {
        return GameTask.<GameTask>find("game = :game", Parameters.with("game", gameId))
                .page((int) ((new Random()).nextLong(count)), 1)
                .firstResult()
                .chain(gameTask -> {
                    if (!gameTask.isResolvable(resolutionContext) ||
                            (currentTask != null && currentTask.gameTask.equals(gameTask))) {
                        return nextTaskUni(resolutionContext, count);
                    }

                    return Uni.createFrom().item(gameTask.resolve(resolutionContext));
                });
    }
}
