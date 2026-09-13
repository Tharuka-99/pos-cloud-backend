package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.CustomerCreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerCreditTransactionRepository extends BaseSyncRepository<CustomerCreditTransaction, String> {

    // 🔴 Sale ID එකෙන් Duplicate Transaction තිබේදැයි පරීක්ෂා කිරීමට
    Optional<CustomerCreditTransaction> findBySaleId(String saleId);

    List<CustomerCreditTransaction> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM CustomerCreditTransaction c " +
            "WHERE c.transactionType = 'CREDIT_GIVEN' " +
            "AND c.createdAt BETWEEN :startDate AND :endDate")
    Float getTotalCreditGivenBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM CustomerCreditTransaction c " +
            "WHERE (c.transactionType = 'PAYMENT_RECEIVED' OR c.transactionType = 'PAID') " +
            "AND c.createdAt BETWEEN :startDate AND :endDate")
    Float getTotalCreditCollectedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // අද දිනය ඇතුළත Customer Credit Repayments වලින් ලැඛුණු Cash එකතුව
    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM CustomerCreditTransaction c " +
            "WHERE (c.transactionType = 'PAYMENT_RECEIVED' OR c.transactionType = 'PAID') " +
            "AND c.paymentMethod = 'CASH' " +
            "AND c.createdAt BETWEEN :startDate AND :endDate")
    Float getTodayCashCreditRepayments(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}