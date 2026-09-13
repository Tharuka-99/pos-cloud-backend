package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.Batch;
import com.blackholesoftware.pos.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final ProductService productService;

    public BatchController(ProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{batchId}")
    public ResponseEntity<ApiResponse<Batch>> updateBatch(
            @PathVariable("batchId") String batchId,
            @RequestBody Batch batchUpdateData) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch updated successfully", productService.updateBatchDetails(batchId, batchUpdateData)));
    }
}