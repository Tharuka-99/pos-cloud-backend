package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brands")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Brand extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String name;
}