package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.Category;
import com.blackholesoftware.pos.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final MasterDataService masterDataService;

    public CategoryController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories fetched successfully", masterDataService.getAllCategories()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category category) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Category created successfully", masterDataService.saveCategory(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> update(@PathVariable String id, @RequestBody Category category) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Category updated successfully", masterDataService.updateCategory(id, category)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        masterDataService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category deleted successfully", null));
    }
}