package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Expense extends BaseSyncEntity {

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private Double amount;

    private String description;
    private String paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User addedBy;

    private LocalDateTime expenseDate = LocalDateTime.now();
}