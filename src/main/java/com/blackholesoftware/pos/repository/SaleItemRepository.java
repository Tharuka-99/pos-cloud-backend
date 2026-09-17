package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SaleItem;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends BaseSyncRepository<SaleItem, String> {
}