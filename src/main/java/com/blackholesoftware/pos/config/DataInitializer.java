package com.blackholesoftware.pos.config;

import com.blackholesoftware.pos.entity.SystemSetting;
import com.blackholesoftware.pos.repository.SystemSettingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SystemSettingRepository settingRepository;

    public DataInitializer(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    public void run(String... args) {
        Map<String, String> defaultSettings = Map.of(
                "SHOP_NAME", "Suneri's Gift Shop",
                "SHOP_ADDRESS", "No. 12, Main Street, Colombo",
                "SHOP_PHONE", "+94 77 123 4567",
                "RECEIPT_HEADER", "Welcome to Suneri's Gift Shop!",
                "RECEIPT_FOOTER", "Thank you for shopping with us! Come again.",
                "PRINTER_NAME", "POS-80",
                "PRINTER_PAPER_SIZE", "80mm", // 80mm or 58mm
                "AUTO_PRINT_RECEIPT", "true"
        );

        defaultSettings.forEach((key, value) -> {
            if (!settingRepository.existsById(key)) {
                settingRepository.save(new SystemSetting(key, value));
            }
        });
    }
}