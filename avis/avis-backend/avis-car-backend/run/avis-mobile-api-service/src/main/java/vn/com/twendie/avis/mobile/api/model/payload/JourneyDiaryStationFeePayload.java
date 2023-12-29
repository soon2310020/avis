package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JourneyDiaryStationFeePayload {

    @NotNull(message = "error.invalid_input")
    @JsonProperty("id")
    private Long id;

    @NotNull(message = "journey_diary_daily_station_fee.missing_station_confirm")
    @JsonProperty("station_confirm")
    private Boolean stationConfirm;

    @NotNull(message = "journey_diary_daily_station_fee.missing_fee_confirm")
    @JsonProperty("fee_confirm")
    private Boolean feeConfirm;

}
