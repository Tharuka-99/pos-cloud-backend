package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Unit extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String shortCode;
}