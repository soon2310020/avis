package com.stg.service3rd.hcm;

import com.stg.service3rd.common.adapter.func.Function;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.hcm.adapter.HcmApiCaller;
import com.stg.service3rd.hcm.adapter.HcmAuthApiCaller;
import com.stg.service3rd.hcm.adapter.HcmFunctions;
import com.stg.service3rd.hcm.dto.error.HCMErrorObject;
import com.stg.service3rd.hcm.dto.resp.HcmEmployeeRes;
import com.stg.service3rd.hcm.exception.HcmApiRequestException;
import com.stg.utils.Common;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_MESSAGE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HcmApi3rdServiceImpl implements HcmApi3rdService {

    private final HcmApiCaller hcmApiCaller;

    private final HcmAuthApiCaller hcmAuthApiCaller;

    @Value("${api3rd.hcm.system}")
    private String systemCode;

    @Override
    public HcmEmployeeRes findEmployeeFromHcm(String identityNumb) {
        try {
            HttpHeaders header = hcmAuthApiCaller.getAuthHeader();
            header.setAccept(List.of(MediaType.APPLICATION_JSON));
            String clientMessageID = Common.generateUUIDId(10);
            header.set(CLIENT_MESSAGE_ID, clientMessageID);
            IFunction hcmUrl = Function.map(HcmFunctions.GET_EMPLOYEE_LIST, Map.of(
                    "ccid", identityNumb,
                    "system", systemCode
            ));
            return hcmApiCaller.get(hcmUrl, header, HcmEmployeeRes.class);
        } catch (Exception ex) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(ex, HCMErrorObject.class);
            throw new HcmApiRequestException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public boolean isEmployeeFromHcm(String identityNumb) {
        HcmEmployeeRes reps = this.findEmployeeFromHcm(identityNumb);
        return reps != null && reps.hasData();
    }
}
