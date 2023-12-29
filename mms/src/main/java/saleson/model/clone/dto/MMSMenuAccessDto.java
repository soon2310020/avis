package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MMSMenuAccessDto {
    private boolean quickStats;
    private boolean distribute;
    private boolean projectHierarchy;
    private boolean capacityUtilization;
    private boolean cycleTimeStatus;
    private boolean toolingStatus;
    private boolean preventiveMaintenance;
    private boolean uptimeStatus;
    private boolean downtime;
    private boolean overallEquipmentEffectiveness;
    private boolean productionRate;
    private boolean utilizationRate;
    private boolean parts;
    private boolean tooling;
    private boolean relocation;
    private boolean disconnection;
    private boolean cycleTime;
    private boolean maintenance;
    private boolean uptime;
    private boolean reset;
    private boolean dataSubmission;
    private boolean toolingBenchmarking;
    private boolean advancedSearch;
}
