package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends BaseSyncRepository<Brand, String> {
}