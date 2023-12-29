package com.stg.service.dto.baas;

import com.stg.utils.Constants;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaasPayOnBehalfRespDto {

    private String message;

    private Constants.Status status;
}
