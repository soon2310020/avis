package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.mobile.api.constraint.NotNullTogether;
import vn.com.twendie.avis.mobile.api.validation.group.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NotNullTogether(fields = {"kmOdoStart", "kmDriverStart"}, groups = StartTrip.class, message = "journey_diary.missing_km")
@NotNullTogether(fields = {"kmOdoCustomerGetIn", "kmDriverCustomerGetIn"}, groups = CustomerGetIn.class, message = "journey_diary.missing_km")
@NotNullTogether(fields = {"kmOdoCustomerGetOut", "kmDriverCustomerGetOut"}, groups = CustomerGetOut.class, message = "journey_diary.missing_km")
@NotNullTogether(fields = {"kmOdoEnd", "kmDriverEnd"}, groups = FinishTrip.class, message = "journey_diary.missing_km")
public class JourneyDiaryPayload {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("contract_id")
    private Long contractId;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    // Start

    @JsonProperty("address_start")
    @NotBlank(groups = StartTrip.class, message = "journey_diary.missing_address")
    private String addressStart;

    @JsonProperty("km_odo_start")
    @PositiveOrZero(groups = StartTrip.class, message = "")
    private BigDecimal kmOdoStart;

    @JsonProperty("km_driver_start")
    @PositiveOrZero(groups = StartTrip.class, message = "")
    @Max(value = 9999999, groups = StartTrip.class, message = "journey_diary.km_driver_over_limit")
    private BigDecimal kmDriverStart;

    // Customer get in

    @JsonProperty("user_customer_id")
    private Long userCustomerId;

    @JsonProperty("customer_name")
    @NotBlank(groups = CustomerGetIn.class, message = "journey_diary.missing_customer_name")
    private String customerNameUsed;

    @JsonProperty("customer_department")
    @NotBlank(groups = CustomerGetIn.class, message = "journey_diary.missing_department_name")
    private String customerDepartment;

    @JsonProperty("address_customer_get_in")
    @NotBlank(groups = CustomerGetIn.class, message = "journey_diary.missing_address")
    private String addressCustomerGetIn;

    @JsonProperty("km_odo_customer_get_in")
    @PositiveOrZero(groups = CustomerGetIn.class, message = "")
    private BigDecimal kmOdoCustomerGetIn;

    @JsonProperty("km_driver_customer_get_in")
    @PositiveOrZero(groups = CustomerGetIn.class, message = "")
    @Max(value = 9999999, groups = CustomerGetIn.class, message = "journey_diary.km_driver_over_limit")
    private BigDecimal kmDriverCustomerGetIn;

    // Customer get out

    @JsonProperty("address_customer_get_out")
    @NotBlank(groups = CustomerGetOut.class, message = "journey_diary.missing_address")
    private String addressCustomerGetOut;

    @JsonProperty("km_odo_customer_get_out")
    @PositiveOrZero(groups = CustomerGetOut.class, message = "")
    private BigDecimal kmOdoCustomerGetOut;

    @JsonProperty("km_driver_customer_get_out")
    @PositiveOrZero(groups = CustomerGetOut.class, message = "")
    @Max(value = 9999999, groups = CustomerGetOut.class, message = "journey_diary.km_driver_over_limit")
    private BigDecimal kmDriverCustomerGetOut;

    // Breakdown

    @JsonProperty("km_odo_breakdown")
    private BigDecimal kmOdoBreakdown;

    @JsonProperty("km_driver_breakdown")
    private BigDecimal kmDriverBreakdown;

    // End

    @JsonProperty("address_end")
    @NotBlank(groups = FinishTrip.class, message = "journey_diary.missing_address")
    private String addressEnd;

    @JsonProperty("km_odo_end")
    @PositiveOrZero(groups = FinishTrip.class, message = "")
    private BigDecimal kmOdoEnd;

    @JsonProperty("km_driver_end")
    @PositiveOrZero(groups = FinishTrip.class, message = "")
    @Max(value = 9999999, groups = FinishTrip.class, message = "journey_diary.km_driver_over_limit")
    private BigDecimal kmDriverEnd;

    @JsonProperty("journey_diary_costs")
    private List<JourneyDiaryCostPayload> journeyDiaryCosts;

}
