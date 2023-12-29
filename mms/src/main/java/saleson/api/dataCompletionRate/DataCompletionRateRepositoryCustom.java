package saleson.api.dataCompletionRate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.model.data.completionRate.CompletionRateData;

public interface DataCompletionRateRepositoryCustom {
    Page<CompletionRateData> getCompanyCompletionRate(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);
}
