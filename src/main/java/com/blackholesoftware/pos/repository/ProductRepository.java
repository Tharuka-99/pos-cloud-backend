package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends BaseSyncRepository<Product, String> {

    List<Product> findByIsDeletedFalse();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.unit " +
            "WHERE p.isDeleted = false")
    List<Product> findAllWithDetails();
}