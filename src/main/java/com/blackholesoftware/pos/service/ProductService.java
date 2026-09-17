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

        Batch batch = new Batch();
        if (dto.getInitialBatch() != null) {
            ProductRequestDTO.InitialBatchDTO batchDto = dto.getInitialBatch();

            batch.setBatchNo(generateBatchNo(batchDto.getBatchNo()));

            // Batch එකට වෙනම barcode එකක් නැත්නම් Product එකේ main barcode එක ගන්නවා
            String finalBatchBarcode = (batchDto.getBarcode() != null && !batchDto.getBarcode().isBlank())
                    ? batchDto.getBarcode().trim()
                    : (dto.getBarcode() != null ? dto.getBarcode().trim() : null);

            batch.setBarcode(finalBatchBarcode);

            batch.setCostPrice(batchDto.getCostPrice() != null ? batchDto.getCostPrice() : 0.0);
            batch.setSellingPrice(batchDto.getSellingPrice() != null ? batchDto.getSellingPrice() : 0.0);
            batch.setDiscountAmount(batchDto.getDiscountAmount() != null ? batchDto.getDiscountAmount() : 0.0);

            Double initQty = batchDto.getInitialQuantity() != null ? batchDto.getInitialQuantity().doubleValue() : 0.0;
            batch.setInitialQuantity(initQty);
            batch.setCurrentQuantity(initQty);

            batch.setManufactureDate(batchDto.getManufactureDate());
            batch.setExpiryDate(batchDto.getExpiryDate());
            batch.setProduct(savedProduct);

            Batch savedBatch = batchRepository.save(batch);

            // 🟢 Barcode එක Product and Batch දෙකටම associate කරලා එක සැරයක් Save කිරීම
            if (finalBatchBarcode != null && !finalBatchBarcode.isBlank()) {
                ProductBarcode batchBarcodeEntity = new ProductBarcode();
                batchBarcodeEntity.setBarcode(finalBatchBarcode);
                batchBarcodeEntity.setProduct(savedProduct);
                batchBarcodeEntity.setBatch(savedBatch);
                barcodeRepository.save(batchBarcodeEntity);
            }
        } else if (dto.getBarcode() != null && !dto.getBarcode().isBlank()) {
            // Initial batch එකක් නැත්නම් විතරක් Primary Product Barcode එක ලෙස Save කිරීම
            ProductBarcode mainBarcode = new ProductBarcode();
            mainBarcode.setBarcode(dto.getBarcode().trim());
            mainBarcode.setProduct(savedProduct);
            barcodeRepository.save(mainBarcode);
        }

        List<Batch> allBatches = batchRepository.findByProduct(savedProduct);
        return mapToDTO(savedProduct, batch, allBatches);
    }

    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return Collections.emptyList();

        List<String> productIds = products.stream().map(Product::getId).toList();

        List<Batch> allBatches = batchRepository.findByProductIdIn(productIds);

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

        if (dto.getBarcode() != null && !dto.getBarcode().isBlank()) {
            if (product.getBarcodes() != null && !product.getBarcodes().isEmpty()) {
                ProductBarcode pb = product.getBarcodes().get(0);
                pb.setBarcode(dto.getBarcode().trim());
                barcodeRepository.save(pb);
            } else {
                ProductBarcode pb = new ProductBarcode();
                pb.setBarcode(dto.getBarcode().trim());
                pb.setProduct(product);
                barcodeRepository.save(pb);
            }
        }

        productRepository.save(product);
    }

    @Transactional
    public Batch addBatchToExistingProduct(String productId, ProductRequestDTO.InitialBatchDTO batchDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Batch batch = new Batch();
        batch.setBatchNo(generateBatchNo(batchDto.getBatchNo()));

        if (batchDto.getBarcode() != null && !batchDto.getBarcode().isBlank()) {
            batch.setBarcode(batchDto.getBarcode().trim());
        }

        batch.setCostPrice(batchDto.getCostPrice() != null ? batchDto.getCostPrice() : 0.0);
        batch.setSellingPrice(batchDto.getSellingPrice() != null ? batchDto.getSellingPrice() : 0.0);
        batch.setDiscountAmount(batchDto.getDiscountAmount() != null ? batchDto.getDiscountAmount() : 0.0);

        Double initQty = batchDto.getInitialQuantity() != null ? batchDto.getInitialQuantity().doubleValue() : 0.0;
        batch.setInitialQuantity(initQty);
        batch.setCurrentQuantity(initQty);

        batch.setManufactureDate(batchDto.getManufactureDate());
        batch.setExpiryDate(batchDto.getExpiryDate());
        batch.setProduct(product);

        Batch savedBatch = batchRepository.save(batch);

        // 🟢 අලුත් Batch එක එකතු වෙද්දී ProductBarcode Table එකටත් Entry එකක් Save කිරීම
        if (batchDto.getBarcode() != null && !batchDto.getBarcode().isBlank()) {
            ProductBarcode batchBarcode = new ProductBarcode();
            batchBarcode.setBarcode(batchDto.getBarcode().trim());
            batchBarcode.setProduct(product);
            batchBarcode.setBatch(savedBatch);
            barcodeRepository.save(batchBarcode);
        }

        return savedBatch;
    }

    @Transactional
    public Batch updateBatchDetails(String batchId, Batch batchUpdate) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + batchId));

        if (batchUpdate.getBarcode() != null && !batchUpdate.getBarcode().isBlank()) {
            String newBarcode = batchUpdate.getBarcode().trim();
            batch.setBarcode(newBarcode);

            // 🟢 ProductBarcode Table එකෙත් අදාළ Batch එකට තිබුණු Record එක Update කිරීම හෝ අලුතින් Save කිරීම
            ProductBarcode existingBarcode = barcodeRepository.findByBatchId(batchId).orElse(null);
            if (existingBarcode != null) {
                existingBarcode.setBarcode(newBarcode);
                barcodeRepository.save(existingBarcode);
            } else {
                ProductBarcode newBatchBarcode = new ProductBarcode();
                newBatchBarcode.setBarcode(newBarcode);
                newBatchBarcode.setProduct(batch.getProduct());
                newBatchBarcode.setBatch(batch);
                barcodeRepository.save(newBatchBarcode);
            }
        }

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
                bDto.setBarcode(b.getBarcode());
                bDto.setCostPrice(b.getCostPrice());
                bDto.setSellingPrice(b.getSellingPrice());
                bDto.setDiscountAmount(b.getDiscountAmount());
                bDto.setCurrentQuantity(b.getCurrentQuantity());
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
            dto.setCurrentStock(batch.getCurrentQuantity());
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