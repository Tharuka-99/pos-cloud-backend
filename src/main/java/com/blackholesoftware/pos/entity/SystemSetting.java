package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SystemSetting extends BaseSyncEntity {

    @Column(name = "setting_key", nullable = false, unique = true)
    private String settingKey; // e.g. "SHOP_NAME", "RECEIPT_FOOTER", "CLOUD_SYNC_URL"

    @Column(name = "setting_value", nullable = false)
    private String settingValue;
}