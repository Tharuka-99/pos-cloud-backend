package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashSession extends BaseSyncEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "opening_balance")
    private Double openingBalance;

    @Column(name = "closing_actual_cash")
    private Double closingActualCash;

    @Column(name = "total_cash_sales")
    private Double totalCashSales = 0.0;

    @Column(name = "total_expenses")
    private Double totalExpenses = 0.0;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status")
    private String status; // OPEN, CLOSED

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    public void customPrePersist() {
        if (this.openedAt == null) {
            this.openedAt = LocalDateTime.now();
        }
    }
}