package com.example.orc.controller;

import com.example.orc.model.Budget;
import com.example.orc.model.JournalLine;
import com.example.orc.model.Telemetry;
import com.example.orc.repository.BudgetRepository;
import com.example.orc.repository.JournalLineRepository;
import com.example.orc.repository.TelemetryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportRestController {
    private final JournalLineRepository journal;
    private final BudgetRepository budgets;
    private final TelemetryRepository telemetry;
    public ReportRestController(JournalLineRepository journal, BudgetRepository budgets, TelemetryRepository telemetry) {
        this.journal = journal; this.budgets = budgets; this.telemetry = telemetry;
    }
    @GetMapping("/profit-loss") public Map<String, BigDecimal> profitLoss() {
        List<JournalLine> lines = journal.findAll();
        BigDecimal sales = credit(lines, "4001");
        BigDecimal carbon = credit(lines, "4002");
        BigDecimal fluid = debit(lines, "5001");
        BigDecimal service = debit(lines, "5002");
        Map<String, BigDecimal> report = new LinkedHashMap<>();
        report.put("orcPowerSalesRevenue", sales); report.put("carbonOffsetCredits", carbon);
        report.put("totalIncome", sales.add(carbon)); report.put("workingFluidCosts", fluid);
        report.put("turbineServicingExpenses", service); report.put("totalExpenses", fluid.add(service));
        report.put("netProfitLoss", sales.add(carbon).subtract(fluid).subtract(service));
        return report;
    }
    @GetMapping("/balance-sheet") public Map<String, BigDecimal> balanceSheet() {
        List<JournalLine> lines = journal.findAll();
        Map<String, BigDecimal> report = new LinkedHashMap<>();
        report.put("plantEquipment", balance(lines, "1001")); report.put("workingFluidInventory", balance(lines, "1002"));
        report.put("cashBank", balance(lines, "1003")); report.put("customerReceivables", balance(lines, "1101"));
        report.put("maintenancePayables", balance(lines, "2001").negate());
        report.put("supplierCreditors", balance(lines, "2002").negate());
        report.put("totalAssets", report.get("plantEquipment").add(report.get("workingFluidInventory")).add(report.get("cashBank")).add(report.get("customerReceivables")));
        report.put("totalLiabilities", report.get("maintenancePayables").add(report.get("supplierCreditors")));
        return report;
    }
    @GetMapping("/budget") public List<Budget> budget() { return budgets.findAll(); }
    @GetMapping("/generation") public List<Telemetry> generation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return telemetry.findByTimestampBetweenOrderByTimestampAsc(from.atStartOfDay(), to.atTime(23, 59, 59));
    }
    private BigDecimal debit(List<JournalLine> rows, String code) {
        return rows.stream().filter(row -> code.equals(row.getAccountCode())).map(JournalLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal credit(List<JournalLine> rows, String code) {
        return rows.stream().filter(row -> code.equals(row.getAccountCode())).map(JournalLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal balance(List<JournalLine> rows, String code) {
        return debit(rows, code).subtract(credit(rows, code));
    }
}