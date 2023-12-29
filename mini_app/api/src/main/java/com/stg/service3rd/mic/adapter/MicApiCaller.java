package com.stg.service3rd.mic.adapter;

import com.stg.service3rd.common.Constants;
import com.stg.service3rd.common.adapter.Api3rdCaller;
import com.stg.service3rd.common.adapter.IApi3rdMethods;
import com.stg.service3rd.common.dto.Host3rd;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static com.stg.service3rd.mic.utils.MicUtil.NV_CODE;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_1;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class MicApiCaller extends Api3rdCaller implements IApi3rdMethods {
    private final MicProperties micProperties;

    public MicApiCaller(MicProperties micProperties) {
        super(new Host3rd(micProperties.getHost(), Constants.HostParty.MIC));
        this.micProperties = micProperties;
    }


    @Override
    public HttpHeaders getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    /**
     * Generate MicChecksum
     */
    public String generateMicChecksum(String transactionId) {
        String data = micProperties.getMerchantSecret() + micProperties.getDviCode() + NV_CODE;
        if (transactionId != null) {
            data = micProperties.getMerchantSecret() + micProperties.getDviCode() + NV_CODE + transactionId;
        }
        byte[] hmac = new HmacUtils(HMAC_SHA_1, micProperties.getMerchantSecret()).hmac(data);
        String encodedString = Base64.getEncoder().encodeToString(hmac);
        return encodedString.replace("=", "%3d").replace(" ", "+");
    }
}
