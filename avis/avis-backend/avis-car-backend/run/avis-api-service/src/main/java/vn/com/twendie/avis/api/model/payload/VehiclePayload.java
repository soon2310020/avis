package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehiclePayload {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("number_plate")
    @NotBlank(message = "vehicle.valid_error.number_plate_wrong_format")
    @Pattern(regexp = "[A-Z0-9]*-[A-Z0-9]*", message = "vehicle.valid_error.number_plate_wrong_format")
    @Size(max = 128, message = "vehicle.valid_error.number_plate_too_long")
    private String numberPlate;

    @JsonProperty("type")
    @NotBlank(message = "vehicle.valid_error.type_is_blank")
    @Size(max = 128, message = "vehicle.valid_error.type_too_long")
    private String type;

    // MT/AT
    @JsonProperty("transmission_type")
    @NotNull(message = "vehicle.valid_error.transmission_type_is_blank")
    @Min(value = 1, message = "vehicle.valid_error.transmission_type_wrong_format")
    @Max(value = 2, message = "vehicle.valid_error.transmission_type_wrong_format")
    private Integer transmissionType;

    @JsonProperty("owner")
    @NotBlank(message = "vehicle.valid_error.owner_is_blank")
    @Size(max = 128, message = "vehicle.valid_error.owner_too_long")
    private String owner;

    @JsonProperty("model")
    @NotBlank(message = "vehicle.valid_error.model_is_blank")
    @Size(max = 128, message = "vehicle.valid_error.model_too_long")
    private String model;

    @JsonProperty("color")
    @NotBlank(message = "vehicle.valid_error.color_is_blank")
    @Size(max = 128, message = "vehicle.valid_error.color_too_long")
    private String color;

    @JsonProperty("number_seat")
    @NotNull(message = "vehicle.valid_error.number_seat_is_blank")
    @Positive(message = "vehicle.valid_error.number_seat_wrong_format")
    @Max(value = 100, message = "vehicle.valid_error.number_seat_too_large")
    private Integer numberSeat;

    // so khung
    @JsonProperty("chassis_no")
    @NotBlank(message = "vehicle.valid_error.chassis_is_blank")
    @Pattern(regexp = "[^\\s]*", message = "vehicle.valid_error.chassis_wrong_format")
    @Size(max = 128, message = "vehicle.valid_error.chassis_too_long")
    private String chassisNo;

    // so may
    @JsonProperty("engine_no")
    @NotBlank(message = "vehicle.valid_error.engine_is_blank")
    @Size(max = 128, message = "vehicle.valid_error.engine_too_long")
    private String engineNo;

    @JsonProperty("year_manufacture")
    @NotNull(message = "vehicle.valid_error.year_manufacture_wrong_format")
    @Min(value = 1000, message = "vehicle.valid_error.year_manufacture_wrong_format")
    @Max(value = 9999, message = "vehicle.valid_error.year_manufacture_wrong_format")
    private Integer yearManufacture;

    @JsonProperty("start_using_date")
//    @NotNull(message = "vehicle.valid_error.start_date_is_blank")
    private Timestamp startUsingDate;

    // so dang ky
    @JsonProperty("registration_no")
    @Size(max = 128, message = "vehicle.valid_error.registration_too_long")
    private String registrationNo;

    @JsonProperty("travel_warrant_expiry_date")
    private Timestamp travelWarrantExpiryDate;

    @JsonProperty("registration_to_date")
    private Timestamp registrationToDate;

    @JsonProperty("insurance_no")
    @Pattern(regexp = "^\\s*\\d+\\s*$", message = "vehicle.valid_error.insurance_wrong_format")
    @Size(max = 128, message = "vehicle.valid_error.insurance_too_long")
    private String insuranceNo;

    @JsonProperty("insurance_expiry_date")
    private Timestamp insuranceExpiryDate;

    @JsonProperty("road_fee_expiry_date")
    private Timestamp roadFeeExpiryDate;

    @JsonProperty("liquidation_date")
    private Timestamp liquidationDate;

    @JsonProperty("place_of_origin")
    @NotBlank(message = "vehicle.valid_error.place_of_origin_is_blank")
    @Size(max = 128, message = "vehicle.valid_error.place_of_origin_too_long")
    private String placeOfOrigin;

    @JsonProperty("status")
    @NotNull(message = "vehicle.valid_error.status_is_blank")
    @Min(value = 0, message = "vehicle.valid_error.status_wrong_format")
    @Max(value = 2, message = "vehicle.valid_error.status_wrong_format")
    private Integer status;

    @JsonProperty("note")
    @Size(max = 500, message = "valid_error.note_too_long")
    private String note;

    @JsonProperty("fuel_type_group_id")
    @NotNull(message = "vehicle.valid_error.fuel_type_group_id_is_blank")
    private Long fuelTypeGroupId;

    @JsonProperty("branch_id")
    @NotNull(message = "valid_error.branch_id_is_blank")
    private Long branchId;

    @JsonProperty("vehicle_supplier_group_id")
    @NotNull(message = "vehicle.valid_error.vehicle_supplier_group_id_is_blank")
    private Long vehicleSupplierGroupId;

    @JsonProperty("operation_admin_id")
    @NotNull(message = "valid_error.operation_admin_id_is_blank")
    private Long operationAdminId;

    @JsonProperty("unit_operator_id")
    @NotNull(message = "valid_error.unit_operator_id_is_blank")
    private Long unitOperatorId;

    @JsonProperty("accountant_id")
    @NotNull(message = "valid_error.accountant_id_is_blank")
    private Long accountantId;

}
