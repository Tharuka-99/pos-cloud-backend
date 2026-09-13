package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductBarcodeRepository extends BaseSyncRepository<ProductBarcode, String> {

    @Query("SELECT pb FROM ProductBarcode pb WHERE pb.product.id IN :productIds")
    List<ProductBarcode> findByProductIdIn(@Param("productIds") List<String> productIds);
}