package com.example.orc.repository;

import com.example.orc.model.JournalLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalLineRepository extends JpaRepository<JournalLine, Long> {
    List<JournalLine> findByOrderByEntryDateDescJournalNumberAsc();
    List<JournalLine> findByAccountCode(String accountCode);
}