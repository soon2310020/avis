package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class MaintenanceInformationDto {
    private String conditionOfTooling;
    private String maintenanceInterval;
    private String upcomingMaintenanceTolerance;
    private String overdueMaintenanceTolerance;
    private String cycleTimeToleranceL1;
    private String cycleTimeToleranceL2;
    private String status;
    private String engineerInCharge;
    private String maintenanceDocument;
    private String instructionVideo;

}
