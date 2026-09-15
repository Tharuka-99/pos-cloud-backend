package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sales_return_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturnItem extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_return_id")
    @JsonBackReference
    private SalesReturn salesReturn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "barcodes"})
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "product"})
    private Batch batch;

    @Column(nullable = false)
    private Double returnQuantity;

    @Column(nullable = false)
    private Double refundUnitPrice;

    private Double totalRefundAmount;
}