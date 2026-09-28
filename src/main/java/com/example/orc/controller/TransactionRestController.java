package com.example.orc.controller;

import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.TransactionKind;
import com.example.orc.service.OrcOperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class TransactionRestController {
    private final OrcOperationsService operations;
    public TransactionRestController(OrcOperationsService operations) { this.operations = operations; }

    @PostMapping("/api/sales-orders") @ResponseStatus(HttpStatus.CREATED)
    public BusinessTransaction sales(@Valid @RequestBody OrderInput input) {
        return operations.createOrder(TransactionKind.SALES_ORDER, input.contactId(), input.productId(), input.quantity(), input.unitPrice(), input.date());
    }
    @GetMapping("/api/sales-orders") public List<BusinessTransaction> sales() { return operations.transactions(TransactionKind.SALES_ORDER); }
    @PostMapping("/api/purchases") @ResponseStatus(HttpStatus.CREATED)
    public BusinessTransaction purchase(@Valid @RequestBody OrderInput input) {
        return operations.createOrder(TransactionKind.PURCHASE_ORDER, input.contactId(), input.productId(), input.quantity(), input.unitPrice(), input.date());
    }
    @GetMapping("/api/purchases") public List<BusinessTransaction> purchases() { return operations.transactions(TransactionKind.PURCHASE_ORDER); }
    @PostMapping("/api/purchases/{id}/receive") public BusinessTransaction receive(@PathVariable Long id) { return operations.receivePurchase(id); }

    @PostMapping("/api/customer-invoices") @ResponseStatus(HttpStatus.CREATED)
    public BusinessTransaction invoice(@Valid @RequestBody ReferenceInput input) { return operations.issueInvoice(input.referenceId(), input.dueDate()); }
    @GetMapping("/api/customer-invoices") public List<BusinessTransaction> invoices() { return operations.transactions(TransactionKind.CUSTOMER_INVOICE); }
    @PostMapping("/api/vendor-bills") @ResponseStatus(HttpStatus.CREATED)
    public BusinessTransaction bill(@Valid @RequestBody ReferenceInput input) { return operations.postVendorBill(input.referenceId(), input.dueDate()); }
    @GetMapping("/api/vendor-bills") public List<BusinessTransaction> bills() { return operations.transactions(TransactionKind.VENDOR_BILL); }
    @PostMapping("/api/payments") @ResponseStatus(HttpStatus.CREATED)
    public BusinessTransaction payment(@Valid @RequestBody PaymentInput input) {
        return operations.recordPayment(input.referenceId(), "RECEIPT".equalsIgnoreCase(input.paymentType()), input.amount(), input.paymentMethod(), input.date());
    }
    @GetMapping("/api/payments") public List<BusinessTransaction> payments(@RequestParam(required = false) String type) {
        return type == null ? operations.transactions(TransactionKind.RECEIPT).stream()
                .collect(java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.toList(), receipts -> {
                    receipts.addAll(operations.transactions(TransactionKind.PAYMENT)); return receipts;
                }))
                : operations.transactions("RECEIPT".equalsIgnoreCase(type) ? TransactionKind.RECEIPT : TransactionKind.PAYMENT);
    }
    @GetMapping("/api/journal") public Object journal() { return operations.journal(); }
    @GetMapping("/api/accounts") public Object accounts() { return operations.accounts(); }

    public record OrderInput(@NotNull Long contactId, @NotNull Long productId, @NotNull @Positive BigDecimal quantity,
            @NotNull @DecimalMin("0.0") BigDecimal unitPrice, LocalDate date) { }
    public record ReferenceInput(@NotNull Long referenceId, LocalDate dueDate) { }
    public record PaymentInput(@NotNull Long referenceId, @NotNull String paymentType,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount, String paymentMethod, LocalDate date) { }
}