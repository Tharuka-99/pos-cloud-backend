package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SalesReturnItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReturnItemRepository extends BaseSyncRepository<SalesReturnItem, String> {
    List<SalesReturnItem> findBySalesReturn_Id(String salesReturnId);
}