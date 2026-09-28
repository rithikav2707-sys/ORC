package com.example.orc.controller;

import com.example.orc.exception.ResourceNotFoundException;
import com.example.orc.model.Telemetry;
import com.example.orc.repository.TelemetryRepository;
import com.example.orc.service.OrcOperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orc/telemetry")
@Validated
public class TelemetryRestController {
    private final TelemetryRepository telemetry;
    private final OrcOperationsService operations;
    public TelemetryRestController(TelemetryRepository telemetry, OrcOperationsService operations) {
        this.telemetry = telemetry;
        this.operations = operations;
    }
    @GetMapping public List<Telemetry> list() { return telemetry.findAll(); }
    @GetMapping("/{id}") public Telemetry get(@PathVariable Long id) {
        return telemetry.findById(id).orElseThrow(() -> new ResourceNotFoundException("Telemetry " + id + " was not found"));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Telemetry create(@Valid @RequestBody TelemetryInput input) {
        Telemetry row = new Telemetry();
        row.setTimestamp(input.timestamp());
        row.setInletTemperature(input.inletTemperature());
        row.setOutletTemperature(input.outletTemperature());
        row.setPressure(input.pressure());
        row.setMassFlowRate(input.massFlowRate());
        row.setTurbineSpeed(input.turbineSpeed());
        row.setPowerOutputKw(input.powerOutputKw());
        row.setSystemStatus(input.systemStatus() == null ? "NORMAL" : input.systemStatus());
        return operations.recordTelemetry(row, input.turbineId(), input.operatingHours(),
                input.internalConsumptionKwh(), input.thermalEnergyInputKw());
    }

    public record TelemetryInput(@NotNull Long turbineId, LocalDateTime timestamp,
            @DecimalMin("0.0") BigDecimal inletTemperature, @DecimalMin("0.0") BigDecimal outletTemperature,
            @DecimalMin("0.0") BigDecimal pressure, @DecimalMin("0.0") BigDecimal massFlowRate,
            @DecimalMin("0.0") BigDecimal turbineSpeed, @NotNull @DecimalMin("0.0") BigDecimal powerOutputKw,
            @NotNull @DecimalMin(value = "0.0001", inclusive = true) BigDecimal operatingHours,
            @DecimalMin("0.0") BigDecimal internalConsumptionKwh,
            @DecimalMin("0.0") BigDecimal thermalEnergyInputKw, String systemStatus) { }
}