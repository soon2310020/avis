package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import vn.com.twendie.avis.api.core.constraint.AllNullOrNot;
import vn.com.twendie.avis.api.core.constraint.FromToday;
import vn.com.twendie.avis.api.core.constraint.StartOfDay;
import vn.com.twendie.avis.api.core.constraint.ValueInList;
import vn.com.twendie.avis.data.model.CodeValueModel;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static java.math.RoundingMode.HALF_UP;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllNullOrNot(fields = {"driverId", "vehicleId"}, message = "contract.invalid_driver_vehicle")
public class ContractWithDriverPayload {

    @NotBlank(message = "contract.invalid_contract_code")
    @JsonProperty("prefix_code")
    private String prefixCode;

    @Pattern(regexp = "\\d{3}", message = "contract.invalid_contract_code")
    @JsonProperty("suffix_code")
    private String suffixCode;

    @NotBlank(message = "contract.invalid_branch")
    @JsonProperty("branch_code")
    private String branchCode;

    @NotBlank(message = "contract.invalid_vehicle_working_area")
    @JsonProperty("vehicle_working_area")
    private String vehicleWorkingArea;

    @JsonProperty("sign_date")
    private Timestamp signDate;

    @NotNull(message = "contract.invalid_customer")
    @Positive(message = "contract.invalid_customer")
    @JsonProperty("customer_id")
    private Long customerId;

    @Positive(message = "contract.invalid_member_customer")
    @JsonProperty("member_customer_id")
    private Long memberCustomerId;

    @Positive(message = "contract.invalid_driver")
    @JsonProperty("driver_id")
    private Long driverId;

    @NotNull(message = "contract.invalid_driver_know_english")
    @JsonProperty("driver_know_english")
    private Boolean driverKnowEnglish;

    @Positive(message = "contract.invalid_vehicle")
    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @NotNull(message = "contract.invalid_parking")
    @Min(value = 1, message = "contract.invalid_parking")
    @Max(value = 2, message = "contract.invalid_parking")
    @JsonProperty("parking_id")
    private Long parkingId;

    @StartOfDay(message = "contract.timestamp_not_start_of_day")
    @JsonProperty("from_datetime")
    private Timestamp fromDatetime;

    @FromToday(message = "contract.end_date_not_in_future")
    @StartOfDay(message = "contract.timestamp_not_start_of_day")
    @JsonProperty("to_datetime")
    private Timestamp toDatetime;

    @ValueInList(values = {"RST001", "RST002", "RST003"}, message = "contract.invalid_rental_type")
    @JsonProperty("rental_service_type_code")
    private String rentalServiceTypeCode;

    @NotNull(message = "contract.invalid_working_time")
    @JsonProperty("working_time_weekend_holiday_from")
    private Time workingTimeWeekendHolidayFrom;

    @NotNull(message = "contract.invalid_working_time")
    @JsonProperty("working_time_weekend_holiday_to")
    private Time workingTimeWeekendHolidayTo;

    @NotNull(message = "contract.invalid_time_use_policy_group_id")
    @Min(value = 0, message = "contract.invalid_time_use_policy_group_id")
    @Max(value = 1, message = "contract.invalid_time_use_policy_group_id")
    @JsonProperty("time_use_policy_group_id")
    private Integer timeUsePolicyGroupId;

    @NotNull(message = "contract.invalid_deposit")
    @PositiveOrZero(message = "contract.invalid_deposit")
    @Max(value = 99999999999L, message = "number.over_limit")
    @JsonProperty("deposit")
    private Long deposit;

    @NotNull(message = "contract.invalid_payment_term")
    @PositiveOrZero(message = "contract.invalid_payment_term")
    @JsonProperty("payment_term")
    private Integer paymentTerm;

    @Min(0)
    @Max(1)
    @NotNull
    @JsonProperty("vat_toll_fee")
    private Integer vatTollFee;

    @NotNull
    @JsonProperty("include_empty_km")
    private Boolean includeEmptyKm;

    @NotNull(message = "contract.invalid_working_time")
    @JsonProperty("working_time_from")
    private Time workingTimeFrom;

    @NotNull(message = "contract.invalid_working_time")
    @JsonProperty("working_time_to")
    private Time workingTimeTo;

    @NotBlank(message = "contract.invalid_working_day")
    @JsonProperty("working_day_code")
    private String workingDayCode;

    @JsonProperty("working_day_value")
    private Integer workingDayValue;

    @NotBlank(message = "contract.invalid_fuel_type")
    @JsonProperty("fuel_type_code")
    private String fuelTypeCode;

    @NotNull(message = "contract.invalid_return_vehicle_early")
    @JsonProperty("return_vehicle_early")
    private Boolean returnVehicleEarly;

    @NotNull(message = "contract.invalid_days_inform_before_return_vehicle")
    @PositiveOrZero(message = "contract.invalid_days_inform_before_return_vehicle")
    @JsonProperty("days_inform_before_return_vehicle")
    private Integer daysInformBeforeReturnVehicle;

    @NotNull(message = "contract.invalid_fuel_adjust_percent")
    @Min(value = 0, message = "contract.invalid_fuel_adjust_percent")
    @Max(value = 100, message = "contract.invalid_fuel_adjust_percent")
    private BigDecimal fuelAdjustPercent;

    @JsonSetter("fuel_adjust_percent")
    private void setFuelAdjustPercent(BigDecimal fuelAdjustPercent) {
        this.fuelAdjustPercent = fuelAdjustPercent.setScale(2, HALF_UP);
    }

    @NotNull(message = "contract.invalid_penalty_rate_early_termination")
    @Min(value = 0, message = "contract.invalid_penalty_rate_early_termination")
    @Max(value = 100, message = "contract.invalid_penalty_rate_early_termination")
    private BigDecimal penaltyRateEarlyTermination;

    @JsonSetter("penalty_rate_early_termination")
    private void setPenaltyRateEarlyTermination(BigDecimal penaltyRateEarlyTermination) {
        this.penaltyRateEarlyTermination = penaltyRateEarlyTermination.setScale(2, HALF_UP);
    }

    @Size(max = 1000, message = "contract.too_long_note")
    private String note;

    @JsonSetter("note")
    public void setNote(String note) {
        this.note = StringUtils.trimToNull(note);
    }

    @JsonIgnore
    private Boolean includeAppendix = false;

    @Valid
    @NotNull(message = "contract.invalid_costs")
    @JsonProperty("costs")
    private List<CodeValueModel> contractCosts;

    @Valid
    @NotNull(message = "contract.invalid_norms")
    @JsonProperty("norms")
    private List<CodeValueModel> contractNorms;

}
