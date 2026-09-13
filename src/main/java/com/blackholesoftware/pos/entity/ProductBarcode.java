package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_barcodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductBarcode extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String barcode;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"barcodes", "hibernateLazyInitializer", "handler"})
    private Product product;
}