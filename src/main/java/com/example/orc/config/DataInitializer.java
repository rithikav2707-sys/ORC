package com.example.orc.config;

import com.example.orc.model.Account;
import com.example.orc.model.Budget;
import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.Contact;
import com.example.orc.model.Product;
import com.example.orc.model.Telemetry;
import com.example.orc.model.TransactionKind;
import com.example.orc.model.Turbine;
import com.example.orc.repository.AccountRepository;
import com.example.orc.repository.BudgetRepository;
import com.example.orc.repository.BusinessTransactionRepository;
import com.example.orc.repository.ContactRepository;
import com.example.orc.repository.ProductRepository;
import com.example.orc.repository.TelemetryRepository;
import com.example.orc.repository.TurbineRepository;
import com.example.orc.service.OrcOperationsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final TurbineRepository turbines;
    private final TelemetryRepository telemetry;
    private final ContactRepository contacts;
    private final ProductRepository products;
    private final AccountRepository accounts;
    private final BudgetRepository budgets;
    private final BusinessTransactionRepository transactions;
    private final OrcOperationsService operations;

    public DataInitializer(TurbineRepository turbines, TelemetryRepository telemetry, ContactRepository contacts,
            ProductRepository products, AccountRepository accounts, BudgetRepository budgets,
            BusinessTransactionRepository transactions, OrcOperationsService operations) {
        this.turbines = turbines; this.telemetry = telemetry; this.contacts = contacts; this.products = products;
        this.accounts = accounts; this.budgets = budgets; this.transactions = transactions; this.operations = operations;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedMasters();
        seedTelemetry();
        seedTransactions();
        seedAccounts();
        seedBudget();
    }

    private void seedMasters() {
        if (turbines.count() == 0) {
            Turbine turbine = new Turbine();
            turbine.setTurbineCode("ORC-T001"); turbine.setTurbineName("Waste Heat Unit 2 Turbine");
            turbine.setCapacityKw(new BigDecimal("300")); turbine.setManufacturer("ORC Systems India");
            turbine.setModel("WHU-300"); turbine.setWorkingFluid("R245fa");
            turbine.setInletTemperature(new BigDecimal("185")); turbine.setOutletTemperature(new BigDecimal("92"));
            turbine.setPressure(new BigDecimal("18.4")); turbine.setMassFlowRate(new BigDecimal("4.8"));
            turbine.setTurbineSpeed(new BigDecimal("3600")); turbine.setEfficiency(new BigDecimal("18.6"));
            turbine.setStatus("RUNNING"); turbine.setUptimePercentage(new BigDecimal("96.0"));
            turbine.setInstallationDate(LocalDate.now().minusYears(2)); turbines.save(turbine);
        }
        if (contacts.count() == 0) {
            contacts.saveAll(List.of(contact("CUS-001", "Regional Power Grid", "CUSTOMER", "power@regionalgrid.example"),
                    contact("VEN-001", "Chemical Refrigerant Supplier", "VENDOR", "orders@refrigerant.example"),
                    contact("VEN-002", "Turbine Maintenance Specialist", "VENDOR", "service@turbinecare.example")));
        }
        if (products.count() == 0) {
            products.saveAll(List.of(product("ORC-KWH", "Recovered ORC kWh Electricity", "GOODS", "kWh", "10.00", "Energy exported to the regional grid"),
                    product("ORC-FLUID", "Organic Working Fluid", "GOODS", "Drum", "12000.00", "R245fa working fluid replenishment"),
                    product("TURBINE-SERVICE", "Turbine Overhaul Service", "SERVICE", "Service", "45000.00", "Preventive turbine maintenance")));
        }
    }

    private void seedTelemetry() {
        if (telemetry.count() != 0) return;
        Turbine turbine = turbines.findAll().get(0);
        for (int hour = 23; hour >= 0; hour--) {
            BigDecimal power = new BigDecimal("245").add(BigDecimal.valueOf((hour % 7) - 3L));
            Telemetry row = new Telemetry();
            row.setTimestamp(LocalDateTime.now().minusHours(hour)); row.setInletTemperature(new BigDecimal("185"));
            row.setOutletTemperature(new BigDecimal("92")); row.setPressure(new BigDecimal("18.4"));
            row.setMassFlowRate(new BigDecimal("4.8")); row.setTurbineSpeed(new BigDecimal("3600"));
            row.setPowerOutputKw(power); row.setEnergyGeneratedKwh(power);
            row.setEnergyExportedKwh(power.subtract(new BigDecimal("8")));
            row.setThermalEfficiency(new BigDecimal("18.6")); row.setWorkingFluid("R245fa");
            row.setSystemStatus("NORMAL"); row.setTurbine(turbine); telemetry.save(row);
        }
    }

    private void seedTransactions() {
        if (!transactions.findAll().isEmpty()) return;
        Contact customer = contacts.findByTypeOrderByNameAsc("CUSTOMER").get(0);
        Contact vendor = contacts.findByTypeOrderByNameAsc("VENDOR").get(0);
        Product power = products.findAll().stream().filter(item -> "ORC-KWH".equals(item.getProductCode())).findFirst().orElseThrow();
        Product fluid = products.findAll().stream().filter(item -> "ORC-FLUID".equals(item.getProductCode())).findFirst().orElseThrow();
        BusinessTransaction sale = operations.createOrder(TransactionKind.SALES_ORDER, customer.getId(), power.getId(),
                new BigDecimal("1842"), new BigDecimal("10.00"), LocalDate.now().minusDays(2));
        sale.setStatus("DELIVERED"); transactions.save(sale);
        BusinessTransaction invoice = operations.issueInvoice(sale.getId(), LocalDate.now().plusDays(28));
        operations.recordPayment(invoice.getId(), true, new BigDecimal("6000.00"), "BANK", LocalDate.now().minusDays(1));
        BusinessTransaction purchase = operations.createOrder(TransactionKind.PURCHASE_ORDER, vendor.getId(), fluid.getId(),
                new BigDecimal("2"), new BigDecimal("12000.00"), LocalDate.now().minusDays(5));
        operations.receivePurchase(purchase.getId());
        BusinessTransaction bill = operations.postVendorBill(purchase.getId(), LocalDate.now().plusDays(25));
        operations.recordPayment(bill.getId(), false, new BigDecimal("4000.00"), "BANK", LocalDate.now());
    }

    private void seedAccounts() {
        if (accounts.count() != 0) return;
        accounts.saveAll(List.of(account("1001", "ORC Power Plant Equipment", "ASSET"),
                account("1002", "Working Fluid Inventory", "ASSET"), account("1003", "Cash/Bank", "ASSET"),
                account("1101", "Customer Receivables", "ASSET"), account("2001", "Maintenance Vendor Payables", "LIABILITY"),
                account("2002", "Chemical Supplier Creditors", "LIABILITY"), account("4001", "ORC Power Sales Revenue", "INCOME"),
                account("4002", "Carbon Offset Credits", "INCOME"), account("5001", "Working Fluid Replenishment Costs", "EXPENSE"),
                account("5002", "Turbine Servicing Expenses", "EXPENSE")));
    }

    private void seedBudget() {
        if (budgets.count() != 0) return;
        Budget budget = new Budget(); budget.setFacilityName("Waste Heat Unit 2"); budget.setBudgetName("FY Operating Plan");
        budget.setStartDate(LocalDate.now().withDayOfYear(1)); budget.setEndDate(LocalDate.now().withMonth(12).withDayOfMonth(31));
        budget.setPlannedRevenue(new BigDecimal("6200000")); budget.setPlannedMaintenanceCost(new BigDecimal("450000"));
        budget.setPlannedFluidCost(new BigDecimal("280000"));
        budget.setActualRevenue(transactions.findByKindOrderByTransactionDateDesc(TransactionKind.CUSTOMER_INVOICE).stream()
            .map(BusinessTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        budget.setActualMaintenanceCost(BigDecimal.ZERO);
        budget.setActualFluidCost(transactions.findByKindOrderByTransactionDateDesc(TransactionKind.VENDOR_BILL).stream()
            .filter(bill -> bill.getProduct() != null && "ORC-FLUID".equals(bill.getProduct().getProductCode()))
            .map(BusinessTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        budgets.save(budget);
    }

    private Contact contact(String code, String name, String type, String email) {
        Contact contact = new Contact(); contact.setContactCode(code); contact.setName(name);
        contact.setType(type); contact.setEmail(email); contact.setStatus("ACTIVE"); return contact;
    }
    private Product product(String code, String name, String type, String unit, String price, String description) {
        Product product = new Product(); product.setProductCode(code); product.setProductName(name); product.setType(type);
        product.setUnit(unit); product.setPrice(new BigDecimal(price)); product.setDescription(description); return product;
    }
    private Account account(String code, String name, String type) {
        Account account = new Account(); account.setAccountCode(code); account.setAccountName(name); account.setAccountType(type); return account;
    }
}