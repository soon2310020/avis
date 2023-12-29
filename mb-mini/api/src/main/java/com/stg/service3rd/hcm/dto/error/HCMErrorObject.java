package com.stg.service3rd.hcm.dto.error;

import com.stg.service3rd.common.dto.error.ErrorDesc;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HCMErrorObject extends ErrorDesc {
    private Object data;
}
