package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "contacts")
public class Contact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String contactCode;
    @NotBlank private String name;
    @NotBlank private String type;
    @Email private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String status = "ACTIVE";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContactCode() { return contactCode; }
    public void setContactCode(String value) { contactCode = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}