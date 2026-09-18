package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.ProductRequestDTO;
import com.blackholesoftware.pos.dto.ProductResponseDTO;
import com.blackholesoftware.pos.entity.Batch;
import com.blackholesoftware.pos.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllProducts() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Products fetched successfully", productService.getAllProducts()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(@RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product created successfully", productService.createProductWithBatch(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(@PathVariable("id") String id, @RequestBody ProductRequestDTO dto) {
        productService.updateProduct(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product updated successfully", null));
    }

    @GetMapping("/{id}/batches")
    public ResponseEntity<ApiResponse<List<Batch>>> getProductBatches(@PathVariable("id") String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Batches fetched successfully", productService.getBatchesByProductId(id)));
    }

    @PostMapping("/{id}/batches")
    public ResponseEntity<ApiResponse<Batch>> addBatchToProduct(
            @PathVariable("id") String id,
            @RequestBody ProductRequestDTO.InitialBatchDTO batchDto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch added successfully", productService.addBatchToExistingProduct(id, batchDto)));
    }
}