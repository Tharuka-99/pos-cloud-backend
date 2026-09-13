package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends BaseSyncRepository<Sale, String>{

    // 1. Cash Sales Sum
    @Query("SELECT COALESCE(SUM(s.netTotal), 0.0) FROM Sale s WHERE s.paymentMethod = 'CASH' AND s.createdAt BETWEEN :start AND :end")
    Double getCashSalesSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Filtered Sales Count
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    Long getSalesCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 3. Credit Payments (PAYMENT_RECEIVED)
    @Query("SELECT COALESCE(SUM(tx.amount), 0.0) FROM CustomerCreditTransaction tx WHERE tx.transactionType = 'PAYMENT_RECEIVED' AND tx.createdAt BETWEEN :start AND :end")
    Double getCreditPaymentsSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 4. Credit Received (CREDIT_GIVEN)
    @Query("SELECT COALESCE(SUM(tx.amount), 0.0) FROM CustomerCreditTransaction tx WHERE tx.transactionType = 'CREDIT_GIVEN' AND tx.createdAt BETWEEN :start AND :end")
    Double getCreditReceivedSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 1. Overall Sales Discount (Sales table එකේ overall_discount column එකේ එකතුව)
    @Query("SELECT COALESCE(SUM(s.overallDiscount), 0.0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    Double getOverallSalesDiscountSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Outstanding Credit Given (CREDIT_GIVEN - PAYMENT_RECEIVED)
    @Query("SELECT (COALESCE((SELECT SUM(tx1.amount) FROM CustomerCreditTransaction tx1 WHERE tx1.transactionType = 'CREDIT_GIVEN' AND tx1.createdAt BETWEEN :start AND :end), 0.0) - " +
            "COALESCE((SELECT SUM(tx2.amount) FROM CustomerCreditTransaction tx2 WHERE tx2.transactionType = 'PAYMENT_RECEIVED' AND tx2.createdAt BETWEEN :start AND :end), 0.0))")
    Double getCreditGivenSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 5. Total Expenses
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end")
    Double getTotalExpensesSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 6. Paid Credits (PAID)
    @Query("SELECT COALESCE(SUM(tx.amount), 0.0) FROM CustomerCreditTransaction tx WHERE tx.transactionType = 'PAID' AND tx.createdAt BETWEEN :start AND :end")
    Double getPaidCreditsSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 7. Total Outstanding Credit (All time)
    @Query("SELECT COALESCE(SUM(c.currentCredit), 0.0) FROM Customer c")
    Double getTotalOutstandingCredit();

    // 8. Credit Remaining Total (Credit Sales)
    @Query("SELECT COALESCE(SUM(CASE WHEN (s.netTotal - s.paidAmount) > 0 THEN (s.netTotal - s.paidAmount) ELSE 0 END), 0.0) FROM Sale s WHERE s.paymentMethod = 'CREDIT' AND s.createdAt BETWEEN :start AND :end")
    Double getCreditRemainingTotal(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // -------------------------------------------------------------
    // FIXED QUERIES (Sale -> items mapping)
    // -------------------------------------------------------------

    // 1. Gross Profit = SUM((unit_price - cost_price - discount) * quantity)
    @Query("SELECT COALESCE(SUM(((i.unitPrice - COALESCE(b.costPrice, i.unitCost, 0.0)) - COALESCE(i.discount, 0.0)) * i.quantity), 0.0) " +
            "FROM Sale s JOIN s.items i LEFT JOIN i.batch b " +
            "WHERE s.createdAt BETWEEN :startDateTime AND :endDateTime")
    Double getGrossProfit(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    // UPDATED: Returned Items Profit using Batch Table fields directly
    // Profit = (sellingPrice - costPrice - discountAmount) * returnQuantity
    @Query("SELECT COALESCE(SUM(((COALESCE(b.sellingPrice, 0.0) - COALESCE(b.costPrice, 0.0) - COALESCE(b.discountAmount, 0.0)) * i.returnQuantity)), 0.0) " +
            "FROM SalesReturn sr JOIN sr.items i LEFT JOIN i.batch b " +
            "WHERE sr.returnedAt BETWEEN :startDateTime AND :endDateTime")
    Double getReturnedProfitLoss(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    // 11. Low Stock Batches
    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.product WHERE b.currentQuantity IS NOT NULL AND b.currentQuantity <= 5 ORDER BY b.currentQuantity ASC")
    List<com.blackholesoftware.pos.entity.Batch> findLowStockBatches(org.springframework.data.domain.Pageable pageable);

    // 12. Top 5 Fast Moving Products (FIXED)
    @Query("SELECT i.product.name AS productName, SUM(i.quantity) AS totalQty " +
            "FROM Sale s JOIN s.items i " +
            "WHERE s.createdAt BETWEEN :start AND :end GROUP BY i.product.name ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    // 13. Daily Sales Trend (Bar Chart Data Query - Fixed Date Cast)
    @Query("SELECT CAST(s.createdAt AS date), COALESCE(SUM(s.netTotal), 0.0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end GROUP BY CAST(s.createdAt AS date) ORDER BY CAST(s.createdAt AS date)")
    List<Object[]> getDailySalesTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 14. Daily Profit Trend (Bar Chart Data Query - Fixed Date Cast)
    @Query("SELECT CAST(s.createdAt AS date), COALESCE(SUM((i.unitPrice - COALESCE(b.costPrice, i.unitCost, 0.0) - COALESCE(i.discount, 0.0)) * i.quantity), 0.0) FROM Sale s JOIN s.items i LEFT JOIN i.batch b WHERE s.createdAt BETWEEN :start AND :end GROUP BY CAST(s.createdAt AS date) ORDER BY CAST(s.createdAt AS date)")
    List<Object[]> getDailyProfitTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Credit Sales වලදී පාරිභෝගිකයා ඒ වෙලාවේ ගෙවූ මුදල (Paid Amount)
    @Query("SELECT COALESCE(SUM(s.paidAmount), 0.0) FROM Sale s WHERE s.paymentMethod = 'CREDIT' AND s.createdAt BETWEEN :start AND :end")
    Double getCreditSalesPaidAmountSum(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}