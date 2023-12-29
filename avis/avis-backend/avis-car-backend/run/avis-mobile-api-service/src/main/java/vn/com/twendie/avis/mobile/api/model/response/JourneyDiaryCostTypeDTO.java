package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class JourneyDiaryCostTypeDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("image_cost_link")
    private String imageCostLink;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonProperty("journey_diary_id")
    private Long journeyDiaryId;

    @JsonProperty("cost_type_id")
    private Long costTypeId;

}
