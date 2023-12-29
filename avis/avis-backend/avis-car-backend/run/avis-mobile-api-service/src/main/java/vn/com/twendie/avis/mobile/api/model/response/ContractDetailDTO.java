package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ContractDetailDTO {

    @JsonProperty("id") Long id;
    @JsonProperty("code") String code;
    @JsonProperty("from_date") Timestamp fromDate;
    @JsonProperty("status") String status;
    @JsonProperty("to_date")Timestamp toDate;
    @JsonProperty("customer_name") String customerName;
    @JsonProperty("member_name") String memberName;
    @JsonProperty("phone_number") String phoneNumber;
    @JsonProperty("address") String address;

    @JsonProperty("vehicle_type") String vehicleType;
    @JsonProperty("vehicle_color") String vehicleColor;
    @JsonProperty("vehicle_number_seat") Integer vehicleNumberSeat;
    @JsonProperty("vehicle_number_plate") String vehicleNumberPlate;

    @JsonProperty("contract_period_type") String contractPeriodType;
    @JsonProperty("country_code") String countryCode;
}
