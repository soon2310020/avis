package com.stg.service3rd.mbalv2.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.stg.service3rd.common.dto.soap.EResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalcPaymentOuput {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("EPolicy")
    private Item item;

    @JsonProperty("EResponse")
    private EResponse message;

    @Setter
    @Getter
    @Schema
    public static class Item {
        @JsonProperty("item")
        private CalcPaymentPolicy policy;
    }

}
