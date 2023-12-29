package saleson.model;

import static saleson.common.service.ExportService.formatter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.api.mold.payload.PartData;
import saleson.common.enumeration.CounterStatus;
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
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.dto.MoldMaintenanceDTO;
import saleson.dto.UserLiteDTO;
import saleson.model.customField.CustomFieldValue;
import saleson.model.data.CompanyLiteData;
import saleson.model.data.MiniComponentData;
import saleson.model.support.Equipment;
import saleson.service.util.NumberUtils;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DynamicUpdate
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder(alphabetic = true)
public class Mold extends Equipment {
	@Id
	@GeneratedValue
	private Long id;

	private String supplierMoldCode;

	@Column(name = "SUPPLIER_COMPANY_ID", insertable = false, updatable = false)
	private Long supplierCompanyId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SUPPLIER_COMPANY_ID")
	private Company supplier;

	@Column(name = "TOOL_MAKER_COMPANY_ID", insertable = false, updatable = false)
	private Long toolMakerCompanyId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TOOL_MAKER_COMPANY_ID")
	private Company toolMaker;

	@Enumerated(EnumType.STRING)
	private ToolingStatus toolingStatus;
	@Transient
	private Integer untilNextPm;
	@Transient
	private Integer pmCheckpoint;
	@Transient
	private Integer pmCheckpointPrediction;
	@Transient
	private PM_STRATEGY pmStrategy;

	// Removed this method after Relocation Process Finished
//	@Override
//	public void setCompanyId(Long companyId) {
//		super.setCompanyId(companyId);
//		if (companyId == null || getSupplierCompanyId() == null) {
//			return;
//		}
//		List<Long> ids = AccessControlUtils.getAllAccessibleCompanyIds(getSupplierCompanyId());
//		if (!ids.contains(companyId)) {
//			return;
//		}
//		Company company = BeanUtils.get(CompanyRepository.class).findById(companyId).orElse(null);
//		setSupplier(company);
//	}

	public ToolingStatus getToolingStatus() {
		return toolingStatus != null ? toolingStatus : MoldUtils.toToolingStatus(this);
	}

	@Override
	public void setEquipmentStatus(EquipmentStatus equipmentStatus) {
		super.setEquipmentStatus(equipmentStatus);
		setToolingStatus(MoldUtils.toToolingStatus(this));
	}

	@Override
	public void setOperatingStatus(OperatingStatus operatingStatus) {
		super.setOperatingStatus(operatingStatus);
		setToolingStatus(MoldUtils.toToolingStatus(this));
	}

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled = true;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.deleted = !enabled;
		setToolingStatus(MoldUtils.toToolingStatus(this));
	}

	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
		this.enabled = !deleted;
		setToolingStatus(MoldUtils.toToolingStatus(this));
	}

	private Double utilizationRate;

