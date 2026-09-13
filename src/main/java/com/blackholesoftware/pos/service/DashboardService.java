package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.dto.DailyTrendDto;
import com.blackholesoftware.pos.dto.DashboardSummaryDto;
import com.blackholesoftware.pos.entity.Batch;
import com.blackholesoftware.pos.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepository dashboardRepository;

    public DashboardSummaryDto getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        // Sales & Payments Data Fetching (Null-Safe Unboxing)
        Double cashSales = toDouble(dashboardRepository.getCashSalesSum(startDateTime, endDateTime));
        Double creditSalesPaidAmount = toDouble(dashboardRepository.getCreditSalesPaidAmountSum(startDateTime, endDateTime));
        Double creditReceived = toDouble(dashboardRepository.getCreditReceivedSum(startDateTime, endDateTime));
        Double totalExpenses = toDouble(dashboardRepository.getTotalExpensesSum(startDateTime, endDateTime));

        Long totalOrders = toLong(dashboardRepository.getSalesCount(startDateTime, endDateTime));
        Double creditPayments = toDouble(dashboardRepository.getCreditPaymentsSum(startDateTime, endDateTime));
        Double paidCredits = toDouble(dashboardRepository.getPaidCreditsSum(startDateTime, endDateTime));
        Double creditRemainingTotal = toDouble(dashboardRepository.getCreditRemainingTotal(startDateTime, endDateTime));

        // Profit & Credit Calculations (Null-Safe)
        Double grossProfit = toDouble(dashboardRepository.getGrossProfit(startDateTime, endDateTime));
        Double returnedProfitLoss = toDouble(dashboardRepository.getReturnedProfitLoss(startDateTime, endDateTime));
        Double overallSalesDiscount = toDouble(dashboardRepository.getOverallSalesDiscountSum(startDateTime, endDateTime));

        Double netProfit = grossProfit - returnedProfitLoss - overallSalesDiscount;
        Double outstandingCredit = toDouble(dashboardRepository.getCreditGivenSum(startDateTime, endDateTime));

        Double totalRevenue = (cashSales + creditSalesPaidAmount) - totalExpenses;

        // Payment Breakdown (Pie Chart) - Pure Cash Sales vs Actual Outstanding Net Credit
        Double totalCashCollected = cashSales + creditSalesPaidAmount;
        Double netCreditGiven = outstandingCredit; // Uses CREDIT_GIVEN sum for accurate breakdown
        Double totalVolume = totalCashCollected + netCreditGiven;
        Integer cashPct = totalVolume > 0 ? (int) Math.round((totalCashCollected / totalVolume) * 100) : 0;
        Integer creditPct = totalVolume > 0 ? (int) Math.round((netCreditGiven / totalVolume) * 100) : 0;

        DashboardSummaryDto.PaymentBreakdown breakdown = new DashboardSummaryDto.PaymentBreakdown(
                totalCashCollected, netCreditGiven, cashPct, creditPct
        );

        // Daily Sales & Profit Trend Calculations (Bar Chart)
        List<Object[]> dailySalesRaw = dashboardRepository.getDailySalesTrend(startDateTime, endDateTime);
        List<Object[]> dailyProfitRaw = dashboardRepository.getDailyProfitTrend(startDateTime, endDateTime);

        // 1. Process Daily Profit Stream (Null-Safe & Format Safe)
        Map<String, Double> profitMap = (dailyProfitRaw != null) ? dailyProfitRaw.stream()
                .filter(row -> row != null && row.length > 0 && row[0] != null)
                .collect(Collectors.toMap(
                        row -> String.valueOf(row[0]),
                        row -> (row.length > 1 && row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0,
                        (existing, replacement) -> existing
                )) : Map.of();

        // 2. Process Daily Sales Loop (Null-Safe & Format Safe)
        List<DailyTrendDto> dailySalesTrend = new ArrayList<>();
        if (dailySalesRaw != null) {
            for (Object[] row : dailySalesRaw) {
                if (row == null || row.length == 0 || row[0] == null) {
                    continue;
                }

                String dateStr = String.valueOf(row[0]);
                Double salesAmount = (row.length > 1 && row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                Double profitAmount = profitMap.getOrDefault(dateStr, 0.0);

                dailySalesTrend.add(new DailyTrendDto(dateStr, salesAmount, profitAmount));
            }
        }

        // Low Stock Fetching
        List<Batch> lowStockEntities = dashboardRepository.findLowStockBatches(PageRequest.of(0, 5));
        List<DashboardSummaryDto.LowStockBatchDto> lowStockBatches = new ArrayList<>();
        if (lowStockEntities != null) {
            for (Batch b : lowStockEntities) {
                if (b != null) {
                    lowStockBatches.add(new DashboardSummaryDto.LowStockBatchDto(
                            b.getProduct() != null ? b.getProduct().getName() : "Unknown",
                            b.getBatchNo(),
                            b.getCurrentQuantity()
                    ));
                }
            }
        }

        // Top Selling Items Fetching (Null-Safe)
        List<Object[]> topSellingRaw = dashboardRepository.findTopSellingProducts(startDateTime, endDateTime, PageRequest.of(0, 5));
        List<DashboardSummaryDto.TopProductDto> topSellingProducts = new ArrayList<>();
        if (topSellingRaw != null) {
            for (Object[] row : topSellingRaw) {
                if (row == null || row.length == 0) continue;

                String productName = toStringVal(row[0], "Unknown Product");
                Integer quantity = (row.length > 1 && row[1] != null) ? ((Number) row[1]).intValue() : 0;

                topSellingProducts.add(new DashboardSummaryDto.TopProductDto(productName, quantity));
            }
        }

        return DashboardSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .cashSales(cashSales)
                .creditPayments(creditPayments)
                .creditReceived(creditReceived)
                .grossProfit(grossProfit)
                .returnedProfitLoss(returnedProfitLoss)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .outstandingCredit(outstandingCredit)
                .paidCredits(paidCredits)
                .paymentBreakdown(breakdown)
                .dailySalesTrend(dailySalesTrend)
                .lowStockBatches(lowStockBatches)
                .topSellingProducts(topSellingProducts)
                .build();
    }

    // --- Null-Safe Helper Utility Methods ---
    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private String toStringVal(Object obj, String defaultValue) {
        return obj != null ? obj.toString() : defaultValue;
    }
}