package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends BaseSyncRepository<Unit, String> {
}