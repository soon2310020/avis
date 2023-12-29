package com.stg.controller;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stg.common.Endpoints.POTENTIAL_CUSTOMER;
import com.stg.constant.ComboCode;
import com.stg.service.PotentialCustomerReferService;
import com.stg.service.PotentialCustomerService;
import com.stg.service.dto.DeleteIdsReq;
import com.stg.service.dto.combo.UserComboSuggestionResp;
import com.stg.service.dto.potentialcustomer.CreatePotentialCustomerReq;
import com.stg.service.dto.potentialcustomer.FlowReq;
import com.stg.service.dto.potentialcustomer.FlowResp;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredHeaderDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredReq;
import com.stg.service.dto.potentialcustomer.SearchPotentialCustomerRes;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = "Potential Customer")
public class PotentialCustomerController {

    private final PotentialCustomerService potentialCustomerService;

    private final PotentialCustomerReferService potentialCustomerReferService;

    /**
     * Refer customer to sale
     */
	@PostMapping(POTENTIAL_CUSTOMER.REFER_SALE)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerReferredDetailDto referCustomerToSale(@PathVariable("id") Long id, @RequestBody @Valid PotentialCustomerReferredReq req) {
	    return potentialCustomerReferService.referToSale(id, req);
    }

	/**
	 * List of referred customers
	 */
    @GetMapping(POTENTIAL_CUSTOMER.SEARCH_REFERRED)
    @ResponseStatus(HttpStatus.OK)
    public Page<PotentialCustomerReferredHeaderDto> searchReferredCustomers(@SortDefaults({
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
            @SortDefault(sort = "potentialCustomer.fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return potentialCustomerReferService.search(pageable, query, date, from, to);
    }

    /**
     * Get referred customer detail by id
     */
    @GetMapping(POTENTIAL_CUSTOMER.REFERRED_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerReferredDetailDto getReferredCustomer(@PathVariable("id") Long id) {
        return potentialCustomerReferService.get(id);
    }

    /**
     * Delete referred customers by ids
     */
    @PostMapping(POTENTIAL_CUSTOMER.DELETE_REFERRED)
    @ResponseStatus(HttpStatus.OK)
    public void deleteReferredCustomer(@RequestBody @Valid DeleteIdsReq req) {
        potentialCustomerReferService.delete(req);
    }

    /**
     * Check allowed flow for customer
     */
    @PostMapping(POTENTIAL_CUSTOMER.FLOW)
    @ResponseStatus(HttpStatus.OK)
    public FlowResp checkReferOrDirect(@PathVariable("id") Long id, @RequestBody @Valid FlowReq req) {
        return potentialCustomerService.checkReferOrDirect(id, req);
    }

    @GetMapping(POTENTIAL_CUSTOMER.SEARCH)
    @ResponseStatus(HttpStatus.OK)
    public Page<SearchPotentialCustomerRes> searchPotentialCustomer(
            @SortDefaults({ @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "fullName", direction = Sort.Direction.ASC) }) Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return potentialCustomerService.search(pageable, query, null, from, to);
    }

    @PostMapping(POTENTIAL_CUSTOMER.URL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDto createPotentialCustomer(@RequestBody @Valid CreatePotentialCustomerReq request) {
        log.info("createPotentialCustomer");
        return potentialCustomerService.create(request);
    }

    @PutMapping(POTENTIAL_CUSTOMER.URL)
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDto updatePotentialCustomer(@RequestBody @Valid CreatePotentialCustomerReq request) {
        log.info("updatePotentialCustomer");
        return potentialCustomerService.update(request);
    }

    @PostMapping(POTENTIAL_CUSTOMER.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePotentialCustomer(@RequestBody @Valid DeleteIdsReq request) {
        potentialCustomerService.delete(request);
    }

    @GetMapping(POTENTIAL_CUSTOMER.URL + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PotentialCustomerDto getPotentialCustomer(@PathVariable("id") Long id) {
        return potentialCustomerService.get(id);
    }

    @GetMapping(POTENTIAL_CUSTOMER.COMBO_SUGGEST)
    @ResponseStatus(HttpStatus.OK)
    public List<UserComboSuggestionResp> suggestCombos(@PathVariable("id") Long id, @RequestParam(name = "combo") List<ComboCode> comboCodes) {
        return potentialCustomerService.suggestCombos(id, comboCodes);
    }

}
