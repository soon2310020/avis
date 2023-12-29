package com.stg.service3rd.toolcrm.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.toolcrm.constant.QuotationAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationSyncDataReq {
    @NotNull
    private UUID uuid;

    @Schema(name = "Action state")
    private QuotationAction state;

    private String healthsTxt; // List<FlexibleSubmitQuestionInput>

    @JsonProperty("direct_submit_status")
    private DirectSubmitStatusReq directSubmitStatus;
    
    public QuotationSyncDataReq(UUID uuid, QuotationAction state) {
        this.uuid = uuid;
        this.state = state;
    }
}
