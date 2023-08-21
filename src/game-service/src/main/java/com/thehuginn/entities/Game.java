package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Game extends PanacheEntityBase {

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum State {CREATED, READY, STARTED, ONGOING, FINISHING, COMPLETED}
    public enum Type {NONE, TASK}

    @Id
    public String gameId;

    @OneToMany(fetch = FetchType.EAGER)
    public List<Player> team;

    public State state;

    public Type type;

    public Game() {}

    public Game(String id) {
        gameId = id;
        team = new ArrayList<>();
        state = State.CREATED;
        type = Type.NONE;
    }

    public boolean addPlayer(Player player) {
        if (team.contains(player)) {
            return false;
        }

        team.add(player);
        return true;
    }

    public void removePlayer(Long playerId) {
        team = team.stream()
                .filter(player -> !Objects.equals(player.id, playerId))
                .toList();
    }

    public GameContext gameContext() {
        return new GameContext(this);
    }
}
