package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "business_transactions")
public class BusinessTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reference;
    @Enumerated(EnumType.STRING) private TransactionKind kind;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "contact_id") private Contact contact;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "product_id") private Product product;
    private LocalDate transactionDate;
    private LocalDate dueDate;
    private String status = "DRAFT";
    @DecimalMin("0.0") private BigDecimal amount = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal paidAmount = BigDecimal.ZERO;
    @Positive private BigDecimal quantity = BigDecimal.ONE;
    @DecimalMin("0.0") private BigDecimal unitPrice = BigDecimal.ZERO;
    private String paymentMethod;
    private String remarks;

    @PrePersist void setTransactionDate() { if (transactionDate == null) transactionDate = LocalDate.now(); }
    public Long getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String value) { reference = value; }
    public TransactionKind getKind() { return kind; }
    public void setKind(TransactionKind value) { kind = value; }
    public Contact getContact() { return contact; }
    public void setContact(Contact value) { contact = value; }
    public Product getProduct() { return product; }
    public void setProduct(Product value) { product = value; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate value) { transactionDate = value; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate value) { dueDate = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal value) { paidAmount = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { quantity = value; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String value) { paymentMethod = value; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String value) { remarks = value; }
    public BigDecimal getBalanceAmount() { return amount.subtract(paidAmount); }
}