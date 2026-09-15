package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.dto.CreditPaymentRequestDTO;
import com.blackholesoftware.pos.dto.CreditSaleRequestDTO;
import com.blackholesoftware.pos.dto.CreditSummaryDTO;
import com.blackholesoftware.pos.entity.Customer;
import com.blackholesoftware.pos.entity.CustomerCreditTransaction;
import com.blackholesoftware.pos.repository.CustomerCreditTransactionRepository;
import com.blackholesoftware.pos.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerCreditService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerCreditTransactionRepository creditTransactionRepository;

    @Transactional
    public CustomerCreditTransaction recordCreditSale(CreditSaleRequestDTO dto) {
        if (dto.getCustomerId() == null || dto.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty for credit sale!");
        }

        // DUPES PREVENT GUARD: Check if transaction for this Sale ID already exists
        if (dto.getSaleId() != null && !dto.getSaleId().trim().isEmpty()) {
            Optional<CustomerCreditTransaction> existingTx = creditTransactionRepository.findBySaleId(dto.getSaleId());
            if (existingTx.isPresent()) {
                return existingTx.get();
            }
        }

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + dto.getCustomerId()));

        float currentCredit = customer.getCurrentCredit() != null ? customer.getCurrentCredit() : 0.0f;

        // Controller එකෙන් Direct එවන්නේ එකතු විය යුතු ණය මුදලයි (Net Debt)
        float addedCredit = dto.getCreditAmount() != null ? dto.getCreditAmount() : 0.0f;

        // Total Amount එකක් DTO එකේ ආවොත් සහ Credit Amount එක 0 වුණොත් විතරක් Calculate කරන්න
        if (addedCredit == 0.0f && dto.getTotalAmount() != null && dto.getTotalAmount() > 0) {
            addedCredit = dto.getTotalAmount();
        }

        float newBalance = currentCredit + addedCredit;

        customer.setCurrentCredit(newBalance);
        customerRepository.save(customer);

        CustomerCreditTransaction transaction = CustomerCreditTransaction.builder()
                .customerId(customer.getId())
                .saleId(dto.getSaleId())
                .transactionType(CustomerCreditTransaction.TransactionType.CREDIT_GIVEN)
                .amount(addedCredit)
                .balanceAfter(newBalance)
                .paymentMethod("CREDIT")
                .note(dto.getNote() != null ? dto.getNote() : "Credit Sale")
                .createdAt(LocalDateTime.now())
                .build();

        return creditTransactionRepository.saveAndFlush(transaction);
    }

    // 2. Record Repayment (ණය ආපසු ගෙවීම - Cash Drawer එකට එකතු වන තැන)
    @Transactional
    public CustomerCreditTransaction recordRepayment(CreditPaymentRequestDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + dto.getCustomerId()));

        float currentCredit = customer.getCurrentCredit() != null ? customer.getCurrentCredit() : 0.0f;
        float paidAmount = dto.getAmountPaid() != null ? dto.getAmountPaid() : 0.0f;
        float newBalance = Math.max(currentCredit - paidAmount, 0.0f);

        customer.setCurrentCredit(newBalance);
        customerRepository.save(customer);

        CustomerCreditTransaction transaction = CustomerCreditTransaction.builder()
                .customerId(customer.getId())
                .transactionType(CustomerCreditTransaction.TransactionType.PAYMENT_RECEIVED)
                .amount(paidAmount)
                .balanceAfter(newBalance)
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod().toUpperCase() : "CASH")
                .note(dto.getNote() != null ? dto.getNote() : "Credit Repayment")
                .createdAt(LocalDateTime.now())
                .build();

        return creditTransactionRepository.save(transaction);
    }

    public List<CustomerCreditTransaction> getCustomerHistory(String customerId) {
        return creditTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public CreditSummaryDTO getCreditSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Double totalGiven = creditTransactionRepository.getTotalCreditGivenBetween(startDate, endDate);
        Double totalCollected = creditTransactionRepository.getTotalCreditCollectedBetween(startDate, endDate);

        List<Customer> customers = customerRepository.findAll();
        double totalOutstanding = customers.stream()
                .mapToDouble(c -> c.getCurrentCredit() != null ? c.getCurrentCredit() : 0.0)
                .sum();

        return CreditSummaryDTO.builder()
                .totalCreditGivenInPeriod(totalGiven != null ? totalGiven.floatValue() : 0.0f)
                .totalCreditCollectedInPeriod(totalCollected != null ? totalCollected.floatValue() : 0.0f)
                .totalOutstandingCreditAllTime((float) totalOutstanding)
                .build();
    }

    @Transactional
    public CustomerCreditTransaction recordCreditPayment(String customerId, Float paidAmount, String paymentMethod) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID eka avashyai!");
        }

        if (paidAmount == null || paidAmount <= 0) {
            throw new IllegalArgumentException("Gevanulada mudala 0 ta vada vadi viya yuthui!");
        }

        // Customer wa hoyageneema
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer hoyaganeemata nohaki viya: " + customerId));

        float currentCredit = customer.getCurrentCredit() != null ? customer.getCurrentCredit() : 0.0f;

        // Naya adukireema (Balance after payment)
        float newBalance = Math.max(0.0f, currentCredit - paidAmount);

        // Customer balance update
        customer.setCurrentCredit(newBalance);
        customerRepository.save(customer);

        // Credit Transaction Record eka sadema
        CustomerCreditTransaction transaction = CustomerCreditTransaction.builder()
                .customerId(customer.getId())
                .transactionType(CustomerCreditTransaction.TransactionType.PAID) // transaction_type = PAID
                .amount(paidAmount)
                .balanceAfter(newBalance)
                .paymentMethod(paymentMethod != null ? paymentMethod.toUpperCase() : "CASH")
                .note("Credit Settlement Payment")
                .createdAt(LocalDateTime.now())
                .build();

        return creditTransactionRepository.saveAndFlush(transaction);
    }
}