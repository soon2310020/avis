package com.stg.service3rd.common.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EResponse {

    @JsonProperty("ResponseCode")
    private String code;
    @JsonProperty("ResponseStrEn")
    private String messageEn;
    @JsonProperty("ResponseStrVi")
    private String messageVi;
    
}
