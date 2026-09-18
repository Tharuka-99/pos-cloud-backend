package com.blackholesoftware.pos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hardware")
public class HardwareController {

    @PostMapping("/open-drawer")
    public ResponseEntity<?> openDrawer() {
        return ResponseEntity.ok().body("Drawer trigger sent");
    }
}