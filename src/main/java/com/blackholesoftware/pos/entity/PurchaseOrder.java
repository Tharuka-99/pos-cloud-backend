package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrder extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String poNumber;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private Double totalAmount;
    private LocalDateTime orderDate = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "po_id")
    private List<PurchaseOrderItem> items;

    public enum Status { PENDING, RECEIVED, CANCELLED }
}