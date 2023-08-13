package com.thehuginn.task;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;

import java.util.Map;

@Entity
public class ResolvedTask extends PanacheEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    public Task unresolvedTask;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resolved_task_tokens", joinColumns = @JoinColumn(name = "resolvedtask_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "token_name")
    @Column(name = "tokens")
    public Map<String, String> tokens;


}
