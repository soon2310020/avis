package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.common.dto.soap.EResponse;
import com.stg.service3rd.common.dto.soap.SoapResponseBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalcPaymentEAppRespBody extends SoapResponseBody {
    
    @JsonProperty("MT_INQUIRY_HSCYBH_EAPP_OUT")
    private CalcPaymentEAppOutput output = new CalcPaymentEAppOutput();

    @Override
    public EResponse getMessage() {
        return output.getMessage();
    }
}
