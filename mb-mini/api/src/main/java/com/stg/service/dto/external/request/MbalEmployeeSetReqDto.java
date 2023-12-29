package com.stg.service.dto.external.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MbalEmployeeSetReqDto {

    @JsonProperty("rmCode")
    private String rmCode;

}
