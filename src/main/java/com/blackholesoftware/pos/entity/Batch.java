package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Batch extends BaseSyncEntity {

    @Column(name = "batch_no", nullable = false, unique = true)
    private String batchNo;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "barcodes"})
    private Product product;

    @Column(nullable = false, name = "cost_price")
    private Double costPrice;

    @Column(nullable = false,name = "selling_price")
    private Double sellingPrice;

    @Column(nullable = false,name = "initial_quantity")
    private Double initialQuantity;

    @Column(nullable = false,name = "current_quantity")
    private Double currentQuantity;

    @Column(nullable = false,name = "discount_amount")
    private Double discountAmount = 0.0;

    private String barcode;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}