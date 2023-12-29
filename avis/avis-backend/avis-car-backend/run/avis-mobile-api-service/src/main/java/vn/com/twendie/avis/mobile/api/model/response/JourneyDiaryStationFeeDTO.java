package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JourneyDiaryStationFeeDTO {

    private Long id;

    @JsonProperty("journey_diary_id")
    private Long journeyDiaryId;

    @JsonProperty("vehicle_plate")
    private String vehiclePlate;

    @JsonProperty("private_code")
    private String privateCode;

    @JsonProperty("vehicle_type_name")
    private String vehicleTypeName;

    @JsonProperty("bot_name")
    private String botName;

    @JsonProperty("stage_name")
    private String stageName;

    @JsonProperty("in_name")
    private String inName;

    @JsonProperty("in_time")
    private Timestamp inTime;

    @JsonProperty("out_name")
    private String outName;

    @JsonProperty("out_time")
    private Timestamp outTime;

    @JsonProperty("check_name")
    private String checkName;

    @JsonProperty("check_time")
    private Timestamp checkTime;

    @JsonProperty("km_on_stage")
    private BigDecimal kmOnStage;

    @JsonProperty("fee")
    private BigDecimal fee;

    @JsonProperty("note")
    private String note;

    @JsonProperty("station_confirm")
    private Boolean stationConfirm;

    @JsonProperty("fee_confirm")
    private Boolean feeConfirm;
    
}
