package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleDetailDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("number_plate")
    private String numberPlate;

    @JsonProperty("type")
    private String type;

    @JsonProperty("transmission_type")
    private Integer transmissionType;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("model")
    private String model;

    @JsonProperty("color")
    private String color;

    @JsonProperty("number_seat")
    private Integer numberSeat;

    @JsonProperty("chassis_no")
    private String chassisNo;

    @JsonProperty("engine_no")
    private String engineNo;

    @JsonProperty("year_manufacture")
    private Integer yearManufacture;

    @JsonProperty("start_using_date")
    private Timestamp startUsingDate;

    @JsonProperty("registration_no")
    private String registrationNo;

    @JsonProperty("travel_warrant_expiry_date")
    private Timestamp travelWarrantExpiryDate;

    @JsonProperty("registration_to_date")
    private Timestamp registrationToDate;

    @JsonProperty("insurance_no")
    private String insuranceNo;

    @JsonProperty("insurance_expiry_date")
    private Timestamp insuranceExpiryDate;

    @JsonProperty("road_fee_expiry_date")
    private Timestamp roadFeeExpiryDate;

    @JsonProperty("liquidation_date")
    private Timestamp liquidationDate;

    @JsonProperty("place_of_origin")
    private String placeOfOrigin;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("in_contract")
    private Boolean inContract;

    @JsonProperty("note")
    private String note;

    @JsonProperty("current_journey_diary_id")
    private Long currentJourneyDiaryId;

    @JsonProperty("current_contract_id")
    private Long currentContractId;

    @JsonProperty("fuel_type_group_id")
    private FuelTypeGroupDTO fuelTypeGroup;

    @JsonProperty("branch_id")
    private BranchDTO branch;

    @JsonProperty("vehicle_supplier_group_id")
    private VehicleSupplierGroupDTO vehicleSupplierGroup;

    @JsonProperty("operation_admin_id")
    private AdminUserDTO operationAdmin;

    @JsonProperty("unit_operator_id")
    private AdminUserDTO unitOperator;

    @JsonProperty("accountant_id")
    private AdminUserDTO accountant;
    
}
