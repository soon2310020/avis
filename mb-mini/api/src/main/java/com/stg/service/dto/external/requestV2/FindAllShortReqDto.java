package com.stg.service.dto.external.requestV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FindAllShortReqDto {
    private String crmIsActive;
    private boolean isReportByRm;
    private boolean isManager;
    private boolean isRm;
    private int page;
    private boolean rm;
    private String rmBlock;
    private String rsId;
    private String scope;
    private int size;
}
