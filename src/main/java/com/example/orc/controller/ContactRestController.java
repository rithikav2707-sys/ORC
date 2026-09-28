package com.example.orc.controller;

import com.example.orc.exception.ResourceNotFoundException;
import com.example.orc.model.Contact;
import com.example.orc.repository.ContactRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactRestController {
    private final ContactRepository contacts;
    public ContactRestController(ContactRepository contacts) { this.contacts = contacts; }
    @GetMapping public List<Contact> list(@RequestParam(required = false) String type) {
        return type == null ? contacts.findAll() : contacts.findByTypeOrderByNameAsc(type.toUpperCase());
    }
    @GetMapping("/{id}") public Contact get(@PathVariable Long id) { return require(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Contact create(@Valid @RequestBody Contact contact) {
        contact.setType(contact.getType().toUpperCase());
        return contacts.save(contact);
    }
    @PutMapping("/{id}") public Contact update(@PathVariable Long id, @Valid @RequestBody Contact input) {
        Contact current = require(id);
        current.setContactCode(input.getContactCode()); current.setName(input.getName());
        current.setType(input.getType().toUpperCase()); current.setEmail(input.getEmail());
        current.setPhone(input.getPhone()); current.setAddress(input.getAddress());
        current.setCity(input.getCity()); current.setState(input.getState()); current.setStatus(input.getStatus());
        return contacts.save(current);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { contacts.delete(require(id)); }
    private Contact require(Long id) {
        return contacts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact " + id + " was not found"));
    }
}