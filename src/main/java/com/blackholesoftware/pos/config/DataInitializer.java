package com.blackholesoftware.pos.config;

import com.blackholesoftware.pos.entity.SystemSetting;
import com.blackholesoftware.pos.repository.SystemSettingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SystemSettingRepository settingRepository;

    public DataInitializer(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, String> defaultSettings = Map.of(
                "SHOP_NAME", "Suneri's Gift Shop",
                "SHOP_ADDRESS", "No. 12, Main Street, Colombo",
                "SHOP_PHONE", "+94 77 123 4567",
                "RECEIPT_HEADER", "Welcome to Suneri's Gift Shop!",
                "RECEIPT_FOOTER", "Thank you for shopping with us! Come again.",
                "PRINTER_NAME", "POS-80",
                "PRINTER_PAPER_SIZE", "80mm",
                "AUTO_PRINT_RECEIPT", "true"
        );

        defaultSettings.forEach((key, value) -> {
            Optional<SystemSetting> existingSetting = settingRepository.findBySettingKey(key);

            if (existingSetting.isPresent()) {
                // Key එක දැනටමත් තිබුණොත් එකම ID එක අරන් Update කරනවා (Duplicate Error එන්නේ නැහැ)
                SystemSetting setting = existingSetting.get();
                setting.setSettingValue(value);
                setting.setUpdatedAt(LocalDateTime.now());
                settingRepository.save(setting);
            } else {
                // Key එක නැත්නම් විතරක් අලුතෙන් Create කරලා Insert කරනවා
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(value);
                setting.setIsSynced(true);
                setting.setUpdatedAt(LocalDateTime.now());
                settingRepository.save(setting);
            }
        });
    }
}