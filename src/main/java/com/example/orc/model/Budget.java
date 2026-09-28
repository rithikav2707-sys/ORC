package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String facilityName;
    private String budgetName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal plannedRevenue = BigDecimal.ZERO;
    private BigDecimal plannedMaintenanceCost = BigDecimal.ZERO;
    private BigDecimal plannedFluidCost = BigDecimal.ZERO;
    private BigDecimal actualRevenue = BigDecimal.ZERO;
    private BigDecimal actualMaintenanceCost = BigDecimal.ZERO;
    private BigDecimal actualFluidCost = BigDecimal.ZERO;
    private String status = "ACTIVE";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String value) { facilityName = value; }
    public String getBudgetName() { return budgetName; }
    public void setBudgetName(String value) { budgetName = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate value) { startDate = value; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate value) { endDate = value; }
    public BigDecimal getPlannedRevenue() { return plannedRevenue; }
    public void setPlannedRevenue(BigDecimal value) { plannedRevenue = value; }
    public BigDecimal getPlannedMaintenanceCost() { return plannedMaintenanceCost; }
    public void setPlannedMaintenanceCost(BigDecimal value) { plannedMaintenanceCost = value; }
    public BigDecimal getPlannedFluidCost() { return plannedFluidCost; }
    public void setPlannedFluidCost(BigDecimal value) { plannedFluidCost = value; }
    public BigDecimal getActualRevenue() { return actualRevenue; }
    public void setActualRevenue(BigDecimal value) { actualRevenue = value; }
    public BigDecimal getActualMaintenanceCost() { return actualMaintenanceCost; }
    public void setActualMaintenanceCost(BigDecimal value) { actualMaintenanceCost = value; }
    public BigDecimal getActualFluidCost() { return actualFluidCost; }
    public void setActualFluidCost(BigDecimal value) { actualFluidCost = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public BigDecimal getPlannedTotalExpense() { return plannedMaintenanceCost.add(plannedFluidCost); }
    public BigDecimal getActualTotalExpense() { return actualMaintenanceCost.add(actualFluidCost); }
    public BigDecimal getRevenueVariance() { return actualRevenue.subtract(plannedRevenue); }
    public BigDecimal getExpenseVariance() { return getPlannedTotalExpense().subtract(getActualTotalExpense()); }
}