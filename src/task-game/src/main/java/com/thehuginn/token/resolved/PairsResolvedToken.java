package com.thehuginn.token.resolved;

import com.thehuginn.resolution.ResolutionContext;
import com.thehuginn.resolution.UnresolvedResult;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@OnDelete(action = OnDeleteAction.CASCADE)
public class PairsResolvedToken extends AbstractResolvedToken {

    private static final String tag = "pairs";

    @ElementCollection(targetClass = Pair.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "pairsresolvedtoken_pairs", foreignKey = @ForeignKey(name = "fk_pairsresolvedtoken_pairs", foreignKeyDefinition = "foreign key (pairsresolvedtoken_id) references PairsResolvedToken (id) on delete cascade"))
    List<Pair> pairs = new ArrayList<>();

    public PairsResolvedToken() {
    }

    public PairsResolvedToken(List<String> allPlayers) {
        List<String> players = new ArrayList<>(allPlayers);
        Collections.shuffle(players);
        // remove last player if we have odd number of players
        players = players.subList(0, (players.size() / 2) * 2);
        for (int i = 0; i < players.size(); i += 2) {
            pairs.add(new Pair(players.get(i), players.get(i + 1)));
        }
    }

    @Override
    public UnresolvedResult resolve(ResolutionContext context) {
        return new UnresolvedResult().appendData(Map.entry(tag, Uni.createFrom().item(pairs)));
    }

    @Override
    public boolean isResolvable(ResolutionContext context) {
        return true;
    }

    @Embeddable
    public static final class Pair {
        public String first;
        public String second;

        public Pair() {
        }

        public Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
