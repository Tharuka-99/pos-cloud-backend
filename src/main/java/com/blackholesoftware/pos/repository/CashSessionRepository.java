package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.CashSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashSessionRepository extends BaseSyncRepository<CashSession, String> {

    @Query("SELECT c FROM CashSession c WHERE c.status = :status ORDER BY c.openedAt DESC")
    List<CashSession> findSessionsByStatus(@Param("status") String status);
}