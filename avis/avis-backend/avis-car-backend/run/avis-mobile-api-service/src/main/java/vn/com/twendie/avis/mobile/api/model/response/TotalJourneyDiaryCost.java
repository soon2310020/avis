package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TotalJourneyDiaryCost {

    @JsonProperty("cost_type")
    private CostTypeDTO costType;

    @JsonProperty("total_value")
    private int totalValue;

}
