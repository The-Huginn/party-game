package com.thehuginn.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Player extends PanacheEntity {
    String name;
}
