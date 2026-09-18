package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.Unit;
import com.blackholesoftware.pos.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final MasterDataService masterDataService;

    public UnitController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Unit>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Units fetched successfully", masterDataService.getAllUnits()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Unit>> create(@RequestBody Unit unit) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Unit created successfully", masterDataService.saveUnit(unit)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Unit>> update(@PathVariable String id, @RequestBody Unit unit) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Unit updated successfully", masterDataService.updateUnit(id, unit)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        masterDataService.deleteUnit(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unit deleted successfully", null));
    }
}