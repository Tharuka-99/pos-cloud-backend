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

    // Latest batch details (Fallback for simple views)
    private String latestBatchNo;
    private Double costPrice;
    private Double sellingPrice;
    private Double discountAmount;
    private Integer currentStock;
    private LocalDate expiryDate;

    // Billing UI එක සඳහා සියලුම Batches ලැයිස්තුව
    private List<BatchDTO> batches;

    @Data
    public static class BatchDTO {
        private String id;
        private String batchNo;
        private Double costPrice;
        private Double sellingPrice;
        private Double discountAmount;
        private Integer currentQuantity;
        private LocalDate expiryDate;
    }
}