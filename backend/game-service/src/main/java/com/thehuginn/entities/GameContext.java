package com.thehuginn.entities;

import java.util.List;

public class GameContext {
    public List<String> players;

    public GameContext(Game game) {
        this.players = game.team.stream()
                .map(player -> player.name)
                .toList();
    }
}
