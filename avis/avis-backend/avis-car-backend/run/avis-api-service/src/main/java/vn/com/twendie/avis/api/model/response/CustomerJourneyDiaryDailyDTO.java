package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.api.constant.AvisApiConstant;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;

@Data
public class CustomerJourneyDiaryDailyDTO {

    private Long id;

    @JsonProperty("date")
    private Timestamp date;

    private String customerNameUsed;

    @JsonProperty("customer_name_used")
    public String[] getCustomerNameUsed() {
        return Objects.isNull(customerNameUsed) ? AvisApiConstant.EMPTY_STRING_ARRAY
                : customerNameUsed.split(" \\\\n");
    }

    private String customerDepartment;

    @JsonProperty("customer_department")
    public String[] getCustomerDepartment() {
        return Objects.isNull(customerDepartment) ? AvisApiConstant.EMPTY_STRING_ARRAY
                : customerDepartment.split(" \\\\n");
    }

    private String tripItinerary;

    @JsonProperty("trip_itinerary")
    public String[] getTripItinerary() {
        return Objects.isNull(tripItinerary) ? AvisApiConstant.EMPTY_STRING_ARRAY
                : tripItinerary.split(" \\\\n");
    }

    @JsonProperty("used_km")
    private BigDecimal usedKm;

    @JsonProperty("used_km_self_drive")
    private BigDecimal usedKmSelfDrive;

    @JsonProperty("working_time_gps_from")
    private Time workingTimeGpsFrom;

    @JsonProperty("working_time_gps_to")
    private Time workingTimeGpsTo;

    @JsonProperty("over_time")
    private Integer overTime;

    @JsonProperty("over_km")
    private BigDecimal overKm;

    @JsonProperty("over_km_self_drive")
    private BigDecimal overKmSelfDrive;

    @JsonProperty("overnight")
    private Integer overnight;

    @JsonProperty("is_holiday")
    private Boolean isHoliday;

    @JsonProperty("is_weekend")
    private Boolean isWeekend;

    @JsonIgnore
    private Boolean isSelfDrive;

    @JsonProperty("is_self_drive")
    public Boolean getBoolSelfDrive() {
        return Objects.nonNull(usedKmSelfDrive);
    }

    @JsonProperty("is_with_driver")
    public Boolean getBoolWithDriver() {
        return Objects.nonNull(usedKm);
    }

    private String driverName;

    @JsonProperty("driver_name")
    public String[] getDriverName() {
        return Objects.isNull(driverName) ? AvisApiConstant.EMPTY_STRING_ARRAY
                : driverName.split(" \\\\n");
    }

    private String vehicleNumberPlate;

    @JsonProperty("vehicle_number_plate")
    public String[] getVehicleNumberPlate() {
        return Objects.isNull(vehicleNumberPlate) ? AvisApiConstant.EMPTY_STRING_ARRAY
                : vehicleNumberPlate.split(" \\\\n");
    }

    @JsonProperty("parking_fee")
    private BigDecimal parkingFee;

    @JsonProperty("tolls_fee")
    private BigDecimal tollsFee;

    private String note;

    @JsonProperty("note")
    public String[] getNote() {
        return Objects.isNull(note) ? AvisApiConstant.EMPTY_STRING_ARRAY
                : note.split(" \\\\n");
    }

    @Column(name = "journey_diary_id")
    private Long journeyDiaryId;
}
