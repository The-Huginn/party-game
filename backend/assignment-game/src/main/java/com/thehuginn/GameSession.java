package com.thehuginn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.common.game.AbstractGameSession;
import com.thehuginn.common.game.task.AbstractTask;
import com.thehuginn.common.services.exposed.resolution.ResolutionContext;
import com.thehuginn.task.NeverEverTask;
import com.thehuginn.task.PubTask;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
public class GameSession extends AbstractGameSession {

    public GameType type = GameType.NONE;

    public enum GameType {
        NONE,
        PUB_MODE,
        NEVER_EVER_MODE
    }

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = AbstractTask.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Cascade(CascadeType.DELETE_ORPHAN)
    @JoinTable(name = "gameSession_tasks", joinColumns = @JoinColumn(name = "gameSession_id", referencedColumnName = "gameId"), inverseJoinColumns = @JoinColumn(name = "task_id", referencedColumnName = "id"))
    public List<? super AbstractTask> tasks;

    public GameSession() {
    }

    public GameSession(String gameId, GameType type) {
        super(gameId);
        this.type = type;
    }

    @Override
    public Uni<Boolean> start(ResolutionContext.Builder resolutionContext) {
        tasks.clear();
        return switch (this.type) {
            case PUB_MODE -> {
                tasks.addAll(PubTask.generateTasks());
                yield this.persist()
                        .replaceWith(Boolean.TRUE);
            }
            case NEVER_EVER_MODE -> {
                tasks.addAll(NeverEverTask.generateTasks());
                yield this.persist()
                        .replaceWith(Boolean.TRUE);
            }
            case NONE -> Uni.createFrom().item(Boolean.FALSE);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<? extends AbstractTask> currentTask(ResolutionContext.Builder resolutionContextBuilder) {
        return GameSession.find("from GameSession g left fetch g.tasks where g.id = :id", Parameters.with("id", this.gameId))
                .project(AbstractTask.class)
                .page(0, 1)
                .firstResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<? extends AbstractTask> nextTask(ResolutionContext.Builder resolutionContextBuilder) {
        return GameSession.find("from GameSession g left fetch g.tasks where g.id = :id", Parameters.with("id", this.gameId))
                .project(AbstractTask.class)
                .page(0, 1)
                .firstResult()
                .call(task -> PanacheEntity.delete(
                        "from gameSession_tasks cross where cross.gameSession_id = :gameId and cross.task_id = :id",
                        Parameters.with("gameId", this.gameId).and("id", task.id)));
    }
}
