package com.example.orc.controller;

import com.example.orc.model.Product;
import com.example.orc.service.OrcOperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRestController {
    private final OrcOperationsService operations;
    public InventoryRestController(OrcOperationsService operations) { this.operations = operations; }
    @PostMapping("/consume") public Product consume(@Valid @RequestBody ConsumptionInput input) {
        return operations.consumeInventory(input.productId(), input.quantity());
    }
    public record ConsumptionInput(@NotNull Long productId, @NotNull @Positive BigDecimal quantity) { }
}