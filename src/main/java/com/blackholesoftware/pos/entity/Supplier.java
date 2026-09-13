package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Supplier extends BaseSyncEntity {

    @Column(nullable = false)
    private String name;

    private String companyName;

    @Column(nullable = false)
    private String phone;

    private String email;
    private String address;

    private LocalDateTime createdAt = LocalDateTime.now();
}