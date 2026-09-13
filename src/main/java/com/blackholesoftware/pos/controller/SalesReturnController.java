package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.ReturnRequestDto;
import com.blackholesoftware.pos.entity.*;
import com.blackholesoftware.pos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales/returns")
public class SalesReturnController {

    @Autowired private SaleRepository saleRepository;
    @Autowired private SalesReturnRepository salesReturnRepository;
    @Autowired private BatchRepository batchRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    // 1. Process Item Return: POST /api/sales/returns
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<SalesReturn>> processReturn(@RequestBody ReturnRequestDto request) {
        try {
            if (request == null || request.getSaleId() == null || request.getProductId() == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid Return Request Data!", null));
            }

            Optional<Sale> saleOpt = saleRepository.findByIdOrInvoiceNumberFlexible(request.getSaleId());
            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(404).body(new ApiResponse<>(false, "අදාළ Original Bill එක සොයාගත නොහැක!", null));
            }
            Sale originalSale = saleOpt.get();

            Optional<Product> productOpt = productRepository.findById(request.getProductId());
            if (productOpt.isEmpty()) {
                return ResponseEntity.status(404).body(new ApiResponse<>(false, "Product එක සොයාගත නොහැක!", null));
            }
            Product product = productOpt.get();

            int returnQty = request.getReturnQuantity() != null ? request.getReturnQuantity() : 1;

            // RESTORE BATCH STOCK ONLY (No extra columns needed)
            Batch batch = null;
            if (request.getBatchId() != null && !request.getBatchId().trim().isEmpty()) {
                batch = batchRepository.findById(request.getBatchId()).orElse(null);
            }

            if (batch != null) {
                int currentQty = batch.getCurrentQuantity() != null ? batch.getCurrentQuantity() : 0;
                batch.setCurrentQuantity(currentQty + returnQty);
                batchRepository.save(batch);
            }

            User processedBy = null;
            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                processedBy = userRepository.findById(request.getUserId()).orElse(null);
            }
            if (processedBy == null) {
                processedBy = originalSale.getCashier();
            }

            double netUnitPrice = 0.0;
            for (SaleItem item : originalSale.getItems()) {
                if (item.getProduct() != null && item.getProduct().getId().equals(product.getId())) {
                    double unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : 0.0;
                    double discount = item.getDiscount() != null ? item.getDiscount() : 0.0;
                    netUnitPrice = Math.max(0, unitPrice - discount);
                    break;
                }
            }

            double refundTotal = netUnitPrice * returnQty;

            SalesReturn salesReturn = SalesReturn.builder()
                    .returnNumber("RET-" + System.currentTimeMillis())
                    .originalSale(originalSale)
                    .processedBy(processedBy)
                    .totalRefundAmount(refundTotal)
                    .reason(request.getReason() != null ? request.getReason() : "Customer Return")
                    .status(SalesReturn.ReturnStatus.UNSETTLED)
                    .returnedAt(LocalDateTime.now())
                    .build();

            SalesReturnItem returnItem = SalesReturnItem.builder()
                    .salesReturn(salesReturn)
                    .product(product)
                    .batch(batch)
                    .returnQuantity(returnQty)
                    .refundUnitPrice(netUnitPrice)
                    .totalRefundAmount(refundTotal)
                    .build();

            List<SalesReturnItem> returnItems = new ArrayList<>();
            returnItems.add(returnItem);
            salesReturn.setItems(returnItems);

            SalesReturn savedReturn = salesReturnRepository.save(salesReturn);

            originalSale.setReturnStatus(Sale.BillReturnStatus.PARTIAL);
            saleRepository.save(originalSale);

            return ResponseEntity.ok(new ApiResponse<>(true, "Bill Return Process completed successfully", savedReturn));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Return Processing Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/return")
    @Transactional
    public ResponseEntity<ApiResponse<SalesReturn>> processReturnSingular(@RequestBody ReturnRequestDto request) {
        return processReturn(request);
    }

    @PostMapping("/process")
    @Transactional
    public ResponseEntity<ApiResponse<SalesReturn>> processReturnLegacy(@RequestBody ReturnRequestDto request) {
        return processReturn(request);
    }

    // 2. Settle Return Note Direct Endpoint
    @PostMapping("/{id}/settle")
    @Transactional
    public ResponseEntity<ApiResponse<SalesReturn>> settleReturnNote(@PathVariable("id") String returnId) {
        try {
            if (returnId == null || returnId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid Return ID!", null));
            }

            String cleanId = returnId.trim();
            Optional<SalesReturn> returnOpt = salesReturnRepository.findById(cleanId);

            if (returnOpt.isEmpty()) {
                returnOpt = salesReturnRepository.findByReturnNumber(cleanId);
            }

            if (returnOpt.isEmpty()) {
                return ResponseEntity.status(404).body(new ApiResponse<>(false, "Return note එක සොයාගත නොහැක!", null));
            }

            SalesReturn salesReturn = returnOpt.get();
            salesReturn.setStatus(SalesReturn.ReturnStatus.SETTLED);
            SalesReturn updatedReturn = salesReturnRepository.save(salesReturn);

            return ResponseEntity.ok(new ApiResponse<>(true, "Return note settled successfully", updatedReturn));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error settling return: " + e.getMessage(), null));
        }
    }

    @GetMapping("/unsettled")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<SalesReturn>>> getUnsettledReturns() {
        try {
            List<SalesReturn> unsettledList = salesReturnRepository.findByStatus(SalesReturn.ReturnStatus.UNSETTLED);
            return ResponseEntity.ok(new ApiResponse<>(true, "Unsettled returns retrieved successfully", unsettledList));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error fetching unsettled returns: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<SalesReturn>> searchReturnByQuery(@RequestParam String query) {
        try {
            String cleanQuery = query != null ? query.trim() : "";
            Optional<SalesReturn> returnOpt = salesReturnRepository.findByReturnNumber(cleanQuery);

            if (returnOpt.isEmpty()) {
                List<SalesReturn> returns = salesReturnRepository.findByOriginalSaleInvoiceNumber(cleanQuery);
                returnOpt = returns.stream().filter(r -> r.getStatus() == SalesReturn.ReturnStatus.UNSETTLED).findFirst();
            }

            if (returnOpt.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Return slip found", returnOpt.get()));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse<>(false, "Unsettled Return slip not found!", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error searching return slip: " + e.getMessage(), null));
        }
    }

    // Get All Returns for Dashboard / Report Page
    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesReturn>>> getAllReturns() {
        try {
            List<SalesReturn> returns = salesReturnRepository.findAll();
            return ResponseEntity.ok(new ApiResponse<>(true, "Returns retrieved successfully", returns));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }
}