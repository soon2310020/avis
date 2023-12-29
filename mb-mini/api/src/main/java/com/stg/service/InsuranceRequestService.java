package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsuranceRequestDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface InsuranceRequestService {

    PaginationResponse<InsuranceRequestDto> list(Long user, int page, int size, String query, String segment, Boolean status, String category, String packageName);

    InsuranceRequestDto insuranceRequestDetail(Long userId, Long contractId);

    void exportList(Long user, String query, String type, String segment, HttpServletResponse response, Boolean status, String category, String packageName) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

}
