package com.thehuginn.resolution;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thehuginn.category.Category;
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
import java.util.Set;

@Entity
public class GameSession extends PanacheEntityBase {

    @Id
    public String gameId;

    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    @JsonIgnore
    public ResolvedTask currentTask = null;

    @ManyToMany(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    public Set<Category> categories = new HashSet<>();

    public GameSession() {}

    public Uni<ResolvedTask> nextTask(ResolutionContext resolutionContext) {
        if (currentTask != null) {
            return Uni.createFrom().item(currentTask);
        }
        return GameTask.count("game = :game", Parameters.with("game", gameId))
                .onItem()
                .transformToUni(count -> {
                    if (count.equals(0L)) {
                        return Uni.createFrom().failure(new IllegalStateException("No more tasks remain for current game"));
                    }

                    return nextTaskUni(resolutionContext);
                })
                .onFailure()
                .recoverWithNull();
    }

    private Uni<ResolvedTask> nextTaskUni(ResolutionContext resolutionContext) {
        return GameTask.<GameTask>find("game = :game", Parameters.with("game", gameId))
                .singleResult()
                .onItem()
                .transformToUni(gameTask -> {
                    if (!gameTask.isResolvable(resolutionContext)) {
                        return nextTaskUni(resolutionContext);
                    }

                    return Uni.createFrom().item(gameTask.resolve(resolutionContext));
                });
    }
}
