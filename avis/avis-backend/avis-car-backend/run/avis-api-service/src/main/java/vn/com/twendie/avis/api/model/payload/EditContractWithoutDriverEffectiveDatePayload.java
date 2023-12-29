package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import vn.com.twendie.avis.api.core.constraint.FromToday;
import vn.com.twendie.avis.api.core.constraint.ValueInList;
import vn.com.twendie.avis.data.model.CodeValueModel;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditContractWithoutDriverEffectiveDatePayload {

    @ValueInList(values = {"RST004", "RST005"}, message = "contract.invalid_rental_type")
    @JsonProperty("rental_service_type_code")
    private String rentalServiceTypeCode;

    @NotNull(message = "contract.invalid_fuel_adjust_percent")
    @Min(value = 0, message = "contract.invalid_fuel_adjust_percent")
    @Max(value = 100, message = "contract.invalid_fuel_adjust_percent")
    private BigDecimal fuelAdjustPercent;

    @JsonSetter("fuel_adjust_percent")
    private void setFuelAdjustPercent(BigDecimal fuelAdjustPercent) {
        this.fuelAdjustPercent = fuelAdjustPercent.setScale(2, HALF_UP);
    }

    @Valid
    @NotNull(message = "contract.invalid_costs")
    @JsonProperty("costs")
    private List<CodeValueModel> contractCosts;

    @Valid
    @NotNull(message = "contract.invalid_norms")
    @JsonProperty("norms")
    private List<CodeValueModel> contractNorms;

    @FromToday(message = "contract.invalid_effective_date")
    @JsonProperty("effective_date")
    private Timestamp effectiveDate;

}
