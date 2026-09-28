package com.example.orc.controller;

import com.example.orc.service.DashboardService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {
    private final DashboardService dashboard;
    public DashboardRestController(DashboardService dashboard) { this.dashboard = dashboard; }
    @GetMapping("/summary") public Map<String, Object> summary() { return dashboard.summary(); }
    @GetMapping("/generation") public Map<String, Object> generation() { return dashboard.generationSeries(); }
}