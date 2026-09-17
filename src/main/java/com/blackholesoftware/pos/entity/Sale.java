package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_invoice", columnList = "invoice_number"),
        @Index(name = "idx_sale_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Sale extends BaseSyncEntity {

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User cashier;

    @Column(name = "sub_total")
    private Double subTotal;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "overall_discount")
    private Double overallDiscount = 0.0;

    @Column(name = "net_total")
    private Double netTotal;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "balance_amount")
    private Double balanceAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status")
    private BillReturnStatus returnStatus = BillReturnStatus.NONE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sale_id")
    @JsonManagedReference
    private List<SaleItem> items;

    public enum PaymentMethod { CASH, CARD, CREDIT, MULTI }
    public enum SyncStatus { PENDING, SYNCED, FAILED }
    public enum BillReturnStatus { NONE, PARTIAL, FULLY_RETURNED }
}