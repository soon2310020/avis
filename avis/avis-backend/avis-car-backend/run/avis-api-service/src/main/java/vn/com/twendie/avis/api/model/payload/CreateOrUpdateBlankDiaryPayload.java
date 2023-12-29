package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import vn.com.twendie.avis.data.model.CodeValueModel;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Data
public class CreateOrUpdateBlankDiaryPayload {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("journey_diary_id")
    @Null(message = "jdd.create_or_update_blank_diary.error.cannot_edit_system_created_diary")
    private Long journeyDiaryId;

    @JsonProperty("contract_id")
    @NotNull(message = "jdd.error.contract_id_is_null")
    private Long contractId;

    @JsonProperty("parent_id")
    private Long parentId;

    @JsonProperty("date")
    @NotNull(message = "jdd.error.date_is_not_found")
    private Timestamp date;

    @JsonProperty("customer_name_used")
    @Size(max = 255, message = "jdd.error.customer_name_used_too_long")
    private String customerNameUsed;

    @JsonProperty("customer_department")
    @Size(max = 255, message = "jdd.error.customer_department_too_long")
    private String customerDepartment;

    @JsonProperty("trip_itinerary")
    @Size(max = 1000, message = "jdd.error.trip_itinerary_too_long")
    private String tripItinerary;

    @JsonProperty("km_start")
    @NotNull(message = "jdd.error.km_start_is_invalid")
    @Min(value = 0, message = "jdd.error.km_start_is_invalid")
    @Max(value = 9999999, message = "jdd.error.km_start_is_invalid")
    private BigDecimal kmStart;

    @JsonProperty("km_customer_get_in")
    @NotNull(message = "jdd.error.km_customer_get_in_is_invalid")
    @Min(value = 0, message = "jdd.error.km_customer_get_in_is_invalid")
    @Max(value = 9999999, message = "jdd.error.km_customer_get_in_is_invalid")
    private BigDecimal kmCustomerGetIn;

    @JsonProperty("km_customer_get_out")
    @NotNull(message = "jdd.error.km_customer_get_out_is_invalid")
    @Min(value = 0, message = "jdd.error.km_customer_get_out_is_invalid")
    @Max(value = 9999999, message = "jdd.error.km_customer_get_out_is_invalid")
    private BigDecimal kmCustomerGetOut;

    @JsonProperty("km_end")
    @NotNull(message = "jdd.error.km_end_is_invalid")
    @Min(value = 0, message = "jdd.error.km_end_is_invalid")
    @Max(value = 9999999, message = "jdd.error.km_end_is_invalid")
    private BigDecimal kmEnd;

    @JsonProperty("working_time_gps_from")
    @NotNull(message = "jdd.error.working_time_gps_is_invalid")
    private Time workingTimeGpsFrom;

    @JsonProperty("working_time_gps_to")
    @NotNull(message = "jdd.error.working_time_gps_is_invalid")
    private Time workingTimeGpsTo;

    @JsonProperty("overnight")
    @NotNull(message = "jdd.error.over_night_is_invalid")
    @Min(value = 0, message = "jdd.error.over_night_is_invalid")
    @Max(value = 1, message = "jdd.error.over_night_is_invalid")
    private Integer overnight;

    @JsonProperty("is_weekend")
    @NotNull(message = "error.blank_input")
    private Boolean isWeekend;

    @JsonProperty("is_self_drive")
    @NotNull(message = "error.blank_input")
    private Boolean isSelfDrive;

    @JsonProperty("driver_name")
    @Size(max = 255, message = "jdd.error.driver_name_too_long")
    private String driverName;

    @JsonProperty("vehicle_number_plate")
    @NotBlank(message = "vehicle.valid_error.number_plate_wrong_format")
    @Pattern(regexp = "[A-Z0-9]*-[A-Z0-9]*", message = "vehicle.valid_error.number_plate_wrong_format")
    @Size(max = 128, message = "vehicle.valid_error.number_plate_too_long")
    private String vehicleNumberPlate;

    @Size(max = 500, message = "jdd.error.note_too_long")
    private String note;

    @JsonSetter("note")
    public void setNote(String note) {
        this.note = StringUtils.trimToNull(note);
    }

    @JsonProperty("costs")
    private List<CodeValueModel> journeyDiaryDailyCostTypes;

    @JsonProperty("is_new")
    private Boolean isNew = false;

    @JsonProperty("flag_finished_modify")
    private Boolean flagFinishedModify;

}
