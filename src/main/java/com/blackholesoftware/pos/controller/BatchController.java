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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class BatchController {

    private final ProductService productService;

    public BatchController(ProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{batchId}")
    public ResponseEntity<ApiResponse<Batch>> updateBatch(
            @PathVariable("batchId") String batchId,
            @RequestBody Batch batchUpdateData) {
        try {
            Batch updatedBatch = productService.updateBatchDetails(batchId, batchUpdateData);
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
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable("batchId") String batchId) {
        try {
            productService.deleteBatch(batchId);
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