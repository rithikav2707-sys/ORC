package com.example.orc.repository;

import com.example.orc.model.Contact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByTypeOrderByNameAsc(String type);
}