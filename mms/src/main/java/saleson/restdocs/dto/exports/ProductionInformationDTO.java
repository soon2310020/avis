package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class ProductionInformationDTO {
    private String approvedCycleTime;
    private String toolmakerApprovedCycleTime;
    private String cycleTimeToleranceL1;
    private String cycleTimeToleranceL2;

    private String targetUptime;
    private String uptimeToleranceL1;
    private String uptimeToleranceL2;
}
