package com.stg.service3rd.hcm;

import com.stg.service3rd.hcm.dto.resp.HcmEmployeeRes;

public interface HcmApi3rdService {
    HcmEmployeeRes findEmployeeFromHcm(String identityNumb) ;
    boolean isEmployeeFromHcm(String identityNumb) ;
}
