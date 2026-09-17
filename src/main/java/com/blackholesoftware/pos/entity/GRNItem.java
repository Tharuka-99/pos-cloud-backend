package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grn_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GRNItem extends BaseSyncEntity {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false,name = "quantity_received")
    private Double quantityReceived;

    @Column(nullable = false,name = "unit_buying_price")
    private Double unitBuyingPrice;

    @Column(nullable = false,name = "unit_selling_price")
    private Double unitSellingPrice;

    @Column(name = "total_price")
    private Double totalPrice;
}