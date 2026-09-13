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
        @Index(name = "idx_sale_invoice", columnList = "invoiceNumber"),
        @Index(name = "idx_sale_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Sale extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User cashier;

    private Double subTotal;
    private Double discountAmount = 0.0;
    private Double overallDiscount = 0.0;
    private Double netTotal;
    private Double paidAmount;
    private Double balanceAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private BillReturnStatus returnStatus = BillReturnStatus.NONE;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sale_id")
    @JsonManagedReference
    private List<SaleItem> items;

    public enum PaymentMethod { CASH, CARD, CREDIT, MULTI }
    public enum SyncStatus { PENDING, SYNCED, FAILED }
    public enum BillReturnStatus { NONE, PARTIAL, FULLY_RETURNED }
}