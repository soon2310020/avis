package com.stg.controller;

import com.stg.utils.Endpoints;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.DashboardService;
import com.stg.service.dto.dashboard.DashboardCustomerListDto;
import com.stg.service.dto.dashboard.DashboardOverviewDto;
import com.stg.service.dto.dashboard.DashboardPackageDto;
import com.stg.service.dto.dashboard.ReleaseQuantityDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "Dashboard APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping(Endpoints.URL_DASHBOARD_OVERVIEW)
    @ResponseStatus(HttpStatus.OK)
    public DashboardOverviewDto dashboardOverview(@AuthenticationPrincipal CustomUserDetails user) {
        return dashboardService.dashboardOverview(Long.valueOf(user.getUserId()));
    }

    @GetMapping(Endpoints.URL_DASHBOARD_RELEASE_QUANTITY)
    @ResponseStatus(HttpStatus.OK)
    public ReleaseQuantityDto releaseQuantity(@AuthenticationPrincipal CustomUserDetails user) {
        return dashboardService.releaseQuantity(Long.valueOf(user.getUserId()));
    }

    @GetMapping(Endpoints.URL_DASHBOARD_INSURANCE_PACKAGE)
    @ResponseStatus(HttpStatus.OK)
    public List<DashboardPackageDto> insurancePackages(@RequestParam(name = "category", defaultValue = "HAPPY", required = false) String category,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        return dashboardService.insurancePackages(Long.valueOf(user.getUserId()), category);
    }

    @GetMapping(Endpoints.URL_DASHBOARD_CUSTOMER_HEALTHY)
    @ResponseStatus(HttpStatus.OK)
    public List<DashboardCustomerListDto> customerHealthy(@AuthenticationPrincipal CustomUserDetails user) {
        return dashboardService.customerHealthy(Long.valueOf(user.getUserId()));
    }

}
