package com.example.orc.service;

import com.example.orc.exception.ResourceNotFoundException;
import com.example.orc.model.Account;
import com.example.orc.model.Budget;
import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.Contact;
import com.example.orc.model.JournalLine;
import com.example.orc.model.Product;
import com.example.orc.model.Telemetry;
import com.example.orc.model.TransactionKind;
import com.example.orc.model.Turbine;
import com.example.orc.repository.AccountRepository;
import com.example.orc.repository.BudgetRepository;
import com.example.orc.repository.BusinessTransactionRepository;
import com.example.orc.repository.ContactRepository;
import com.example.orc.repository.JournalLineRepository;
import com.example.orc.repository.ProductRepository;
import com.example.orc.repository.TelemetryRepository;
import com.example.orc.repository.TurbineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrcOperationsService {
    private final TurbineRepository turbines;
    private final TelemetryRepository telemetry;
    private final ContactRepository contacts;
    private final ProductRepository products;
    private final BusinessTransactionRepository transactions;
    private final JournalLineRepository journalLines;
    private final AccountRepository accounts;
    private final BudgetRepository budgets;

    public OrcOperationsService(TurbineRepository turbines, TelemetryRepository telemetry,
            ContactRepository contacts, ProductRepository products, BusinessTransactionRepository transactions,
            JournalLineRepository journalLines, AccountRepository accounts, BudgetRepository budgets) {
        this.turbines = turbines;
        this.telemetry = telemetry;
        this.contacts = contacts;
        this.products = products;
        this.transactions = transactions;
        this.journalLines = journalLines;
        this.accounts = accounts;
        this.budgets = budgets;
    }

    public Turbine requireTurbine(Long id) {
        return turbines.findById(id).orElseThrow(() -> new ResourceNotFoundException("Turbine " + id + " was not found"));
    }

    public Contact requireContact(Long id) {
        return contacts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact " + id + " was not found"));
    }

    public Product requireProduct(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product " + id + " was not found"));
    }

    @Transactional
    public Telemetry recordTelemetry(Telemetry input, Long turbineId, BigDecimal operatingHours,
            BigDecimal internalConsumptionKwh, BigDecimal thermalEnergyInputKw) {
        if (operatingHours == null || operatingHours.signum() <= 0) throw new IllegalArgumentException("Operating time must be greater than zero");
        if (input.getPowerOutputKw() == null || input.getPowerOutputKw().signum() < 0) throw new IllegalArgumentException("Power output cannot be negative");
        input.setTurbine(requireTurbine(turbineId));
        input.setTimestamp(input.getTimestamp() == null ? LocalDateTime.now() : input.getTimestamp());
        input.setEnergyGeneratedKwh(input.getPowerOutputKw().multiply(operatingHours).setScale(2, RoundingMode.HALF_UP));
        BigDecimal internalUse = internalConsumptionKwh == null ? BigDecimal.ZERO : internalConsumptionKwh;
        if (internalUse.signum() < 0 || internalUse.compareTo(input.getEnergyGeneratedKwh()) > 0) {
            throw new IllegalArgumentException("Internal consumption must be between zero and generated energy");
        }
        input.setEnergyExportedKwh(input.getEnergyGeneratedKwh().subtract(internalUse).setScale(2, RoundingMode.HALF_UP));
        if (thermalEnergyInputKw != null && thermalEnergyInputKw.signum() > 0) {
            input.setThermalEfficiency(input.getPowerOutputKw().divide(thermalEnergyInputKw, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        }
        if (input.getWorkingFluid() == null || input.getWorkingFluid().isBlank()) input.setWorkingFluid(input.getTurbine().getWorkingFluid());
        return telemetry.save(input);
    }

    @Transactional
    public BusinessTransaction createOrder(TransactionKind kind, Long contactId, Long productId, BigDecimal quantity,
            BigDecimal unitPrice, LocalDate date) {
        if (kind != TransactionKind.SALES_ORDER && kind != TransactionKind.PURCHASE_ORDER) throw new IllegalArgumentException("Unsupported order type");
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
        if (unitPrice == null || unitPrice.signum() < 0) throw new IllegalArgumentException("Unit price cannot be negative");
        Contact contact = requireContact(contactId);
        String expectedType = kind == TransactionKind.SALES_ORDER ? "CUSTOMER" : "VENDOR";
        if (!expectedType.equalsIgnoreCase(contact.getType())) throw new IllegalArgumentException("Select a " + expectedType.toLowerCase() + " contact");
        Product product = requireProduct(productId);
        BusinessTransaction order = new BusinessTransaction();
        order.setKind(kind);
        order.setContact(contact);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setAmount(quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP));
        order.setTransactionDate(date == null ? LocalDate.now() : date);
        order.setStatus("DRAFT");
        order.setReference(newReference(kind == TransactionKind.SALES_ORDER ? "SO" : "PO"));
        return transactions.save(order);
    }

    @Transactional
    public BusinessTransaction issueInvoice(Long salesOrderId, LocalDate dueDate) {
        BusinessTransaction order = requireTransaction(salesOrderId, TransactionKind.SALES_ORDER);
        if (transactions.findByKindOrderByTransactionDateDesc(TransactionKind.CUSTOMER_INVOICE).stream()
                .anyMatch(invoice -> order.getReference().equals(invoice.getRemarks()))) {
            throw new IllegalArgumentException("An invoice has already been issued for this order");
        }
        BusinessTransaction invoice = copyFinancialReference(order, TransactionKind.CUSTOMER_INVOICE, "INV");
        invoice.setStatus("ISSUED");
        invoice.setDueDate(dueDate == null ? LocalDate.now().plusDays(30) : dueDate);
        order.setStatus("DELIVERED");
        transactions.save(order);
        invoice = transactions.save(invoice);
        for (Budget budget : budgets.findAll()) {
            if (!containsDate(budget, invoice.getTransactionDate())) continue;
            budget.setActualRevenue(budget.getActualRevenue().add(invoice.getAmount()));
            budgets.save(budget);
        }
        post(invoice, "SALES", "Customer receivable", "1101", invoice.getAmount(),
            "ORC power sales revenue", "4001", invoice.getAmount());
        return invoice;
    }

    @Transactional
    public BusinessTransaction postVendorBill(Long purchaseOrderId, LocalDate dueDate) {
        BusinessTransaction order = requireTransaction(purchaseOrderId, TransactionKind.PURCHASE_ORDER);
        if (!"RECEIVED".equals(order.getStatus())) throw new IllegalArgumentException("Receive the purchase order before posting its bill");
        if (transactions.findByKindOrderByTransactionDateDesc(TransactionKind.VENDOR_BILL).stream()
                .anyMatch(bill -> order.getReference().equals(bill.getRemarks()))) {
            throw new IllegalArgumentException("A bill has already been posted for this purchase order");
        }
        BusinessTransaction bill = copyFinancialReference(order, TransactionKind.VENDOR_BILL, "BILL");
        bill.setStatus("POSTED");
        bill.setDueDate(dueDate == null ? LocalDate.now().plusDays(30) : dueDate);
        bill = transactions.save(bill);
        boolean service = "SERVICE".equalsIgnoreCase(order.getProduct().getType());
        for (Budget budget : budgets.findAll()) {
            if (!containsDate(budget, bill.getTransactionDate())) continue;
            if (service) budget.setActualMaintenanceCost(budget.getActualMaintenanceCost().add(bill.getAmount()));
            else budget.setActualFluidCost(budget.getActualFluidCost().add(bill.getAmount()));
            budgets.save(budget);
        }
        String debitName = service ? "Turbine servicing expenses" : "Working fluid inventory";
        String debitCode = service ? "5002" : "1002";
        String payableName = service ? "Maintenance vendor payables" : "Chemical supplier creditors";
        String payableCode = service ? "2001" : "2002";
        post(bill, "PURCHASE", debitName, debitCode, bill.getAmount(), payableName, payableCode, bill.getAmount());
        return bill;
    }

    @Transactional
    public BusinessTransaction receivePurchase(Long orderId) {
        BusinessTransaction order = requireTransaction(orderId, TransactionKind.PURCHASE_ORDER);
        if ("RECEIVED".equals(order.getStatus())) throw new IllegalArgumentException("Purchase order has already been received");
        Product product = order.getProduct();
        if ("GOODS".equalsIgnoreCase(product.getType())) {
            product.setStockQuantity(product.getStockQuantity().add(order.getQuantity()));
            products.save(product);
        }
        order.setStatus("RECEIVED");
        return transactions.save(order);
    }

    @Transactional
    public Product consumeInventory(Long productId, BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Consumed quantity must be greater than zero");
        Product product = requireProduct(productId);
        if (product.getStockQuantity().compareTo(quantity) < 0) throw new IllegalArgumentException("Insufficient inventory for this consumption");
        product.setStockQuantity(product.getStockQuantity().subtract(quantity));
        product = products.save(product);
        BigDecimal cost = quantity.multiply(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
        if (cost.signum() > 0) {
            String reference = newReference("USE");
            saveManualLine("JRN-" + reference, "PURCHASE", "Working fluid consumption", reference,
                "Working fluid replenishment costs", "5001", cost, BigDecimal.ZERO);
            saveManualLine("JRN-" + reference, "PURCHASE", "Working fluid consumption", reference,
                "Working fluid inventory", "1002", BigDecimal.ZERO, cost);
        }
        return product;
    }

    @Transactional
    public BusinessTransaction recordPayment(Long referenceId, boolean receipt, BigDecimal amount, String method, LocalDate date) {
        TransactionKind referenceKind = receipt ? TransactionKind.CUSTOMER_INVOICE : TransactionKind.VENDOR_BILL;
        BusinessTransaction target = requireTransaction(referenceId, referenceKind);
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Payment amount must be greater than zero");
        if (amount.compareTo(target.getBalanceAmount()) > 0) throw new IllegalArgumentException("Payment exceeds outstanding balance");
        target.setPaidAmount(target.getPaidAmount().add(amount));
        target.setStatus(target.getBalanceAmount().signum() == 0 ? "PAID" : "PARTIAL");
        transactions.save(target);
        BusinessTransaction payment = new BusinessTransaction();
        payment.setKind(receipt ? TransactionKind.RECEIPT : TransactionKind.PAYMENT);
        payment.setContact(target.getContact());
        payment.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        payment.setPaidAmount(payment.getAmount());
        payment.setPaymentMethod(method == null ? "BANK" : method);
        payment.setTransactionDate(date == null ? LocalDate.now() : date);
        payment.setStatus("COMPLETED");
        payment.setReference(newReference(receipt ? "RCT" : "PAY"));
        payment.setRemarks(target.getReference());
        payment = transactions.save(payment);
        if (receipt) {
                post(payment, "CASH", "Cash/Bank", "1003", amount, "Customer receivable", "1101", amount);
        } else {
            String code = "SERVICE".equalsIgnoreCase(target.getProduct().getType()) ? "2001" : "2002";
            String name = code.equals("2001") ? "Maintenance vendor payables" : "Chemical supplier creditors";
            post(payment, "CASH", name, code, amount, "Cash/Bank", "1003", amount);
        }
        return payment;
    }

    public List<Account> accounts() { return accounts.findAll(); }
    public List<JournalLine> journal() { return journalLines.findByOrderByEntryDateDescJournalNumberAsc(); }
    public List<BusinessTransaction> transactions(TransactionKind kind) { return transactions.findByKindOrderByTransactionDateDesc(kind); }
    public List<BusinessTransaction> allTransactions() { return transactions.findTop10ByOrderByTransactionDateDesc(); }

    private BusinessTransaction requireTransaction(Long id, TransactionKind kind) {
        BusinessTransaction transaction = transactions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " was not found"));
        if (transaction.getKind() != kind) throw new IllegalArgumentException("Transaction is not a " + kind.name().toLowerCase().replace('_', ' '));
        return transaction;
    }

    private BusinessTransaction copyFinancialReference(BusinessTransaction order, TransactionKind kind, String prefix) {
        BusinessTransaction copy = new BusinessTransaction();
        copy.setKind(kind);
        copy.setContact(order.getContact());
        copy.setProduct(order.getProduct());
        copy.setQuantity(order.getQuantity());
        copy.setUnitPrice(order.getUnitPrice());
        copy.setAmount(order.getAmount());
        copy.setTransactionDate(LocalDate.now());
        copy.setReference(newReference(prefix));
        copy.setRemarks(order.getReference());
        return copy;
    }

        private void post(BusinessTransaction transaction, String type, String debitName, String debitCode,
            BigDecimal debit, String creditName, String creditCode, BigDecimal credit) {
        if (debit.compareTo(credit) != 0) throw new IllegalStateException("Journal entry is not balanced");
        saveLine(transaction, type, debitName, debitCode, debit, BigDecimal.ZERO);
        saveLine(transaction, type, creditName, creditCode, BigDecimal.ZERO, credit);
    }

    private void saveLine(BusinessTransaction transaction, String type, String name, String code, BigDecimal debit, BigDecimal credit) {
        JournalLine line = new JournalLine();
        line.setJournalNumber("JRN-" + transaction.getReference());
        line.setJournalType(type);
        line.setEntryDate(transaction.getTransactionDate());
        line.setDescription(transaction.getKind().name().replace('_', ' ') + " - " + transaction.getContact().getName());
        line.setReference(transaction.getReference());
        line.setAccountCode(code);
        line.setAccountName(name);
        line.setDebit(debit);
        line.setCredit(credit);
        journalLines.save(line);
    }

    private void saveManualLine(String journalNumber, String type, String description, String reference,
            String accountName, String accountCode, BigDecimal debit, BigDecimal credit) {
        JournalLine line = new JournalLine();
        line.setJournalNumber(journalNumber); line.setJournalType(type); line.setEntryDate(LocalDate.now());
        line.setDescription(description); line.setReference(reference); line.setAccountCode(accountCode);
        line.setAccountName(accountName); line.setDebit(debit); line.setCredit(credit); journalLines.save(line);
    }

    private String newReference(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean containsDate(Budget budget, LocalDate date) {
        return budget.getStartDate() != null && budget.getEndDate() != null
                && !date.isBefore(budget.getStartDate()) && !date.isAfter(budget.getEndDate());
    }
}