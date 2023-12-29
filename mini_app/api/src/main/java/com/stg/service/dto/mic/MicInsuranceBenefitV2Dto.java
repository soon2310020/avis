package com.stg.service.dto.mic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
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
