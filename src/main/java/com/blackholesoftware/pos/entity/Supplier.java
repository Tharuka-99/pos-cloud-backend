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

    @Column(name = "company_name")
    private String companyName;

    @Column(nullable = false)
    private String phone;

    private String email;
    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}