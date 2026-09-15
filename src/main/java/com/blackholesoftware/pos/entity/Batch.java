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

    @Column(nullable = false, unique = true)
    private String batchNo;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "barcodes"})
    private Product product;

    @Column(nullable = false)
    private Double costPrice;

    @Column(nullable = false)
    private Double sellingPrice;

    @Column(nullable = false)
    private Double initialQuantity;

    @Column(nullable = false)
    private Double currentQuantity;

    @Column(nullable = false)
    private Double discountAmount = 0.0;

    private String barcode;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private LocalDateTime createdAt = LocalDateTime.now();
}