//	private String assetNumber; // delete
//	private Integer scrapRate; // delete

	private Instant operatedStartAt;

	private Integer contractedCycleTime;
	private Integer toolmakerContractedCycleTime;
	private Double weightedAverageCycleTime;
	private Double lastCycleTime;
	private Integer designedShot; // Forecasted Max shots - Depreciation term (Shots)
	private Integer lastShot;
	private Instant lastShotAt;
	private Instant lastShotAtVal;
	private Instant lastShotMadeAt; // last shot with shot count > 0

	@Transient
	private Integer inactivePeriod; //number of inactive month
	@Transient
	private Integer accumulatedShot;

	private String toolingLetter; // add
	//	private String name; // delete
	private String toolingType;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean backup;

	private String toolingComplexity; // add

	@Convert(converter = BooleanYnConverter.class)
	private Boolean familyTool; // If multiple parts connected then true

	private Integer lifeYears; // Forecasted Tool Life - Depreciation Term (Years)
	private Double salvageValue;
	@Enumerated(EnumType.STRING)
	private CurrencyType salvageCurrency;

	private String poNumber;
	private Instant poDate;

	public Double getSlCurrentBookValue() {
		if (getPoDate() != null && lifeYears != null) {
			if (lifeYears.equals(getSlDepreciation())) {
				return salvageValue != null ? salvageValue : 0D;
			}
			double assetCost = cost != null ? cost : 0;
			return ValueUtils.max(NumberUtils.roundOffNumber(assetCost - (getSlDepreciation() * getSlYearlyDepreciation())), 0D);
		}

		return 0D;
	}

	public Integer getSlDepreciation() {
		if (getPoDate() != null && lifeYears != null)
			return ValueUtils.min(DateUtils.getCeilRoundedYearBetween(getPoDate(), Instant.now()).intValue() - 1, lifeYears);

		return 0;
	}

	public String getSlDepreciationTitle() {
		return getSlDepreciation() + " of " + getSlDepreciationTerm() + " years";
	}

	public Integer getSlDepreciationTerm() {
		return lifeYears != null ? lifeYears : 0;
	}

	public Double getSlYearlyDepreciation() {
		if (getPoDate() != null && lifeYears != null) {
			double salvage = salvageValue != null ? salvageValue : 0;
			double assetCost = cost != null ? cost : 0;
			return NumberUtils.roundOffNumber((assetCost - salvage) / lifeYears);
		}
		return 0D;
	}

	public Instant getSlLatestDepreciationPoint() {
		if (getPoDate() != null && lifeYears != null) {
			Instant lastDepreciationPoint = DateUtils.plusYears(getPoDate(), lifeYears);
			Instant latestDepreciationPoint = DateUtils.plusYears(getPoDate(), getSlDepreciation());
			return ValueUtils.min(latestDepreciationPoint, lastDepreciationPoint);
		}

		return null;
	}

//	public Double getUpCurrentBookValue() {
//		if (getPoDate() != null && lifeYears != null) {
//			long months = ValueUtils.min(DateUtils.getCeilRoundedMonthBetween(getPoDate(), Instant.now()).intValue() - 1, (lifeYears * 12));
//			double salvage = salvageValue != null ? salvageValue : 0;
//			double assetCost = cost != null ? cost : 0;
//			double monthly = (assetCost - salvage) / (lifeYears * 12);
//			return ValueUtils.max(NumberUtils.roundOffNumber(assetCost - (months * monthly)), 0D);
//		}
//
//		return null;
//	}

	@Transient
	private Double upCurrentBookValue;
	@Transient
	private Double upDepreciation;

	//	public Double getUpDepreciation() {
//		if (lastShot != null)
//			return (double) lastShot;
//
//		return 0D;
//	}
	public Integer getUpDepreciationTerm() {
		return designedShot;
	}

	public Double getUpDepreciationPerShot() {
		if (designedShot != null) {
			double assetCost = cost != null ? cost : 0;
			double salvage = salvageValue != null ? salvageValue : 0;
			return NumberUtils.roundOffNumber((assetCost - salvage) / designedShot);
		}

		return 0D;
	}

	public String getDepreciationPercentage() {
		if (upDepreciation == null || designedShot == null || designedShot == 0) {
			return "";
		}
		return String.valueOf(Math.min(Math.round(upDepreciation / getUpDepreciationTerm() * 100 * 10) / 10d, 100));
	}

	public String getDepreciationPercentageTitle() {
		if (upDepreciation == null || designedShot == null || designedShot == 0) {
			return "";
		}
		return getDepreciationPercentage() + "% (" + formatter.format(upDepreciation) + " / " + formatter.format(getUpDepreciationTerm());
	}

	public Double getUpDepreciationPerShotRaw() {
		if (designedShot != null) {
			double assetCost = cost != null ? cost : 0;
			double salvage = salvageValue != null ? salvageValue : 0;
			return (assetCost - salvage) / designedShot;
		}

		return 0D;
	}

	public Instant getUpLatestDepreciationPoint() {
		if (getPoDate() != null) {
			if ((lastShot != null ? lastShot : 0) < designedShot) {
				Instant latest = DateUtils.plusMonths(getPoDate(), DateUtils.getCeilRoundedMonthBetween(getPoDate(), Instant.now()).intValue() - 1);
				if (latest.compareTo(Instant.now()) < 0) {
					return latest;
				} else {
					return DateUtils.plusMonths(getPoDate(), DateUtils.getCeilRoundedMonthBetween(getPoDate(), Instant.now()).intValue() - 2);
				}
			} else {
				return lastShotAt;
			}
		}

		return null;
	}

	private Integer madeYear;

	/* Physical Information */
	private String size;

	@Enumerated(EnumType.STRING)
	private SizeUnit sizeUnit;

	private String weight; // Tool Weight

	@Enumerated(EnumType.STRING)
	private WeightUnit weightUnit;

	private Double shotSize; // Shot Weight

	/*
		@Enumerated(EnumType.STRING)
		private RunnerType runnerType;
	*/
	private String runnerType;
	private String runnerMaker;
	private Double weightRunner; // add
	private String hotRunnerDrop; // add Hot Runner of Drop
	private String hotRunnerZone; // add Hot Runner Zone

	private String injectionMachineId;
	private Double quotedMachineTonnage; // add
	private Double currentMachineTonnage; // add

	/* Maintenance Information */
