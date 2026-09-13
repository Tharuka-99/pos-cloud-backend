package com.blackholesoftware.pos.dto;

import lombok.Data;

@Data
public class ExpenseRequestDto {
    private String categoryName;
    private Double amount;
    private String description;
    private String paymentMethod; // CASH, CARD, BANK_TRANSFER
    private String userId;        // Expense එක ඇතුළත් කළ Cashier/Admin ID
}