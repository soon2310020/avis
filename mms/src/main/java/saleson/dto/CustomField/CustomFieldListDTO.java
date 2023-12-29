package saleson.dto.CustomField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldListDTO {
    private List<CustomFieldDTO> customFieldDTOList = new ArrayList<>();
}