//	private Integer warrantedShot; // delete
	private Integer preventCycle;
	private String engineer;
	private String engineerDate; // delete

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(//
			name = "MOLD_ENGINEER", //
			joinColumns = @JoinColumn(name = "MOLD_ID"), //
			inverseJoinColumns = @JoinColumn(name = "USER_ID")//
	)
	private List<User> engineersInCharge = new ArrayList<>();//list for get first item to sort function

	@Transient
	private List<UserLiteDTO> engineers;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(//
			name = "MOLD_PLANT_ENGINEER", //
			joinColumns = @JoinColumn(name = "MOLD_ID"), //
			inverseJoinColumns = @JoinColumn(name = "USER_ID")//
	)
	private List<User> plantEngineersInCharge = new ArrayList<>();

	@Transient
	private List<UserLiteDTO> plantEngineers;

	@Enumerated(EnumType.STRING)
	private ToolingCondition toolingCondition;

	@Lob
	private String toolDescription;

	private Integer preventUpcoming; // upcoming maintenance tolerance
	private Integer preventOverdue; // overdue maintenance tolerance
	private Integer cycleTimeLimit1; // contrantedCycleTime 기준 L1 : cycleTimeLimit1 <= L1 < cycleTimeLimit2

	@Enumerated(EnumType.STRING)
	private OutsideUnit cycleTimeLimit1Unit;

	private Integer cycleTimeLimit2; // contrantedCycleTime 기준 L2 : cycleTimeLimit2 < L2

	@Enumerated(EnumType.STRING)
	private OutsideUnit cycleTimeLimit2Unit;

	// 온도 제한
//	private Integer upperLimitTemperature; // delete
//	@Enumerated(EnumType.STRING)
//	private TemperatureUnit upperLimitTemperatureUnit; // delete
//
//	private Integer lowerLimitTemperature; // delete
//	@Enumerated(EnumType.STRING)
//	private TemperatureUnit lowerLimitTemperatureUnit; // delete

	/* Cost Information */
	private Double cost; // for not Dyson
	@Enumerated(EnumType.STRING)
	private CurrencyType costCurrencyType;
	private Integer accumulatedMaintenanceCost; // for not Dyson

	private Integer totalCmCount; // 전체 고장정비 횟수
	private Integer totalCmCosts; // 전체 고장정비 비용.

	@Enumerated(EnumType.STRING)
	private CycleTimeStatus cycleTimeStatus;

	@Enumerated(EnumType.STRING)
	private EfficiencyStatus efficiencyStatus;

	@Enumerated(EnumType.STRING)
	private MisconfigureStatus misconfigureStatus; // Preset 등록 요청 후 완료 되지 않은 채 데이터가 들어오는 경우

	/* add info */
	private String resin;

	private Integer uptimeTarget;
	private Integer uptimeLimitL1;
	private Integer uptimeLimitL2;
	private String labour;
	//	private String hourPerShift; // delete
	private String shiftsPerDay; // hour per day
	private String productionDays; // day per week
	//	private String productionWeeks; // delete
