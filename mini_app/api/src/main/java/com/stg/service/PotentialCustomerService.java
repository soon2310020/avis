package com.stg.service;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stg.constant.ComboCode;
import com.stg.service.dto.combo.UserComboSuggestionResp;
import com.stg.service.dto.potentialcustomer.CreatePotentialCustomerReq;
import com.stg.service.dto.potentialcustomer.FlowReq;
import com.stg.service.dto.potentialcustomer.FlowResp;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDto;
import com.stg.service.dto.potentialcustomer.SearchPotentialCustomerRes;
import com.stg.utils.excel.ExportType;

public interface PotentialCustomerService
        extends SearchService<SearchPotentialCustomerRes>, DetailService<PotentialCustomerDto>,
        DeleteService, CreateOrUpdateService<CreatePotentialCustomerReq, PotentialCustomerDto> {

    FlowResp checkReferOrDirect(Long id, FlowReq req);

    List<UserComboSuggestionResp> suggestCombos(Long id, List<ComboCode> comboCodes);

    Page<SearchPotentialCustomerRes> adminSearch(Pageable pageable, String query, LocalDate from, LocalDate to);

    void adminExport(Pageable pageable, String query, LocalDate from, LocalDate to, ExportType type,
            HttpServletResponse response);
    
}
