package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends BaseSyncRepository<Customer, String> {
    Optional<Customer> findByPhone(String phone);
}