package com.blackholesoftware.pos.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummaryDto {

    // Summary Cards
    private Double totalRevenue;
    private Long totalOrders;
    private Double cashSales;
    private Double creditPayments;
    private Double creditReceived;
    private Double grossProfit;
    private Double returnedProfitLoss;
    private Double totalExpenses;
    private Double netProfit;
    private Double outstandingCredit;
    private Double paidCredits;

    // Pie Chart Data
    private PaymentBreakdown paymentBreakdown;

    // Fast Moving & Low Stock
    private List<LowStockBatchDto> lowStockBatches;
    private List<TopProductDto> topSellingProducts;
    private List<DailyTrendDto> dailySalesTrend;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class PaymentBreakdown {
        private Double cashTotal;
        private Double creditRemainingTotal;
        private Integer cashPercentage;
        private Integer creditPercentage;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class LowStockBatchDto {
        private String productName;
        private String batchNo;
        private Integer currentQuantity;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class TopProductDto {
        private String productName;
        private Integer totalQuantitySold;
    }
}