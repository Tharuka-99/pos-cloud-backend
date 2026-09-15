package com.blackholesoftware.pos.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderRequestDto {
    private String supplierId;
    private List<PoItemDto> items;

    @Data
    public static class PoItemDto {
        private String productId;
        private Double quantity;
        private Double estimatedUnitCost;
    }
}