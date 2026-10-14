package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.dashboard.DashboardResponse;
import com.miniCRM.miniCRM.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VER')")
    public DashboardResponse dashboard() {
        return dashboardService.montar();
    }
}
