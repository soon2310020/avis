package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.common.dto.soap.EResponse;
import com.stg.service3rd.common.dto.soap.SoapResponseBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalcPaymentRespBody extends SoapResponseBody {
    
    @JsonProperty("MT_CALC_PAYMENT_OUTPUT")
    private CalcPaymentOuput ouput = new CalcPaymentOuput();

    @Override
    public EResponse getMessage() {
        return ouput.getMessage();
    }
}
