package com.stg.service3rd.mb_card.adapter;

import com.stg.config.jackson.Jackson;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import com.stg.service3rd.mb_card.adapter.property.MbCardProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static com.stg.utils.Constants.HOST_PARTY;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class MbCardApiCaller extends Api3rdCaller implements IApi3rdMethods {
    private final MbCardProperties mbCardProperties;
    @Autowired
    private Jackson jackson;

    @Autowired
    public MbCardApiCaller(MbCardProperties mbCardProperties) {
        super(new Host3rd(mbCardProperties.getHost(), HOST_PARTY.MB_CARD));
        this.mbCardProperties = mbCardProperties;
    }


    /***/
    public HttpHeaders getAuthHeaderHmac(Object payload) {
        HttpHeaders headers = this.getAuthHeader();
        headers.add("hmac", this.generateChecksum(payload));
        return headers;
    }

    /***/
    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.add("user", mbCardProperties.getMerchant());
        return headers;
    }

    private String generateChecksum(Object payload) {
        byte[] hmac = new HmacUtils(HMAC_SHA_256, mbCardProperties.getMerchantSecret())
                .hmac(jackson.toJson(payload));
        return Base64.getEncoder().encodeToString(hmac);
    }
}
