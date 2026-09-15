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
// @CrossOrigin(origins = "*") ඉවත් කර ඇත (Global Security/Cors Config මගින් CORS handle වේ)
public class SystemSettingController {

    private final SystemSettingRepository settingRepository;

    public SystemSettingController(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    // 1. Get All Settings
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

    // 2. Update Bulk Settings
    @PostMapping("/update-all")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings) {
        try {
            settings.forEach((key, value) -> {
                // මුලින්ම මේ key එක database එකේ තියෙනවද බලන්න
                SystemSetting setting = settingRepository.findBySettingKey(key)
                        .orElse(new SystemSetting());

                setting.setSettingKey(key);
                setting.setSettingValue(value);

                // අවශ්‍ය නම් මෙතැනට sync timestamps හෝ වෙනත් fields ඩාලා තියාගන්න පුළුවන්

                settingRepository.save(setting);
            });
            return ResponseEntity.ok(Map.of("message", "Settings updated successfully!"));
        } catch (Exception e) {
            // Error එක බලාගන්න console එකට print කරnna
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating settings: " + e.getMessage()));
        }
    }
}