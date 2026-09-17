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

    // වෙනස් කළ යුතු රේඛාව:
    @ManyToOne
    @JoinColumn(name = "added_by_id")
    private User addedBy;

    @Column(name = "expense_date")
    private LocalDateTime expenseDate = LocalDateTime.now();
}