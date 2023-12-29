package com.stg.service;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.MbEmployee;
import com.stg.entity.MbEmployeeImportHis;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.mb_employee.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface MbEmployeeService {
    void importMbEmployeeData(MultipartFile multipartFile, CustomUserDetails customerDetail);

    PaginationResponse<? extends IMbEmployeeDataDTO> listEmployees(int size, int page, String query, MbEmployeeSearchType searchType,MbManagerUnit mbManagerUnit);

    MbEmployeeDTO employeeDetail(String mbId);

    PaginationResponse<MbEmployeeHIsDTO> listEmployeeHis(int size, int page);

    void saveImportHistory(MbEmployeeImportHis mbEmployeeImportHis);

    MbEmployeeDTO editMbEmployee(MbEmployeeReq req, String id);

    CompletableFuture<MbImportThreadResult> saveOrUpdateEmployees(Collection<MbEmployee> employees);
    void updateManagingUnitForCustomers(Collection<MbEmployee> employees);
}
