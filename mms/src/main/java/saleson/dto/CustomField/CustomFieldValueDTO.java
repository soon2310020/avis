package saleson.dto.CustomField;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.customField.CustomFieldValue;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomFieldValueDTO {
    private Long id;
    private Long objectId;
    private String value;

    public CustomFieldValueDTO(String value) {
        this.value = value;
    }

    public CustomFieldValueDTO(CustomFieldValue customFieldValue) {
        ValueUtils.map(customFieldValue, this);
    }
}
