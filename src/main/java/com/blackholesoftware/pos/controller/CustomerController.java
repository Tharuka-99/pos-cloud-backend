package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.CreditPaymentRequestDTO;
import com.blackholesoftware.pos.dto.CreditSaleRequestDTO;
import com.blackholesoftware.pos.dto.CustomerPaymentDto;
import com.blackholesoftware.pos.entity.Customer;
import com.blackholesoftware.pos.entity.CustomerCreditTransaction;
import com.blackholesoftware.pos.repository.CustomerRepository;
import com.blackholesoftware.pos.service.CustomerCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerCreditService customerCreditService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers loaded", customerRepository.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> addCustomer(@RequestBody Customer customer) {
        if (customerRepository.findByPhone(customer.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Phone number already exists!", null));
        }

        if (customer.getCurrentCredit() == null) customer.setCurrentCredit(0.0f);
        if (customer.getCreditLimit() == null) customer.setCreditLimit(0.0);

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer registered successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable String id, @RequestBody Customer customerDetails) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "Customer not found", null));
        }

        Customer existingCustomer = optionalCustomer.get();

        if (customerDetails.getName() != null) existingCustomer.setName(customerDetails.getName());
        if (customerDetails.getPhone() != null) existingCustomer.setPhone(customerDetails.getPhone());
        if (customerDetails.getCreditLimit() != null) existingCustomer.setCreditLimit(customerDetails.getCreditLimit());
        if (customerDetails.getCurrentCredit() != null) existingCustomer.setCurrentCredit(customerDetails.getCurrentCredit());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully", updatedCustomer));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> patchCustomer(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "Customer not found", null));
        }

        Customer existingCustomer = optionalCustomer.get();

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    existingCustomer.setName((String) value);
                    break;
                case "phone":
                    existingCustomer.setPhone((String) value);
                    break;
                case "creditLimit":
                    existingCustomer.setCreditLimit(((Number) value).doubleValue());
                    break;
                case "currentCredit":
                    existingCustomer.setCurrentCredit(((Number) value).floatValue());
                    break;
            }
        });

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer patched successfully", updatedCustomer));
    }

    @PostMapping("/{id}/add-credit")
    public ResponseEntity<ApiResponse<CustomerCreditTransaction>> addCredit(@PathVariable String id, @RequestBody CustomerPaymentDto payment) {
        CreditSaleRequestDTO dto = new CreditSaleRequestDTO();
        dto.setCustomerId(id);
        dto.setCreditAmount(payment != null && payment.getAmount() != null ? payment.getAmount().floatValue() : 0.0f);
        dto.setNote(payment != null ? payment.getNotes() : "Manual Credit Addition");

        CustomerCreditTransaction transaction = customerCreditService.recordCreditSale(dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Credit added successfully", transaction));
    }

    @PostMapping("/{id}/pay-credit")
    public ResponseEntity<ApiResponse<CustomerCreditTransaction>> payCredit(@PathVariable String id, @RequestBody CustomerPaymentDto payment) {
        CreditPaymentRequestDTO dto = new CreditPaymentRequestDTO();
        dto.setCustomerId(id);
        dto.setAmountPaid(payment != null && payment.getAmount() != null ? payment.getAmount().floatValue() : 0.0f);
        dto.setNote(payment != null ? payment.getNotes() : "Manual Credit Repayment");

        CustomerCreditTransaction transaction = customerCreditService.recordRepayment(dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment recorded successfully", transaction));
    }
}