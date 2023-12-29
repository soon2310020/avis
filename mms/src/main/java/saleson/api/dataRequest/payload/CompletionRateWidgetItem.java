package saleson.api.dataRequest.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletionRateWidgetItem {
    private Long numberItem;
    private Double completedPercent;
}
