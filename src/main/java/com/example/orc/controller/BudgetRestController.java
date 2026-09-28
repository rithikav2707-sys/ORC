package com.example.orc.controller;

import com.example.orc.exception.ResourceNotFoundException;
import com.example.orc.model.Budget;
import com.example.orc.repository.BudgetRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetRestController {
    private final BudgetRepository budgets;
    public BudgetRestController(BudgetRepository budgets) { this.budgets = budgets; }
    @GetMapping public List<Budget> list() { return budgets.findAll(); }
    @GetMapping("/{id}") public Budget get(@PathVariable Long id) { return require(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Budget create(@Valid @RequestBody Budget budget) { return budgets.save(budget); }
    @PutMapping("/{id}") public Budget update(@PathVariable Long id, @Valid @RequestBody Budget input) {
        Budget current = require(id);
        current.setFacilityName(input.getFacilityName()); current.setBudgetName(input.getBudgetName());
        current.setStartDate(input.getStartDate()); current.setEndDate(input.getEndDate());
        current.setPlannedRevenue(input.getPlannedRevenue()); current.setPlannedMaintenanceCost(input.getPlannedMaintenanceCost());
        current.setPlannedFluidCost(input.getPlannedFluidCost()); current.setActualRevenue(input.getActualRevenue());
        current.setActualMaintenanceCost(input.getActualMaintenanceCost()); current.setActualFluidCost(input.getActualFluidCost());
        current.setStatus(input.getStatus()); return budgets.save(current);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { budgets.delete(require(id)); }
    private Budget require(Long id) { return budgets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Budget " + id + " was not found")); }
}