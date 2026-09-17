package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderItem extends BaseSyncEntity {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "estimated_unit_cost")
    private Double estimatedUnitCost;

    @Column(name = "total_price")
    private Double totalPrice;
}