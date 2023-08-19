package com.thehuginn.token.resolved;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thehuginn.task.ResolvedTask;
import com.thehuginn.task.ResolvedToken;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractResolvedToken extends PanacheEntity implements ResolvedToken {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolvedTask_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ResolvedTask resolvedTask;
}
