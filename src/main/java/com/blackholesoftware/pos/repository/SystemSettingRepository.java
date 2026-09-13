package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends BaseSyncRepository<SystemSetting, String> {
}