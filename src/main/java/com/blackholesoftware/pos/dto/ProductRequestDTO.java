package com.blackholesoftware.pos.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private String categoryId;
    private String brandId;
    private String unitId;
    private Integer reorderLevel;
    private String barcode;
    private String imageUrl;

    private InitialBatchDTO initialBatch;

    @Data
    public static class InitialBatchDTO {
        private String batchNo;
        private String barcode; // 🟢 Batch Level Barcode එකතු කරන ලදී
        private Double costPrice;
        private Double sellingPrice;
        private Double discountAmount;
        private Double initialQuantity;
        private LocalDate manufactureDate;
        private LocalDate expiryDate;
    }
}