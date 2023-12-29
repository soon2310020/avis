package com.emoldino.api.common.resource.composite.tolstp.dto;

import static com.emoldino.api.common.resource.composite.datimp.service.enumeration.MaintenanceIntervalType.MONTHLY_TIME_BASED;
import static com.emoldino.api.common.resource.composite.datimp.service.enumeration.MaintenanceIntervalType.SHOT_BASED;
import static com.emoldino.api.common.resource.composite.datimp.service.enumeration.MaintenanceIntervalType.WEEKLY_TIME_BASED;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_FREQUENCY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.RECURR_CONSTRAINT_TYPE;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.location.enumeration.AreaType;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.api.common.resource.composite.datimp.service.enumeration.MonthDays;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import saleson.api.mold.payload.PartData;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.PresetStatus;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.ToolingCondition;
import saleson.common.enumeration.UseageType;
import saleson.common.enumeration.WeightUnit;
import saleson.common.util.DateUtils;
import saleson.model.Mold;
import saleson.model.data.CompanyLiteData;
import saleson.model.data.MiniComponentData;

@Data
@NoArgsConstructor
public class TolStpItem {
	private Long id;
	// Mold ID
	private String equipmentCode;
	private String supplierMoldCode;

	// Mold Status
	private EquipmentStatus equipmentStatus;
	private OperatingStatus operatingStatus;
	private Instant lastShotAt;
	private Instant lastShotAtVal;
	private Boolean dataFilterEnabled;
	private Boolean backUp;

	//Counter
	private Long counterId;
	private String counterCode;

	private String toolingLetter; // add
	private String toolingType;
	private String toolingComplexity; // add
	private Integer designedShot; // Forecasted Max shots - Depreciation term (Shots)
	private Integer lifeYears; // Forecasted Tool Life - Depreciation Term (Years)
	private Integer madeYear;
	private String engineer;
	private List<UserLiteDTO> engineers;
	private List<UserLiteDTO> plantEngineers;
	private String toolDescription;
	// Company
	private Long companyIdByLocation;
//	private Long companyId;
	private String companyName;
	private String companyCode;
	//Part
	private List<PartData> parts;

	/*Physical Information*/
	private String size;
	//template sync
	private String sizeW;
	private String sizeL;
	private String sizeH;

	private SizeUnit sizeUnit;

	private String weight; // Tool Weight
	private WeightUnit weightUnit;

	private String toolingSizeView;
	private String toolingWeightView;
	private Double shotSize; // Shot Weight

	private Long toolMakerCompanyId;
	private String toolMakerCompanyCode;
	private String toolMakerCompanyName;
	private String injectionMachineId;
	private Integer totalCavities;
	private Double quotedMachineTonnage; // add
//	private Double currentMachineTonnage; // add

	/*Runner System Information*/
	private String runnerTypeTitle;
	private String runnerType;
	private String runnerMaker;
	private Double weightRunner; // add
	private String weightUnitTitle;
	private String hotRunnerDrop; // add Hot Runner of Drop
	private String hotRunnerZone; // add Hot Runner Zone

	/*Dynamic Information*/
	private ToolingStatus toolingStatus;
	private Integer accumulatedShot;
//	private Instant lastShotMadeAt; // last shot with shot count > 0
	private String lastShotDate;
	private String lastShotDateVal;
//	private String lastShotDateTime;
	private Integer lastShot;
	private Long remainingPartsCount;

	// Plant
	private Long locationId;
	private String locationName;
	private String locationCode;
	private Long areaId;
	private String areaName;
	private AreaType areaType;


	private Double lastCycleTime;
	private Double weightedAverageCycleTime;

	/* Cost Information */
	private Double cost; // for not Dyson
	private Integer accumulatedMaintenanceCost; // for not Dyson
	private Double salvageValue;
	private CurrencyType salvageCurrency;
	private CurrencyType costCurrencyType;
//	private Integer totalCmCount; // 전체 고장정비 횟수
//	private Integer totalCmCosts; // 전체 고장정비 비용.
	private Instant poDate;
	private String poNumber;
	private String memo;

	/*S.L. Depreciation U.P. Depreciation*/
	private Double slCurrentBookValue;
	private Integer slDepreciation;
	private String slDepreciationTitle;
	private Integer slDepreciationTerm;
	private Double slYearlyDepreciation;
	private Instant slLatestDepreciationPoint;
	private Double upCurrentBookValue;
	private Double upDepreciation;
	private Integer upDepreciationTerm;
	private Double upDepreciationPerShot;
	private String depreciationPercentage;
	private String depreciationPercentageTitle;
	private Double upDepreciationPerShotRaw;
	private Instant upLatestDepreciationPoint;

