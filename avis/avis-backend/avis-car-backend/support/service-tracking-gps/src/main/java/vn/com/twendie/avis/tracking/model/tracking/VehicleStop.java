package vn.com.twendie.avis.tracking.model.tracking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import static vn.com.twendie.avis.api.core.util.DateUtils.T_MEDIUM_PATTERN;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleStop implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("ID")
	private long id;

	@JsonProperty("OrderDay")
	private int orderDay;

    @JsonProperty("Trip")
    private String trip;

	@JsonProperty("TotalMinuteStop")
	private double totalMinuteStop;

	@JsonProperty("TotalTimeStop")
	private int totalTimeStop;

	@JsonProperty("Longitude")
	private double longitude;

	@JsonProperty("Latitude")
	private double latitude;

	@JsonProperty("MinutesOfManchineOn")
	private int minutesOfManchineOn;

	@JsonProperty("MinutesOfAirConditioningOn")
	private int minutesOfAirConditioningOn;

	@JsonProperty("LngLat")
	private String lngLat;

	@JsonProperty("Date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
	private Date date;

	@JsonProperty("StartTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
	private Date startTime;

	@JsonProperty("EndTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = T_MEDIUM_PATTERN)
	private Date endTime;

	@JsonProperty("Address")
	private String address;

    @JsonProperty("VBefore")
    private int vBefore;

    @JsonProperty("VehiclePlate")
    private String vehiclePlate;

    @JsonProperty("PrivateCode")
    private String privateCode;

}