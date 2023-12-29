package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVersionDto {
    private Long id;
    private String name;
    private String description;
    private boolean enabled;
    private String parentName;
}
