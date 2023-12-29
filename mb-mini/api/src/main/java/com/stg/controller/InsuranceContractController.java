package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.insurance.InsuranceContractListDto;
import com.stg.service.dto.insurance.InsuranceContractThirdPartyListDto;
import com.stg.utils.Endpoints;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.InsuranceContractService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsuranceContractDto;
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
@Tag(name = "InsuranceContract APIs")
public class InsuranceContractController {

    private final InsuranceContractService insuranceContractService;

    @GetMapping(Endpoints.URL_INSURANCE_CONTRACT_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<InsuranceContractListDto> insuranceContracts(@AuthenticationPrincipal CustomUserDetails user,
                                                                           @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                           @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                                           @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                                                           @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                                                           @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                                                           @RequestParam(name = "category", defaultValue = "", required = false) String category) {
        return insuranceContractService.list(Long.valueOf(user.getUserId()), page, size, query, segment, packageName, category);
    }

    @GetMapping(Endpoints.URL_INSURANCE_CONTRACT_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public InsuranceContractDto insuranceInfo(@AuthenticationPrincipal CustomUserDetails user,
                                             @Valid @PathVariable("id") Long contractId) {
        return insuranceContractService.contractDetail(Long.valueOf(user.getUserId()), contractId);
    }

    @GetMapping(Endpoints.URL_INSURANCE_CONTRACT_LIST_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportListInsuranceContract(@AuthenticationPrincipal CustomUserDetails user,
                                    @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                    @RequestParam("type") String type,
                                    @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                    @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                    @RequestParam(name = "category", defaultValue = "", required = false) String category,
                                    HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        insuranceContractService.exportList(Long.valueOf(user.getUserId()), query, type, segment, packageName, category, response);
    }

    @GetMapping(Endpoints.URL_INSURANCE_CONTRACT_THIRD_PARTY_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<InsuranceContractThirdPartyListDto> insuranceContractsThirdParty(@AuthenticationPrincipal CustomUserDetails user,
                                                                                               @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                                               @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                                                               @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                                                                               @RequestParam(name = "source", required = false, defaultValue = "") String source) {
        return insuranceContractService.listThirdParty(Long.valueOf(user.getUserId()), page, size, query, source);
    }

    @GetMapping(Endpoints.URL_INSURANCE_CONTRACT_THIRD_PARTY_EXPORT_LIST)
    @ResponseStatus(HttpStatus.OK)
    public void exportListInsuranceContractThirdParty(@AuthenticationPrincipal CustomUserDetails user,
                                            @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                            @RequestParam("type") String type,
                                            @RequestParam(name = "source", required = false, defaultValue = "") String source,
                                            HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        insuranceContractService.exportListThirdParty(Long.valueOf(user.getUserId()), query, source, type, response);
    }

    @GetMapping(Endpoints.URL_INSURANCE_CONTRACT_THIRD_PARTY_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public InsuranceContractDto insuranceThirdPartyDetail(@AuthenticationPrincipal CustomUserDetails user,
                                                                    @Valid @PathVariable("id") Long contractId) {
        return insuranceContractService.contractThirdPartyDetail(Long.valueOf(user.getUserId()), contractId);
    }
}
