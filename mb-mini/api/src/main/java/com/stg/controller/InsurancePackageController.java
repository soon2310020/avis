package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.utils.Endpoints;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.InsurancePackageService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsurancePackageDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "InsurancePackage Apis")
public class InsurancePackageController {
    private final InsurancePackageService insurancePackageService;

    @GetMapping(Endpoints.URL_INSURANCE_PACKAGE_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<InsurancePackageDto> insurancePackages(@AuthenticationPrincipal CustomUserDetails user,
                                                                     @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                     @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                                     @RequestParam(name = "query", required = false) String query,
                                                                     @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                                                     @RequestParam(name = "category", defaultValue = "", required = false) String category) {
        return insurancePackageService.list(Long.valueOf(user.getUserId()), page, size, query, category, packageName);
    }

    @GetMapping(Endpoints.URL_INSURANCE_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public InsurancePackageDto insuranceInfo(@AuthenticationPrincipal CustomUserDetails user,
                                             @Valid @PathVariable("id") Integer id) {
        return insurancePackageService.insuranceDetail(Long.valueOf(user.getUserId()), id);
    }

    @GetMapping(Endpoints.URL_INSURANCE_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportList(@AuthenticationPrincipal CustomUserDetails user,
                           @RequestParam(name = "query", required = false) String query,
                           @RequestParam("type") String type,
                           @RequestParam(name = "category", defaultValue = "", required = false) String category,
                           @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                           HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        insurancePackageService.exportList(Long.valueOf(user.getUserId()), query, type, category, response, packageName);
    }

}
