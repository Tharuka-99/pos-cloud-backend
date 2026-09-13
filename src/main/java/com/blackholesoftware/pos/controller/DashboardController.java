package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.DashboardSummaryDto;
import com.blackholesoftware.pos.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            DashboardSummaryDto summaryDto = dashboardService.getDashboardSummary(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard summary loaded successfully", summaryDto));
        } catch (Exception e) {
            log.error("Error loading dashboard summary", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Dashboard Error: " + e.getMessage(), null));
        }
    }
}