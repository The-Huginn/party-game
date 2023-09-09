package com.thehuginn.entities;

import java.util.List;

public class GameContext {
    public List<Player> players;

    public GameContext(Game game) {
        this.players = game.team;
    }
}
