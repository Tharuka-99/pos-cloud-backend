package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.dto.ProductRequestDTO;
import com.blackholesoftware.pos.dto.ProductResponseDTO;
import com.blackholesoftware.pos.entity.*;
import com.blackholesoftware.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ProductBarcodeRepository barcodeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;

    public ProductService(
            ProductRepository productRepository,
            BatchRepository batchRepository,
            ProductBarcodeRepository barcodeRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            UnitRepository unitRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.barcodeRepository = barcodeRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.unitRepository = unitRepository;
    }

    @Transactional
    public ProductResponseDTO createProductWithBatch(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getReorderLevel() != null) product.setReorderLevel(dto.getReorderLevel());

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId()).ifPresent(product::setCategory);
        }
        if (dto.getBrandId() != null) {
            brandRepository.findById(dto.getBrandId()).ifPresent(product::setBrand);
        }
        if (dto.getUnitId() != null) {
            unitRepository.findById(dto.getUnitId()).ifPresent(product::setUnit);
        }

        Product savedProduct = productRepository.save(product);

        if (dto.getBarcode() != null && !dto.getBarcode().isBlank()) {
            ProductBarcode barcode = new ProductBarcode();
            barcode.setBarcode(dto.getBarcode().trim());
            barcode.setProduct(savedProduct);
            barcodeRepository.save(barcode);
        }

        Batch batch = new Batch();
        if (dto.getInitialBatch() != null) {
            ProductRequestDTO.InitialBatchDTO batchDto = dto.getInitialBatch();

            batch.setBatchNo(generateBatchNo(batchDto.getBatchNo()));
            batch.setCostPrice(batchDto.getCostPrice() != null ? batchDto.getCostPrice() : 0.0);
            batch.setSellingPrice(batchDto.getSellingPrice() != null ? batchDto.getSellingPrice() : 0.0);
            batch.setDiscountAmount(batchDto.getDiscountAmount() != null ? batchDto.getDiscountAmount() : 0.0);

            // 🟢 Decimal Support: Double mapping for initial and current quantity
            Double initQty = batchDto.getInitialQuantity() != null ? batchDto.getInitialQuantity().doubleValue() : 0.0;
            batch.setInitialQuantity(initQty);
            batch.setCurrentQuantity(initQty);

            batch.setManufactureDate(batchDto.getManufactureDate());
            batch.setExpiryDate(batchDto.getExpiryDate());
            batch.setProduct(savedProduct);

            batchRepository.save(batch);
        }

        List<Batch> allBatches = batchRepository.findByProduct(savedProduct);
        return mapToDTO(savedProduct, batch, allBatches);
    }

    // Single Query Optimized Batch Fetching (Loop Queries Removed)
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return Collections.emptyList();

        List<String> productIds = products.stream().map(Product::getId).toList();

        // Single Query eken all batches fetch karanna
        List<Batch> allBatches = batchRepository.findByProductIdIn(productIds);

        // Map batches by product ID for in-memory mapping
        Map<String, List<Batch>> productBatchMap = allBatches.stream()
                .filter(b -> b.getProduct() != null)
                .collect(Collectors.groupingBy(b -> b.getProduct().getId()));

        List<ProductResponseDTO> responseList = new ArrayList<>();

        for (Product product : products) {
            List<Batch> batches = productBatchMap.getOrDefault(product.getId(), Collections.emptyList());
            Batch latestBatch = batches.isEmpty() ? null : batches.get(batches.size() - 1);
            responseList.add(mapToDTO(product, latestBatch, batches));
        }

        return responseList;
    }

    public List<Batch> getBatchesByProductId(String productId) {
        return batchRepository.findByProductId(productId);
    }

    @Transactional
    public void updateProduct(String id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getReorderLevel() != null) product.setReorderLevel(dto.getReorderLevel());

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId()).ifPresent(product::setCategory);
        } else {
            product.setCategory(null);
        }

        if (dto.getBrandId() != null) {
            brandRepository.findById(dto.getBrandId()).ifPresent(product::setBrand);
        } else {
            product.setBrand(null);
        }

        if (dto.getUnitId() != null) {
            unitRepository.findById(dto.getUnitId()).ifPresent(product::setUnit);
        } else {
            product.setUnit(null);
        }

        productRepository.save(product);
    }

    @Transactional
    public Batch addBatchToExistingProduct(String productId, ProductRequestDTO.InitialBatchDTO batchDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Batch batch = new Batch();
        batch.setBatchNo(generateBatchNo(batchDto.getBatchNo()));
        batch.setCostPrice(batchDto.getCostPrice() != null ? batchDto.getCostPrice() : 0.0);
        batch.setSellingPrice(batchDto.getSellingPrice() != null ? batchDto.getSellingPrice() : 0.0);
        batch.setDiscountAmount(batchDto.getDiscountAmount() != null ? batchDto.getDiscountAmount() : 0.0);

        // 🟢 Decimal Support: Double mapping
        Double initQty = batchDto.getInitialQuantity() != null ? batchDto.getInitialQuantity().doubleValue() : 0.0;
        batch.setInitialQuantity(initQty);
        batch.setCurrentQuantity(initQty);

        batch.setManufactureDate(batchDto.getManufactureDate());
        batch.setExpiryDate(batchDto.getExpiryDate());
        batch.setProduct(product);

        return batchRepository.save(batch);
    }

    @Transactional
    public Batch updateBatchDetails(String batchId, Batch batchUpdate) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + batchId));

        if (batchUpdate.getCostPrice() != null) batch.setCostPrice(batchUpdate.getCostPrice());
        if (batchUpdate.getSellingPrice() != null) batch.setSellingPrice(batchUpdate.getSellingPrice());
        if (batchUpdate.getDiscountAmount() != null) batch.setDiscountAmount(batchUpdate.getDiscountAmount());
        if (batchUpdate.getCurrentQuantity() != null) batch.setCurrentQuantity(batchUpdate.getCurrentQuantity());
        if (batchUpdate.getExpiryDate() != null) batch.setExpiryDate(batchUpdate.getExpiryDate());
        if (batchUpdate.getManufactureDate() != null) batch.setManufactureDate(batchUpdate.getManufactureDate());

        return batchRepository.save(batch);
    }

    private ProductResponseDTO mapToDTO(Product product, Batch batch, List<Batch> batches) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) dto.setCategoryName(product.getCategory().getName());
        if (product.getBrand() != null) dto.setBrandName(product.getBrand().getName());
        if (product.getUnit() != null) {
            dto.setUnitName(product.getUnit().getName());
            dto.setUnitShortCode(product.getUnit().getShortCode());
        }

        if (product.getBarcodes() != null && !product.getBarcodes().isEmpty()) {
            dto.setPrimaryBarcode(product.getBarcodes().get(0).getBarcode());
        }

        if (batches != null && !batches.isEmpty()) {
            List<ProductResponseDTO.BatchDTO> batchDTOs = batches.stream().map(b -> {
                ProductResponseDTO.BatchDTO bDto = new ProductResponseDTO.BatchDTO();
                bDto.setId(b.getId());
                bDto.setBatchNo(b.getBatchNo());
                bDto.setCostPrice(b.getCostPrice());
                bDto.setSellingPrice(b.getSellingPrice());
                bDto.setDiscountAmount(b.getDiscountAmount());
                bDto.setCurrentQuantity(b.getCurrentQuantity()); // 🟢 Preserves Double precision
                bDto.setExpiryDate(b.getExpiryDate());
                return bDto;
            }).toList();

            dto.setBatches(batchDTOs);
        }

        if (batch != null) {
            dto.setLatestBatchNo(batch.getBatchNo());
            dto.setCostPrice(batch.getCostPrice());
            dto.setSellingPrice(batch.getSellingPrice());
            dto.setDiscountAmount(batch.getDiscountAmount());
            dto.setCurrentStock(batch.getCurrentQuantity()); // 🟢 Preserves Double precision
            dto.setExpiryDate(batch.getExpiryDate());
        }

        return dto;
    }

    private String generateBatchNo(String rawBatchNo) {
        if (rawBatchNo == null || rawBatchNo.trim().isEmpty()) {
            return "B-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }
        return rawBatchNo.trim();
    }
}