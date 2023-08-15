package com.thehuginn.resolution;

import com.thehuginn.entities.Player;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ResolutionContext {

    private final String locale;
    private final String gameId;
    private final Player player;
    private final List<Player> players;
    private final List<Player> shuffledPlayers;

    private ResolutionContext(String locale) {
        this.locale = locale;
        gameId = null;
        player = null;
        players = null;
        shuffledPlayers = null;
    }

    private ResolutionContext(
            String locale,
            String gameId,
            Player player,
            List<Player> players
    ) {
        this.locale = locale;
        this.gameId = gameId;
        this.player = player;
        this.players = Collections.unmodifiableList(players);
        this.shuffledPlayers = players.stream()
                .filter(player1 -> !player1.equals(player))
                .collect(Collectors.toList());
        Collections.shuffle(this.shuffledPlayers);
    }

    public static Builder builder(String gameId) {
        return new Builder(gameId);
    }

    public static ResolutionContext locale(String locale) {
        return new ResolutionContext(locale);
    }

    public static class Builder {
        private String locale = "en";
        private final String gameId;
        private Player player = null;
        private List<Player> players = null;

        private Builder(@Nonnull String gameId) {
            this.gameId = gameId;
        }

        public Builder locale(@Nonnull String locale) {
            this.locale = locale;
            return this;
        }

        public Builder player(@Nonnull Player player) {
            this.player = player;
            return this;
        }

        public Builder players(@Nonnull List<Player> players) {
            this.players = players;
            return this;
        }

        public ResolutionContext build() {
            return new ResolutionContext(locale, gameId, player, players);
        }
    }

    public String getLocale() {
        return locale;
    }

    public String getGameId() {
        return gameId;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getRandomPlayer(Integer index) {
        if (index < 0 || index >= shuffledPlayers.size()) {
            throw new ArrayIndexOutOfBoundsException("Not enough players to resolve this task");
        }
        return shuffledPlayers.get(index);
    }
}
