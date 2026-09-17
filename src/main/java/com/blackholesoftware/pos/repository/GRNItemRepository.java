package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.GRNItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GRNItemRepository extends BaseSyncRepository<GRNItem, String> {
    List<GRNItem> findByGrn_Id(String grnId);
}