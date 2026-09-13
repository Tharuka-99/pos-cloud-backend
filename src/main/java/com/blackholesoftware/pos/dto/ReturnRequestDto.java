package com.blackholesoftware.pos.dto;

import lombok.Data;

@Data
public class ReturnRequestDto {
    private String saleId;
    private String productId;
    private String batchId;
    private Integer returnQuantity;
    private String reason;
    private String userId;
}