package com.blackholesoftware.pos.controller; // ඔයාගේ package path එක දෙන්න

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    // app_users Sync Request එක Receive කරගෙන OK 200 Return කරනවා
    @PostMapping("/app_users")
    public ResponseEntity<?> syncAppUsers(@RequestBody List<Map<String, Object>> users) {
        // TODO: Remote DB එකට Users ලා save කරන්න ඕනි නම් මෙතන logic එක ලියන්න
        return ResponseEntity.ok(Map.of("message", "Users synced successfully"));
    }

    // Generic Sync Endpoint (අනෙකුත් Tables සඳහා ඕනි නම්)
    @PostMapping("/{tableName}")
    public ResponseEntity<?> syncTable(@PathVariable String tableName, @RequestBody List<Map<String, Object>> data) {
        return ResponseEntity.ok(Map.of("message", tableName + " synced successfully"));
    }
}