package saleson.model.clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.*;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class MoldVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String toolingId;
    private String toolingLetter; // add
    //	private String name; // delete
    private String toolingType;

    private String toolingComplexity; // add
    private Boolean familyTool; // If multiple parts connected then true


    private Integer designedShot;
    private Integer madeYear;
    private Integer contractedCycleTime;
    private Long location;
    @Lob
    private String toolDescription;

//
/* Physical Information */
    private String size;
    @Enumerated(EnumType.STRING)
    private SizeUnit sizeUnit;
    private String weight; // Tool Weight
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;
    private Double shotSize; // Shot Weight
    private Long toolMaker;
    private String injectionMachineId;
    private Double quotedMachineTonnage; // add
    private Double currentMachineTonnage; // add
//runner
/*
    @Enumerated(EnumType.STRING)
    private RunnerType runnerType;
*/
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
    private Integer cycleTimeLimit2;
    @Enumerated(EnumType.STRING)
    private OutsideUnit cycleTimeLimit1Unit;
    @Enumerated(EnumType.STRING)
    private OutsideUnit cycleTimeLimit2Unit;
    private String engineerNames;
    private String engineerIds;
    private String maintenanceDocuments;
    private String instructionVideo;
//  Cost
    private Double cost;
    private String memo;
//    Supplier Information
    private Long supplier;
    private Integer uptimeTarget;
    private Integer uptimeLimitL1;
    private Integer uptimeLimitL2;
    private String labour;
    private String shiftsPerDay; // hour per day
    private String productionDays; // day per week
    private Integer maxCapacityPerWeek; // add

//    Part
    private String parts;
    private String partsName;
    //mold part object save by type moldId,partId,cavity;...
//    private String moldPartObject;
//    access
    private  String moldAuthorities;

    private Integer totalCavities;

    private Integer toolmakerContractedCycleTime;
}
