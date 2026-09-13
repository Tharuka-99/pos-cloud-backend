package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends BaseSyncRepository<Category, String> {
}