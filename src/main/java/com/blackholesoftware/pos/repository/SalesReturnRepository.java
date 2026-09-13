package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesReturnRepository extends BaseSyncRepository<SalesReturn, String> {

    // 1. SalesReturn Entity එකේ originalSale field එක හරහා Sale Entity එකේ id එක exist වෙනවාදැයි පරීක්ෂා කිරීම
    boolean existsByOriginalSaleId(String saleId);

    // 2. Return Number (RET-xxxx) එකෙන් Return record එක සොයා ගැනීමට
    Optional<SalesReturn> findByReturnNumber(String returnNumber);

    // 3. Status (UNSETTLED / SETTLED) අනුව Returns සෙවීමට
    List<SalesReturn> findByStatus(SalesReturn.ReturnStatus status);

    // 4. Original Sale හි invoiceNumber එක හරහා Returns සෙවීමට (Nested Property Mapping)
    List<SalesReturn> findByOriginalSaleInvoiceNumber(String invoiceNumber);

}