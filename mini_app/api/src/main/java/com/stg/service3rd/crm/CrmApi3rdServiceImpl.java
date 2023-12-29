package com.stg.service3rd.crm;

import com.stg.common.Jackson;
import com.stg.errors.UserNotFoundException;
import com.stg.service.caching.RmExcelDataCaching;
import com.stg.service.dto.quotation.crm.RmExcelInfo;
import com.stg.service3rd.common.adapter.func.Function;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.crm.adapter.CrmApiCaller;
import com.stg.service3rd.crm.adapter.CrmFunctions;
import com.stg.service3rd.crm.dto.resp.RmInfoResp;
import com.stg.service3rd.crm.dto.resp.RmUserDetailResp;
import com.stg.service3rd.crm.dto.resp.RmUserInfo;
import com.stg.service3rd.crm.exception.CrmApi3rdException;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.stg.service3rd.common.Constants.HeaderKey.BEARER;
import static com.stg.service3rd.common.Constants.HeaderKey.CLIENT_MESSAGE_ID;
import static com.stg.service3rd.crm.adapter.CrmApiCaller.AUTHORIZATION;
import static com.stg.service3rd.mbal.constant.Common.generateUUIDId;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmApi3rdServiceImpl implements CrmApi3rdService {
    private final Jackson jackson;
    private final CrmApiCaller crmApiCaller;
    private final RmExcelDataCaching rmExcelDataCaching;

    /**
     * @return not null
     */
    @Override
    public RmUserInfo getUserInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BEARER + token);

        try {
            RmUserInfo crmUser = crmApiCaller.get(CrmFunctions.GET_USER_INFO, headers, RmUserInfo.class);
            if (crmUser == null) {
                throw new UserNotFoundException("crm-user info not found");
            }
            return crmUser;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new CrmApi3rdException(errorObject.getErrorMessage());
        }
    }

    @Override
    @Cacheable(value = "cache:crmInfo")
    public RmInfoResp getCrmData(String rmCode) {
        IFunction reqUri = Function.map(CrmFunctions.GET_CRM_BY_CODE, Map.of("rmCode", rmCode));
        try {
            String response = crmApiCaller.get(reqUri, crmApiCaller.getAuthHeader(), String.class);
            if (response == null) throw new CrmApi3rdException("Mã RM chưa chính xác");

            RmInfoResp resp = jackson.fromJson(response.substring(1), RmInfoResp.class);
            RmExcelInfo rmExcelInfo = rmExcelDataCaching.getData(rmCode);
            if (rmExcelInfo != null) {
                RmInfoResp.HrisEmployee hrisEmployee = resp.getHrisEmployee();
                if (!StringUtils.hasText(hrisEmployee.getFullName())) {
                    hrisEmployee.setFullName(rmExcelInfo.getFullName());
                }
                if (!StringUtils.hasText(hrisEmployee.getEmail())) {
                    hrisEmployee.setEmail(rmExcelInfo.getEmail());
                }
                if (!StringUtils.hasText(hrisEmployee.getPhoneNumber())) {
                    hrisEmployee.setPhoneNumber(rmExcelInfo.getPhone());
                }
                if (!StringUtils.hasText(hrisEmployee.getPhoneNumber2())) {
                    hrisEmployee.setPhoneNumber2(rmExcelInfo.getPhone2());
                }
            }

            return resp;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new CrmApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    /**
     * @return not null
     */
    @Override
    public RmUserDetailResp getCrmDataByUsername(String username, String token) {
        IFunction reqUri = Function.map(CrmFunctions.GET_CRM_BY_USERNAME, Map.of("username", username));
        try {
            String clientMessageId = generateUUIDId(20);
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, BEARER + token);
            headers.add(CLIENT_MESSAGE_ID, clientMessageId);
            headers.setContentType(APPLICATION_JSON);

            /*return crmApiCaller.get(reqUri, headers, RmUserDetailResp.class);*/
            String response = crmApiCaller.get(reqUri, headers, String.class);
            if (response == null) throw new CrmApi3rdException("username chưa chính xác");
            return jackson.fromJson(response, RmUserDetailResp.class);
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new CrmApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

}
