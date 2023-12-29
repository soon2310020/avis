package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.user.CustomerIdentifier;
import com.stg.service.CustomerService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.customer.CustomerDetailDto;
import com.stg.service.dto.customer.CustomerDto;
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


@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "Customer Apis")
public class CustomerController {

    private final CustomerService customerService;

    // filter illustrationBoards
    @GetMapping(Endpoints.URL_CUSTOMER_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<CustomerDto> customerFilter(@AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                          @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                          @RequestParam(name = "queryName", required = false, defaultValue = "") String queryName,
                                                          @RequestParam(name = "segment", required = false, defaultValue = "") String segment) {
        return customerService.customerFilterList(Long.valueOf(user.getUserId()), page, size, queryName, segment);
    }

    @GetMapping(Endpoints.URL_CUSTOMER_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public CustomerDetailDto customerDetail(@AuthenticationPrincipal CustomUserDetails user,
                                            @Valid @PathVariable("id") Long id) {
        return customerService.customerDetail(Long.valueOf(user.getUserId()), id);
    }

    @GetMapping(Endpoints.URL_CUSTOMER_LIST_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportListCustomers(@AuthenticationPrincipal CustomUserDetails user,
                                    @RequestParam(name = "queryName", required = false) String queryName,
                                    @RequestParam("type") String type,
                                    @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                    HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        customerService.exportListCustomers(Long.valueOf(user.getUserId()), queryName, type, response, segment);
    }

    @GetMapping(Endpoints.URL_CUSTOMER_LIST_EXPORT_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public void exportCustomerDetail(@AuthenticationPrincipal CustomUserDetails user,
                                     @Valid @PathVariable("id") Long id, @RequestParam("type") String type,
                                     HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        customerService.exportCustomerDetail(Long.valueOf(user.getUserId()), id, type, response);
    }

    @GetMapping(Endpoints.MINI_APP_CUSTOMER)
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto getCustomer(@AuthenticationPrincipal CustomerIdentifier identifier) {
        return customerService.customerDetail(identifier.getMbId());
    }

    @PutMapping(Endpoints.MINI_APP_CUSTOMER)
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto updateCustomer(@AuthenticationPrincipal CustomerIdentifier identifier,
                                      @Valid @RequestBody CustomerDto customerDto) {
        return customerService.updateCustomer(identifier.getMbId(), customerDto);
    }

}
