package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JourneyDiaryCostPayload {

    @JsonProperty("journey_diary_id")
    private Long journeyDiaryId;

    @JsonProperty("code")
    private String code;

    @NotNull(message = "cost_type.invalid_cost_value")
    @PositiveOrZero(message = "cost_type.invalid_cost_value")
    @JsonProperty("value")
    private BigDecimal value;

}
