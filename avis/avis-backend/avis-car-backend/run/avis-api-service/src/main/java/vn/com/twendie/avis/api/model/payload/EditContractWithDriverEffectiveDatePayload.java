package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import vn.com.twendie.avis.api.core.constraint.FromToday;
import vn.com.twendie.avis.api.core.constraint.ValueInList;
import vn.com.twendie.avis.data.model.CodeValueModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditContractWithDriverEffectiveDatePayload {

    @ValueInList(values = {"RST001", "RST002", "RST003"}, message = "contract.invalid_rental_type")
    @JsonProperty("rental_service_type_code")
    private String rentalServiceTypeCode;

    @NotNull(message = "contract.invalid_costs")
    @JsonProperty("costs")
    private List<CodeValueModel> contractCosts;

    @NotNull(message = "contract.invalid_norms")
    @JsonProperty("norms")
    private List<CodeValueModel> contractNorms;

    @JsonProperty("working_time_weekend_holiday_from")
    private Time workingTimeWeekendHolidayFrom;

    @JsonProperty("working_time_weekend_holiday_to")
    private Time workingTimeWeekendHolidayTo;

    @JsonProperty("working_time_from")
    private Time workingTimeFrom;

    @JsonProperty("working_time_to")
    private Time workingTimeTo;

    @NotBlank(message = "contract.invalid_working_day")
    @JsonProperty("working_day_code")
    private String workingDayCode;

    @JsonProperty("working_day_value")
    private Integer workingDayValue;

    @NotNull(message = "contract.invalid_fuel_adjust_percent")
    @Min(value = 0, message = "contract.invalid_fuel_adjust_percent")
    @Max(value = 100, message = "contract.invalid_fuel_adjust_percent")
    private BigDecimal fuelAdjustPercent;

    @JsonSetter("fuel_adjust_percent")
    private void setFuelAdjustPercent(BigDecimal fuelAdjustPercent) {
        this.fuelAdjustPercent = fuelAdjustPercent.setScale(2, HALF_UP);
    }

    @FromToday(message = "contract.invalid_effective_date")
    @JsonProperty("effective_date")
    private Timestamp effectiveDate;

}
