package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product extends BaseSyncEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    private Integer reorderLevel = 5;

    @Column(name = "current_stock")
    private Double currentStock = 0.0;

    @Lob
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("product")
    private List<ProductBarcode> barcodes;
}