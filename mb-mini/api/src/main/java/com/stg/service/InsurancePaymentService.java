package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.*;
import com.stg.utils.excel.ExportType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface InsurancePaymentService {

    PaginationResponse<InsurancePaymentListDTO> list(Long userId, int page, int size, String query, String dateFrom, String dateTo,
                                                     String status, String micStatus, String mbalStatus, String controlState, String category, String packageName,
                                                     String installmentStatus, String paymentType, String autoPayStatus);

    InsurancePaymentDetailDTO paymentDetail(Long userId, Long packageId);

    void exportList(Long userId, String type, String query, String dateFrom, String dateTo,
                    String status, String micStatus, String mbalStatus, String controlState, String category, HttpServletResponse response, String packageName,
                    String installmentStatus, String paymentType, String autoPayStatus) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    InsurancePaymentDto updateControlState(Long userId, Long paymentId, UpdateControlStateReqDto updateControlStateReqDto);

    PaginationResponse<InsurancePaymentDto> listWaitingPayment(String query, String dateFrom, String dateTo, String status, int page, int size);

    InsurancePaymentDto detailWaitingPayment(Long paymentId);

    void exportWaitingPayment(ExportType type, String query, String dateFrom, String dateTo, String status, HttpServletResponse response);

}
