package saleson.api.dataRequest.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import saleson.model.DataRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllDataRequest {
    private Page<DataRequest> data;
    private long allCount;
    private long dataRegistrationCount;
    private long dataCompletionCount;
}
