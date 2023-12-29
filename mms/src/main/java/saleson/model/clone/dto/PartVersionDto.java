package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartVersionDto {
    private Long id;
    private String name;
    private String partCode;
    private String resinCode;
    private String resinGrade;
    private String designRevision;
    private String size;
    private String sizeUnit;
    private String weight;
    private String weightUnit;
    private Long categoryId;
    private String categoryName;
    private boolean enabled;
}
