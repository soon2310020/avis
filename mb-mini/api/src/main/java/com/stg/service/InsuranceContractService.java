package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsuranceContractDto;
import com.stg.service.dto.insurance.InsuranceContractListDto;
import com.stg.service.dto.insurance.InsuranceContractThirdPartyListDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface InsuranceContractService {

    PaginationResponse<InsuranceContractListDto> list(Long user, int page, int size, String query, String segment, String packageName, String category);

    InsuranceContractDto contractDetail(Long userId, Long contractId);

    void exportList(Long idUser, String query, String type, String segment, String packageName, String category, HttpServletResponse response)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    PaginationResponse<InsuranceContractThirdPartyListDto> listThirdParty(Long user, int page, int size, String query, String source);

    void exportListThirdParty(Long idUser, String query, String source, String type, HttpServletResponse response)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    InsuranceContractDto contractThirdPartyDetail(Long userId, Long contractId);

    InsuranceContractDto buildDetailContract(InsuranceContractDto contractDto, Long customerId, Long requestId);

    void listContractRenewal();
}
