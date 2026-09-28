package com.example.orc.service;

import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.Telemetry;
import com.example.orc.model.TransactionKind;
import com.example.orc.model.Turbine;
import com.example.orc.repository.BusinessTransactionRepository;
import com.example.orc.repository.ContactRepository;
import com.example.orc.repository.ProductRepository;
import com.example.orc.repository.TelemetryRepository;
import com.example.orc.repository.TurbineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final TurbineRepository turbines;
    private final TelemetryRepository telemetry;
    private final BusinessTransactionRepository transactions;
    private final ContactRepository contacts;
    private final ProductRepository products;

    public DashboardService(TurbineRepository turbines, TelemetryRepository telemetry,
            BusinessTransactionRepository transactions, ContactRepository contacts, ProductRepository products) {
        this.turbines = turbines;
        this.telemetry = telemetry;
        this.transactions = transactions;
        this.contacts = contacts;
        this.products = products;
    }

    public Map<String, Object> summary() {
        LocalDate today = LocalDate.now();
        List<BusinessTransaction> all = transactions.findAll();
        List<BusinessTransaction> invoices = ofKind(all, TransactionKind.CUSTOMER_INVOICE);
        List<BusinessTransaction> bills = ofKind(all, TransactionKind.VENDOR_BILL);
        List<Telemetry> recent = telemetry.findTop24ByOrderByTimestampDesc();
        Telemetry latest = telemetry.findTop1ByOrderByTimestampDesc().stream().findFirst().orElse(null);
        List<Turbine> units = turbines.findAll();
        BigDecimal generated = recent.stream().map(Telemetry::getEnergyGeneratedKwh).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal efficiency = recent.isEmpty() ? units.stream().map(Turbine::getEfficiency).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : recent.stream().map(Telemetry::getThermalEfficiency)
                .reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(recent.size()), 2, RoundingMode.HALF_UP);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentPowerKw", latest == null ? BigDecimal.ZERO : latest.getPowerOutputKw());
        result.put("energyGeneratedKwh", generated.setScale(2, RoundingMode.HALF_UP));
        result.put("thermalEfficiency", efficiency.setScale(2, RoundingMode.HALF_UP));
        result.put("turbineStatus", units.stream().filter(t -> "RUNNING".equalsIgnoreCase(t.getStatus())).count() > 0 ? "RUNNING" : "STOPPED");
        result.put("uptime", units.stream().map(Turbine::getUptimePercentage).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(Math.max(units.size(), 1)), 1, RoundingMode.HALF_UP));
        result.put("inletTemperature", latest == null ? BigDecimal.ZERO : latest.getInletTemperature());
        result.put("outletTemperature", latest == null ? BigDecimal.ZERO : latest.getOutletTemperature());
        result.put("pressure", latest == null ? BigDecimal.ZERO : latest.getPressure());
        result.put("massFlowRate", latest == null ? BigDecimal.ZERO : latest.getMassFlowRate());
        result.put("turbineSpeed", latest == null ? BigDecimal.ZERO : latest.getTurbineSpeed());
        result.put("workingFluid", latest == null ? units.stream().findFirst().map(Turbine::getWorkingFluid).orElse("R245fa") : latest.getWorkingFluid());
        result.put("systemStatus", latest == null ? "NO DATA" : latest.getSystemStatus());
        result.put("monthlySales", sumMonth(invoices, today));
        result.put("monthlyPurchases", sumMonth(bills, today));
        result.put("receivables", outstanding(invoices));
        result.put("pendingInvoices", invoices.stream().filter(t -> t.getBalanceAmount().signum() > 0).count());
        result.put("payables", outstanding(bills));
        result.put("pendingBills", bills.stream().filter(t -> t.getBalanceAmount().signum() > 0).count());
        result.put("stockQuantity", products.findAll().stream().filter(p -> p.getProductCode() != null && p.getProductCode().contains("FLUID"))
                .map(p -> p.getStockQuantity()).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("topCustomers", topContacts(invoices));
        result.put("topVendors", topContacts(bills));
        result.put("recentTransactions", transactions.findTop10ByOrderByTransactionDateDesc());
        return result;
    }

    public Map<String, Object> generationSeries() {
        LocalDateTime end = LocalDateTime.now();
        List<Telemetry> day = telemetry.findByTimestampBetweenOrderByTimestampAsc(end.minusHours(23), end);
        List<String> labels = day.stream().map(t -> t.getTimestamp().toLocalTime().withSecond(0).withNano(0).toString()).toList();
        List<BigDecimal> powers = day.stream().map(Telemetry::getPowerOutputKw).toList();
        List<String> monthLabels = new ArrayList<>();
        List<BigDecimal> monthValues = new ArrayList<>();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = YearMonth.now().minusMonths(offset);
            List<Telemetry> monthRows = telemetry.findByTimestampBetweenOrderByTimestampAsc(
                    month.atDay(1).atStartOfDay(), month.atEndOfMonth().atTime(23, 59, 59));
            monthLabels.add(month.toString());
            monthValues.add(monthRows.stream().map(Telemetry::getEnergyGeneratedKwh).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return Map.of("hourLabels", labels, "powerOutput", powers, "monthLabels", monthLabels, "monthlyEnergy", monthValues);
    }

    private List<BusinessTransaction> ofKind(List<BusinessTransaction> rows, TransactionKind kind) {
        return rows.stream().filter(row -> row.getKind() == kind).toList();
    }
    private BigDecimal sumMonth(List<BusinessTransaction> rows, LocalDate date) {
        return rows.stream().filter(row -> row.getTransactionDate() != null && YearMonth.from(row.getTransactionDate()).equals(YearMonth.from(date)))
                .map(BusinessTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal outstanding(List<BusinessTransaction> rows) {
        return rows.stream().map(BusinessTransaction::getBalanceAmount).filter(v -> v.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private List<Map<String, Object>> topContacts(List<BusinessTransaction> rows) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Long> counts = new LinkedHashMap<>();
        rows.forEach(row -> {
            String name = row.getContact() == null ? "Unassigned" : row.getContact().getName();
            totals.merge(name, row.getAmount(), BigDecimal::add);
            counts.merge(name, 1L, Long::sum);
        });
        return totals.entrySet().stream().sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder())).limit(5)
                .map(entry -> Map.<String, Object>of("name", entry.getKey(), "amount", entry.getValue(), "orders", counts.get(entry.getKey())))
                .toList();
    }
}