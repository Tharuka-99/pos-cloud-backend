package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends BaseSyncRepository<Sale, String> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    List<Sale> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    // Fast & Flexible Indexed Search (Supports ID, Exact Invoice, or Trimmed search)
    @Query("SELECT s FROM Sale s WHERE " +
            "s.id = :identifier OR " +
            "LOWER(TRIM(s.invoiceNumber)) = LOWER(TRIM(:identifier)) OR " +
            "LOWER(TRIM(s.invoiceNumber)) = LOWER(CONCAT('inv-', TRIM(:identifier)))")
    Optional<Sale> findByIdOrInvoiceNumberFlexible(@Param("identifier") String identifier);

    @Query("SELECT s FROM Sale s WHERE s.paymentMethod = 'CREDIT' OR (s.netTotal - s.paidAmount) > 0 ORDER BY s.createdAt DESC")
    List<Sale> findAllCreditDueSales();
}