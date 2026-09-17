package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.GRNItem;
import org.springframework.stereotype.Repository;

@Repository
public interface GRNItemRepository extends BaseSyncRepository<GRNItem, String> {
}