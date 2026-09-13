package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "grns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GRN extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String grnNumber;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @OneToOne
    @JoinColumn(name = "po_id")
    private PurchaseOrder purchaseOrder;

    private Double totalAmount;
    private Double paidAmount;
    private Double dueAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime receivedDate = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "grn_id")
    private List<GRNItem> items;

    public enum PaymentStatus { PAID, PARTIAL, CREDIT }
}