package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.PurchaseOrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends BaseSyncRepository<PurchaseOrderItem, String> {
    List<PurchaseOrderItem> findByPurchaseOrderId(String purchaseOrderId);
}