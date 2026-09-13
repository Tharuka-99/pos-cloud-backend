package com.blackholesoftware.pos.dto;

import lombok.Data;

@Data
public class CustomerPaymentDto {
    private Double amount;
    private String paymentMethod; // CASH, CARD
    private String notes;
}