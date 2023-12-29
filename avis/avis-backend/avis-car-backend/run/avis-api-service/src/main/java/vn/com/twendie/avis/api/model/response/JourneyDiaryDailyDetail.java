package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDiaryDailyDetail {

    @JsonProperty("contract")
    private ContractDTO contract;

    @JsonProperty("date_statistic")
    private DateStatistic dateStatistic;

    @JsonProperty("journey_diary_dailies")
    private List<JourneyDiaryDailyDTO> journeyDiaryDailies;

    @JsonProperty("total")
    private TotalRow totalRow;

}
