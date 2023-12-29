package com.stg.service.dto.quotation.crm;

import com.stg.service3rd.crm.dto.resp.RmInfoResp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RmInfoRespDto extends RmInfoResp {
    private String mbCode;
}
