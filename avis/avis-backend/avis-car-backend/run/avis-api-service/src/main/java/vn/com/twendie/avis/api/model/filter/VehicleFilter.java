package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.data.model.Branch;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleFilter {

    @JsonProperty("type")
    private String type;

    @JsonProperty("number_plate")
    private String numberPlate;

    @JsonProperty("color")
    private String color;

    @JsonProperty("number_seat")
    private Integer numberSeat;

    @JsonProperty("model")
    private String model;

    @JsonProperty("vehicle_supplier_group")
    private VehicleSupplierGroupFilter vehicleSupplierGroup;

    @JsonProperty("travel_warrant_expiry_date")
    private Timestamp travelWarrantExpiryDate;

    @JsonProperty("registration_to_date")
    private Timestamp registrationToDate;

    @JsonProperty("road_fee_expiry_date")
    private Timestamp roadFeeExpiryDate;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonIgnore
    private Branch branch;

    @JsonProperty("status")
    private Integer status;

}
