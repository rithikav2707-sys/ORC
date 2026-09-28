package com.example.orc.controller;

import com.example.orc.model.Budget;
import com.example.orc.model.Account;
import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.Contact;
import com.example.orc.model.Product;
import com.example.orc.model.Telemetry;
import com.example.orc.model.TransactionKind;
import com.example.orc.model.Turbine;
import com.example.orc.repository.BudgetRepository;
import com.example.orc.repository.AccountRepository;
import com.example.orc.repository.BusinessTransactionRepository;
import com.example.orc.repository.ContactRepository;
import com.example.orc.repository.JournalLineRepository;
import com.example.orc.repository.ProductRepository;
import com.example.orc.repository.TelemetryRepository;
import com.example.orc.repository.TurbineRepository;
import com.example.orc.service.DashboardService;
import com.example.orc.service.OrcOperationsService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private final DashboardService dashboard;
    private final OrcOperationsService operations;
    private final TurbineRepository turbines;
    private final TelemetryRepository telemetry;
    private final ContactRepository contacts;
    private final ProductRepository products;
    private final BusinessTransactionRepository transactions;
    private final BudgetRepository budgets;
    private final AccountRepository accountRepository;
    private final JournalLineRepository journal;

    public WebController(DashboardService dashboard, OrcOperationsService operations, TurbineRepository turbines,
            TelemetryRepository telemetry, ContactRepository contacts, ProductRepository products,
            BusinessTransactionRepository transactions, BudgetRepository budgets, AccountRepository accountRepository,
            JournalLineRepository journal) {
        this.dashboard = dashboard; this.operations = operations; this.turbines = turbines; this.telemetry = telemetry;
        this.contacts = contacts; this.products = products; this.transactions = transactions; this.budgets = budgets;
        this.accountRepository = accountRepository; this.journal = journal;
    }

    @GetMapping("/") public String home(Model model) {
        model.addAttribute("summary", dashboard.summary());
        model.addAttribute("journal", journal.findByOrderByEntryDateDescJournalNumberAsc().stream().limit(8).toList());
        return "dashboard";
    }
    @GetMapping("/login") public String login() { return "login"; }
    @GetMapping("/turbines") public String turbines(Model model) { return page(model, "turbines", "ORC System", turbines.findAll()); }
    @GetMapping("/telemetry") public String telemetry(Model model) { return page(model, "telemetry", "Telemetry Data", telemetry.findAll().stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).toList()); }
    @GetMapping("/contacts") public String contacts(@RequestParam(required = false) String type, Model model) {
        List<Contact> rows = type == null ? contacts.findAll() : contacts.findByTypeOrderByNameAsc(type.toUpperCase());
        model.addAttribute("contactType", type == null ? "" : type.toUpperCase());
        return page(model, "contacts", type == null ? "Customers & Vendors" : type.equalsIgnoreCase("VENDOR") ? "Vendors" : "Customers", rows);
    }
    @GetMapping("/products") public String products(Model model) { return page(model, "products", "Products & Services", products.findAll()); }
    @GetMapping("/sales-orders") public String sales(Model model) { return page(model, "sales-orders", "Sales Orders", operations.transactions(TransactionKind.SALES_ORDER)); }
    @GetMapping("/purchase-orders") public String purchases(Model model) { return page(model, "purchase-orders", "Purchase Orders", operations.transactions(TransactionKind.PURCHASE_ORDER)); }
    @GetMapping("/invoices") public String invoices(Model model) { return page(model, "invoices", "Customer Invoices", operations.transactions(TransactionKind.CUSTOMER_INVOICE)); }
    @GetMapping("/vendor-bills") public String bills(Model model) { return page(model, "vendor-bills", "Vendor Bills", operations.transactions(TransactionKind.VENDOR_BILL)); }
    @GetMapping("/payments") public String payments(Model model) {
        return page(model, "payments", "Payments", transactions.findByKindInOrderByTransactionDateDesc(List.of(TransactionKind.RECEIPT, TransactionKind.PAYMENT)));
    }
    @GetMapping("/accounts") public String accounts(Model model) { return page(model, "accounts", "Chart of Accounts", operations.accounts()); }
    @GetMapping("/budgets") public String budgets(Model model) { return page(model, "budgets", "Facility Budgets", budgets.findAll()); }
    @GetMapping("/reports") public String reports() { return "reports"; }
    @GetMapping("/reports/profit-loss") public String profitLoss(Model model) { model.addAttribute("reportTitle", "Profit and Loss"); model.addAttribute("lines", journal.findAll()); return "reports"; }
    @GetMapping("/reports/balance-sheet") public String balanceSheet(Model model) { model.addAttribute("reportTitle", "Balance Sheet"); model.addAttribute("lines", journal.findAll()); return "reports"; }
    @GetMapping("/reports/generation") public String generationReport(Model model) {
        model.addAttribute("reportTitle", "Generation Report"); model.addAttribute("lines", telemetry.findAll()); return "reports";
    }
    @GetMapping("/settings") public String settings(Model model) {
        model.addAttribute("systemName", "Industrial Waste Heat to Electricity");
        model.addAttribute("plantName", "Waste Heat Unit 2"); model.addAttribute("electricityRate", new BigDecimal("10.00"));
        model.addAttribute("workingFluid", "R245fa"); model.addAttribute("databaseStatus", "Connected");
        return "settings";
    }

    @GetMapping({"/accounts/new", "/accounts/{id}/edit"}) public String accountForm(@PathVariable(required = false) Long id, Model model) {
        model.addAttribute("pageKey", "accounts"); model.addAttribute("title", id == null ? "Add account" : "Edit account");
        model.addAttribute("record", id == null ? new Account() : accountRepository.findById(id).orElseThrow());
        return "module-form";
    }
    @PostMapping("/accounts") public String saveAccount(@Valid @ModelAttribute("record") Account account, RedirectAttributes flash) {
        account.setAccountType(account.getAccountType().toUpperCase());
        account.setActive(true);
        accountRepository.save(account); flash.addFlashAttribute("notice", "Account saved"); return "redirect:/accounts";
    }
    @PostMapping("/accounts/{id}/delete") public String deleteAccount(@PathVariable Long id, RedirectAttributes flash) {
        accountRepository.deleteById(id); flash.addFlashAttribute("notice", "Account deleted"); return "redirect:/accounts";
    }

    @GetMapping({"/budgets/new", "/budgets/{id}/edit"}) public String budgetForm(@PathVariable(required = false) Long id, Model model) {
        Budget budget = id == null ? new Budget() : budgets.findById(id).orElseThrow();
        model.addAttribute("pageKey", "budgets"); model.addAttribute("title", id == null ? "Add budget" : "Edit budget");
        model.addAttribute("record", budget); return "module-form";
    }
    @PostMapping("/budgets") public String saveBudget(@Valid @ModelAttribute("record") Budget budget, RedirectAttributes flash) {
        if (budget.getEndDate() != null && budget.getStartDate() != null && budget.getEndDate().isBefore(budget.getStartDate())) {
            throw new IllegalArgumentException("Budget end date must be on or after the start date");
        }
        budgets.save(budget); flash.addFlashAttribute("notice", "Budget saved"); return "redirect:/budgets";
    }
    @PostMapping("/budgets/{id}/delete") public String deleteBudget(@PathVariable Long id, RedirectAttributes flash) {
        budgets.deleteById(id); flash.addFlashAttribute("notice", "Budget deleted"); return "redirect:/budgets";
    }

    @GetMapping({"/turbines/new", "/turbines/{id}/edit"}) public String turbineForm(@PathVariable(required = false) Long id, Model model) {
        model.addAttribute("pageKey", "turbines"); model.addAttribute("title", id == null ? "Add turbine" : "Edit turbine");
        model.addAttribute("record", id == null ? new Turbine() : operations.requireTurbine(id)); return "module-form";
    }
    @PostMapping("/turbines") public String saveTurbine(@Valid @ModelAttribute("record") Turbine turbine, BindingResult errors, RedirectAttributes flash) {
        if (errors.hasErrors()) return "module-form";
        turbines.save(turbine); flash.addFlashAttribute("notice", "Turbine saved"); return "redirect:/turbines";
    }
    @PostMapping("/turbines/{id}/delete") public String deleteTurbine(@PathVariable Long id, RedirectAttributes flash) {
        turbines.delete(operations.requireTurbine(id)); flash.addFlashAttribute("notice", "Turbine deleted"); return "redirect:/turbines";
    }

    @GetMapping({"/contacts/new", "/contacts/{id}/edit"}) public String contactForm(@PathVariable(required = false) Long id, Model model) {
        Contact contact = id == null ? new Contact() : contacts.findById(id).orElseThrow();
        model.addAttribute("pageKey", "contacts"); model.addAttribute("title", id == null ? "Add contact" : "Edit contact");
        model.addAttribute("record", contact); return "module-form";
    }
    @PostMapping("/contacts") public String saveContact(@Valid @ModelAttribute("record") Contact contact, BindingResult errors, RedirectAttributes flash) {
        if (errors.hasErrors()) return "module-form";
        contact.setType(contact.getType().toUpperCase()); contacts.save(contact); flash.addFlashAttribute("notice", "Contact saved"); return "redirect:/contacts";
    }
    @PostMapping("/contacts/{id}/delete") public String deleteContact(@PathVariable Long id, RedirectAttributes flash) {
        contacts.deleteById(id); flash.addFlashAttribute("notice", "Contact deleted"); return "redirect:/contacts";
    }

    @GetMapping({"/products/new", "/products/{id}/edit"}) public String productForm(@PathVariable(required = false) Long id, Model model) {
        Product product = id == null ? new Product() : products.findById(id).orElseThrow();
        model.addAttribute("pageKey", "products"); model.addAttribute("title", id == null ? "Add product" : "Edit product");
        model.addAttribute("record", product); return "module-form";
    }
    @PostMapping("/products") public String saveProduct(@Valid @ModelAttribute("record") Product product, BindingResult errors, RedirectAttributes flash) {
        if (errors.hasErrors()) return "module-form";
        product.setType(product.getType().toUpperCase()); products.save(product); flash.addFlashAttribute("notice", "Product saved"); return "redirect:/products";
    }
    @PostMapping("/products/{id}/delete") public String deleteProduct(@PathVariable Long id, RedirectAttributes flash) {
        products.deleteById(id); flash.addFlashAttribute("notice", "Product deleted"); return "redirect:/products";
    }

    @GetMapping("/{module}/new") public String newModuleForm(@PathVariable String module, Model model) {
        if (!List.of("telemetry", "sales-orders", "purchase-orders", "payments").contains(module)) return "redirect:/";
        model.addAttribute("pageKey", module); model.addAttribute("title", "Add " + module.replace('-', ' '));
        model.addAttribute("contacts", contacts.findAll()); model.addAttribute("customers", contacts.findByTypeOrderByNameAsc("CUSTOMER"));
        model.addAttribute("vendors", contacts.findByTypeOrderByNameAsc("VENDOR")); model.addAttribute("products", products.findAll());
        model.addAttribute("turbines", turbines.findAll()); model.addAttribute("invoices", operations.transactions(TransactionKind.CUSTOMER_INVOICE));
        model.addAttribute("bills", operations.transactions(TransactionKind.VENDOR_BILL));
        return "module-form";
    }
    @PostMapping("/telemetry") public String saveTelemetry(@RequestParam Long turbineId, @RequestParam BigDecimal powerOutputKw,
            @RequestParam BigDecimal operatingHours, @RequestParam(required = false) BigDecimal internalConsumptionKwh,
            @RequestParam(required = false) BigDecimal thermalEnergyInputKw, @RequestParam BigDecimal inletTemperature,
            @RequestParam BigDecimal outletTemperature, @RequestParam BigDecimal pressure,
            @RequestParam BigDecimal massFlowRate, @RequestParam BigDecimal turbineSpeed, RedirectAttributes flash) {
        Telemetry input = new Telemetry(); input.setPowerOutputKw(powerOutputKw); input.setInletTemperature(inletTemperature);
        input.setOutletTemperature(outletTemperature); input.setPressure(pressure); input.setMassFlowRate(massFlowRate);
        input.setTurbineSpeed(turbineSpeed); input.setSystemStatus("NORMAL");
        operations.recordTelemetry(input, turbineId, operatingHours, internalConsumptionKwh, thermalEnergyInputKw);
        flash.addFlashAttribute("notice", "Telemetry recorded"); return "redirect:/telemetry";
    }
    @PostMapping("/{module}") public String saveOrder(@PathVariable String module, @RequestParam Long contactId,
            @RequestParam Long productId, @RequestParam BigDecimal quantity, @RequestParam BigDecimal unitPrice,
            @RequestParam(required = false) LocalDate date, RedirectAttributes flash) {
        TransactionKind kind = module.equals("sales-orders") ? TransactionKind.SALES_ORDER : TransactionKind.PURCHASE_ORDER;
        operations.createOrder(kind, contactId, productId, quantity, unitPrice, date);
        flash.addFlashAttribute("notice", "Order saved"); return "redirect:/" + module;
    }
    @PostMapping("/payments") public String savePayment(@RequestParam Long referenceId, @RequestParam String paymentType,
            @RequestParam BigDecimal amount, @RequestParam(required = false) String paymentMethod, RedirectAttributes flash) {
        operations.recordPayment(referenceId, paymentType.equalsIgnoreCase("RECEIPT"), amount, paymentMethod, LocalDate.now());
        flash.addFlashAttribute("notice", "Payment recorded and journal posted"); return "redirect:/payments";
    }
    @PostMapping("/purchase-orders/{id}/receive") public String receivePurchase(@PathVariable Long id, RedirectAttributes flash) {
        operations.receivePurchase(id); flash.addFlashAttribute("notice", "Goods received and inventory updated"); return "redirect:/purchase-orders";
    }
    @PostMapping("/sales-orders/{id}/invoice") public String issueInvoice(@PathVariable Long id, RedirectAttributes flash) {
        operations.issueInvoice(id, LocalDate.now().plusDays(30)); flash.addFlashAttribute("notice", "Invoice issued and journal posted"); return "redirect:/invoices";
    }
    @PostMapping("/purchase-orders/{id}/bill") public String createBill(@PathVariable Long id, RedirectAttributes flash) {
        operations.postVendorBill(id, LocalDate.now().plusDays(30)); flash.addFlashAttribute("notice", "Vendor bill posted and journal recorded"); return "redirect:/vendor-bills";
    }

    private String page(Model model, String key, String title, Object rows) {
        model.addAttribute("pageKey", key); model.addAttribute("title", title); model.addAttribute("rows", rows);
        return "module-list";
    }
}