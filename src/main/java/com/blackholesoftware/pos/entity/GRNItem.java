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

    @Column(nullable = false)
    private Double quantityReceived;

    @Column(nullable = false)
    private Double unitBuyingPrice;

    @Column(nullable = false)
    private Double unitSellingPrice;

    private Double totalPrice;
}