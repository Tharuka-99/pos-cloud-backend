package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.Brand;
import com.blackholesoftware.pos.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final MasterDataService masterDataService;

    public BrandController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Brand>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Brands fetched successfully", masterDataService.getAllBrands()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Brand>> create(@RequestBody Brand brand) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Brand created successfully", masterDataService.saveBrand(brand)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Brand>> update(@PathVariable String id, @RequestBody Brand brand) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Brand updated successfully", masterDataService.updateBrand(id, brand)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        masterDataService.deleteBrand(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Brand deleted successfully", null));
    }
}