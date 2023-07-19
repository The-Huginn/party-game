package com.thehuginn.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Game extends PanacheEntityBase {

    @Id
    public String gameId;

    @OneToMany(fetch = FetchType.EAGER)
    public List<Player> team;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum State {CREATED, READY, STARTED, ONGOING, FINISHING, COMPLETED}
    public State state;

    public Game() {}

    public Game(String id) {
        gameId = id;
        team = new ArrayList<>();
        state = State.CREATED;
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
                .filter(player -> player.id != playerId)
                .toList();
    }
}
