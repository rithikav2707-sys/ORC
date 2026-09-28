package com.example.orc.controller;

import com.example.orc.exception.ResourceNotFoundException;
import com.example.orc.model.Turbine;
import com.example.orc.repository.TurbineRepository;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orc/turbines")
public class TurbineRestController {
    private final TurbineRepository turbines;
    public TurbineRestController(TurbineRepository turbines) { this.turbines = turbines; }

    @GetMapping public List<Turbine> list() { return turbines.findAll(); }
    @GetMapping("/{id}") public Turbine get(@PathVariable Long id) { return require(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Turbine create(@Valid @RequestBody Turbine turbine) {
        turbine.setStatus(turbine.getStatus() == null ? "STOPPED" : turbine.getStatus().toUpperCase());
        return turbines.save(turbine);
    }
    @PutMapping("/{id}") public Turbine update(@PathVariable Long id, @Valid @RequestBody Turbine input) {
        Turbine current = require(id);
        current.setTurbineCode(input.getTurbineCode());
        current.setTurbineName(input.getTurbineName());
        current.setCapacityKw(input.getCapacityKw());
        current.setManufacturer(input.getManufacturer());
        current.setModel(input.getModel());
        current.setWorkingFluid(input.getWorkingFluid());
        current.setInletTemperature(input.getInletTemperature());
        current.setOutletTemperature(input.getOutletTemperature());
        current.setPressure(input.getPressure());
        current.setMassFlowRate(input.getMassFlowRate());
        current.setTurbineSpeed(input.getTurbineSpeed());
        current.setEfficiency(input.getEfficiency());
        current.setStatus(input.getStatus());
        current.setUptimePercentage(input.getUptimePercentage());
        current.setInstallationDate(input.getInstallationDate());
        return turbines.save(current);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) {
        turbines.delete(require(id));
    }
    private Turbine require(Long id) {
        return turbines.findById(id).orElseThrow(() -> new ResourceNotFoundException("Turbine " + id + " was not found"));
    }
}