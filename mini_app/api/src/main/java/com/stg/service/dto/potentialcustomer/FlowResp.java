package com.stg.service.dto.potentialcustomer;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FlowResp {

    private FlowType flow;
    
    private BigDecimal tsa;
    
    private Boolean healthCheckup; /** null: k hiển thị gì || true: Khả năng phải đi khám || false: Không phải đi khám */
    
    public static FlowResp of(FlowType type, BigDecimal tsa, Boolean healthCheckup) {
        FlowResp resp = new FlowResp();
        resp.setFlow(type);
        resp.setTsa(tsa);
        resp.setHealthCheckup(healthCheckup);
        return resp;
    }
}
