package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.InsurancePaymentService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsurancePaymentDetailDTO;
import com.stg.service.dto.insurance.InsurancePaymentDto;
import com.stg.service.dto.insurance.InsurancePaymentListDTO;
import com.stg.service.dto.insurance.UpdateControlStateReqDto;
import com.stg.utils.Endpoints;
import com.stg.utils.excel.ExportType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "InsurancePayment Apis")
public class InsurancePaymentController {

    private final InsurancePaymentService insurancePaymentService;

    @GetMapping(Endpoints.URL_INSURANCE_PAYMENT_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<InsurancePaymentListDTO> insurancePayments(@AuthenticationPrincipal CustomUserDetails user,
                                                                         @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                         @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                                         @RequestParam(name = "query", required = false) String query,
                                                                         @RequestParam(name = "dateFrom", required = false, defaultValue = "") String dateFrom,
                                                                         @RequestParam(name = "dateTo", required = false, defaultValue = "") String dateTo,
                                                                         @RequestParam(name = "status", required = false, defaultValue = "") String status,
                                                                         @RequestParam(name = "micStatus", required = false, defaultValue = "") String micStatus,
                                                                         @RequestParam(name = "mbalStatus", required = false, defaultValue = "") String mbalStatus,
                                                                         @RequestParam(name = "controlState", required = false, defaultValue = "") String controlState,
                                                                         @RequestParam(name = "category", defaultValue = "", required = false) String category,
                                                                         @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                                                         @RequestParam(name = "installmentStatus", defaultValue = "", required = false) String installmentStatus,
                                                                         @RequestParam(name = "paymentType", defaultValue = "", required = false) String paymentType,
                                                                         @RequestParam(name = "autoPayStatus", required = false) Boolean autoPayStatus) {
        return insurancePaymentService.list(Long.valueOf(user.getUserId()), page, size, query, dateFrom, dateTo, status, micStatus, mbalStatus, controlState, category, packageName, installmentStatus, paymentType, autoPayStatus == null ? "" : Boolean.toString(autoPayStatus));
    }

    @GetMapping(Endpoints.URL_INSURANCE_PAYMENT_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public InsurancePaymentDetailDTO insuranceInfo(@AuthenticationPrincipal CustomUserDetails user,
                                                  @Valid @PathVariable("id") Long paymentId) {
        return insurancePaymentService.paymentDetail(Long.valueOf(user.getUserId()), paymentId);
    }

    @GetMapping(Endpoints.URL_INSURANCE_PAYMENT_LIST_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportList(@AuthenticationPrincipal CustomUserDetails user,
                           @RequestParam(name = "query", required = false) String query,
                           @RequestParam(name = "type") String type,
                           @RequestParam(name = "dateFrom", required = false, defaultValue = "") String dateFrom,
                           @RequestParam(name = "dateTo", required = false, defaultValue = "") String dateTo,
                           @RequestParam(name = "status", required = false, defaultValue = "") String status,
                           @RequestParam(name = "micStatus", required = false, defaultValue = "") String micStatus,
                           @RequestParam(name = "mbalStatus", required = false, defaultValue = "") String mbalStatus,
                           @RequestParam(name = "controlState", required = false, defaultValue = "") String controlState,
                           @RequestParam(name = "category", defaultValue = "", required = false) String category,
                           @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                           @RequestParam(name = "installmentStatus", defaultValue = "", required = false) String installmentStatus,
                           @RequestParam(name = "paymentType", defaultValue = "", required = false) String paymentType,
                           //@RequestParam(name = "mbalAppNo", defaultValue = "", required = false) String mbalAppNo,
                           @RequestParam(name = "autoPayStatus", required = false) Boolean autoPayStatus,
                           HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        insurancePaymentService.exportList(Long.valueOf(user.getUserId()), type, query, dateFrom, dateTo, status, micStatus, mbalStatus, controlState, category, response, packageName, installmentStatus, paymentType, autoPayStatus == null ? "" : Boolean.toString(autoPayStatus));
    }

    @PutMapping(Endpoints.URL_UPDATE_CONTROL_STATE_INSURANCE_PAYMENT)
    @ResponseStatus(HttpStatus.OK)
    public InsurancePaymentDto updateControlState(@AuthenticationPrincipal CustomUserDetails user,
                                                  @Valid @PathVariable("id") Long paymentId,
                                                  @Valid @RequestBody UpdateControlStateReqDto updateControlStateReqDto) {
        return insurancePaymentService.updateControlState(Long.valueOf(user.getUserId()), paymentId, updateControlStateReqDto);
    }

    @GetMapping(Endpoints.URL_INSURANCE_PAYMENT_WAITING_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<InsurancePaymentDto> listWaitingPayment(@AuthenticationPrincipal CustomUserDetails user,
                                                                      @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                                                      @RequestParam(name = "dateFrom", required = false, defaultValue = "") String dateFrom,
                                                                      @RequestParam(name = "dateTo", required = false, defaultValue = "") String dateTo,
                                                                      @RequestParam(name = "status", required = false, defaultValue = "") String status,
                                                                      @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                      @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        return insurancePaymentService.listWaitingPayment(query, dateFrom, dateTo, status, page, size);
    }

    @GetMapping(Endpoints.URL_INSURANCE_PAYMENT_WAITING_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public InsurancePaymentDto detailWaitingPayment(@AuthenticationPrincipal CustomUserDetails user,
                                                  @PathVariable(name = "id") Long paymentId) {
        return insurancePaymentService.detailWaitingPayment(paymentId);
    }

    @GetMapping(Endpoints.URL_INSURANCE_PAYMENT_WAITING_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportWaitingPayment(@AuthenticationPrincipal CustomUserDetails user,
                                     @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                     @RequestParam(name = "dateFrom", required = false, defaultValue = "") String dateFrom,
                                     @RequestParam(name = "dateTo", required = false, defaultValue = "") String dateTo,
                                     @RequestParam(name = "status", required = false, defaultValue = "") String status,
                                     @RequestParam(name = "type") ExportType type,
                                     HttpServletResponse response) {
        insurancePaymentService.exportWaitingPayment(type, query, dateFrom, dateTo, status, response);
    }
}
