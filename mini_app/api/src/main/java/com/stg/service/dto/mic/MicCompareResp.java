package com.stg.service.dto.mic;

import com.stg.constant.MicCompareType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MicCompareResp {

    private boolean result = false;

    private boolean fromContractInput = false;

    private MicCompareType compareType;

    private Integer nhom;

}
