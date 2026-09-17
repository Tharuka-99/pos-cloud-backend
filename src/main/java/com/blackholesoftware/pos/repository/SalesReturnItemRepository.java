package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SalesReturnItem;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesReturnItemRepository extends BaseSyncRepository<SalesReturnItem, String> {
}