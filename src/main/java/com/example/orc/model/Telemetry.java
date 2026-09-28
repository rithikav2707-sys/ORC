package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry")
public class Telemetry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "turbine_id")
    @NotNull private Turbine turbine;
    private LocalDateTime timestamp;
    @DecimalMin("0.0") private BigDecimal inletTemperature = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal outletTemperature = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal pressure = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal massFlowRate = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal turbineSpeed = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal powerOutputKw = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal energyGeneratedKwh = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal energyExportedKwh = BigDecimal.ZERO;
    @DecimalMin("0.0") private BigDecimal thermalEfficiency = BigDecimal.ZERO;
    private String workingFluid;
    private String systemStatus = "NORMAL";

    @PrePersist void setTimestamp() { if (timestamp == null) timestamp = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Turbine getTurbine() { return turbine; }
    public void setTurbine(Turbine turbine) { this.turbine = turbine; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public BigDecimal getInletTemperature() { return inletTemperature; }
    public void setInletTemperature(BigDecimal value) { inletTemperature = value; }
    public BigDecimal getOutletTemperature() { return outletTemperature; }
    public void setOutletTemperature(BigDecimal value) { outletTemperature = value; }
    public BigDecimal getPressure() { return pressure; }
    public void setPressure(BigDecimal value) { pressure = value; }
    public BigDecimal getMassFlowRate() { return massFlowRate; }
    public void setMassFlowRate(BigDecimal value) { massFlowRate = value; }
    public BigDecimal getTurbineSpeed() { return turbineSpeed; }
    public void setTurbineSpeed(BigDecimal value) { turbineSpeed = value; }
    public BigDecimal getPowerOutputKw() { return powerOutputKw; }
    public void setPowerOutputKw(BigDecimal value) { powerOutputKw = value; }
    public BigDecimal getEnergyGeneratedKwh() { return energyGeneratedKwh; }
    public void setEnergyGeneratedKwh(BigDecimal value) { energyGeneratedKwh = value; }
    public BigDecimal getEnergyExportedKwh() { return energyExportedKwh; }
    public void setEnergyExportedKwh(BigDecimal value) { energyExportedKwh = value; }
    public BigDecimal getThermalEfficiency() { return thermalEfficiency; }
    public void setThermalEfficiency(BigDecimal value) { thermalEfficiency = value; }
    public String getWorkingFluid() { return workingFluid; }
    public void setWorkingFluid(String workingFluid) { this.workingFluid = workingFluid; }
    public String getSystemStatus() { return systemStatus; }
    public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
}