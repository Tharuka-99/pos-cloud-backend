package com.blackholesoftware.pos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hardware")
public class HardwareController {

    @PostMapping("/open-drawer")
    public ResponseEntity<?> openDrawer() {
        // Cash drawer command logic (or simple success response)
        return ResponseEntity.ok().body("Drawer trigger sent");
    }
}