	/*Supplier Information*/
	private Long supplierCompanyId;
	private String supplierCompanyName;
	private String supplierCompanyCode;
	private String labour;
	private String shiftsPerDay; // hour per day
	private String productionDays; // day per week
	private Integer maxCapacityPerWeek; // add Theoretical number of parts to be produced
	private Integer dailyMaxCapacity; // add daily capacity part produced
	private Integer passedDays; // how long does it pass from registered date to current

	/*Production Information*/
	private Integer contractedCycleTime;
	private Double contractedCycleTimeSeconds;
	private Integer toolmakerContractedCycleTime;
	private Double toolmakerContractedCycleTimeSeconds;
	private Integer cycleTimeLimit1; // contrantedCycleTime 기준 L1 : cycleTimeLimit1 <= L1 < cycleTimeLimit2
	private OutsideUnit cycleTimeLimit1Unit;
	private Integer cycleTimeLimit2; // contrantedCycleTime 기준 L2 : cycleTimeLimit2 < L2
	private OutsideUnit cycleTimeLimit2Unit;
	private Integer uptimeTarget;
	private Integer uptimeLimitL1;
	private Integer uptimeLimitL2;

	/* Maintenance Information */
	private Integer preventCycle;
	private Integer preventUpcoming; // upcoming maintenance tolerance
	private Integer preventOverdue; // overdue maintenance tolerance
	private String useageTypeName;
	private Instant lastMaintenanceDate;

	private Instant lastRefurbishmentDate;

	private Instant lastWorkOrderDate;
	private Long workOrderCount;

	/*Other*/
	private ToolingCondition toolingCondition;
	private Integer inactivePeriod; //number of inactive month
	private NotificationStatus dataSubmission;
	private List<FieldValue> customFields;
//	private Map<Long, List<CustomFieldValueDTO>> customFieldValueMap = new HashMap<>();
//    private Map<Long, Map<Long, List<CustomFieldValueDTO>>> customFieldValueMapForPart = new HashMap<>();
	private List<CompanyLiteData> upperTierCompanies;
	//mold maintenance alert
//    private MoldMaintenanceDTO moldMaintenance;
	private MaintenanceStatus maintenanceStatus;
	private Integer maintenanceLastShotMade; // last shot made since the last period
	public Integer pmCheckpointPrediction;
//	private Integer maintenancePeriod;
	private String moldStatus;
	private String counterStatus;
	private Double ctCompliance;
	private Integer pmCheckpoint;
	private Integer untilNextPm;

	private PM_STRATEGY pmStrategy;
	//for template sync
	private PM_FREQUENCY pmFrequency;
	private String schedDayOfWeek;
	private String schedStartDate;
	private Integer schedInterval;
	private Integer schedOrdinalNum;
	private RECURR_CONSTRAINT_TYPE recurrConstraintType;
	private Integer recurrNum;
	private String recurrDueDate;
	private Integer schedUpcomingTolerance;

	//unused in tooling table
	private Boolean familyTool; // If multiple parts connected then true
	private CycleTimeStatus cycleTimeStatus;
	private EfficiencyStatus efficiencyStatus;
	private MisconfigureStatus misconfigureStatus; // Preset 등록 요청 후 완료 되지 않은 채 데이터가 들어오는 경우
	private String resin;
	private Double utilizationRate;
	private String supplierForToolMaker; // In case Tool-maker created a tooling, free enter to supplier instead
	private UseageType useageType;
	private Boolean locationChanged;
	private Boolean maintenanced;
	private Integer maintenanceCount;
	private Instant operatedStartAt;
	private boolean deleted;
	private String day;
	private String week;
	private String month;
	//customize value
	private long totalProduced;
	private long predictedQuantity;
	private long totalMaxCapacity;
	private PresetStatus counterPresetStatus;
	private Integer nextMaintenanceShot;
	private String sizeUnitTitle;
	private String toolingConditionTitle;
	private String costCurrencyTypeTitle;
	private Integer contractedCycleTimeToCalculation;
	private MiniComponentData machineMini;
	private String poDateFormat;

	private Double tco;

