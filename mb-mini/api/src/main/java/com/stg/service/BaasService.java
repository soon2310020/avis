package com.stg.service;

import com.stg.adapter.dto.CommonAdapterResponse;
import com.stg.entity.AdditionalProduct;
import com.stg.entity.InsurancePayment;
import com.stg.service.dto.baas.BaasPayOnBehalfRespDto;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service.dto.external.RmInfoResp;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface BaasService {

    void payOnBehalfProcess(String accessToken, String amount, String type, String gcnbh,
                            String appNoId, String idGiaoDich, String ullVersion, String cif,
                            String fundingSource, String cardType, String createOrderId, String hookTypeCode);

    OauthToken generateToken();

    RmInfoResp getCrmData(String rmCode);

    BaasPayOnBehalfRespDto micManualPayOnBehalf(String idGiaoDich);

    BaasPayOnBehalfRespDto mbalManualPayOnBehalf(String idGiaoDich);

    void checkPermission(Long userId);

    void flexiblePayOnBehalfProcess(String cif, String accessToken, String mbTransactionId, InsurancePayment insurancePayment,  List<AdditionalProduct> micAdditionalProducts);

    <P> CommonAdapterResponse callGetCardPartnerAdapter(String urlTemplate, Map<String, String> params, HttpMethod method, HttpHeaders headers, P param);

    OauthToken generateTokenGetListCard();

    <P> CommonAdapterResponse getListCard(String urlTemplate, Map<String, String> params, HttpMethod method, HttpHeaders headers, P param);
}
