package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends BaseSyncRepository<SystemSetting, String> {

    boolean existsBySettingKey(String settingKey);

    Optional<SystemSetting> findBySettingKey(String settingKey);

}