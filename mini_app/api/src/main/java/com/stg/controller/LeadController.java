package com.stg.controller;

import static com.stg.service.dto.quotation.code.ErrorCodeSyncLead.BAD_REQUEST;
import static com.stg.service.dto.quotation.code.ErrorCodeSyncLead.INTERNAL_SERVER_ERROR;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.stg.common.Endpoints.LEAD;
import com.stg.controller.aspect.RequestLog;
import com.stg.errors.ApplicationException;
import com.stg.service.LeadService;
import com.stg.service.dto.SyncResp;
import com.stg.service.dto.lead.LeadSyncDto;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = "Leads")
public class LeadController {

    private final LeadService leadService;

    @PostMapping(LEAD.SYNC)
    @RequestLog
    public ResponseEntity<SyncResp> syncLead(@Valid @RequestBody LeadSyncDto reqDto) {
        leadService.syncLead(reqDto);
        return ResponseEntity.ok(SyncResp.success());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SyncResp> handleException(Exception e) {
        SyncResp syncLeadResp = new SyncResp();

        if (e instanceof ApplicationException) {
            ApplicationException ae = (ApplicationException) e;
            syncLeadResp.setErrorCode(ae.getErrorDto().getErrorCode());
            syncLeadResp.setErrorMessage(ae.getErrorDto().getMessage());
            return ResponseEntity.status(ae.getErrorDto().getHttpStatus()).body(syncLeadResp);
        } else {
            if (e instanceof MethodArgumentNotValidException || e instanceof ConstraintViolationException) {
                syncLeadResp.setErrorCode(BAD_REQUEST.getCode());
                syncLeadResp.setErrorMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(syncLeadResp);
            }

            syncLeadResp.setErrorCode(INTERNAL_SERVER_ERROR.getCode());
            syncLeadResp.setErrorMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(syncLeadResp);
        }
    }

}
