package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketFeeReportFilter {

    @JsonProperty("from")
    private Timestamp from;

    @JsonProperty("to")
    private Timestamp to;

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("vehicle_number_plates")
    private List<String> vehicleNumberPlates;

}
