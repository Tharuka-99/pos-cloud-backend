package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.GRN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GRNRepository extends BaseSyncRepository<GRN, String> {
    Optional<GRN> findByGrnNumber(String grnNumber);
}