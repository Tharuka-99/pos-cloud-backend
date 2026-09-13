package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_credit_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditTransaction extends BaseSyncEntity {

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "sale_id")
    private String saleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "balance_after", nullable = false)
    private Float balanceAfter;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        CREDIT_GIVEN,
        PAYMENT_RECEIVED,
        PAID
    }
}