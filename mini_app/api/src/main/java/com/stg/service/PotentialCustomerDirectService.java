package com.stg.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.DirectSubmitStatus;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.service.dto.potentialcustomer.CreateBmhnReq;
import com.stg.service.dto.potentialcustomer.CreateBmhnRes;
import com.stg.service.dto.potentialcustomer.InitDirectReq;
import com.stg.service.dto.potentialcustomer.InitDirectRes;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectSubmitResp;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectSubmitStatusReq;
import com.stg.service.dto.potentialcustomer.SearchDirectPotentialCustomerRes;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.utils.excel.ExportType;

public interface PotentialCustomerDirectService
        extends SearchService<SearchDirectPotentialCustomerRes>, DetailService<PotentialCustomerDirectDetailDto>,
        DeleteService, CreateOrUpdateService<InitDirectReq, InitDirectRes> {

    CreateBmhnRes createBmhn(CreateBmhnReq request);

    QuotationDto generateQuotation(Long id);

    PotentialCustomerDirectSubmitResp submit(Long id);

    Future<DirectSubmitStatus> submit(SubmitPotentialCustomerReq request);

    void saveSubmitStatus(PotentialCustomerDirect direct, PotentialCustomerDirectSubmitStatusReq request);

    List<SubmitPotentialCustomerReq> getNextRetry(int page);

    Page<SearchDirectPotentialCustomerRes> searchByPotentialCustomer(Pageable pageable, Long potentialCustomerId);

    Page<SearchDirectPotentialCustomerRes> adminSearch(Pageable pageable, String query, LocalDate from, LocalDate to, AppStatus appStatus);

    void adminExport(Pageable pageable, String query, LocalDate from, LocalDate to, AppStatus appStatus,
            ExportType type, HttpServletResponse response);

}
