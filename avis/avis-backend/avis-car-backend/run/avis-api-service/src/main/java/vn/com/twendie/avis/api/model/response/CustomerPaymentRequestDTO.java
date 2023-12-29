package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class CustomerPaymentRequestDTO {

    private Long id;

    @JsonProperty("contract_code")
    private String contractCode;

    @JsonProperty("contract_type_id")
    private Long contractTypeId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("admin_name")
    private String adminName;

    @JsonProperty("customer_address")
    private String customerAddress;

    @JsonProperty("driver_name")
    private String driverName;

    @JsonProperty("vehicle_number_plate")
    private String vehicleNumberPlate;

    @JsonProperty("from_date")
    private Timestamp fromDate;

    @JsonProperty("to_date")
    private Timestamp toDate;

    @JsonProperty("created_at")
    private Timestamp createdAt;

}
