package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MasterSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MasterSyncScheduler.class);

    @Autowired private GenericSyncService syncService;

    // Repositories Injection
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private UnitRepository unitRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SystemSettingRepository systemSettingRepository; // 🟢 SystemSettingRepository

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductBarcodeRepository productBarcodeRepository;
    @Autowired private BatchRepository batchRepository;

    @Autowired private CashSessionRepository cashSessionRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private CustomerCreditTransactionRepository creditTransactionRepository;

    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Autowired private GRNRepository grnRepository;
    @Autowired private GRNItemRepository grnItemRepository;

    @Autowired private SaleRepository saleRepository;
    @Autowired private SaleItemRepository saleItemRepository;
    @Autowired private SalesReturnRepository salesReturnRepository;
    @Autowired private SalesReturnItemRepository salesReturnItemRepository;

    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    public void syncAllTables() {
        logger.info("=================== [MASTER SYNC EXECUTION STARTED] ===================");

        try {
            // 1. Core Lookups & System Settings
            syncService.syncTableToCloud(userRepository, "app_users");
            syncService.syncTableToCloud(categoryRepository, "categories");
            syncService.syncTableToCloud(brandRepository, "brands");
            syncService.syncTableToCloud(unitRepository, "units");
            syncService.syncTableToCloud(supplierRepository, "suppliers");
            syncService.syncTableToCloud(customerRepository, "customers");
            syncService.syncTableToCloud(systemSettingRepository, "system-settings");

            // 2. Inventory Items & Batches
            syncService.syncTableToCloud(productRepository, "products");
            syncService.syncTableToCloud(productBarcodeRepository, "product-barcodes");
            syncService.syncTableToCloud(batchRepository, "batches");

            // 3. Daily Operations & Cash Management
            syncService.syncTableToCloud(cashSessionRepository, "cash-sessions");
            syncService.syncTableToCloud(expenseRepository, "expenses");
            syncService.syncTableToCloud(creditTransactionRepository, "customer-credit-transactions");

            // 4. Procurement (PO & GRN)
            syncService.syncTableToCloud(purchaseOrderRepository, "purchase-orders");
            syncService.syncTableToCloud(purchaseOrderItemRepository, "purchase-order-items");
            syncService.syncTableToCloud(grnRepository, "grns");
            syncService.syncTableToCloud(grnItemRepository, "grn-items");

            // 5. Sales & Line Items
            syncService.syncTableToCloud(saleRepository, "sales");
            syncService.syncTableToCloud(saleItemRepository, "sale-items");

            // 6. Returns & Return Line Items
            syncService.syncTableToCloud(salesReturnRepository, "sales-returns");
            syncService.syncTableToCloud(salesReturnItemRepository, "sales-return-items");

            logger.info("=================== [MASTER SYNC EXECUTION COMPLETED] ===================");
        } catch (Exception e) {
            logger.error("Critical error during Master Sync execution: {}", e.getMessage(), e);
        }
    }
}