package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoldVersionDto {
    private Long id;
    private String toolingId;
    private String toolingLetter; // add
    private String toolingType;

    private String toolingComplexity; // add
    private Boolean familyTool; // If multiple parts connected then true
    private Integer designedShot;
    private Integer madeYear;
    private Integer contractedCycleTime;
    private Long location;
    private String locationName;
    private String toolDescription;

    //
    /* Physical Information */
    private String size;
//    private String sizeUnit;
    private String sizeUnitStr;

    private String weight; // Tool Weight
//    private String weightUnit;
    private String weightUnitStr;
    private Double shotSize; // Shot Weight
    private Long toolMaker;
    private String toolMakerName;
    private String injectionMachineId;
    private Double quotedMachineTonnage; // add
    private Double currentMachineTonnage; // add
    //runner
    private String runnerType;
    private String runnerMaker;
    private Double weightRunner; // add
    private String hotRunnerDrop; // add Hot Runner of Drop
    private String hotRunnerZone; // add Hot Runner Zone

    //    main
    private String conditionOfTooling;
    private Integer preventCycle;
    private Integer preventUpcoming;		// upcoming maintenance tolerance
    private Integer preventOverdue;			// overdue maintenance tolerance
    private Integer cycleTimeLimit1;		// contrantedCycleTime 기준 L1 : cycleTimeLimit1 <= L1 < cycleTimeLimit2
    private String cycleTimeLimit1UnitStr;
    private Integer cycleTimeLimit2;
    private String cycleTimeLimit2UnitStr;
    private List<String> engineerNameArr;
    private String maintenanceDocuments;
    private String instructionVideo;

//    Cost
    private Integer cost;
    private String memo;

    //    Supplier Information
    private Long supplier;
    private String supplierName;
    private Integer uptimeTarget;
    private Integer uptimeLimitL1;
    private Integer uptimeLimitL2;
    private String labour;
    private String shiftsPerDay; // hour per day
    private String productionDays; // day per week
    private Integer maxCapacityPerWeek; // add

    //    Part
    private String moldParts;
    private List<String> moldPartNames;
    //    access
    private String moldAuthorities;
    private List<String> authorities;

    private Integer totalCavities;
}
