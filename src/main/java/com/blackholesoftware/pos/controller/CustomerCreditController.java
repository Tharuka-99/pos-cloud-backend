package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.CreditPaymentRequestDTO;
import com.blackholesoftware.pos.dto.CreditSaleRequestDTO;
import com.blackholesoftware.pos.dto.CreditSummaryDTO;
import com.blackholesoftware.pos.entity.CustomerCreditTransaction;
import com.blackholesoftware.pos.repository.CustomerCreditTransactionRepository;
import com.blackholesoftware.pos.service.CustomerCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer-credit-transactions")
public class CustomerCreditController {

    @Autowired
    private CustomerCreditService creditService;

    @Autowired
    private CustomerCreditTransactionRepository creditTransactionRepository;

    // GET /api/customer-credit-transactions - All Transactions List for Dashboard
    @GetMapping
    public ResponseEntity<List<CustomerCreditTransaction>> getAllCreditTransactions() {
        return ResponseEntity.ok(creditTransactionRepository.findAll());
    }

    @PostMapping("/sale")
    public ResponseEntity<CustomerCreditTransaction> recordCreditSale(@RequestBody CreditSaleRequestDTO dto) {
        return ResponseEntity.ok(creditService.recordCreditSale(dto));
    }

    @PostMapping("/pay")
    public ResponseEntity<CustomerCreditTransaction> recordRepayment(@RequestBody CreditPaymentRequestDTO dto) {
        return ResponseEntity.ok(creditService.recordRepayment(dto));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerCreditTransaction>> getCustomerHistory(@PathVariable String customerId) {
        return ResponseEntity.ok(creditService.getCustomerHistory(customerId));
    }

    @GetMapping("/summary")
    public ResponseEntity<CreditSummaryDTO> getCreditSummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(creditService.getCreditSummary(startDate, endDate));
    }
}