package com.thehuginn;

import com.thehuginn.entities.Game;
import com.thehuginn.entities.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPlayersChanged {

    private static Player player(long id, String name) {
        Player player = new Player();
        player.id = id;
        player.name = name;
        return player;
    }

    @Test
    void testNewGameHasNoChangedPlayers() {
        Game game = new Game("game");

        Assertions.assertFalse(game.hasPlayersChanged());
        Assertions.assertFalse(game.gameContext().playersChanged);
    }

    @Test
    void testAddingPlayerChangesPlayers() {
        Game game = new Game("game");
        game.addPlayer(player(1L, "player1"));

        Assertions.assertTrue(game.hasPlayersChanged());
        Assertions.assertTrue(game.gameContext().playersChanged);
    }

    @Test
    void testAddingExistingPlayerDoesNotChangePlayers() {
        Game game = new Game("game");
        game.addPlayer(player(1L, "player1"));
        game.playersChanged = false;

        Assertions.assertNull(game.addPlayer(player(2L, "player1")));
        Assertions.assertFalse(game.hasPlayersChanged());
    }

    @Test
    void testRemovingPlayerChangesPlayers() {
        Game game = new Game("game");
        game.addPlayer(player(1L, "player1"));
        game.playersChanged = false;

        Assertions.assertFalse(game.removePlayer(2L));
        Assertions.assertFalse(game.hasPlayersChanged());

        Assertions.assertTrue(game.removePlayer(1L));
        Assertions.assertTrue(game.hasPlayersChanged());
    }

    @Test
    void testMissingValueMeansNoChange() {
        Game game = new Game("game");
        game.playersChanged = null;

        Assertions.assertFalse(game.gameContext().playersChanged);
    }
}
