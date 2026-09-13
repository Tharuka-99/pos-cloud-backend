package com.blackholesoftware.pos.dto;

import lombok.Data;

@Data
public class CreditPaymentRequestDTO {
    private String customerId;
    private Float amountPaid;
    private String paymentMethod; // CASH, CARD
    private String note;
}