package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.PurchaseOrderItem;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderItemRepository extends BaseSyncRepository<PurchaseOrderItem, String> {
}