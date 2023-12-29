package saleson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.*;
import saleson.common.util.DataUtils;
import saleson.common.util.StringUtils;
import saleson.model.Mold;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolingDTO {
    private Long id;
    @JsonProperty("code")
    private String equipmentCode;

    @JsonProperty("counter_id")
    private Long counterId;
    @JsonProperty("counter_code")
    private String counterCode;
    @JsonProperty("letter")
    private String toolingLetter;
    @JsonProperty("type")
    private String toolingType;
    @JsonProperty("complexity")
    private String toolingComplexity;
    @JsonProperty("max_shots")
    private Integer designedShot;

    @JsonProperty("life")
    private Integer lifeYears;

    @JsonProperty("made_year")
    private Integer madeYear;
    @JsonProperty("approved_cycle_time")
    private Double contractedCycleTime;
    @JsonProperty("description")
    private String toolDescription;

    private List<MoldPartDTO> parts;
//    @JsonProperty("category_name")
//    private String categoryName;
//    @JsonProperty("project_name")
//    private String projectName;
//    @JsonProperty("id")
//    private Long id;
//    @JsonProperty("name")
//    private String name;
//    @JsonProperty("cavities")
//    private Integer cavities;

//physic
    private String size;
    @JsonProperty("size_width")
    private Integer sizeWidth;
    @JsonProperty("size_length")
    private Integer sizeLength;
    @JsonProperty("size_height")
    private Integer sizeHeight;
    @JsonProperty("size_unit")
    @Enumerated(EnumType.STRING)
    private SizeUnit sizeUnit;

    private Double weight; // Tool Weight
    @JsonProperty("weight_unit")
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;


    @JsonProperty("shot_weight")
    private Double shotSize;

    @JsonProperty("tool_maker_name")
    private String toolMakerCompanyName;

    @JsonProperty("injection_molding_machine_id")
    private String injectionMachineId;

    @JsonProperty("machine_tonnage")
    private Double quotedMachineTonnage; // add
    @JsonProperty("current_machine_tonnage")
    private Double currentMachineTonnage; // add

//runner_sys_info
    @JsonProperty("runner_type")
    @Enumerated(EnumType.STRING)
    private RunnerType runnerType;
    @JsonProperty("runner_maker")
    private String runnerMaker;
    @JsonProperty("runner_weight")
    private Double weightRunner; // add
    @JsonProperty("hot_runner_number_drop")
    private String hotRunnerDrop; // add Hot Runner of Drop
    @JsonProperty("hot_runner_zone")
    private String hotRunnerZone; // add Hot Runner Zone

//dynamic-info
    @JsonProperty("op_status")
    @Enumerated(EnumType.STRING)
    private OperatingStatus operatingStatus;


    @JsonProperty("shots")
    private Integer lastShot;
    @JsonProperty("last_shot_time")
    private Instant lastShotAt;

    @JsonProperty("utilization_rate")
    private Double utilizationRate;
    @JsonProperty("location_name")
    private String locationName;
    @JsonProperty("cycle_time")
    private Double lastCycleTime;
    //maintenance_info
    @JsonProperty("maintenance_interval")
//    private Integer maintenanceInterval;
    private Integer preventCycle;

    @JsonProperty("upcoming_maintenance_tolerance")
    private Integer preventUpcoming;
    @JsonProperty("overdue_maintenance_tolerance")
    private Integer preventOverdue;
    @JsonProperty("cycle_time_tolerance_l1")
    private Double cycleTimeLimit1;
    @JsonProperty("cycle_time_tolerance_l1_unit")
    @Enumerated(EnumType.STRING)
    private OutsideUnit cycleTimeLimit1Unit;
    @JsonProperty("cycle_time_tolerance_l2")
    private Double cycleTimeLimit2;
    @JsonProperty("cycle_time_tolerance_l2_unit")
    @Enumerated(EnumType.STRING)
    private OutsideUnit cycleTimeLimit2Unit;
    @JsonProperty("cycle_time_status")
    @Enumerated(EnumType.STRING)
    private CycleTimeStatus cycleTimeStatus;
    @JsonProperty("engineer_in_charge")
    private List<String> engineerNames;

//cost_info

    @JsonProperty("tooling_cost")
    private Integer cost; // for not Dyson
    @JsonProperty("accumulated_maintenance_cost")
    private Integer accumulatedMaintenanceCost; // for not Dyson

    @JsonProperty("memo")
    private String memo;

//supplier_info
    @JsonProperty("supplier_name")
    private String supplierCompanyName;
    @JsonProperty("uptime_target")
    private Integer uptimeTarget;
    @JsonProperty("uptime_tolerance_l1")
    private Integer uptimeLimitL1;
    @JsonProperty("uptime_tolerance_l2")
    private Integer uptimeLimitL2;
    @JsonProperty("labour")
    private String labour;

    @JsonProperty("daily_production_hour")
    private Integer dailyProductionHour;
    @JsonProperty("weekly_production_day")
    private Integer weeklyProductionDay;
    @JsonProperty("weekly_max_capacity")
    private Integer maxCapacityPerWeek;


    public static ToolingDTO convertToDTO(Mold mold){

        /* wrong saved column value error (Dyson)
                mysql> select distinct RUNNER_TYPE from MOLD ;
                +-------------+
                | RUNNER_TYPE |
                +-------------+
                | HOT         |
                | COLD        |
                | NULL        |
                | Hot runner  |
                | Cold runner |
                +-------------+

         */

        if (mold.getRunnerType() != null) {
            AtomicReference<Boolean> found = new AtomicReference<>(false);
            Arrays.stream(RunnerType.values()).forEach(r -> {
                if (mold.getRunnerType().toUpperCase().startsWith(r.name())) {
                    mold.setRunnerType(r.name());
                    found.set(true);
                    return;
                }
            });

            if (!found.get()) {
                mold.setRunnerType(null);
            }
        }

        ToolingDTO toolingDTO = DataUtils.mapper.map(mold,ToolingDTO.class);
        try{
            if(!StringUtils.isEmpty(mold.getSize())){
                String[] sizeArr=mold.getSize().toLowerCase().split("x");
                if(sizeArr.length>=3){
                    toolingDTO.setSizeWidth(Integer.valueOf(sizeArr[0].trim()));
                    toolingDTO.setSizeLength(Integer.valueOf(sizeArr[1].trim()));
                    toolingDTO.setSizeHeight(Integer.valueOf(sizeArr[2].trim()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            if (mold.getShiftsPerDay() != null)
                toolingDTO.setDailyProductionHour(Integer.valueOf(mold.getShiftsPerDay()));
            if (mold.getProductionDays() != null)
                toolingDTO.setWeeklyProductionDay(Integer.valueOf(mold.getProductionDays()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mold.getMoldParts() != null) {
            toolingDTO.setParts(mold.getMoldParts().stream().map(mp-> MoldPartDTO.convertToDTO(mp)).collect(Collectors.toList()));
        }

        return toolingDTO;

    }
}
