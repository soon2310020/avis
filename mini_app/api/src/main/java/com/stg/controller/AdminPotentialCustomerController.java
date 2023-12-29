package com.stg.controller;

import java.time.LocalDate;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stg.common.AdminEndpoints.POTENTIAL_CUSTOMER;
import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.service.PotentialCustomerDirectService;
import com.stg.service.PotentialCustomerReferService;
import com.stg.service.PotentialCustomerService;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectSubmitResp;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredHeaderDto;
import com.stg.service.dto.potentialcustomer.SearchDirectPotentialCustomerRes;
import com.stg.service.dto.potentialcustomer.SearchPotentialCustomerRes;
import com.stg.utils.excel.ExportType;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = "Admin Potential Customer")
public class AdminPotentialCustomerController {

    private final PotentialCustomerService potentialCustomerService;

    private final PotentialCustomerReferService potentialCustomerReferService;

    private final PotentialCustomerDirectService potentialCustomerDirectService;

    /**
     * List of referred customers
     */
    @GetMapping(POTENTIAL_CUSTOMER.SEARCH_REFERRED)
    @ResponseStatus(HttpStatus.OK)
    public Page<PotentialCustomerReferredHeaderDto> searchReferredCustomers(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "appStatus", required = false) AppStatus appStatus,
            @RequestParam(name = "leadStatus", required = false) String leadStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return potentialCustomerReferService.adminSearch(pageable, query, from, to, appStatus, leadStatus);
    }

    @GetMapping(POTENTIAL_CUSTOMER.EXPORT_REFERRED)
    @ResponseStatus(HttpStatus.OK)
    public void exportReferredCustomers(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "appStatus", required = false) AppStatus appStatus,
            @RequestParam(name = "leadStatus", required = false) String leadStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "type") ExportType type, HttpServletResponse response) {
        potentialCustomerReferService.adminExport(pageable, query, from, to, appStatus, leadStatus, type, response);
    }

    /**
     * List of referred customers by customer
     */
    @GetMapping(POTENTIAL_CUSTOMER.SEARCH_REFERRED_BY_CUSTOMER)
    @ResponseStatus(HttpStatus.OK)
    public Page<PotentialCustomerReferredHeaderDto> searchReferredCustomers(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @PathVariable("id") Long potentialCustomerId) {
        return potentialCustomerReferService.searchByPotentialCustomer(pageable, potentialCustomerId);
    }

    /**
     * Get referred customer detail by id
     */
    @GetMapping(POTENTIAL_CUSTOMER.REFERRED_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerReferredDetailDto getReferredCustomer(@PathVariable("id") Long id) {
        return potentialCustomerReferService.get(id, null);
    }

    /**
     * List of customers
     */
    @GetMapping(POTENTIAL_CUSTOMER.SEARCH)
    @ResponseStatus(HttpStatus.OK)
    public Page<SearchPotentialCustomerRes> searchPotentialCustomer(
            @SortDefaults({ @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return potentialCustomerService.adminSearch(pageable, query, from, to);
    }

    @GetMapping(POTENTIAL_CUSTOMER.EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportPotentialCustomer(
            @SortDefaults({ @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "type") ExportType type, HttpServletResponse response) {
        potentialCustomerService.adminExport(pageable, query, from, to, type, response);
    }

    /**
     * Get customer detail by id
     */
    @GetMapping(POTENTIAL_CUSTOMER.DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDto getPotentialCustomer(@PathVariable("id") Long id) {
        return potentialCustomerService.get(id, null);
    }

    /**
     * List of direct customers
     */
    @GetMapping(POTENTIAL_CUSTOMER.SEARCH_DIRECT)
    @ResponseStatus(HttpStatus.OK)
    public Page<SearchDirectPotentialCustomerRes> searchDirectCustomers(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "appStatus", required = false) AppStatus appStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return potentialCustomerDirectService.adminSearch(pageable, query, from, to, appStatus);
    }

    @GetMapping(POTENTIAL_CUSTOMER.EXPORT_DIRECT)
    @ResponseStatus(HttpStatus.OK)
    public void exportPotentialCustomer(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "appStatus", required = false) AppStatus appStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "type") ExportType type, HttpServletResponse response) {
        potentialCustomerDirectService.adminExport(pageable, query, from, to, appStatus, type, response);
    }

    /**
     * List of direct customers by customer
     */
    @GetMapping(POTENTIAL_CUSTOMER.SEARCH_DIRECT_BY_CUSTOMER)
    @ResponseStatus(HttpStatus.OK)
    public Page<SearchDirectPotentialCustomerRes> searchDirectCustomers(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @PathVariable("id") Long potentialCustomerId) {
        return potentialCustomerDirectService.searchByPotentialCustomer(pageable, potentialCustomerId);
    }

    /**
     * Get direct customer detail by id
     */
    @GetMapping(POTENTIAL_CUSTOMER.DIRECT_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDirectDetailDto getDirectCustomer(@PathVariable("id") Long id) {
        return potentialCustomerDirectService.get(id, null);
    }

    /**
     * Get direct customer detail by id
     */
    @PostMapping(POTENTIAL_CUSTOMER.DIRECT_SUBMIT)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDirectSubmitResp submitDirectCustomer(@PathVariable("id") Long id) {
        return potentialCustomerDirectService.submit(id);
    }

}
