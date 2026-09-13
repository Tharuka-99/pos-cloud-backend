package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends BaseSyncRepository<Expense, String> {
    List<Expense> findAllByOrderByExpenseDateDesc();
}