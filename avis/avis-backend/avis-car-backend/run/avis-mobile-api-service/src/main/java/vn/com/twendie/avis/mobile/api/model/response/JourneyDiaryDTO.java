package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDiaryDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("driver_id")
    private Long driverId;

    @JsonProperty("contract_id")
    private Long contractId;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("time_start")
    private Timestamp timeStart;

    @JsonProperty("address_start")
    private String addressStart;

    @JsonProperty("km_odo_start")
    private BigDecimal kmOdoStart;

    @JsonProperty("km_driver_start")
    private BigDecimal kmDriverStart;

    @JsonProperty("image_odo_link_start")
    private String imageOdoLinkStart;

    @JsonProperty("time_customer_get_in")
    private Timestamp timeCustomerGetIn;

    @JsonProperty("address_customer_get_in")
    private String addressCustomerGetIn;

    @JsonProperty("km_odo_customer_get_in")
    private BigDecimal kmOdoCustomerGetIn;

    @JsonProperty("km_driver_customer_get_in")
    private BigDecimal kmDriverCustomerGetIn;

    @JsonProperty("customer_name_used")
    private String customerNameUsed;

    @JsonProperty("customer_department")
    private String customerDepartment;

    @JsonProperty("image_odo_link_customer_get_in")
    private String imageOdoLinkCustomerGetIn;

    @JsonProperty("image_customer_get_in_link")
    private String imageCustomerGetInLink;

    @JsonProperty("time_customer_get_out")
    private Timestamp timeCustomerGetOut;

    @JsonProperty("address_customer_get_out")
    private String addressCustomerGetOut;

    @JsonProperty("km_driver_customer_get_out")
    private BigDecimal kmDriverCustomerGetOut;

    @JsonProperty("km_odo_customer_get_out")
    private BigDecimal kmOdoCustomerGetOut;

    @JsonProperty("image_odo_link_customer_get_out")
    private String imageOdoLinkCustomerGetOut;

    @JsonProperty("image_customer_get_out_link")
    private String imageCustomerGetOutLink;

    @JsonProperty("time_breakdown")
    private Timestamp timeBreakdown;

    @JsonProperty("km_odo_breakdown")
    private BigDecimal kmOdoBreakdown;

    @JsonProperty("km_driver_breakdown")
    private BigDecimal kmDriverBreakdown;

    @JsonProperty("image_odo_breakdown_link")
    private String imageOdoBreakdownLink;

    @JsonProperty("image_breakdown_link")
    private String imageBreakdownLink;

    @JsonProperty("time_end")
    private Timestamp timeEnd;

    @JsonProperty("address_end")
    private String addressEnd;

    @JsonProperty("km_odo_end")
    private BigDecimal kmOdoEnd;

    @JsonProperty("km_driver_end")
    private BigDecimal kmDriverEnd;

    @JsonProperty("image_odo_link_end")
    private String imageOdoLinkEnd;

    @JsonProperty("step")
    private Integer step;

}