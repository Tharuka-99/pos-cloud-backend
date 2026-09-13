package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.Supplier;
import com.blackholesoftware.pos.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final MasterDataService masterDataService;

    public SupplierController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Supplier>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Suppliers fetched successfully", masterDataService.getAllSuppliers()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> create(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Supplier created successfully", masterDataService.saveSupplier(supplier)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> update(@PathVariable String id, @RequestBody Supplier supplier) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Supplier updated successfully", masterDataService.updateSupplier(id, supplier)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        masterDataService.deleteSupplier(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Supplier deleted successfully", null));
    }
}