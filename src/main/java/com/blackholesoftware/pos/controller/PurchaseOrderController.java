package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.GrnReceiveRequestDto;
import com.blackholesoftware.pos.dto.PurchaseOrderRequestDto;
import com.blackholesoftware.pos.entity.GRN;
import com.blackholesoftware.pos.entity.PurchaseOrder;
import com.blackholesoftware.pos.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrderRequestDto dto) {
        return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(dto));
    }

    @PostMapping("/{id}/receive-grn")
    public ResponseEntity<GRN> receiveGrnAndStock(
            @PathVariable("id") String poId,
            @RequestBody GrnReceiveRequestDto dto) {
        GRN grn = purchaseOrderService.processGrnAndStockUpdate(poId, dto);
        return ResponseEntity.ok(grn);
    }
}