//	private String supplierTagId; // delete
	private Integer maxCapacityPerWeek; // add Theoretical number of parts to be produced
	private Integer dailyMaxCapacity; // add daily capacity part produced
	private Integer passedDays; // how long does it pass from registered date to current

	private String supplierForToolMaker; // In case Tool-maker created a tooling, free enter to supplier instead

	@Enumerated(EnumType.STRING)
	private NotificationStatus dataSubmission;

	@Column(length = 20)
	@Enumerated(EnumType.STRING)
	private UseageType useageType;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean locationChanged;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean maintenanced;

	private Integer maintenanceCount;
	private Integer totalCavities;

	@DataLeakDetector(disabled = true)
	@Column(name = "COUNTER_ID", insertable = false, updatable = false)
	private Long counterId;

	@DataLeakDetector(disabled = true)
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	private Counter counter;

	@DataLeakDetector(disabled = true)
	@JsonIgnore
	@Column(name = "MACHINE_ID", insertable = false, updatable = false)
	private Long machineId;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	private Machine machine;

	@Transient
	private Boolean dataFilterEnabled;

	// mold 권한
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "moldId")
	private Set<MoldAuthority> moldAuthorities = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "mold")
	private List<MoldPart> moldParts = new ArrayList<>();

	private String day;
	private String week;
	private String month;

	//customize value
	@Transient
	private long totalProduced;
	@Transient
	private long predictedQuantity;
	@Transient
	private long totalMaxCapacity;

	@Transient
	private List<PartData> partsByProject;

	@Transient
	private Long lastWorkOrderId;

	@Transient
	private Long workOrderHistory;

	@Transient
	private Map<Long, List<CustomFieldValue>> customFieldValueMap = new HashMap<>();

	@Transient
	private Map<Long, Map<Long, List<CustomFieldValue>>> customFieldValueMapForPart = new HashMap<>();

	@Transient
	private List<CompanyLiteData> upperTierCompanies;

	@DataLeakDetector(disabled = true)
	public List<CompanyLiteData> getUpperTierCompanies() {
		return upperTierCompanies;
	}

	private Instant lastMaintenanceDate;

	private Instant lastRefurbishmentDate;

	@Transient
	private MoldMaintenanceDTO moldMaintenance;

	@Transient
	private Double ctCompliance;

	@Transient
	private Long remainingPartsCount;

	public Mold(Long id) {
		this.id = id;
	}

	@DataLeakDetector(disabled = true)
	public Long getCompanyId() {
		return super.getCompanyId();
	}

	public String getCounterCode() {
		return counter == null ? "" : counter.getEquipmentCode();
	}

	@DataLeakDetector(disabled = true)
	public Long getCounterId() {
		return counterId;
	}

	@DataLeakDetector(disabled = true)
	public Long getSupplierCompanyId() {
		return supplier == null ? null : supplier.getId();

	}

	public String getSupplierCompanyName() {
		return supplier == null ? supplierForToolMaker : supplier.getName();
	}

	public String getSupplierCompanyCode() {
		return supplier == null ? "" : supplier.getCompanyCode();
	}

	public Long getToolMakerCompanyId() {
		return toolMaker == null ? null : toolMaker.getId();
	}

	public String getToolMakerCompanyName() {
		return toolMaker == null ? "" : toolMaker.getName();
	}

	/**
	 * 카운터 PRESET STATUS
	 *
	 * @return
	 */
	public PresetStatus getCounterPresetStatus() {
		return counter == null ? null : counter.getPresetStatus();
	}

	public void setLastShot(Integer lastShot) {
		this.lastShot = lastShot;
		resetUtilizationRate();
	}

	public void setDesignedShot(Integer designedShot) {
		this.designedShot = designedShot;
		resetUtilizationRate();
	}

	private void resetUtilizationRate() {
		if (this.lastShot != null && this.designedShot != null && this.designedShot != 0) {
			double utilizationRate = ((double) this.lastShot / (double) this.designedShot) * 100;
			utilizationRate = Math.round(utilizationRate * 10) / 10d;
			if (this.utilizationRate == null || this.utilizationRate != utilizationRate) {
				setUtilizationRate(utilizationRate);
			}
		}
	}

	public Integer getNextMaintenanceShot() {
		int preventCycle = this.preventCycle == null ? 0 : this.preventCycle;
		int maintenanceCount = this.maintenanceCount == null ? 0 : this.maintenanceCount;
		return preventCycle * (maintenanceCount + 1);
	}

	public String getRunnerTypeTitle() {
		return this.runnerType;
//		return this.runnerType == null ? "" : this.runnerType.getTitle();
	}

	public String getSizeUnitTitle() {
		return this.sizeUnit == null ? "" : this.sizeUnit.getTitle();
	}

	public String getWeightUnitTitle() {
		return this.weightUnit == null ? "" : this.weightUnit.getTitle();
	}

	public String getToolingConditionTitle() {
		return this.toolingCondition == null ? "" : this.toolingCondition.getTitle();
	}

