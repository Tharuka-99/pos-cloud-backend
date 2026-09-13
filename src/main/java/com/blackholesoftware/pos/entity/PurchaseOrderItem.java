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
    private Integer quantity;

    private Double estimatedUnitCost;
    private Double totalPrice;
}