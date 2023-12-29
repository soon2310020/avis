package saleson.api.dataRequest.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRateWidgetData {
    private CompletionRateWidgetItem company;
    private CompletionRateWidgetItem location;
    private CompletionRateWidgetItem part;
    private CompletionRateWidgetItem mold;
    private CompletionRateWidgetItem machine;
    private Double overallCompletionRate;

}
