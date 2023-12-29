package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class DynamicInformationDto {
    private String op;
    private String noOfShots;
    private String lastDateOfShots;
    private String utilisationRate;
    private String remainingPartsCount;
    private String location;
    private String cycleTime;
    private String weightedAverageCycleTime;
}
