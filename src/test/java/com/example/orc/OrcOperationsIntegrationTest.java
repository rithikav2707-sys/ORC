package com.example.orc;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.Contact;
import com.example.orc.model.JournalLine;
import com.example.orc.model.Product;
import com.example.orc.model.Telemetry;
import com.example.orc.model.TransactionKind;
import com.example.orc.model.Turbine;
import com.example.orc.repository.BusinessTransactionRepository;
import com.example.orc.repository.ContactRepository;
import com.example.orc.repository.JournalLineRepository;
import com.example.orc.repository.ProductRepository;
import com.example.orc.repository.TurbineRepository;
import com.example.orc.service.OrcOperationsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrcOperationsIntegrationTest {
    @Autowired private OrcOperationsService operations;
    @Autowired private TurbineRepository turbines;
    @Autowired private ContactRepository contacts;
    @Autowired private ProductRepository products;
    @Autowired private BusinessTransactionRepository transactions;
    @Autowired private JournalLineRepository journal;

    @Test
    void calculatesGeneratedAndExportedEnergyAndEfficiency() {
        Turbine turbine = turbines.findAll().get(0);
        Telemetry input = new Telemetry();
        input.setPowerOutputKw(new BigDecimal("250"));
        Telemetry saved = operations.recordTelemetry(input, turbine.getId(), new BigDecimal("2"),
                new BigDecimal("50"), new BigDecimal("1000"));

        assertThat(saved.getEnergyGeneratedKwh()).isEqualByComparingTo("500.00");
        assertThat(saved.getEnergyExportedKwh()).isEqualByComparingTo("450.00");
        assertThat(saved.getThermalEfficiency()).isEqualByComparingTo("25.00");
    }

    @Test
    void paymentUpdatesBalanceAndPostsBalancedJournalLines() {
        Contact customer = contacts.findByTypeOrderByNameAsc("CUSTOMER").get(0);
        Product electricity = products.findAll().stream().filter(product -> "ORC-KWH".equals(product.getProductCode())).findFirst().orElseThrow();
        BusinessTransaction order = operations.createOrder(TransactionKind.SALES_ORDER, customer.getId(), electricity.getId(),
                new BigDecimal("10"), new BigDecimal("10"), LocalDate.now());
        BusinessTransaction invoice = operations.issueInvoice(order.getId(), LocalDate.now().plusDays(30));
        BusinessTransaction payment = operations.recordPayment(invoice.getId(), true, new BigDecimal("25"), "BANK", LocalDate.now());

        BusinessTransaction updatedInvoice = transactions.findById(invoice.getId()).orElseThrow();
        assertThat(updatedInvoice.getPaidAmount()).isEqualByComparingTo("25.00");
        assertThat(updatedInvoice.getBalanceAmount()).isEqualByComparingTo("75.00");
        List<JournalLine> posted = journal.findAll().stream()
                .filter(line -> line.getReference().equals(invoice.getReference()) || line.getReference().equals(payment.getReference()))
                .toList();
        BigDecimal debits = posted.stream().map(JournalLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credits = posted.stream().map(JournalLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(debits).isEqualByComparingTo(credits);
    }

    @Test
    void inventoryConsumptionReducesStockAndPostsBalancedEntry() {
        Product fluid = products.findAll().stream().filter(product -> "ORC-FLUID".equals(product.getProductCode())).findFirst().orElseThrow();
        BigDecimal originalStock = fluid.getStockQuantity();
        Product updated = operations.consumeInventory(fluid.getId(), new BigDecimal("0.5"));

        assertThat(updated.getStockQuantity()).isEqualByComparingTo(originalStock.subtract(new BigDecimal("0.5")));
        List<JournalLine> lines = journal.findAll().stream().filter(line -> line.getAccountCode().equals("5001")
                && "Working fluid consumption".equals(line.getDescription())).toList();
        assertThat(lines).hasSize(1);
        JournalLine expense = lines.get(0);
        JournalLine inventory = journal.findAll().stream().filter(line -> expense.getJournalNumber().equals(line.getJournalNumber())
                && line.getAccountCode().equals("1002")).findFirst().orElseThrow();
        assertThat(expense.getDebit()).isEqualByComparingTo(inventory.getCredit());
    }
}