package com.stg.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.stg.common.Endpoints.QUOTATION;
import com.stg.controller.aspect.RequestLog;
import com.stg.service.FlexQuotationService;
import com.stg.service.dto.quotation.QuotationSyncDataDto;
import com.stg.service.dto.quotation.QuotationSyncDataResp;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class QuotationSyncController {

    private final FlexQuotationService flexCreateProcess;

    @PostMapping(QUOTATION.SYNC_DATA)
    @RequestLog
    public ResponseEntity<QuotationSyncDataResp> syncQuotation(@Valid @RequestBody QuotationSyncDataDto reqDto) {
        return ResponseEntity.ok(flexCreateProcess.syncQuotation(reqDto));
    }

}
