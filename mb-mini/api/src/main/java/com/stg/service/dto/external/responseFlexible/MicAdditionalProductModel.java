package com.stg.service.dto.external.responseFlexible;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MicAdditionalProductModel {

    private BigDecimal fee;
    private String transactionId;
    private String miniAssuredId;
    private String gcn;

}
