package com.stg.service.dto.external.responseV2;

import com.stg.service3rd.common.dto.soap.EResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PolicyMbalResponse {

    private int code;
    private String strEn;
    private String strVi;
}
