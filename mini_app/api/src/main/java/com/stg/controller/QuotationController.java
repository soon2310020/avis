package com.stg.controller;

import java.time.LocalDate;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.stg.common.Endpoints.QUOTATION;
import com.stg.config.security.auth.AccessToken;
import com.stg.constant.QuotationType;
import com.stg.service.ComboSearchService;
import com.stg.service.CrmAuthenticationService;
import com.stg.service.FlexQuotationService;
import com.stg.service.QuotationService;
import com.stg.service.dto.combo.DeleteComboRequest;
import com.stg.service.dto.mbal.ConfirmQuoteFlexibleReqDto;
import com.stg.service.dto.quotation.GenQRCodeResp;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service.dto.quotation.QuotationSearchDto;
import com.stg.service.dto.quotation.UpdateQuoteAndGenQRCodeReq;
import com.stg.service3rd.mbal.dto.req.FlexibleProcessReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleRespModel;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = "Bang minh hoa")
public class QuotationController {
    private final QuotationService quotationService;
    private final FlexQuotationService flexCreateProcess;
    private final CrmAuthenticationService crmAuthenticationService;
    private final ComboSearchService comboSearchService;

    @PostMapping(QUOTATION.CREATE_PROCESS)
    @ResponseStatus(HttpStatus.OK)
    public FlexibleRespModel flexCreateProcess(@Valid @RequestBody FlexibleProcessReq reqDto) {
        return flexCreateProcess.flexCreateProcess(reqDto);
    }

    @PostMapping(QUOTATION.URL)
    @ResponseStatus(HttpStatus.OK)
    public QuotationDto createQuotation(@Valid @RequestBody QuotationHeaderDto reqDto) {
        return quotationService.createQuotation(reqDto);
    }

    @PostMapping(QUOTATION.CONFIRM)
    @ResponseStatus(HttpStatus.OK)
    public QuotationDto flexConfirmQuotation(@Valid @RequestBody ConfirmQuoteFlexibleReqDto reqDto) {
        return flexCreateProcess.flexConfirmQuote(reqDto);
    }

    @PostMapping(QUOTATION.UPDATE_AND_GEN_QRCODE)
    @ResponseStatus(HttpStatus.OK)
    public GenQRCodeResp flexUpdateAndGenQRCode(@Valid @RequestBody UpdateQuoteAndGenQRCodeReq reqDto) {
        return flexCreateProcess.updateAndGenQRCode(reqDto);
    }

    @GetMapping(value = QUOTATION.FIND_QUOTE_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public QuotationDto findQuotationDetail(@RequestParam(value = "uuid") UUID uuid) {
        return flexCreateProcess.findQuotationDetail(uuid);
    }

    /*===============================================================================================================*/

    @GetMapping(QUOTATION.URL + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QuotationDto findQuotation(@PathVariable("id") Long id) {
        return quotationService.findQuotation(id);
    }

    @GetMapping(QUOTATION.URL + "/search")
    @ResponseStatus(HttpStatus.OK)
    public Page<QuotationSearchDto> searchQuotation(Pageable pageable,
                                                    @RequestParam(name = "q", required = false) String query,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                    @RequestParam(name = "type", required = false) String quotationType) {
        if (QuotationType.COMBO.toString().equals(quotationType)) {
            log.info("QuotationType.COMBO");
            return comboSearchService.searchCombo(pageable, query, from, to);
        }
        return quotationService.search(pageable, query, date, from, to);
    }

    @PostMapping(QUOTATION.URL + "/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteQuotation(@RequestBody DeleteComboRequest request) {
        if (QuotationType.COMBO.toString().equals(request.getType())) {
            comboSearchService.deleteCombo(request.getIds());
        } else {
            quotationService.deleteQuotation(request.getIds());
        }
    }

    @GetMapping(QUOTATION.URL + "/{id}/download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> downloadPDF(@PathVariable("id") Long id) {
        return quotationService.downloadPDF(id, null);
    }

    @GetMapping(QUOTATION.URL + "/{id}/crm-download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> downloadPDFByCrm(@PathVariable("id") Long id, @RequestParam String loginToken) {
        AccessToken accessToken = crmAuthenticationService.verifyToken(loginToken);
        return quotationService.downloadPDF(id, accessToken.getUsername());
    }

}
