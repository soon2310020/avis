package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmStationFeesPayload {

    @JsonProperty("journey_diary_station_fees")
    private List<JourneyDiaryStationFeePayload> journeyDiaryStationFees;

    @JsonProperty("note")
    private String note;

}
