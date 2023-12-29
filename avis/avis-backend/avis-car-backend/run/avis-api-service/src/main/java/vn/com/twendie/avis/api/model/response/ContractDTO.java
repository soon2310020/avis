package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.twendie.avis.data.model.CodeValueModel;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("prefix_code")
    private String prefixCode;

    @JsonProperty("suffix_code")
    private String suffixCode;

    @JsonProperty("code")
    private String code;

    @JsonProperty("term")
    private Integer term;

    @JsonProperty("vehicle_working_area")
    private String vehicleWorkingArea;

    @JsonProperty("sign_date")
    private Timestamp signDate;

    @JsonProperty("driver_is_transferred_another")
    private Boolean driverIsTransferredAnother;

    @JsonProperty("driver_know_english")
    private Boolean driverKnowEnglish;

    @JsonProperty("vehicle_is_transferred_another")
    private Boolean vehicleIsTransferredAnother;

    @JsonProperty("parking_id")
    private Long parkingId;

    @JsonProperty("from_datetime")
    private Timestamp fromDatetime;

    @JsonProperty("to_datetime")
    private Timestamp toDatetime;

    @JsonProperty("working_time_weekend_holiday_from")
    private Time workingTimeWeekendHolidayFrom;

    @JsonProperty("working_time_weekend_holiday_to")
    private Time workingTimeWeekendHolidayTo;

    @JsonProperty("time_use_policy_group_id")
    private Integer timeUsePolicyGroupId;

    @JsonProperty("deposit")
    private Long deposit;

    @JsonProperty("payment_term")
    private Integer paymentTerm;

    @JsonProperty("vat_toll_fee")
    private Integer vatTollFee;

    @JsonProperty("is_include_empty_km")
    private Boolean includeEmptyKm;

    @JsonProperty("working_time_from")
    private Time workingTimeFrom;

    @JsonProperty("working_time_to")
    private Time workingTimeTo;

    @JsonProperty("working_day_value")
    private Integer workingDayValue;

    @JsonProperty("return_vehicle_early")
    private Boolean returnVehicleEarly;

    @JsonProperty("days_inform_before_return_vehicle")
    private Integer daysInformBeforeReturnVehicle;

    @JsonProperty("days_inform_before_early_termination")
    private Integer daysInformBeforeEarlyTermination;

    @JsonProperty("fuel_adjust_percent")
    private BigDecimal fuelAdjustPercent;

    @JsonProperty("penalty_rate_early_termination")
    private BigDecimal penaltyRateEarlyTermination;

    @JsonProperty("date_early_termination")
    private Timestamp dateEarlyTermination;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("note")
    private String note;

    @JsonProperty("include_appendix")
    private Boolean includeAppendix;

    @JsonProperty("contract_type")
    private ContractTypeDTO contractType;

    @JsonProperty("branch")
    private BranchDTO branch;

    @JsonProperty("customer")
    private CustomerDTO customer;

    @JsonProperty("member_customer")
    private MemberCustomerDTO memberCustomer;

    @JsonProperty("driver")
    private DriverDTO driver;

    @JsonProperty("vehicle")
    private VehicleDTO vehicle;

    @JsonProperty("rental_service_type")
    private RentalServiceTypeDTO rentalServiceType;

    @JsonProperty("working_day")
    private WorkingDayDTO workingDay;

    @JsonProperty("fuel_type")
    private FuelTypeDTO fuelType;

    @JsonProperty("created_by")
    private UserDTO createdBy;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("costs")
    private List<CodeValueModel> contractCostTypes;

    @JsonProperty("norms")
    private List<CodeValueModel> contractNormLists;

}
