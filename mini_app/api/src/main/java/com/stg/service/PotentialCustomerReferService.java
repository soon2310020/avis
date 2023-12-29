package com.stg.service;

import java.time.LocalDate;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredHeaderDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredReq;
import com.stg.utils.excel.ExportType;

public interface PotentialCustomerReferService extends SearchService<PotentialCustomerReferredHeaderDto>,
        DetailService<PotentialCustomerReferredDetailDto>, DeleteService {

    PotentialCustomerReferredDetailDto referToSale(Long customerId, PotentialCustomerReferredReq req);

    Page<PotentialCustomerReferredHeaderDto> searchByPotentialCustomer(Pageable pageable, Long potentialCustomerId);

    Page<PotentialCustomerReferredHeaderDto> adminSearch(Pageable pageable, String query, LocalDate from, LocalDate to,
            AppStatus appStatus, String leadStatus);

    void adminExport(Pageable pageable, String query, LocalDate from, LocalDate to, AppStatus appStatus, String leadStatus,
            ExportType type, HttpServletResponse response);

}
