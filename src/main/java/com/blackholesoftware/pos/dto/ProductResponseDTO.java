package com.blackholesoftware.pos.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductResponseDTO {
    private String id;
    private String name;
    private String description;
    private String imageUrl;

    private String categoryName;
    private String brandName;
    private String unitName;
    private String unitShortCode;

    private String primaryBarcode;

    // Latest batch details
    private String latestBatchNo;
    private Double costPrice;
    private Double sellingPrice;
    private Double discountAmount;
    private Double currentStock;
    private LocalDate expiryDate;

    private List<BatchDTO> batches;

    @Data
    public static class BatchDTO {
        private String id;
        private String batchNo;
        private String barcode; // 🟢 Batch Level Barcode එකතු කරන ලදී
        private Double costPrice;
        private Double sellingPrice;
        private Double discountAmount;
        private Double currentQuantity;
        private LocalDate expiryDate;
    }
}