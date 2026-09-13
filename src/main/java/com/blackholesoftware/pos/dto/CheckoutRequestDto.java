package com.blackholesoftware.pos.dto;

import com.blackholesoftware.pos.entity.Sale;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequestDto {
    private String customerId;
    private String cashierId;
    private Sale.PaymentMethod paymentMethod;
    private Double discountAmount = 0.0;
    private Double overallDiscount = 0.0;
    private Double paidAmount = 0.0;

    // Return Credit Handlers (Red Underline Fixes)
    private Double returnCreditDeduction = 0.0;
    private String appliedReturnId;

    private List<CheckoutItemDto> items;

    @Data
    public static class CheckoutItemDto {
        private String productId;
        private String batchId;
        private Integer quantity;
        private Double unitPrice;
        private Double discount = 0.0;
        private Double totalPrice;
    }
}