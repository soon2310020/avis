package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DateStatistic {

    @JsonProperty("dates_of_month")
    private long datesOfMonth;

    @JsonProperty("contract_working_days")
    private long contractWorkingDays;

    @JsonProperty("real_working_days")
    private long realWorkingDays;

}
