package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.CheckoutRequestDto;
import com.blackholesoftware.pos.dto.CreditSaleRequestDTO;
import com.blackholesoftware.pos.entity.*;
import com.blackholesoftware.pos.repository.*;
import com.blackholesoftware.pos.service.CustomerCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired private SaleRepository saleRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BatchRepository batchRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CashSessionRepository cashSessionRepository;
    @Autowired private SalesReturnRepository salesReturnRepository;
    @Autowired private CustomerCreditService customerCreditService;

    @GetMapping("/initial-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInitialSalesData() {
        try {
            List<Sale> sales = saleRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<Batch> batches = batchRepository.findAll();

            Map<String, Object> data = new HashMap<>();
            data.put("sales", sales);
            data.put("products", products);
            data.put("batches", batches);

            return ResponseEntity.ok(new ApiResponse<>(true, "Initial sales data loaded successfully", data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error loading initial sales data: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Sale>>> getAllSales() {
        try {
            List<Sale> sales = saleRepository.findAll();
            return ResponseEntity.ok(new ApiResponse<>(true, "Sales list loaded successfully", sales));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error loading sales list: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<Sale>> getSaleByIdOrInvoice(@PathVariable String identifier) {
        String cleanIdentifier = (identifier != null) ? identifier.trim() : "";

        if (cleanIdentifier.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid Search Key!", null));
        }

        Optional<Sale> saleOpt = saleRepository.findById(cleanIdentifier);
        if (saleOpt.isEmpty()) {
            saleOpt = saleRepository.findByInvoiceNumber(cleanIdentifier);
        }
        if (saleOpt.isEmpty()) {
            saleOpt = saleRepository.findByIdOrInvoiceNumberFlexible(cleanIdentifier);
        }
        if (saleOpt.isEmpty() && !cleanIdentifier.toUpperCase().startsWith("INV-")) {
            saleOpt = saleRepository.findByInvoiceNumber("INV-" + cleanIdentifier);
        }

        if (saleOpt.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Sale loaded successfully", saleOpt.get()));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "මෙම ID / Invoice අංකයට අදාළ Bill එක සොයාගත නොහැකි විය!", null));
        }
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<ApiResponse<Sale>> processCheckout(@RequestBody CheckoutRequestDto request) {
        try {
            if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Checkout cart empty!", null));
            }

            User cashier = null;
            String cashierIdStr = request.getCashierId() != null ? request.getCashierId().trim() : "";
            if (!cashierIdStr.isEmpty() && !"ADMIN_ID".equalsIgnoreCase(cashierIdStr)) {
                cashier = userRepository.findById(cashierIdStr).orElse(null);
            }

            if (cashier == null) {
                cashier = userRepository.findAll().stream().findFirst().orElseGet(() -> {
                    User sysUser = new User();
                    sysUser.setUsername("admin");
                    sysUser.setPassword("1234");
                    sysUser.setFullName("System Admin");
                    sysUser.setRole(User.Role.ADMIN);
                    return userRepository.save(sysUser);
                });
            }

            String customerIdStr = request.getCustomerId() != null ? request.getCustomerId().trim() : "";
            Customer customer = null;
            if (!customerIdStr.isEmpty()) {
                customer = customerRepository.findById(customerIdStr).orElse(null);
            }

            double grossSubTotal = 0.0;
            double totalItemDiscounts = 0.0;
            List<SaleItem> saleItems = new ArrayList<>();

            for (CheckoutRequestDto.CheckoutItemDto itemDto : request.getItems()) {
                if (itemDto.getProductId() == null || itemDto.getProductId().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid Product ID!", null));
                }

                Optional<Product> productOpt = productRepository.findById(itemDto.getProductId());
                if (productOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Product ID නොමැත: " + itemDto.getProductId(), null));
                }
                Product product = productOpt.get();

                Batch batch = null;
                String batchIdStr = itemDto.getBatchId() != null ? itemDto.getBatchId().trim() : "";

                if (!batchIdStr.isEmpty() && !batchIdStr.startsWith("prod-")) {
                    batch = batchRepository.findById(batchIdStr).orElse(null);
                }

                if (batch == null) {
                    List<Batch> batches = batchRepository.findByProduct(product);
                    batch = batches.stream()
                            .filter(b -> b.getCurrentQuantity() != null && b.getCurrentQuantity() > 0)
                            .findFirst()
                            .orElse(null);
                }

                int reqQty = itemDto.getQuantity() != null ? itemDto.getQuantity() : 0;

                // DEDUCT BOTH BATCH STOCK AND PRODUCT TOTAL STOCK
                if (batch != null) {
                    if (batch.getCurrentQuantity() < reqQty) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(
                                false, product.getName() + " සඳහා ප්‍රමාණවත් Batch Stock නොමැත! Available: " + batch.getCurrentQuantity(), null));
                    }
                    batch.setCurrentQuantity(batch.getCurrentQuantity() - reqQty);
                    batchRepository.save(batch);

                    double currentProdStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
                    product.setCurrentStock(Math.max(0, currentProdStock - reqQty));
                    productRepository.save(product);

                } else {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(
                            false, product.getName() + " සඳහා කිසිදු Active Batch එකක් නොමැත!", null));
                }

                double unitPrice = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : 0.0;
                double itemDiscount = itemDto.getDiscount() != null ? itemDto.getDiscount() : 0.0;

                grossSubTotal += (unitPrice * reqQty);
                totalItemDiscounts += (itemDiscount * reqQty);

                double itemNetUnitPrice = Math.max(0, unitPrice - itemDiscount);
                double itemTotalPrice = itemNetUnitPrice * reqQty;

                SaleItem saleItem = new SaleItem();
                saleItem.setProduct(product);
                saleItem.setBatch(batch);
                saleItem.setCustomer(customer);
                saleItem.setQuantity(reqQty);
                saleItem.setUnitPrice(unitPrice);
                saleItem.setUnitCost(batch.getCostPrice() != null ? batch.getCostPrice() : 0.0);
                saleItem.setDiscount(itemDiscount);
                saleItem.setTotalPrice(itemTotalPrice);

                saleItems.add(saleItem);
            }

            double billOverallDiscount = request.getOverallDiscount() != null ? request.getOverallDiscount() : 0.0;

            double returnCreditAmount = request.getReturnCreditDeduction() != null ? request.getReturnCreditDeduction() : 0.0;
            if (returnCreditAmount == 0.0 && request.getAppliedReturnId() != null && !request.getAppliedReturnId().trim().isEmpty()) {
                String returnKey = request.getAppliedReturnId().trim();
                Optional<SalesReturn> returnOpt = salesReturnRepository.findById(returnKey);
                if (returnOpt.isEmpty()) {
                    returnOpt = salesReturnRepository.findByReturnNumber(returnKey);
                }
                if (returnOpt.isPresent()) {
                    SalesReturn appliedReturn = returnOpt.get();
                    returnCreditAmount = appliedReturn.getTotalRefundAmount() != null ? appliedReturn.getTotalRefundAmount() : 0.0;
                    appliedReturn.setStatus(SalesReturn.ReturnStatus.SETTLED);
                    salesReturnRepository.save(appliedReturn);
                }
            }

            double grandTotalDiscount = totalItemDiscounts + billOverallDiscount + returnCreditAmount;
            double netTotal = Math.max(0, grossSubTotal - grandTotalDiscount);

            Sale sale = new Sale();
            sale.setInvoiceNumber("INV-" + System.currentTimeMillis());
            sale.setCashier(cashier);
            sale.setCustomer(customer);
            sale.setSubTotal(grossSubTotal);
            sale.setDiscountAmount(grandTotalDiscount);
            sale.setOverallDiscount(billOverallDiscount);
            sale.setNetTotal(netTotal);
            sale.setItems(saleItems);

            Sale.PaymentMethod paymentEnum = request.getPaymentMethod() != null
                    ? request.getPaymentMethod()
                    : Sale.PaymentMethod.CASH;
            sale.setPaymentMethod(paymentEnum);

            double actualCashAddedToDrawer = 0.0;
            double remainingCreditDebt = 0.0;

            if (paymentEnum == Sale.PaymentMethod.CREDIT) {
                if (customerIdStr.isEmpty() || customer == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(false, "ණයට ලබාදීමට Customer කෙනෙකු තෝරන්න!", null));
                }

                double paidInput = request.getPaidAmount() != null ? request.getPaidAmount() : 0.0;
                remainingCreditDebt = Math.max(0.0, netTotal - paidInput);

                // Check Credit Limit Only
                float currentCredit = customer.getCurrentCredit() != null ? customer.getCurrentCredit() : 0.0f;
                if (customer.getCreditLimit() != null && customer.getCreditLimit() > 0 && (currentCredit + remainingCreditDebt) > customer.getCreditLimit()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(
                            false, "Credit Limit පැහැර හැර ඇත! Limit: LKR " + customer.getCreditLimit(), null));
                }

                sale.setPaidAmount(paidInput);
                sale.setBalanceAmount(remainingCreditDebt);

                if (paidInput > 0) {
                    actualCashAddedToDrawer = paidInput;
                }

            } else {
                double paidInput = request.getPaidAmount() != null ? request.getPaidAmount() : netTotal;

                if (paidInput < netTotal) {
                    if (customerIdStr.isEmpty() || customer == null) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "අඩුවෙන් ගෙවන විට (ණය වෙන විට) Customer කෙනෙකු තෝරන්න!", null));
                    }
                    remainingCreditDebt = netTotal - paidInput;
                    sale.setPaidAmount(paidInput);
                    sale.setBalanceAmount(remainingCreditDebt);

                    if (paymentEnum == Sale.PaymentMethod.CASH) {
                        actualCashAddedToDrawer = paidInput;
                    }
                } else {
                    sale.setPaidAmount(paidInput);
                    sale.setBalanceAmount(paidInput - netTotal);

                    if (paymentEnum == Sale.PaymentMethod.CASH) {
                        actualCashAddedToDrawer = netTotal;
                    }
                }
            }

            Sale savedSale = saleRepository.save(sale);

            // ONLY SINGLE PLACE: RECORD CREDIT & UPDATE CUSTOMER BALANCE ACCURATELY
            if (remainingCreditDebt > 0 && customer != null) {
                CreditSaleRequestDTO creditDto = new CreditSaleRequestDTO();
                creditDto.setCustomerId(customer.getId());
                creditDto.setSaleId(savedSale.getId());
                creditDto.setCreditAmount((float) remainingCreditDebt); // Direct Credit Amount Pass
                creditDto.setNote("Bill Sale: " + savedSale.getInvoiceNumber());

                customerCreditService.recordCreditSale(creditDto);
            }

            // AUTO UPDATE CASH SESSION
            if (paymentEnum == Sale.PaymentMethod.CASH && actualCashAddedToDrawer > 0) {
                try {
                    Optional<CashSession> activeSessionOpt = cashSessionRepository.findAll()
                            .stream()
                            .filter(s -> "OPEN".equalsIgnoreCase(s.getStatus()) || s.getClosedAt() == null)
                            .findFirst();

                    if (activeSessionOpt.isPresent()) {
                        CashSession activeSession = activeSessionOpt.get();
                        double currentTotal = activeSession.getTotalCashSales() != null ? activeSession.getTotalCashSales() : 0.0;
                        activeSession.setTotalCashSales(currentTotal + actualCashAddedToDrawer);
                        cashSessionRepository.save(activeSession);
                    }
                } catch (Exception ex) {
                    System.err.println("Cash Session Auto Update Error: " + ex.getMessage());
                }
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Sale completed successfully", savedSale));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Checkout Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/credit-due")
    public ResponseEntity<ApiResponse<List<Sale>>> getCreditDueSales() {
        List<Sale> creditSales = saleRepository.findAllCreditDueSales();
        return ResponseEntity.ok(new ApiResponse<>(true, "Credit sales retrieved", creditSales));
    }

    @PostMapping("/{saleId}/pay-credit")
    @Transactional
    public ResponseEntity<ApiResponse<Sale>> payCreditForSale(
            @PathVariable String saleId,
            @RequestBody Map<String, Double> payload) {

        Double paymentAmount = payload.get("amount");
        if (paymentAmount == null || paymentAmount <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "වැරදි මුදලක්!", null));
        }

        Sale sale = saleRepository.findById(saleId).orElse(null);
        if (sale == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Bill එක හමුවූයේ නැත!", null));
        }

        double currentPaid = sale.getPaidAmount() != null ? sale.getPaidAmount() : 0.0;
        double newPaid = currentPaid + paymentAmount;
        double netTotal = sale.getNetTotal() != null ? sale.getNetTotal() : 0.0;
        double newBalance = Math.max(0.0, netTotal - newPaid);

        sale.setPaidAmount(newPaid);
        sale.setBalanceAmount(newBalance);

        // RECORD REPAYMENT VIA SERVICE TO PREVENT DUPES AND SAVE TRANSACTION
        if (sale.getCustomer() != null) {
            customerCreditService.recordCreditPayment(
                    sale.getCustomer().getId(),
                    paymentAmount.floatValue(),
                    "CASH"
            );
        }

        Sale updatedSale = saleRepository.save(sale);
        return ResponseEntity.ok(new ApiResponse<>(true, "ණය මුදල සාර්ථකව ගෙවන ලදී!", updatedSale));
    }
}