package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsurancePackageDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface InsurancePackageService {

    PaginationResponse<InsurancePackageDto> list(Long user, int page, int size, String query, String category, String packageName);

    InsurancePackageDto insuranceDetail(Long id, Integer packageId);

    void exportList(Long idUser, String query, String type, String category, HttpServletResponse response, String packageName) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

}
