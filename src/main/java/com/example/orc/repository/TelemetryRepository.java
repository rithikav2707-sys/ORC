package com.example.orc.repository;

import com.example.orc.model.Telemetry;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    List<Telemetry> findTop24ByOrderByTimestampDesc();
    List<Telemetry> findByTimestampBetweenOrderByTimestampAsc(LocalDateTime start, LocalDateTime end);
    List<Telemetry> findTop1ByOrderByTimestampDesc();
}