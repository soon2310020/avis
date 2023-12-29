package com.stg.service.dto.external.responseFlexible;

import com.stg.utils.ApplicationStatus;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleCreateProcessRespDto {

    private Integer processId;

}
