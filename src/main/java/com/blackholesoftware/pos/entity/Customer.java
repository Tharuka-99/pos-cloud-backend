package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer extends BaseSyncEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    private String email;
    private String address;

    private Double creditLimit = 0.0;

    @Column(name = "current_credit")
    private Float currentCredit;

    private Integer loyaltyPoints = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}