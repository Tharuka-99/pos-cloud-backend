package com.blackholesoftware.pos.dto;

import lombok.Data;
import java.util.List;

@Data
public class GrnReceiveRequestDto {
    private String purchaseOrderId;
    private List<GrnItemDto> receivedItems;

    @Data
    public static class GrnItemDto {
        private String productId;
        private Double receivedQty;
        Double costPrice;
        private Double sellingPrice;
        private Double discountAmount;
        private String barcode; // 👈 Barcode detection field
    }
}