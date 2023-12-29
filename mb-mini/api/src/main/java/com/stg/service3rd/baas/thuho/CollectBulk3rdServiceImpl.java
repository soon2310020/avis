package com.stg.service3rd.baas.thuho;

import com.stg.service3rd.baas.adapter.BaasApiCaller;
import com.stg.service3rd.baas.adapter.BaasFunctions;
import com.stg.service3rd.baas.dto.req.CollectBulkPaymentReq;
import com.stg.service3rd.baas.dto.req.RegisterAutoDebitReq;
import com.stg.service3rd.baas.dto.resp.CollectBulkPaymentResp;
import com.stg.service3rd.baas.dto.resp.QueryBulkPaymentResp;
import com.stg.service3rd.baas.dto.resp.RegisterAutoDebitResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectBulk3rdServiceImpl implements CollectBulk3rdService {
    private final BaasApiCaller baasApiCaller;

    /* Đăng ký thu hộ non-otp */
    @Override
    public RegisterAutoDebitResp registerAutoDebitNonOTP(RegisterAutoDebitReq request) throws Exception {
        return baasApiCaller.post(BaasFunctions.REGISTER_AUTO_DEBIT_NON_OTP, baasApiCaller.getAuthHeader(), request, RegisterAutoDebitResp.class);
    }

    /* Thu hộ theo lô */
    @Override
    public CollectBulkPaymentResp collectBulkPayments(CollectBulkPaymentReq request) throws Exception {
        HttpHeaders headers = baasApiCaller.getAuthHeader();
        headers.set("transactionId", "todo...");
        headers.set("signature", "todo...");

        return baasApiCaller.post(BaasFunctions.COLLECT_BULK_PAYMENTS, headers, request, CollectBulkPaymentResp.class);
    }

    /* Truy vấn thông tin lô:
     * page >0
     * 0<limit<=1000
     * */
    @Override
    public QueryBulkPaymentResp searchBulkPaymentInfo(String bulkId, Integer page, Integer limit) throws Exception {
        if (page == null) page = 1;
        if (limit == null) limit = 10;

        return baasApiCaller.get(BaasFunctions.SEARCH_BULK_PAYMENT_INFO, baasApiCaller.getAuthHeader(),
                QueryBulkPaymentResp.class,
                Map.of(
                        "bulkId", bulkId,
                        "page", page,
                        "limit", limit
                ));
    }
}
