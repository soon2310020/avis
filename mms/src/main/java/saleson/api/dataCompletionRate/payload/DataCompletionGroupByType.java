package saleson.api.dataCompletionRate.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import saleson.common.enumeration.ObjectType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataCompletionGroupByType {
    private Long id;
    private String orderId;
    private String managerName;
    private String companyName;
    private ObjectType objectType;
    private Page<DataCompletionItem> items;
}
