package vn.com.twendie.avis.tracking.model.tracking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static vn.com.twendie.avis.api.core.util.DateUtils.T_MEDIUM_PATTERN;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleFeeStation implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("VehiclePlate")
    private String vehiclePlate;

    @JsonProperty("PrivateCode")
    private String privateCode;

    @JsonProperty("VehicleTypeName")
    private String vehicleTypeName;

    @JsonProperty("BotName")
    private String botName;

    @JsonProperty("StageName")
    private String stageName;

    @JsonProperty("InName")
    private String inName;

    @JsonProperty("InTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
    private Date inTime;

    @JsonProperty("OutName")
    private String outName;

    @JsonProperty("OutTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
    private Date outTime;

    @JsonProperty("CheckName")
    private String checkName;

    @JsonProperty("CheckTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
    private Date checkTime;

    @JsonProperty("KmOnStage")
    private BigDecimal kmOnStage;

    @JsonProperty("Fee")
    private BigDecimal fee;

    @JsonProperty("Note")
    private String note;

}
