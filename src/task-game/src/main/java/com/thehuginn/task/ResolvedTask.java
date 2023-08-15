package com.thehuginn.task;

import com.thehuginn.resolution.ResolutionContext;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class ResolvedTask extends PanacheEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    public Task unresolvedTask;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "resolved_task_tokens", joinColumns = @JoinColumn(name = "resolvedtask_id", referencedColumnName = "id"))
//    @MapKeyColumn(name = "token_name")
//    @Column(name = "tokens")
//    public Map<String, String> tokens;


    public ResolvedTask() {}

    public static ResolvedTask resolve(GameTask gameTask, ResolutionContext resolutionContext) {
        return null;
    }
}
