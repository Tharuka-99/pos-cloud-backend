package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.ExpenseRequestDto;
import com.blackholesoftware.pos.entity.CashSession;
import com.blackholesoftware.pos.entity.Expense;
import com.blackholesoftware.pos.entity.User;
import com.blackholesoftware.pos.repository.CashSessionRepository;
import com.blackholesoftware.pos.repository.ExpenseRepository;
import com.blackholesoftware.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private CashSessionRepository cashSessionRepository;
    @Autowired private UserRepository userRepository;

    // 1. Create Expense & Update Active Cash Session
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Expense>> createExpense(@RequestBody ExpenseRequestDto request) {
        try {
            if (request.getAmount() == null || request.getAmount() <= 0) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "වලංගු මුදලක් (Amount) ඇතුළත් කරන්න!", null));
            }

            if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Expense Category එකක් තෝරන්න!", null));
            }

            // User Verification
            User addedBy = null;
            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                addedBy = userRepository.findById(request.getUserId()).orElse(null);
            }
            if (addedBy == null) {
                addedBy = userRepository.findAll().stream().findFirst().orElse(null);
            }

            Expense expense = new Expense();
            expense.setCategoryName(request.getCategoryName());
            expense.setAmount(request.getAmount());
            expense.setDescription(request.getDescription());

            String payMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH";
            expense.setPaymentMethod(payMethod);
            expense.setAddedBy(addedBy);

            Expense savedExpense = expenseRepository.save(expense);

            // 💡 Expense payment method එක CASH නම් පමණක් active Cash Session එකේ totalExpenses වැඩි කරන්න
            if ("CASH".equalsIgnoreCase(payMethod)) {
                Optional<CashSession> activeSessionOpt = cashSessionRepository.findAll()
                        .stream()
                        .filter(s -> "OPEN".equalsIgnoreCase(s.getStatus()) || s.getClosedAt() == null)
                        .findFirst();

                if (activeSessionOpt.isPresent()) {
                    CashSession session = activeSessionOpt.get();
                    double currentExpenses = session.getTotalExpenses() != null ? session.getTotalExpenses() : 0.0;
                    session.setTotalExpenses(currentExpenses + request.getAmount());
                    cashSessionRepository.save(session);
                }
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Expense සටහන් කිරීම සාර්ථකයි!", savedExpense));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Expense Error: " + e.getMessage(), null));
        }
    }

    // 2. Get All Expenses
    @GetMapping
    public ResponseEntity<ApiResponse<List<Expense>>> getAllExpenses() {
        List<Expense> list = expenseRepository.findAllByOrderByExpenseDateDesc();
        return ResponseEntity.ok(new ApiResponse<>(true, "Expenses loaded successfully", list));
    }

    // 3. Delete Expense
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable String id) {
        Optional<Expense> expOpt = expenseRepository.findById(id);
        if (expOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Expense එක හමුවූයේ නැත!", null));
        }

        Expense exp = expOpt.get();

        // Cash Expense එකක් Delete කළහොත් Active Session එකෙන් නැවතත් එම ගණන අඩුවිය යුතුයි
        if ("CASH".equalsIgnoreCase(exp.getPaymentMethod())) {
            Optional<CashSession> activeSessionOpt = cashSessionRepository.findAll()
                    .stream()
                    .filter(s -> "OPEN".equalsIgnoreCase(s.getStatus()) || s.getClosedAt() == null)
                    .findFirst();

            if (activeSessionOpt.isPresent()) {
                CashSession session = activeSessionOpt.get();
                double currentExpenses = session.getTotalExpenses() != null ? session.getTotalExpenses() : 0.0;
                session.setTotalExpenses(Math.max(0.0, currentExpenses - exp.getAmount()));
                cashSessionRepository.save(session);
            }
        }

        expenseRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Expense එක සාර්ථකව ඉවත් කරන ලදී!", null));
    }
}