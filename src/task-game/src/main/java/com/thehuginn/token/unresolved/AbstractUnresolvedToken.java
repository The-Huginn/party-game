package com.thehuginn.token.unresolved;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractUnresolvedToken extends PanacheEntityBase implements Token {

    @Id
    @JsonProperty
    String key;

    public AbstractUnresolvedToken() {}

    protected AbstractUnresolvedToken(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
