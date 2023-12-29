package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class ListJourneyDiaryByContactIdDTO {

    @JsonProperty("time_start")
    private Date timeStart;

    @JsonProperty("time_end")
    private Date timeEnd;

    @JsonProperty("km_start")
    private BigDecimal kmStart;

    @JsonProperty("km_end")
    private BigDecimal kmEnd;

    @JsonProperty("km_total")
    private BigDecimal kmTotal;

    @JsonProperty("km_empty_total")
    private BigDecimal kmEmptyTotal;

    @JsonProperty("km_gps")
    private BigDecimal kmGps;

    @JsonProperty("total_cost")
    private BigDecimal totalCost;

}
