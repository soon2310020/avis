package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.InsuranceRequestService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsuranceRequestDto;
import com.stg.utils.Endpoints;
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

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "InsuranceRequest APIs")
public class InsuranceRequestController {

    private final InsuranceRequestService insuranceRequestService;

    @GetMapping(Endpoints.URL_INSURANCE_REQUEST_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<InsuranceRequestDto> insurancePackages(@AuthenticationPrincipal CustomUserDetails user,
                                                                     @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                     @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                                     @RequestParam(name = "query", required = false) String query,
                                                                     @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                                                     @RequestParam(name = "status", required = false) Boolean status,
                                                                     @RequestParam(name = "category", defaultValue = "", required = false) String category,
                                                                     @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName) {
        return insuranceRequestService.list(Long.valueOf(user.getUserId()), page, size, query, segment, status, category, packageName);
    }

    @GetMapping(Endpoints.URL_INSURANCE_REQUEST_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public InsuranceRequestDto insuranceInfo(@AuthenticationPrincipal CustomUserDetails user,
                                             @Valid @PathVariable("id") Long requestId) {
        return insuranceRequestService.insuranceRequestDetail(Long.valueOf(user.getUserId()), requestId);
    }

    @GetMapping(Endpoints.URL_INSURANCE_REQUEST_LIST_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportListCustomers(@AuthenticationPrincipal CustomUserDetails user,
                                    @RequestParam(name = "query", required = false) String query,
                                    @RequestParam("type") String type,
                                    @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                    @RequestParam(name = "status", required = false) Boolean status,
                                    @RequestParam(name = "category", defaultValue = "", required = false) String category,
                                    @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                    HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        insuranceRequestService.exportList(Long.valueOf(user.getUserId()), query, type, segment, response, status, category, packageName);
    }

}
