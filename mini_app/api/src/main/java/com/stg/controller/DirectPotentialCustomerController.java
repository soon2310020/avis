package com.stg.controller;

import java.time.LocalDate;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stg.common.Endpoints.POTENTIAL_CUSTOMER;
import com.stg.service.PotentialCustomerDirectService;
import com.stg.service.dto.DeleteIdsReq;
import com.stg.service.dto.potentialcustomer.CreateBmhnReq;
import com.stg.service.dto.potentialcustomer.CreateBmhnRes;
import com.stg.service.dto.potentialcustomer.InitDirectReq;
import com.stg.service.dto.potentialcustomer.InitDirectRes;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectDetailDto;
import com.stg.service.dto.potentialcustomer.SearchDirectPotentialCustomerRes;
import com.stg.service.dto.quotation.QuotationDto;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = "Potential Direct Customer")
public class DirectPotentialCustomerController {

    private final PotentialCustomerDirectService potentialCustomerDirectService;

    @PostMapping(POTENTIAL_CUSTOMER.INIT_DIRECT)
    @ResponseStatus(HttpStatus.OK)
    public InitDirectRes initDirect(@Valid @RequestBody InitDirectReq request) {
       return potentialCustomerDirectService.create(request);
    }

    @PostMapping(POTENTIAL_CUSTOMER.URL + "/create-bmhn")
    @ResponseStatus(HttpStatus.OK)
    public CreateBmhnRes createBmhn(@Valid @RequestBody CreateBmhnReq request) {
        return potentialCustomerDirectService.createBmhn(request);
    }

    @GetMapping(POTENTIAL_CUSTOMER.SEARCH_DIRECT)
    @ResponseStatus(HttpStatus.OK)
    public Page<SearchDirectPotentialCustomerRes> searchDirectPotentialCustomer(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return potentialCustomerDirectService.search(pageable, query, null, from, to);
    }

    @PostMapping(POTENTIAL_CUSTOMER.DELETE_DIRECT)
    @ResponseStatus(HttpStatus.OK)
    public void deleteDirectPotentialCustomer(@RequestBody @Valid DeleteIdsReq request) {
        potentialCustomerDirectService.delete(request);
    }

    @GetMapping(POTENTIAL_CUSTOMER.DIRECT_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDirectDetailDto getDirect(@PathVariable("id") Long id) {
        return potentialCustomerDirectService.get(id);
    }

    /**
     * Generate quotation example for direct
     */
    @GetMapping(POTENTIAL_CUSTOMER.DIRECT_QUOTATION_EXAMPLE)
    @ResponseStatus(HttpStatus.OK)
    public QuotationDto genQuotationExample(@PathVariable("id") Long id) {
        return potentialCustomerDirectService.generateQuotation(id);
    }

}
