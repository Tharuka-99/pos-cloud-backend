package com.blackholesoftware.pos.dto;

import lombok.Data;

@Data
public class CreditSaleRequestDTO {
    private String customerId;
    private Float totalAmount;
    private String saleId;
    private Float creditAmount;
    private String note;
}