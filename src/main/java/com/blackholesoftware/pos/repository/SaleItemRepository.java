package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SaleItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends BaseSyncRepository<SaleItem, String> {
    List<SaleItem> findBySaleId(String saleId);
}