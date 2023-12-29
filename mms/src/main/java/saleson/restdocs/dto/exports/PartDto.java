package saleson.restdocs.dto.exports;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartDto {
    private Long id;
    private String category;
    private String project;
    private String partID;
    private String partName;
    private String partResinCode;
    private String partResinGrade;
    private String designRevisionLevel;
    private String partVolumn;
    private String partWeight;
    private String numberOfCavities;
    private String weeklyDemand;
    private String quantityRequired;

}
