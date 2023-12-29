package com.stg.service3rd.mbal;

import com.stg.service.dto.mbal.GenerateQuotation;
import com.stg.service.dto.mbal.QuotationModel;
import com.stg.service3rd.mbal.adapter.MbalFunctions;
import com.stg.service3rd.mbal.dto.req.*;
import com.stg.service3rd.mbal.dto.resp.*;
import com.stg.service3rd.mbal.dto.FlexibleCreateQuoteModel;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
public interface MbalApi3rdService {
    List<OccupationResp> getOccupations();

    ICInfoResp getIcData(String icCode);

    FlexibleRespModel flexCreateProcess(FlexibleProcessReq request) throws Exception;

    FlexibleCreateQuoteModel flexCreateQuote(FlexibleQuoteReq request) throws Exception;
    FlexibleRespModel flexConfirmQuote(long processId) throws Exception;

    QuotationModel generateQuotation(MbalFunctions functions, GenerateQuotation request) throws Exception;

    byte[] flexDownloadQuotationPdf(Long processId) throws Exception;

    byte[] downloadQuotationPdf(MbalFunctions functions, Long quotationId, Long submissionId) throws Exception;
    
    ICDataResp getIcByRmCode(String rmCode);
    
    List<ICDataResp> searchIcByBranchCode(String branchCode);
    
    List<ICDataResp> searchIcByIcCode(String icCode);
    
    SubmitPotentialCustomerResp submitPotentialCustomer(@Valid SubmitPotentialCustomerReq body) throws Exception;

    FlexibleCheckTSAResp checkTSA(FlexibleCheckTSAReq checkTSAReq) throws Exception;
}
