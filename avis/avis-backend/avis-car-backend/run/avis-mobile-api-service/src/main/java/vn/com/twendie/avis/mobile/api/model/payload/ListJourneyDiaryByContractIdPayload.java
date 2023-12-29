package vn.com.twendie.avis.mobile.api.model.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListJourneyDiaryByContractIdPayload {
    private Long contractId;

    private Long fromTime;

    private Long toTime;
}
