package com.example.orc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "turbines")
public class Turbine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String turbineCode;
    @NotBlank private String turbineName;
    @DecimalMin("0.0") private BigDecimal capacityKw = BigDecimal.ZERO;
    private String manufacturer;
    private String model;
    private String workingFluid = "R245fa";
    private BigDecimal inletTemperature = BigDecimal.ZERO;
    private BigDecimal outletTemperature = BigDecimal.ZERO;
    private BigDecimal pressure = BigDecimal.ZERO;
    private BigDecimal massFlowRate = BigDecimal.ZERO;
    private BigDecimal turbineSpeed = BigDecimal.ZERO;
    private BigDecimal efficiency = BigDecimal.ZERO;
    private String status = "STOPPED";
    private BigDecimal uptimePercentage = BigDecimal.ZERO;
    private LocalDate installationDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTurbineCode() { return turbineCode; }
    public void setTurbineCode(String turbineCode) { this.turbineCode = turbineCode; }
    public String getTurbineName() { return turbineName; }
    public void setTurbineName(String turbineName) { this.turbineName = turbineName; }
    public BigDecimal getCapacityKw() { return capacityKw; }
    public void setCapacityKw(BigDecimal capacityKw) { this.capacityKw = capacityKw; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getWorkingFluid() { return workingFluid; }
    public void setWorkingFluid(String workingFluid) { this.workingFluid = workingFluid; }
    public BigDecimal getInletTemperature() { return inletTemperature; }
    public void setInletTemperature(BigDecimal inletTemperature) { this.inletTemperature = inletTemperature; }
    public BigDecimal getOutletTemperature() { return outletTemperature; }
    public void setOutletTemperature(BigDecimal outletTemperature) { this.outletTemperature = outletTemperature; }
    public BigDecimal getPressure() { return pressure; }
    public void setPressure(BigDecimal pressure) { this.pressure = pressure; }
    public BigDecimal getMassFlowRate() { return massFlowRate; }
    public void setMassFlowRate(BigDecimal massFlowRate) { this.massFlowRate = massFlowRate; }
    public BigDecimal getTurbineSpeed() { return turbineSpeed; }
    public void setTurbineSpeed(BigDecimal turbineSpeed) { this.turbineSpeed = turbineSpeed; }
    public BigDecimal getEfficiency() { return efficiency; }
    public void setEfficiency(BigDecimal efficiency) { this.efficiency = efficiency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getUptimePercentage() { return uptimePercentage; }
    public void setUptimePercentage(BigDecimal uptimePercentage) { this.uptimePercentage = uptimePercentage; }
    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }
}