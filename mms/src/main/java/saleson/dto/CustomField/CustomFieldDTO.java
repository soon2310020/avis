package saleson.dto.CustomField;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.customField.PropertyGroup;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomFieldDTO {
	private Long id;
	private ObjectType objectType;
	private PropertyGroup propertyGroup;
	private String fieldName;

	private Boolean required;
	private Boolean defaultInput;
	private Integer position = 0;

	private List<CustomFieldValueDTO> customFieldValueDTOList = new ArrayList<>();

}
