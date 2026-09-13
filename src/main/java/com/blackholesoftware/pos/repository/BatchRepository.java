package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.Batch;
import com.blackholesoftware.pos.entity.Product;
import com.blackholesoftware.pos.repository.BaseSyncRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends BaseSyncRepository<Batch, String> {
    List<Batch> findByProduct(Product product);
    List<Batch> findByProductId(String productId);

    // Products සියල්ලටම අදාළ Batches එක Single Query එකෙන් ගන්න
    @Query("SELECT b FROM Batch b WHERE b.product.id IN :productIds")
    List<Batch> findByProductIdIn(@Param("productIds") List<String> productIds);

    Optional<Batch> findByProductAndSellingPriceAndCostPriceAndDiscountAmount(
            Product product,
            Double sellingPrice,
            Double costPrice,
            Double discountAmount
    );

    Optional<Batch> findByProductAndSellingPriceAndCostPriceAndDiscountAmountAndBarcode(
            Product product,
            Double sellingPrice,
            Double costPrice,
            Double discountAmount,
            String barcode
    );
}