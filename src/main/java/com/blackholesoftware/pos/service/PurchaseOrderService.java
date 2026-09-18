package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.dto.GrnReceiveRequestDto;
import com.blackholesoftware.pos.repository.GRNRepository;
import com.blackholesoftware.pos.dto.PurchaseOrderRequestDto;
import com.blackholesoftware.pos.entity.*;
import com.blackholesoftware.pos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private GRNRepository grnRepository;

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc();
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequestDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + dto.getSupplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + System.currentTimeMillis());
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrder.Status.PENDING);
        po.setOrderDate(LocalDateTime.now());
        po.setIsSynced(false);
        po.setUpdatedAt(LocalDateTime.now());

        double totalPoAmount = 0.0;
        List<PurchaseOrderItem> itemList = new ArrayList<>();

        for (PurchaseOrderRequestDto.PoItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found ID: " + itemDto.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);

            Double qty = itemDto.getQuantity() != null ? itemDto.getQuantity().doubleValue() : 0.0;
            item.setQuantity(qty);

            double cost = itemDto.getEstimatedUnitCost() != null ? itemDto.getEstimatedUnitCost() : 0.0;
            item.setEstimatedUnitCost(cost);

            double total = cost * qty;
            item.setTotalPrice(total);
            item.setIsSynced(false);
            item.setUpdatedAt(LocalDateTime.now());

            totalPoAmount += total;
            itemList.add(item);
        }

        po.setItems(itemList);
        po.setTotalAmount(totalPoAmount);

        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public GRN processGrnAndStockUpdate(String poId, GrnReceiveRequestDto dto) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO Not Found with ID: " + poId));

        GRN grn = new GRN();
        grn.setGrnNumber("GRN-" + System.currentTimeMillis());
        grn.setSupplier(po.getSupplier());
        grn.setPurchaseOrder(po);
        grn.setReceivedDate(LocalDateTime.now());
        grn.setPaymentStatus(GRN.PaymentStatus.PAID);
        grn.setIsSynced(false);
        grn.setUpdatedAt(LocalDateTime.now());

        List<GRNItem> grnItems = new ArrayList<>();
        double grandTotal = 0.0;

        for (GrnReceiveRequestDto.GrnItemDto grnItemDto : dto.getReceivedItems()) {
            Product product = productRepository.findById(grnItemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found: " + grnItemDto.getProductId()));

            Double sellingPrice = grnItemDto.getSellingPrice() != null ? grnItemDto.getSellingPrice() : 0.0;
            Double costPrice = grnItemDto.getCostPrice() != null ? grnItemDto.getCostPrice() : 0.0;
            Double discountAmount = grnItemDto.getDiscountAmount() != null ? grnItemDto.getDiscountAmount() : 0.0;
            Double recQty = grnItemDto.getReceivedQty() != null ? grnItemDto.getReceivedQty().doubleValue() : 0.0;
            String itemBarcode = grnItemDto.getBarcode() != null ? grnItemDto.getBarcode().trim() : "";

            // 1. Price/Barcode matching batch lookup
            Optional<Batch> existingBatchOpt = batchRepository
                    .findByProductAndSellingPriceAndCostPriceAndDiscountAmountAndBarcode(
                            product, sellingPrice, costPrice, discountAmount, itemBarcode
                    );

            if (existingBatchOpt.isPresent()) {
                Batch existingBatch = existingBatchOpt.get();
                Double currentBatchQty = existingBatch.getCurrentQuantity() != null ? existingBatch.getCurrentQuantity() : 0.0;
                existingBatch.setCurrentQuantity(currentBatchQty + recQty);
                existingBatch.setIsSynced(false);
                existingBatch.setUpdatedAt(LocalDateTime.now());
                batchRepository.save(existingBatch);
            } else {
                Batch newBatch = new Batch();
                newBatch.setBatchNo("BN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
                newBatch.setProduct(product);
                newBatch.setCostPrice(costPrice);
                newBatch.setSellingPrice(sellingPrice);
                newBatch.setDiscountAmount(discountAmount);
                newBatch.setBarcode(itemBarcode);
                newBatch.setInitialQuantity(recQty);
                newBatch.setCurrentQuantity(recQty);
                newBatch.setCreatedAt(LocalDateTime.now());
                newBatch.setIsSynced(false);
                newBatch.setUpdatedAt(LocalDateTime.now());
                batchRepository.save(newBatch);
            }

            // 2. Product Total Stock Update
            Double currentProductStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0.0;
            product.setCurrentStock(currentProductStock + recQty);
            product.setIsSynced(false);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            // 3. GRN Item Record Creation
            GRNItem grnItem = new GRNItem();
            grnItem.setProduct(product);
            grnItem.setQuantityReceived(recQty);
            grnItem.setUnitBuyingPrice(costPrice);
            grnItem.setUnitSellingPrice(sellingPrice);
            double lineTotal = costPrice * recQty;
            grnItem.setTotalPrice(lineTotal);
            grnItem.setIsSynced(false);
            grnItem.setUpdatedAt(LocalDateTime.now());

            grnItems.add(grnItem);
            grandTotal += lineTotal;
        }

        grn.setItems(grnItems);
        grn.setTotalAmount(grandTotal);
        grn.setPaidAmount(grandTotal);
        grn.setDueAmount(0.0);

        GRN savedGrn = grnRepository.save(grn);

        po.setStatus(PurchaseOrder.Status.RECEIVED);
        po.setIsSynced(false);
        po.setUpdatedAt(LocalDateTime.now());
        purchaseOrderRepository.save(po);

        return savedGrn;
    }

    public List<GRN> getAllGrns() {
        return grnRepository.findAll();
    }
}