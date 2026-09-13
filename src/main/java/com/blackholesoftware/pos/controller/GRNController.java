package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.entity.GRN;
import com.blackholesoftware.pos.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grns")
public class GRNController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<GRN>> getAllGrns() {
        return ResponseEntity.ok(purchaseOrderService.getAllGrns());
    }
}