	@QueryProjection
	public TolStpItem(//
			Mold mold, //
			Float ctCompliance, //
			Integer dueDate, //
			Integer pmCheckpoint, //
			Integer untilNextPm, //
			PM_STRATEGY pmStrategy, //

			//for template
			PM_FREQUENCY pmFrequency, //
			String schedDayOfWeek, //
			String schedStartDate, //
			Integer schedInterval, //
			Integer schedOrdinalNum, //
			RECURR_CONSTRAINT_TYPE recurrConstraintType, //
			Integer recurrNum, //
			String recurrDueDate, //
			Integer schedUpcomingTolerance, //

			Integer inactiveDays, //
			Integer lastShotMade, //
			MaintenanceStatus maintenanceStatus, //
//			Integer maintenancePeriod, //
			Boolean dataFilterEnabled, //
			Integer accumulatedShot, //
			Long remainingPartsCount, //
			Instant lastWorkOrderAt, //
			Long workOrderCount//
	) {
		ValueUtils.map(mold, this);
		String zoneId = LocationUtils.getZoneIdByLocationId(locationId);

		this.ctCompliance = ValueUtils.toDouble(ctCompliance, null);
		this.pmCheckpointPrediction = dueDate;
		this.pmCheckpoint = pmCheckpoint;
		this.untilNextPm = untilNextPm;
		this.pmStrategy = pmStrategy;

		this.pmFrequency = pmFrequency;
		this.schedDayOfWeek = schedDayOfWeek;
		this.schedStartDate = schedStartDate;
		this.schedInterval = schedInterval;
		this.schedOrdinalNum = schedOrdinalNum;
		this.recurrConstraintType = recurrConstraintType;
		this.recurrNum = recurrNum;
		this.recurrDueDate = recurrDueDate;
		this.schedUpcomingTolerance = schedUpcomingTolerance;

		if (inactiveDays != null) {
			this.inactivePeriod = Math.round(inactiveDays / 30);
		}
		this.upLatestDepreciationPoint = getUpLatestDepreciationPoint(this.poDate, this.lastShot, this.lastShotAt, this.designedShot);
		//mold maintenance
		this.maintenanceLastShotMade = lastShotMade;
		this.maintenanceStatus = maintenanceStatus;
//		this.maintenancePeriod = maintenancePeriod;
		this.dataFilterEnabled = dataFilterEnabled;
		this.backUp = mold.getBackup();
		this.accumulatedShot = accumulatedShot;
		this.remainingPartsCount = remainingPartsCount;
		this.lastWorkOrderDate = lastWorkOrderAt;
		this.workOrderCount = workOrderCount;

		this.lastShotDate = DateUtils2.format(mold.getLastShotAt(), DateUtils2.DatePattern.yyyy_MM_dd, zoneId);
		this.lastShotDateVal = DateUtils2.format(mold.getLastShotAtVal(), DateUtils2.DatePattern.yyyy_MM_dd, zoneId);
		//remove because data leak
/*
		if (mold.getEngineersInCharge() != null) {
			this.engineers = mold.getEngineersInCharge().stream() //
					.map(UserLiteDTO::new) //
					.collect(Collectors.toList());
		}
		if (mold.getPlantEngineersInCharge() != null) {
			this.plantEngineers = mold.getPlantEngineersInCharge().stream() //
					.map(UserLiteDTO::new) //
					.collect(Collectors.toList());
		}
*/
		slipSize();
	}

	public static Instant getUpLatestDepreciationPoint(Instant poDate, Integer lastShot, Instant lastShotAt, Integer designedShot) {
		if (poDate != null) {
			if ((lastShot != null ? lastShot : 0) < designedShot) {
				Instant latest = DateUtils.plusMonths(poDate, DateUtils.getCeilRoundedMonthBetween(poDate, Instant.now()).intValue() - 1);
				if (latest.compareTo(Instant.now()) < 0) {
					return latest;
				} else {
					return DateUtils.plusMonths(poDate, DateUtils.getCeilRoundedMonthBetween(poDate, Instant.now()).intValue() - 2);
				}
			} else {
				return lastShotAt;
			}
		}
		return null;
	}

	@Data
	public static class AccumulatedShot {
		private Long moldId;
		private Integer accumulatedShot;

		public AccumulatedShot(Long moldId, Integer accumulatedShot) {
			this.moldId = moldId;
			this.accumulatedShot = accumulatedShot;
		}
	}

