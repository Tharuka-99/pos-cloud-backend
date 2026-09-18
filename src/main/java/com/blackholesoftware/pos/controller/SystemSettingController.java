package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.entity.SystemSetting;
import com.blackholesoftware.pos.repository.SystemSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingController {

    private final SystemSettingRepository settingRepository;

    public SystemSettingController(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllSettings() {
        try {
            List<SystemSetting> settings = settingRepository.findAll();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Database Error: " + e.getMessage()));
        }
    }

    @PostMapping("/update-all")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings) {
        try {
            settings.forEach((key, value) -> {
                SystemSetting setting = settingRepository.findBySettingKey(key)
                        .orElse(new SystemSetting());

                setting.setSettingKey(key);
                setting.setSettingValue(value);
                setting.setIsSynced(false); // Local change flag for cloud sync

                settingRepository.save(setting);
            });
            return ResponseEntity.ok(Map.of("message", "Settings updated successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating settings: " + e.getMessage()));
        }
    }
}