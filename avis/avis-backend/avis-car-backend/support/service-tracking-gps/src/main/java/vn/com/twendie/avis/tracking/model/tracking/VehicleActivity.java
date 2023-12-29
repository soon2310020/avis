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
public class VehicleActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("VehicleTypeName")
    private Object vehicleTypeName;

    @JsonProperty("VehiclePlate")
    private String vehiclePlate;

    @JsonProperty("ReportDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
    private Date reportDate;

    @JsonProperty("StartTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
    private Date startTime;

    @JsonProperty("EndTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
    private Date endTime;

    @JsonProperty("TotalTime")
    private int totalTime;

    @JsonProperty("KmGPS")
    private BigDecimal kmGPS;

    @JsonProperty("KmOfPulseMechanical")
    private double kmOfPulseMechanical;

    @JsonProperty("ConstantNorms")
    private double constantNorms;

    @JsonProperty("Norms")
    private double norms;

    @JsonProperty("StartBuilding")
    private String startBuilding;

    @JsonProperty("StartRoad")
    private String startRoad;

    @JsonProperty("StartCommune")
    private String startCommune;

    @JsonProperty("StartDistrict")
    private String startDistrict;

    @JsonProperty("StartProvince")
    private String startProvince;

    @JsonProperty("EndBuilding")
    private String endBuilding;

    @JsonProperty("EndRoad")
    private String endRoad;

    @JsonProperty("EndCommune")
    private String endCommune;

    @JsonProperty("EndDistrict")
    private String endDistrict;

    @JsonProperty("EndProvince")
    private String endProvince;

    @JsonProperty("IsSupplemental")
    private Boolean isSupplemental;

}
