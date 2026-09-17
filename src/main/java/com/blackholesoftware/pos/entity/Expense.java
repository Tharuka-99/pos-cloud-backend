package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Expense extends BaseSyncEntity {

    @Column(nullable = false,name = "category_name")
    private String categoryName;

    @Column(nullable = false)
    private Double amount;

    private String description;

    @Column(name = "payment_method")
    private String paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User addedBy;

    @Column(name = "expense_date")
    private LocalDateTime expenseDate = LocalDateTime.now();
}