package com.example.orc.repository;

import com.example.orc.model.BusinessTransaction;
import com.example.orc.model.TransactionKind;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessTransactionRepository extends JpaRepository<BusinessTransaction, Long> {
    List<BusinessTransaction> findByKindOrderByTransactionDateDesc(TransactionKind kind);
    List<BusinessTransaction> findByKindInOrderByTransactionDateDesc(List<TransactionKind> kinds);
    List<BusinessTransaction> findByKindAndTransactionDateBetween(TransactionKind kind, LocalDate start, LocalDate end);
    List<BusinessTransaction> findTop10ByOrderByTransactionDateDesc();
}