//	public String getUpperLimitTemperatureUnitText() {
//		return this.upperLimitTemperatureUnit == null ? "" : this.upperLimitTemperatureUnit.getTitle();
//	}
//	public String getLowerLimitTemperatureUnitText() {
//		return this.lowerLimitTemperatureUnit == null ? "" : this.lowerLimitTemperatureUnit.getTitle();
//	}

	public Double getContractedCycleTimeSeconds() {
		return contractedCycleTime == null ? null : (contractedCycleTime / 10.0);
	}

	public Instant getLastShotAt() {
		if (getCreatedAt() == null || (lastShotAt != null && lastShotAt.compareTo(getCreatedAt()) >= 0)) {
			return lastShotAt;
		}
		return null;
	}

	public String getLastShotDateTime() {
		return dataFilterEnabled != null && dataFilterEnabled ? DateUtils.getDateTime(getLastShotAtVal()) : DateUtils.getDateTime(getLastShotAt());
	}

	public String getLastShotDate() {
		return DateUtils.getDate(getLastShotAt());
	}

	public String getLastShotDateVal() {
		return DateUtils.getDate(getLastShotAtVal());
	}

	public String getUseageTypeName() {
		return useageType == null ? "" : useageType.getTitle();
	}

	public List<String> getEngineerNames() {
		List<String> result = new ArrayList<>();
		if (engineersInCharge != null && engineersInCharge.size() > 0) {
			result = engineersInCharge.stream().map(x -> x.getName()).collect(Collectors.toList());
		}
		return result;
	}

	public List<String> getPlantEngineerNames() {
		List<String> result = new ArrayList<>();
		if (plantEngineersInCharge != null && plantEngineersInCharge.size() > 0) {
			result = plantEngineersInCharge.stream().map(User::getName).collect(Collectors.toList());
		}
		return result;
	}

	public void setEngineersInCharge(List<User> engineersInCharge) {
		this.engineersInCharge = engineersInCharge;
		if (engineersInCharge != null && !engineersInCharge.isEmpty()) {
			this.engineer = engineersInCharge.get(0).getName();
		}
	}

	public String getCostCurrencyTypeTitle() {
		return costCurrencyType == null ? null : costCurrencyType.getTitle();
	}

	public String getToolingSizeView() {
		String customSize = size;
		if (customSize == null || customSize.equalsIgnoreCase("") || customSize.equalsIgnoreCase("undefinedxundefinedxundefined") || customSize.equalsIgnoreCase("xx")) {
			return "";
		}
		customSize = customSize.replaceAll("undefined", "0");
		if (customSize.startsWith("x")) {
			customSize = "0" + customSize;
		}
		customSize = customSize.replaceAll("x\\s*x", "x 0 x");
		if (customSize.endsWith("x")) {
			customSize = customSize + "0";
		}
		customSize = customSize.replaceAll("x", " x ").replaceAll("\\s+", " ").trim();
		String unit = getSizeUnit() != null ? getSizeUnit().getTitle() : "";
		return customSize + " " + unit;
	}

	public String getToolingWeightView() {
		return ObjectUtils.isEmpty(weight) ? "" : weight + " " + (weightUnit != null ? weightUnit.getTitle() : "");
	}

	public void setInactivePeriod() {
		long days = ChronoUnit.DAYS.between(lastShotAt != null ? lastShotAt : getCreatedAt(), Instant.now());
		this.inactivePeriod = Math.round(days / 30);
	}

	public Double getToolmakerContractedCycleTimeSeconds() {
		if (toolmakerContractedCycleTime == null) {
			return null;
		}

		return toolmakerContractedCycleTime / 10.0;
	}

	// moldParts 등록 시
	public List<MoldPart> getMoldParts() {
		if (moldParts == null || moldParts.size() == 0) {
			return new ArrayList<>();
		}
		Comparator<MoldPart> compareById = Comparator.comparing(MoldPart::getId);
		Collections.sort(moldParts, compareById);
		return new ArrayList<>(moldParts);
	}

	// use to get current mold parts
	public List<MoldPart> getCurrentMoldParts() {
		if (!ObjectUtils.isEmpty(this.moldParts)) {
			Comparator<MoldPart> compareById = Comparator.comparing(MoldPart::getId);
			Collections.sort(moldParts, compareById);
		}
		return this.moldParts;
	}

	// mold에 해당하는 part 정보
	public List<PartData> getParts() {
		if (ObjectUtils.isEmpty(moldParts)) {
			return new ArrayList<>();
		}

		List<PartData> parts = new ArrayList<>();

		Comparator<MoldPart> compareById = Comparator.comparing(MoldPart::getId);
		Collections.sort(moldParts, compareById);
		for (MoldPart moldPart : moldParts) {
			PartData partData = new PartData();

			if (moldPart.getPart() != null) {
				Part part = moldPart.getPart();
				partData.setPartId(part.getId());
				partData.setPartCode(part.getPartCode());
				partData.setPartName(part.getName());
				partData.setCavity(moldPart.getCavity());
				partData.setTotalCavities(moldPart.getTotalCavities());
				partData.setResinCode(moldPart.getResinCode());
				partData.setResinGrade(moldPart.getResinGrade());
				partData.setPartVolume(moldPart.getPartSize());
				partData.setPartWeight(moldPart.getPartWeight());
				partData.setDesignRevision(moldPart.getDesignRevision());

				if (part.getCategory() != null) {
					Category project = part.getCategory();
					partData.setProjectId(project.getId());
					partData.setProjectName(project.getName());

					if (project.getParent() != null && project.getParent().getParent() != null) {
						partData.setCategoryId(project.getParent().getParent().getId());
						partData.setCategoryName(project.getParent().getParent().getName());
					} else if (project.getGrandParent() != null) {
						partData.setCategoryId(project.getGrandParent().getId());
						partData.setCategoryName(project.getGrandParent().getName());
					}
				}
				partData.setWeeklyDemand(moldPart.getPart().getWeeklyDemand());
				partData.setQuantityRequired(moldPart.getPart().getQuantityRequired());

				if (customFieldValueMapForPart.containsKey(part.getId())) {
					partData.setCustomFieldValueMap(customFieldValueMapForPart.get(part.getId()));
				}
			}

			parts.add(partData);
		}

		return parts;
	}

	public Integer getContractedCycleTimeToCalculation() {
		return this.contractedCycleTime == null ? this.toolmakerContractedCycleTime : this.contractedCycleTime;
	}

	public MiniComponentData getMachineMini() {
		if (machine != null) {
			return new MiniComponentData(machine.getId(), machine.getMachineCode());
		}
		return null;
	}

	public String getMoldStatus() {
		return MoldUtils.toMoldStatus(this);
	}

	public CounterStatus getCounterStatus() {
		return counter == null ? CounterStatus.NOT_INSTALLED : counter.getCounterStatus();
	}

	public String getPoDateFormat() {
		if (getPoDate() != null) {
			return DateUtils2.format(getPoDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
		}
		return null;
	}

	public Double getTco() {
		if (this.cost == null && this.accumulatedMaintenanceCost == null) {
			return null;
		}
		Double acquisitionCost = this.cost != null ? this.cost : 0d;
		Double maintenanceCost = this.accumulatedMaintenanceCost != null ? this.accumulatedMaintenanceCost : 0d;
		return acquisitionCost + maintenanceCost;
	}

	public MaintenanceStatus getMaintenanceStatus() {
		if (getLastShot() != null && getPmCheckpoint() != null) {
			if (getLastShot() >= getPmCheckpoint()) {
				return MaintenanceStatus.OVERDUE;
			} else {
				return MaintenanceStatus.UPCOMING;
			}
		} else {
			return null;
		}
	}
}
