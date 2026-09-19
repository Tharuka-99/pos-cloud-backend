package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.Batch;
import com.blackholesoftware.pos.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final ProductService productService;

    public BatchController(ProductService productService) {
        this.productService = productService;
    }

    // Helper method to clean extra suffixes like :1 or spaces
    private String sanitizeBatchId(String id) {
        if (id == null) return null;
        if (id.contains(":")) {
            return id.split(":")[0].trim();
        }
        return id.trim();
    }

    @PutMapping("/{batchId}")
    public ResponseEntity<ApiResponse<Batch>> updateBatch(
            @PathVariable("batchId") String rawBatchId,
            @RequestBody Batch batchUpdateData) {
        try {
            String cleanBatchId = sanitizeBatchId(rawBatchId);
            Batch updatedBatch = productService.updateBatchDetails(cleanBatchId, batchUpdateData);
            return ResponseEntity.ok(new ApiResponse<>(true, "Batch updated successfully", updatedBatch));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating batch: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{batchId}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable("batchId") String rawBatchId) {
        try {
            String cleanBatchId = sanitizeBatchId(rawBatchId);
            productService.deleteBatch(cleanBatchId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Batch deleted successfully", null));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting batch: " + e.getMessage(), null));
        }
    }
}