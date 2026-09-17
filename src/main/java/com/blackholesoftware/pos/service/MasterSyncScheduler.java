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
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private UnitRepository unitRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BatchRepository batchRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private CashSessionRepository cashSessionRepository;
    @Autowired private CustomerCreditTransactionRepository creditTransactionRepository;
    @Autowired private SaleRepository saleRepository;
    @Autowired private SalesReturnRepository salesReturnRepository;

    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    public void syncAllTables() {
        logger.info("=================== [MASTER SYNC EXECUTION STARTED] ===================");

        try {
            // 1. Core Lookups & Master Data First
            syncService.syncTableToCloud(categoryRepository, "categories");
            syncService.syncTableToCloud(brandRepository, "brands");
            syncService.syncTableToCloud(unitRepository, "units");
            syncService.syncTableToCloud(supplierRepository, "suppliers");
            syncService.syncTableToCloud(customerRepository, "customers");
            syncService.syncTableToCloud(userRepository, "app_users");

            // 2. Inventory Items
            syncService.syncTableToCloud(productRepository, "products");
            syncService.syncTableToCloud(batchRepository, "batches");

            // 3. Transactions & Daily Operational Data
            syncService.syncTableToCloud(cashSessionRepository, "cash-sessions");
            syncService.syncTableToCloud(expenseRepository, "expenses");
            syncService.syncTableToCloud(creditTransactionRepository, "credit-transactions");
            syncService.syncTableToCloud(saleRepository, "sales");
            syncService.syncTableToCloud(salesReturnRepository, "sales-returns");

            logger.info("=================== [MASTER SYNC EXECUTION COMPLETED] ===================");
        } catch (Exception e) {
            logger.error("Critical error during Master Sync execution: {}", e.getMessage(), e);
        }
    }
}