package com.thehuginn.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Game extends PanacheEntityBase {

    public enum State {
        CREATED,
        LOBBY,
        READY,
        STARTED,
        ONGOING,
        FINISHING,
        COMPLETED
    }

    public enum Type {
        NONE,
        TASK,
        PUB
    }

    @Id
    public String gameId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Player> team = new ArrayList<>();

    public State state;

    public Type type;

    public LocalDateTime lastAccess;

    public Game() {
    }

    public Game(String id) {
        gameId = id;
        team = new ArrayList<>();
        state = State.CREATED;
        type = Type.TASK;
    }

    public static Uni<Game> findByIdUpdateTimestamp(String id) {
        return Game.<Game> findById(id)
                .onItem().ifNotNull().call(game -> {
                    game.lastAccess = LocalDateTime.now();
                    return game.persist();
                });
    }

    public Player addPlayer(Player player) {
        if (team.contains(player)) {
            return null;
        }

        team.add(player);
        return player;
    }

    public boolean removePlayer(Long playerId) {
        return team.removeIf(player -> Objects.equals(player.id, playerId));
    }

    public GameContext gameContext() {
        return new GameContext(this);
    }
}
