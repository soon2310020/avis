package saleson.api.dataCompletionRate.payload;

import lombok.Data;
import saleson.model.data.completionRate.CompletionRateData;

@Data
public class DataCompletionItem {
    private String name;
    private String dueDate;
    private boolean completed;
    private Object data;
}