	public String getPmStrategyTitle() { //
		if (Objects.nonNull(this.pmStrategy)) { //
			switch (this.pmStrategy) { //
			case SHOT_BASED: //
				return SHOT_BASED.getTitle();
			case TIME_BASED: //
				if (this.pmFrequency != null) { //
					if (PM_FREQUENCY.WEEKLY.equals(this.pmFrequency)) { //
						return WEEKLY_TIME_BASED.getTitle();
					} else if (PM_FREQUENCY.MONTHLY.equals(this.pmFrequency)) { //
						return MONTHLY_TIME_BASED.getTitle();
					}
				}
				break;
			}
		}
		return SHOT_BASED.getTitle();
	}

	/**
	 * @return FIRST("1st"), SECOND("2nd"), THIRD("3rd"), FORTH("4th");
	 */
	public String getSchedOrdinalNum(Integer schedOrdinalNum) { //
		if (schedOrdinalNum != null) { //
			switch (schedOrdinalNum) { //
			case 1:
				return MonthDays.FIRST.getTitle();
			case 2:
				return MonthDays.SECOND.getTitle();
			case 3:
				return MonthDays.THIRD.getTitle();
			case 4:
				return MonthDays.FORTH.getTitle();
			}
		}
		return MonthDays.FIRST.getTitle();
	}

	//Maintenance Interval (default 50000)
	public Integer getPreventCycleInShotBased() { //
		return getPmStrategyTitle().equals(SHOT_BASED.getTitle()) ? this.preventCycle : null;
	}

	//Upcoming Maintenance Tolerance (default 10000)
	public Integer getPreventUpcomingInShotBased() { //
		return getPmStrategyTitle().equals(SHOT_BASED.getTitle()) ? this.preventUpcoming : null;
	}

	// Schedule every (week(s))
	public Integer getSchedInterval() { //
		return getPmStrategyTitle().equals(WEEKLY_TIME_BASED.getTitle()) ? this.schedInterval : null;
	}

	// On Date (Monday, Tuesday, etc)
	public String getSchedDayOfWeek() { //
		return getPmStrategyTitle().equals(WEEKLY_TIME_BASED.getTitle()) ? WordUtils.capitalizeFully(this.schedDayOfWeek) : null;
	}

	// First schedule starts on (yyyyMMdd)
	public String getSchedStartDate() { //
		return getPmStrategyTitle().equals(WEEKLY_TIME_BASED.getTitle()) ? this.schedStartDate : null;
	}

	// Schedule every (month(s))
	public Integer getSchedIntervalMonth() { //
		return getPmStrategyTitle().equals(MONTHLY_TIME_BASED.getTitle()) ? this.schedInterval : null;
	}

	// On (1st ,2nd,etc.)
	public String getSchedOrdinalNumTitle() { //
		return getPmStrategyTitle().equals(MONTHLY_TIME_BASED.getTitle()) ? getSchedOrdinalNum(this.schedOrdinalNum) : "";
	}

	// Date (Monday, Tuesday, etc)
	public String getSchedDayOfWeekMonth() { //
		return getPmStrategyTitle().equals(MONTHLY_TIME_BASED.getTitle()) ? WordUtils.capitalizeFully(this.schedDayOfWeek) : null;
	}

	// First schedule starts on (yyyyMMdd)
	public String getSchedStartDateMonth() { //
		return getPmStrategyTitle().equals(MONTHLY_TIME_BASED.getTitle()) ? this.schedStartDate : null;
	}

	// Continue
	public String getForever() { //
		return PM_STRATEGY.TIME_BASED.equals(pmStrategy) && RECURR_CONSTRAINT_TYPE.FOREVER.equals(this.recurrConstraintType) ? "TRUE" : "";
	}

	public String getBackUp() {
		return this.backUp ? "Yes" : "No";
	}


	// OEM Engineer in Charge
	public String getEngineerEmailsStr() { //
		return (this.engineers != null) ? //
				this.engineers.stream()//
						.map(UserLiteDTO::getEmail)//
						.collect(Collectors.joining(", ")) //
				: "";
	}

	// Plant Engineer in Charge
	public String getPlantEngineerEmailsStr() { //
		return (this.plantEngineers != null) ? //
				this.plantEngineers.stream()//
						.map(UserLiteDTO::getEmail)//
						.collect(Collectors.joining(", ")) //
				: "";
	}

	public void slipSize() { //
		if (isValidSize()) { //
			String[] parts = this.size.split("x");
			if (parts.length == 3) { //
				this.sizeW = parts[0];
				this.sizeL = parts[1];
				this.sizeH = parts[2];
			} else { //
				System.err.println("Invalid input format: Size must have exactly 3 parts separated by 'x'.");
			}
		}
	}

	public boolean isValidSize() { //
		return size != null && size.matches("^[0-9].*");
	}

}
