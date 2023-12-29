package com.stg.service3rd.crm;

import com.stg.service3rd.crm.dto.resp.RmUserInfo;
import com.stg.service3rd.crm.dto.resp.RmInfoResp;
import com.stg.service3rd.crm.dto.resp.RmUserDetailResp;

public interface CrmApi3rdService {
    RmUserInfo getUserInfo(String token);

    RmInfoResp getCrmData(String rmCode);
    
    RmUserDetailResp getCrmDataByUsername(String username, String token);
    
}
