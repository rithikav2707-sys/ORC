package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "journal_lines")
public class JournalLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String journalNumber;
    private String journalType;
    private LocalDate entryDate;
    private String description;
    private String reference;
    private String accountCode;
    private String accountName;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;

    public Long getId() { return id; }
    public String getJournalNumber() { return journalNumber; }
    public void setJournalNumber(String value) { journalNumber = value; }
    public String getJournalType() { return journalType; }
    public void setJournalType(String value) { journalType = value; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate value) { entryDate = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getReference() { return reference; }
    public void setReference(String value) { reference = value; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String value) { accountCode = value; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String value) { accountName = value; }
    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal value) { debit = value; }
    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal value) { credit = value; }
}