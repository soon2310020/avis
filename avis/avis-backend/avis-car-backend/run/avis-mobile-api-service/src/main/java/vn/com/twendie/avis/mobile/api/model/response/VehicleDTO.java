package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDTO {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("number_plate")
	private String numberPlate;

	@JsonProperty("type")
	private String type;

    @JsonProperty("owner")
    private String owner;

	@JsonProperty("model")
	private String model;

    @JsonProperty("color")
    private String color;

	@JsonProperty("chassis_no")
	private String chassisNo;

	@JsonProperty("engine_no")
	private String engineNo;

	@JsonProperty("year_manufacture")
	private Integer yearManufacture;

    @JsonProperty("registration_no")
    private String registrationNo;

    @JsonProperty("status")
    private Integer status;
}