package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sale_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaleItem extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "barcodes"})
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "product"})
    private Batch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "unit_cost")
    private Double unitCost;

    private Double discount = 0.0;

    @Column(name = "total_price")
    private Double totalPrice;
}