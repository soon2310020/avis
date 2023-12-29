package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MicInsuranceBenefitV2Dto {

    private String mainOne;
    private String mainTwo;

    private String mainThree;
    private String mainThreeOne;
    private String mainThreeTwo;
    private String mainThreeThree;
    private String mainThreeFour;
    private String mainThreeFive;
    private String mainThreeSix;
    private String mainThreeSeven;

    private String subOne;
    private String subOneOne;
    private String subOneTwo;

    private String subTwo;
    private String subThree;
    private String subFour;

    private String name;
    private Integer id;
    private BigDecimal phi;
    private BigDecimal regularFee;

}
