package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String parentAccount;
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String value) { accountCode = value; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String value) { accountName = value; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String value) { accountType = value; }
    public String getParentAccount() { return parentAccount; }
    public void setParentAccount(String value) { parentAccount = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}