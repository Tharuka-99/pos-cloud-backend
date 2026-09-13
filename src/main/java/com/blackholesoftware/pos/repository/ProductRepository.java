package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends BaseSyncRepository<Product, String> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.unit")
    List<Product> findAllWithDetails();
}