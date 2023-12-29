package com.stg.service3rd.mbal;

import com.stg.errors.dto.ErrorDto;
import com.stg.service.dto.mbal.GenerateQuotation;
import com.stg.service.dto.mbal.QuotationModel;
import com.stg.service3rd.common.adapter.func.Function;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.adapter.MbalApiCaller;
import com.stg.service3rd.mbal.adapter.MbalFunctions;
import com.stg.service3rd.mbal.dto.FlexibleCreateQuoteModel;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mbal.dto.req.*;
import com.stg.service3rd.mbal.dto.resp.*;
import com.stg.service3rd.mbal.exception.MbalApi3rdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stg.config.redis.CacheConfiguration.RAM_CACHING;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Slf4j
@Service
@RequiredArgsConstructor
public class MbalApi3rdServiceImpl implements MbalApi3rdService {
    private final MbalApiCaller mbalConnector;

    /*@Value("${api3rd.mbal.refer-direct.client_id}")
    private String referDirectClientId;
    @Value("${api3rd.mbal.refer-direct.client_secret}")
    private String referDirectClientSecret;*/

    @Override
    @Cacheable(value = "cache:occupations", unless = "#result.size()==0", cacheManager = RAM_CACHING)
    public List<OccupationResp> getOccupations() {
        try {
            OccupationResp[] resp = mbalConnector.get(MbalFunctions.GET_OCCUPATIONS, mbalConnector.getAuthHeader(), OccupationResp[].class);

            if (resp == null) return List.of();
            return List.of(resp);
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new MbalApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    @Cacheable(value = "cache:icInfo")
    public ICInfoResp getIcData(String icCode) {
        try {
            return mbalConnector.get(MbalFunctions.GET_IC, mbalConnector.getAuthHeader(), ICInfoResp.class, Map.of("code", icCode));
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new MbalApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public FlexibleRespModel flexCreateProcess(FlexibleProcessReq request) throws Exception {
        return mbalConnector.post(MbalFunctions.FLEX_CREATE_PROCESSES, request, FlexibleRespModel.class);
    }

    @Override
    public FlexibleCreateQuoteModel flexCreateQuote(FlexibleQuoteReq request) throws Exception {
        IFunction reqUri = Function.map(MbalFunctions.FLEX_CREATE_QUOTE, Map.of("processId", request.getProcessId()));

        return mbalConnector.post(reqUri, request, FlexibleCreateQuoteModel.class);
    }

    @Override
    public FlexibleRespModel flexConfirmQuote(long processId) throws Exception {
        IFunction reqUri = Function.map(MbalFunctions.FLEX_CONFIRM_QUOTE, Map.of("processId", processId));

        return mbalConnector.post(reqUri, null, FlexibleRespModel.class);
    }

    @Override
    public byte[] flexDownloadQuotationPdf(Long processId) throws Exception {
        IFunction reqUri = Function.map(MbalFunctions.DOWNLOAD_QUOTE_ULRP_3_0_PDF, Map.of("processId", processId));
        return mbalConnector.get(reqUri, byte[].class);
    }

    /**
     * GEN_QUOTE_ULSP("/api/v1/ulsp/quotation"),
     * GEN_QUOTE_ULRP("/api/v1/ulrp/quotation"),
     * GEN_QUOTE_ULRP_3_0("/api/v1/ulrp30/quotation"),
     */
    @Override
    public QuotationModel generateQuotation(MbalFunctions functions, GenerateQuotation request) throws Exception {
        return mbalConnector.post(functions, request, QuotationModel.class);
    }

    /**
     * DOWNLOAD_QUOTE_ULSP("/api/v1/ulsp/quotations/completed-pdf"),
     * DOWNLOAD_QUOTE_ULRP("/api/v1/ulrp/quotations/completed-pdf"),
     * DOWNLOAD_QUOTE_ULRP_3_0("/api/v1/ulrp30/quotations/completed-pdf"),
     */
    @Override
    public byte[] downloadQuotationPdf(MbalFunctions functions, Long quotationId, Long submissionId) throws Exception {
        return mbalConnector.get(functions, byte[].class,
                Map.of("quotationId", quotationId, "submissionId", submissionId)
        );
    }

	@Override
	public List<ICDataResp> searchIcByBranchCode(String branchCode) {
        // Fix me
        return List.of();
	}

	@Override
	public ICDataResp getIcByRmCode(String rmCode) {
        // Fix me
        ICDataResp ic = new ICDataResp();
        return ic;
	}

    /**
     * Remove @Valid => log into third_party_log
     * */
    @Override
    public SubmitPotentialCustomerResp submitPotentialCustomer(SubmitPotentialCustomerReq body) throws Exception {
        /*HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(referDirectClientId, referDirectClientSecret);
        headers.setContentType(APPLICATION_JSON);*/
        return mbalConnector.post(MbalFunctions.SUBMIT_POTENTIAL_CUSTOMER, body, SubmitPotentialCustomerResp.class);
    }

    @Override
    public FlexibleCheckTSAResp checkTSA(FlexibleCheckTSAReq request) throws Exception {
        return mbalConnector.post(MbalFunctions.FLEX_CHECK_TSA, request, FlexibleCheckTSAResp.class);
    }

    @Override
    public List<ICDataResp> searchIcByIcCode(String icCode) {
        // Fix me
        return List.of();
    }

}
