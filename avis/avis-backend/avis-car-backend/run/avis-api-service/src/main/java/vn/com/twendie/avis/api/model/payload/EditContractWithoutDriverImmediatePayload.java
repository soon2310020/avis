package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import vn.com.twendie.avis.api.core.constraint.AllNullOrNot;
import vn.com.twendie.avis.api.core.constraint.FromToday;
import vn.com.twendie.avis.api.core.constraint.StartOfDay;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

import static java.math.RoundingMode.HALF_UP;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllNullOrNot(fields = {"driverId", "vehicleId"}, message = "contract.invalid_driver_vehicle")
public class EditContractWithoutDriverImmediatePayload {

    @NotBlank(message = "contract.invalid_branch")
    @JsonProperty("branch_code")
    private String branchCode;

    @NotBlank(message = "contract.invalid_vehicle_working_area")
    @JsonProperty("vehicle_working_area")
    private String vehicleWorkingArea;

    @JsonProperty("sign_date")
    private Timestamp signDate;

    @Positive(message = "contract.invalid_driver")
    @JsonProperty("driver_id")
    private Long driverId;

    @Positive(message = "contract.invalid_vehicle")
    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @StartOfDay(message = "contract.timestamp_not_start_of_day")
    @JsonProperty("from_datetime")
    private Timestamp fromDatetime;

    @FromToday(message = "contract.end_date_not_in_future")
    @StartOfDay(message = "contract.timestamp_not_start_of_day")
    @JsonProperty("to_datetime")
    private Timestamp toDatetime;

    @NotNull(message = "contract.invalid_deposit")
    @PositiveOrZero(message = "contract.invalid_deposit")
    @Max(value = 99999999999L, message = "number.over_limit")
    @JsonProperty("deposit")
    private Long deposit;

    @NotNull(message = "contract.invalid_payment_term")
    @PositiveOrZero(message = "contract.invalid_payment_term")
    @JsonProperty("payment_term")
    private Integer paymentTerm;

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

    @NotNull(message = "contract.invalid_days_inform_before_early_termination")
    @PositiveOrZero(message = "contract.invalid_days_inform_before_early_termination")
    @JsonProperty("days_inform_before_early_termination")
    private Integer daysInformBeforeEarlyTermination;

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

}
