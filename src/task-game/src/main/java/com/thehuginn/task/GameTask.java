package com.thehuginn.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.entities.Game;
import com.thehuginn.entities.Player;
import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.token.resolved.AbstractResolvedToken;
import com.thehuginn.token.resolved.ResolvedToken;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.util.ArrayList;
import java.util.List;

@Entity
public class GameTask extends PanacheEntityBase implements ResolvableTask {

    public static class GameTaskPK {
        public String game;

        public long id;

        public GameTaskPK() {}

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof GameTaskPK taskPK)) {
                return false;
            } else {
                return game.equals(taskPK.game) && id == taskPK.id;
            }
        }

        @Override
        public int hashCode() {
            return game.hashCode() + Long.hashCode(id);
        }
    }

    @Id
    public String game;

    @Id
    @GeneratedValue
    public long id;

    @ManyToOne(fetch = FetchType.EAGER)
    public Task unresolvedTask;

    @ManyToOne(fetch = FetchType.EAGER)
    public Player assignedPlayer;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH},
            targetEntity = AbstractResolvedToken.class
    )
    public List<ResolvedToken> tokens;
    public GameTask() {}

    @Override
    public ResolvedTask resolve(ResolutionContext context) {
//        return unresolvedTask.resolve(context);
        return null;
    }

    /**
     * Change with caution from Uni, such as internally we use shallow-copy,
     *  and we persist all objects right away
     */
    public static Uni<Void> generateGameTasks(Game game, List<Task> tasks, ResolutionContext resolutionContext) throws CloneNotSupportedException {
        List<GameTask> createdTasks = new ArrayList<>();
        for (var task : tasks) {
            for (short amount = 0; amount < task.frequency; amount++) {
                GameTask gameTask = new GameTask();
                gameTask.game = game.gameId;
                gameTask.unresolvedTask = task;
                gameTask.tokens = task.tokens.stream().map(token -> token.resolve(resolutionContext)).toList();
                if (task.repeat.equals(Task.Repeat.PER_PLAYER)) {
                    for (Player player: resolutionContext.getPlayers()) {
                        GameTask shallowCopy = (GameTask) gameTask.clone();
                        shallowCopy.assignedPlayer = player;
                        createdTasks.add(shallowCopy);
                    }
                } else {
                    createdTasks.add(gameTask);
                }
            }
        }

        Uni<Void> newGameTasks = Uni.combine()
                .all()
                .unis(createdTasks.stream()
                        .map(gameTask -> gameTask.persist())
                        .toList())
                .usingConcurrencyOf(1)
                .discardItems();

        return GameTask.delete("game", game.gameId)
                .onItem()
                .invoke(aLong -> {
                    if (aLong.compareTo(0L) > 0) {
                        Log.infof("Previous game [%s] was deleted with %d tasks remaining");
                    }
                })
                .replaceWith(newGameTasks);
    }
}
