package saleson.api.mold.payload;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_FREQUENCY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.RECURR_CONSTRAINT_TYPE;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlan;
import com.emoldino.api.asset.resource.base.mold.enumeration.RelocationType;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.api.equipment.payload.EquipmentPayload;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ContinentName;
import saleson.common.enumeration.CorrectiveStatus;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.DashboardSettingLevel;
import saleson.common.enumeration.DayOfWeek;
import saleson.common.enumeration.DowntimeStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.common.enumeration.MoldStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.PresetStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.RefurbishmentStatus;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.enumeration.TabType;
import saleson.common.enumeration.TemperatureUnit;
import saleson.common.enumeration.ToolingCondition;
import saleson.common.enumeration.UseageType;
import saleson.common.enumeration.WeightUnit;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.CustomField.CustomFieldDTO;
import saleson.model.Mold;
import saleson.model.MoldAuthority;
import saleson.model.MoldPart;
import saleson.model.QCategory;
import saleson.model.QCompany;
import saleson.model.QContinent;
import saleson.model.QCounter;
import saleson.model.QLocation;
import saleson.model.QLogUserAlert;
import saleson.model.QMold;
import saleson.model.QMoldCorrective;
import saleson.model.QMoldCycleTime;
import saleson.model.QMoldDataSubmission;
import saleson.model.QMoldDetachment;
import saleson.model.QMoldDisconnect;
import saleson.model.QMoldDowntimeEvent;
import saleson.model.QMoldEfficiency;
import saleson.model.QMoldLocation;
import saleson.model.QMoldMaintenance;
import saleson.model.QMoldMisconfigure;
import saleson.model.QMoldPart;
import saleson.model.QMoldRefurbishment;
import saleson.model.QPart;
import saleson.model.QUser;
import saleson.model.QUserAlert;
import saleson.model.User;
import saleson.model.customField.QCustomFieldValue;

@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoldPayload extends EquipmentPayload {
	private String filterCode;
	private String toolingOrSensorCode;

	/* Multipart Form Data */
	private String payload;
	private MultipartFile[] files;
	private MultipartFile[] secondFiles;
	private MultipartFile[] thirdFiles;
	private MultipartFile[] forthFiles;

	private String supplierMoldCode;

	private String assetNumber;
	private Integer scrapRate;

	private Long partId;
	private String name;

	private Boolean familyTool;
	private Double cost;
	private CurrencyType costCurrencyType;
	private Integer accumulatedMaintenanceCost;
	private Integer lastShot;
	private Integer designedShot;
	private Integer preventCycle;
	private Integer preventUpcoming;
	private Integer preventOverdue;
	private Integer contractedCycleTime;
	private Double contractedCycleTimeSec;
	private Integer toolmakerContractedCycleTime;
	private Double toolmakerContractedCycleTimeSec;
	private Integer cycleTimeLimit1;		// contrantedCycleTime 기준 L1 : cycleTimeLimit1 <= L1 < cycleTimeLimit2
	private Integer cycleTimeLimit2;		// contrantedCycleTime 기준 L2 : cycleTimeLimit2 < L2

	private Integer upperLimitTemperature;
	private TemperatureUnit upperLimitTemperatureUnit;
	private Integer lowerLimitTemperature;
	private TemperatureUnit lowerLimitTemperatureUnit;

	private String locationCode;

	private String engineer;
	private String engineerDate;
	private List<Long> engineerIds;
	private List<String> engineerEmails;
	private List<Long> plantEngineerIds;
	private List<String> plantEngineerEmails;
	private MoldLocationStatus moldLocationStatus;

	private String toolingType;
	private Boolean backup;
	private String toolingLetter; // add
	private String toolingComplexity; // add
	private Integer lifeYears;				// Forecasted Tool Life
	private Double salvageValue;
	private CurrencyType salvageCurrency;
	private String poNumber;
	private String poDate;
	private Integer madeYear;
	private SizeUnit sizeUnit;
	private WeightUnit weightUnit;
	private Double shotSize;
//	private RunnerType runnerType;
	private String runnerType;
	private String runnerMaker;
	private Double weightRunner; // add
	private String hotRunnerDrop;
	private String hotRunnerZone;
	private Double quotedMachineTonnage; // add
//	private Double currentMachineTonnage; // add
	private String maker;			// toolMaperCompanyName
	private Integer maxCapacityPerWeek; // add
	private String injectionMachineId; // add
	private OutsideUnit cycleTimeLimit1Unit; // add
	private OutsideUnit cycleTimeLimit2Unit; // add


	private Integer warrantedShot;
	private ToolingCondition toolingCondition;

//	private Integer cavity;
	private String size;
	private String weight;
	private String utilization;
	private String resin;
	private UseageType useageType;

	/* supplier */
	private Integer uptimeTarget;
	private Integer uptimeLimitL1;
	private Integer uptimeLimitL2;
	private String labour;
	private String hourPerShift;
	private String shiftsPerDay;
	private String productionDays;
	private String productionWeeks;
	private String supplierTagId;

	private Long toolMakerCompanyId;
	private String toolMakerCompanyCode;
	private String toolMakerCompanyName;
	private Long supplierCompanyId;
	private String supplierCompanyCode;
	private String supplierCompanyName;
	private String supplierForToolMaker;

	private Boolean locationChanged;
	private Boolean lastAlert;
	private Boolean maintenanced;
	private Long maintenanceStartTime;
	private Long maintenanceEndTime;

	private MaintenanceStatus maintenanceStatus;
	private NotificationStatus notificationStatus;

	private String message;   // 상태 변경 메세지

	private String[] authorities;

	private String extraStatus;

	// 카테고리
	private Long categoryId;
	private Long projectId;

	// MoldPart 조회 조건
	private String moldPartWhereCondition; 		// ALL: default, ACTIVE: enabled + working


	// 상세검색
	private String anyQuery;
	private String noneQuery;
	private String toolId;
	private String partCode;
	private String locationName;
	private String supplier;
	private Double utilizationRate1;
	private Double utilizationRate2;
	private Long remainingPartsCount;
	private String toolCondition;			// ToolingCondition + DISCARDED + FAILURE
	private String toolDescription;

	private String timePeriod;
	private Integer totalCmCount;

	private MoldPart[] moldParts;

	private String serverName;

	// Misconfigure
	private MisconfigureStatus misconfigureStatus;

//
	private boolean allDataUser;

/*
	//for access group with tree
	@JsonIgnore
	private List<Long> accessCompanyIds=new ArrayList<>();
	@JsonIgnore
	private List<Long> accessMoldIds=new ArrayList<>();
	@JsonIgnore
	private boolean rootCompany = false;
*/
	//custom field
	private List<CustomFieldDTO> customFieldDTOList = new ArrayList<>();
	private List<Long> ids;

	private SpecialAlertType specialAlertType;
	private SpecialAlertType routeType;
	private Integer totalCavities;

	private PageType pageType;

	private DashboardSettingLevel inactiveLevel;
	private Instant inactiveFrom;
	private Instant inactiveTo;
	private Instant level1;
	private Instant level2;
	private Instant level3;
	private Long counterId;
	private String counterCode;

	private String accumulatedShotFilter;

	private TabType tabType;

	private Boolean dashboardRedirected;
	private Boolean tabbedDashboardRedirected;
	private ContinentName continentName;

	private Integer accumulatedShots;

	private Long tabId;

	private Boolean isAllData;

	private Boolean isModalSelected;
	private Boolean isMapRedirect;

	private Boolean isDefaultTab;
	//import
	private String engineerEmailsStr;
	private String plantEngineerEmailsStr;
	private String sizeW;
	private String sizeL;
	private String sizeH;
	private String sizeUnitTitle;
	private String weightUnitTitle;
	private String cycleTimeLimit1UnitTitle; // add
	private String cycleTimeLimit2UnitTitle; // add



	private String alertDate;

	private List<MoldStatus> moldStatusList;
//	private List<ToolingStatus> moldStatusList;
	private List<CounterStatus> counterStatusList;

	private Boolean isToolingScreen;

	private String searchEquipmentCode;


	private Long dataRequestId;

	//mold pm plan
	private PM_STRATEGY pmStrategy;
	private PM_FREQUENCY pmFrequency;
	private String schedStartDate;
	private Integer schedInterval;
	private Integer schedOrdinalNum;
	private DayOfWeek schedDayOfWeek;
	private Integer schedUpcomingTolerance;
	private RECURR_CONSTRAINT_TYPE recurrConstraintType;
	private Integer recurrNum;
	private String recurrDueDate;

	// pm plan for data import

	private String maintenanceIntervalType;
	//temp Shot Based
	private String preventCycleIn;
	private String preventUpcomingIn;
	//temp Weekly Time Based
	private String schedIntervalWeek;

	private String schedDayOfWeekTitle;
	private String schedStartDateWeek;

	//temp Weekly Time Based
	private String schedIntervalMonthly;
	private String schedOrdinalNumTitle;
	private String schedDayOfWeekMonthlyTitle;
	private String schedStartDateMonthly;

	//Continue (only for Time Based)
	private String forever;
	private String recurrNumIn;
	private String recurrDueDateIn;
	//Upcoming Maintenance Tolerance (only for Time Based)
	private String schedUpcomingToleranceIn;

	//filter for Overall Utilization dashboard
	private ToolingUtilizationStatus utilizationStatus;

	public List<ToolingStatus> getToolingStatusList(){
		if (moldStatusList == null) return null;
		List<ToolingStatus> toolingStatusList= moldStatusList.stream().map(moldStatus -> {
			switch (moldStatus) {
				case IN_PRODUCTION:
					return ToolingStatus.IN_PRODUCTION;
				case IDLE:
					return ToolingStatus.IDLE;
				case NOT_WORKING:
					return ToolingStatus.INACTIVE;
				case SENSOR_OFFLINE:
					return ToolingStatus.SENSOR_OFFLINE;
				case SENSOR_DETACHED:
					return ToolingStatus.SENSOR_DETACHED;
				case NO_SENSOR:
					return ToolingStatus.NO_SENSOR;
				case ON_STANDBY:
					return ToolingStatus.ON_STANDBY;
				default:
					return ToolingStatus.UNKNOWN;
			}
		}).collect(Collectors.toList());
		return toolingStatusList;
	}

	public List<Long> getPartIdsFromMoldParts() {
		if (moldParts == null) {
			return null;
		}
		List<Long> result = Arrays.stream(moldParts)
				.map(mp -> mp.getPartId())
				.collect(Collectors.toList());
		result.removeAll(Collections.singleton(null));
		return result;
	}

	public List<String> getPartCodesFromMoldParts(){
		if (moldParts == null) {
			return null;
		}
		List<String> result = Arrays.stream(moldParts)
				.map(mp -> mp.getPartCode())
				.collect(Collectors.toList());
		result.removeAll(Collections.singleton(null));
		return result;
	}

	public Mold getModelFromJson(ObjectMapper objectMapper) throws IOException {
		return objectMapper.readValue(payload, Mold.class);
	}

	public Mold getModel() {
		Mold mold = new Mold();
		bindData(mold);
		return mold;
	}

	public Mold getModel(Mold mold) {
		bindData(mold);
		return mold;
	}

	private void bindData(Mold mold) {
//		mold.setAssetNumber(getAssetNumber());
//		mold.setScrapRate(getScrapRate());
		mold.setRunnerMaker(getRunnerMaker());

		mold.setEquipmentCode(StringUtils.trimWhitespace(getEquipmentCode()));
		mold.setEquipmentStatus(getEquipmentStatus());

		mold.setSalvageValue(getSalvageValue());
		mold.setSalvageCurrency(getSalvageCurrency());
		mold.setPoNumber(getPoNumber());
		try {
			if (getPoDate() != null) {
			mold.setPoDate(DateUtils2.toInstant(getPoDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS));
			}
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			throw com.emoldino.framework.util.DataUtils.newDataValueInvalidException("tooling", "po_date", getPoDate());
		}
		/*
		M01
		mold.setPartId(getPartId());
		*/

//		mold.setName(getName());
		mold.setDesignedShot(getDesignedShot());
		mold.setPreventCycle(getPreventCycle());
		mold.setPreventUpcoming(getPreventUpcoming());
		mold.setPreventOverdue(0); //set overdue tolerance = 0 as default
		mold.setContractedCycleTime(getContractedCycleTime());
		mold.setToolmakerContractedCycleTime(getToolmakerContractedCycleTime());

		mold.setCycleTimeLimit1(getCycleTimeLimit1());
		mold.setCycleTimeLimit2(getCycleTimeLimit2());

//		mold.setUpperLimitTemperature(getUpperLimitTemperature());
//		mold.setUpperLimitTemperatureUnit(getUpperLimitTemperatureUnit());
//		mold.setLowerLimitTemperature(getLowerLimitTemperature());
//		mold.setLowerLimitTemperatureUnit(getLowerLimitTemperatureUnit());

//		mold.setEngineer(getEngineer());//update by first enginneers
//		mold.setEngineerDate(getEngineerDate());
		mold.setToolingLetter(getToolingLetter());
		mold.setToolingComplexity(getToolingComplexity());
		mold.setWeightRunner(getWeightRunner());
		mold.setHotRunnerDrop(getHotRunnerDrop());
		mold.setHotRunnerZone(getHotRunnerZone());
		mold.setQuotedMachineTonnage(getQuotedMachineTonnage());
//		mold.setCurrentMachineTonnage(getCurrentMachineTonnage());
		mold.setMaxCapacityPerWeek(getMaxCapacityPerWeek());

		mold.setSize(getSize());
		mold.setWeight(getWeight());
		mold.setMemo(getMemo());

//		mold.setFamilyTool(getFamilyTool() != null ? getFamilyTool() : (moldParts.length > 1 ? true : false));
		mold.setToolingType(getToolingType());
		if(getBackup()!=null){
			mold.setBackup(getBackup());
		}
		mold.setToolingComplexity(getToolingComplexity());
		mold.setLifeYears(getLifeYears());				// Forecasted Tool Life
		mold.setMadeYear(getMadeYear());
		mold.setSizeUnit(getSizeUnit());
		mold.setWeightUnit(getWeightUnit());
		mold.setShotSize(getShotSize());
		mold.setRunnerType(getRunnerType());
//		mold.setWarrantedShot(getWarrantedShot());
		mold.setToolingCondition(getToolingCondition());
		mold.setToolDescription(getToolDescription());
		mold.setCost(getCost());
		mold.setCostCurrencyType(getCostCurrencyType());
//		mold.setAccumulatedMaintenanceCost(getAccumulatedMaintenanceCost());
		mold.setInjectionMachineId(getInjectionMachineId());
		mold.setCycleTimeLimit1Unit(getCycleTimeLimit1Unit());
		mold.setCycleTimeLimit2Unit(getCycleTimeLimit2Unit());

		mold.setUseageType(getUseageType());
		mold.setResin(getResin());

		/* supplier */
		mold.setUptimeTarget(getUptimeTarget() != null ? getUptimeTarget() :
				((getToolingComplexity() == null || getToolingComplexity().equalsIgnoreCase("C")) ? 90 :
						(getToolingComplexity().equalsIgnoreCase("B") ? 85 : 80)));
		mold.setUptimeLimitL1(getUptimeLimitL1());
		mold.setUptimeLimitL2(getUptimeLimitL2());
		mold.setLabour(getLabour());
//		mold.setHourPerShift(getHourPerShift());
		mold.setShiftsPerDay(getShiftsPerDay());
		mold.setProductionDays(getProductionDays());
//		mold.setProductionWeeks(getProductionWeeks());
//		mold.setSupplierTagId(getSupplierTagId());

		// Basic shot
		mold.setLastShot(getLastShot());

		//Total cavities
		mold.setTotalCavities(getTotalCavities());

		// 초기 등록 시 location 정보 저장
		mold.setLocationId(getLocationId());

		mold.setSupplierForToolMaker(getSupplierForToolMaker());

		mold.setSupplierMoldCode(supplierMoldCode);
		mold.setRemainingPartsCount(remainingPartsCount);

		if (engineerIds != null && engineerIds.size() > 0) {
			List<User> engineerSet = new ArrayList<>();

			for (Long id : engineerIds) {
				User engineer = new User();
				engineer.setId(id);
				engineerSet.add(engineer);
			}
			mold.setEngineersInCharge(engineerSet);
		}else if(engineerEmails != null && engineerEmails.size() > 0){
			List<User> engineerSet = new ArrayList<>();

			for (String email : engineerEmails) {
				User engineer = new User();
				engineer.setEmail(email);
				engineerSet.add(engineer);
			}
			mold.setEngineersInCharge(engineerSet);
		}else if(engineerIds != null && engineerIds.size() == 0){
			mold.setEngineersInCharge(new ArrayList<>());
		}

		if (plantEngineerIds != null && plantEngineerIds.size() > 0) {
			List<User> plantEngineerSet = new ArrayList<>();

			for (Long id : plantEngineerIds) {
				User engineer = new User();
				engineer.setId(id);
				plantEngineerSet.add(engineer);
			}
			mold.setPlantEngineersInCharge(plantEngineerSet);
		}else if(plantEngineerEmails != null && plantEngineerEmails.size() > 0){
			List<User> plantEngineerSet = new ArrayList<>();

			for (String email : plantEngineerEmails) {
				User engineer = new User();
				engineer.setEmail(email);
				plantEngineerSet.add(engineer);
			}
			mold.setPlantEngineersInCharge(plantEngineerSet);
		}else if(plantEngineerIds != null && plantEngineerIds.size() == 0){
			mold.setPlantEngineersInCharge(new ArrayList<>());
		}

		if (authorities != null) {
			Set<MoldAuthority> moldAuthorities = new HashSet<>();
			for (String authority : authorities) {
				MoldAuthority moldAuthority = new MoldAuthority(authority);

				mold.getMoldAuthorities().removeIf(ma -> true);

				moldAuthorities.add(moldAuthority);
				/*boolean isMatched = false;
				while (iterator.hasNext()) {
					MoldAuthority ma = iterator.next();
					if (authority.equals(ma.getAuthority())) {
						isMatched = true;
						break;
					}
				}*/
			}
			mold.setMoldAuthorities(moldAuthorities);
		}
	}

	private void hideCMS(BooleanBuilder builder, QMold mold){
		if(getHideCMS() != null && getHideCMS()){
			builder.and(mold.counter.isNull()
				.or(mold.counter.equipmentCode.startsWith("NCM")).or(mold.counter.equipmentCode.startsWith("EMA")));
		}

	}
	public Predicate getPredicate() {

		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		if(getTabType()!=null){
			if(TabType.DIGITAL.equals(getTabType())){
				builder.and(mold.counterId.isNotNull());
			} else if(TabType.NON_DIGITAL.equals(getTabType())){
				builder.and(mold.counterId.isNull());
			}
		}
		if(getToolingOrSensorCode()!=null&&!StringUtils.isEmpty(getToolingOrSensorCode())){
			builder.and(
					mold.equipmentCode.contains(getToolingOrSensorCode()).or(counter.equipmentCode.contains(getToolingOrSensorCode()))
			);
		}


		if (getCompanyId() != null) {
			builder.and(mold.companyId.eq(getCompanyId()));
		}
		if (getLocationId() != null) {
			builder.and(mold.locationId.eq(getLocationId()));
		}

		if(getId()!=null){
			builder.and(mold.id.eq(getId()));
		}
		//for export
		if(ids != null && ids.size() > 0 || (isDefaultTab != null && !isDefaultTab)){
			builder.and(mold.id.in(ids));
		}

		if (getOperatingStatus() != null) {
			builder.and(mold.operatingStatus.eq(getOperatingStatus()));
		}

		if (getEquipmentStatus() != null) {
			builder.and(mold.equipmentStatus.eq(getEquipmentStatus()));
		}
		if(getOperatingStatuses() != null){
			builder.and(mold.operatingStatus.in(getOperatingStatuses()));
		}
		if (getEquipmentStatuses() != null) {
			builder.and(mold.equipmentStatus.in(getEquipmentStatuses()));
		}
		//Kepha ADD for #913 [ add new where in clause for legacy compatibility
		if (getOpStatus() != null) {
			String[] arr = getOpStatus().split(",");
			List<OperatingStatus> list = new ArrayList<OperatingStatus>();
			for(int i=0; i<arr.length; i++) {
				list.add(OperatingStatus.get(arr[i]));
			}
			builder.and(mold.operatingStatus.in(list));
		}

		if (getOpStatus() != null) {
			String[] arr = getEquipStatus().split(",");
			List<EquipmentStatus> list = new ArrayList<EquipmentStatus>();
			for(int i=0; i<arr.length; i++) {
				list.add(EquipmentStatus.get(arr[i]));
			}
			builder.and(mold.equipmentStatus.in(list));
		}

		if (getLocationChanged() != null && getLocationChanged() == true) {
			builder.and(mold.locationChanged.isTrue());
		}

//		if (getAdminPage() != null && getAdminPage() == false){
//			builder.and(mold.dataSubmission.eq(NotificationStatus.APPROVED)
//				.or(mold.dataSubmission.isNull()));
//		}

		if (!StringUtils.isEmpty(getEquipmentCode())) {
			builder.and(mold.equipmentCode.eq(getEquipmentCode().trim()));
		}
		if (CollectionUtils.isNotEmpty(getEquipmentCodes())) {
			builder.and(mold.equipmentCode.in(getEquipmentCodes()));
		}

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(mold.counter.isNull()
				.or(mold.counter.equipmentCode.startsWith("NCM")));
		}
*/
		hideCMS(builder,mold);

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFields(getQuery()));
		}

		if ("op-status-is-not-null".equals(getExtraStatus())) {
			builder.and(mold.equipmentStatus.isNotNull());
		}

		if ("op-status-is-null".equals(getExtraStatus())) {
			builder.and(mold.operatingStatus.isNull());
		}

		if (PresetStatus.READY.name().equals(getExtraStatus())) {
			builder.and(mold.counter.presetStatus.eq(PresetStatus.READY));
		}

		if(!StringUtils.isEmpty(getWhere())){
			String[] words = StringUtils.delimitedListToStringArray(getWhere().trim(),",");
			if(words.length > 0){
				Map<String, String> range = new HashMap<>();
				String operation = "and";
				for(String word: words){
					word = word.trim();
					if(word.isEmpty()) continue;
					if(word.startsWith("counter")){
						String[] values = word.split("counter", 2);
						if(values != null && values.length == 2 && !values[1].equals("")) {
							builder.and(mold.counterId.in(
									JPAExpressions
											.select(counter.id)
											.from(counter)
											.where(counter.equipmentCode.contains(values[1]))
							));
						}
					}else if(word.startsWith("part")){
						String[] values = word.split("part", 2);
						if(values != null && values.length == 2 && !values[1].equals("")){
							builder.and(mold.id.in(
									JPAExpressions
											.select(moldPart.mold.id)
											.from(moldPart)
											.innerJoin(part).on(moldPart.partId.eq(part.id))
											.where(part.partCode.contains(values[1]).and(part.deleted.isNull().or(part.deleted.isFalse())))
							));
						}

					} else {
					String op = loadRange(range,word);
					if(!StringUtils.isEmpty(op))operation=op;
				}
				}

				if(!range.isEmpty()){
					if(operation.equals("and")) {

						Predicate[] predicateArray = predicatesMoldPartAndByRange(moldPart,range);

						builder.and(mold.id.in(
								JPAExpressions
										.select(moldPart.moldId)
										.from(moldPart)
										.groupBy(moldPart.moldId)
										.having(predicateArray)
						));
					}else{

						Predicate predicate =predicateMoldPartOrByRange(moldPart,range);
						if(predicate != null) {
							builder.and(mold.id.in(
									JPAExpressions
											.select(moldPart.moldId)
											.from(moldPart)
											.groupBy(moldPart.moldId)
											.having(predicate)
							));
						}
					}
				}
			}
		}



		if (!StringUtils.isEmpty(getStatus())) {
			if(Arrays.stream(CompanyType.values()).map(Enum::name).anyMatch(getStatus()::equalsIgnoreCase)){
				Stream.of(CompanyType.values())
						.filter(companyType -> getStatus().equalsIgnoreCase(companyType.name()))
						.forEach(companyType -> {
							builder.and(mold.location.company.companyType.eq(companyType));
						});
			} else if(this.serverName.equals("dyson")) {
				Stream.of(NotificationStatus.values())
						.filter(dataSubmission -> getStatus().equalsIgnoreCase(dataSubmission.name()))
						.forEach(dataSubmission -> {
							if(getStatus().equalsIgnoreCase(NotificationStatus.DISAPPROVED.name())) {
								builder.and(mold.dataSubmission.isNull()
											.or(mold.dataSubmission.eq(dataSubmission)));
							}else{
								builder.and(mold.dataSubmission.eq(dataSubmission));
							}
						});
			}else {
				Stream.of(CompanyType.values())
						.filter(companyType -> getStatus().equalsIgnoreCase(companyType.name()))
						.forEach(companyType -> {
							builder.and(mold.location.company.companyType.eq(companyType));
						});
			}
		}


		//check deleted
		if(isAllData == null || !isAllData) {
			checkDeleted(mold, builder);
		}
		// check status
		checkDataStatus(mold, builder);


		// 상세검색
		if (getAnyQuery() != null && !getAnyQuery().trim().isEmpty()) {

			String[] words = StringUtils.delimitedListToStringArray(getAnyQuery().trim(), ",");

			if (words.length > 0) {

				BooleanBuilder anyBuiler = new BooleanBuilder();

				for (String word : words) {
					word = word.trim();
					if (word.isEmpty()) {
						continue;
					}
					anyBuiler.or(mold.equipmentCode.contains(word))
							.or(mold.location.name.contains(word))
							.or(mold.location.locationCode.contains(word))
							.or(mold.location.company.name.contains(word))
							.or(mold.resin.contains(word))
							.or(
									mold.location.company.name.contains(word)
											//.and(mold.location.company.companyType.eq(CompanyType.SUPPLIER))
							)

							.or(mold.id.in(
									JPAExpressions
											.select(moldPart.mold.id)
											.from(moldPart)
											.innerJoin(part).on(moldPart.partId.eq(part.id))
											.where(part.partCode.contains(word).and(part.deleted.isNull().or(part.deleted.isFalse())))
							));

				}

				if (anyBuiler.getValue() != null) {
					builder.and(anyBuiler);
				}
			}
		}


		if (getNoneQuery() != null && !getNoneQuery().trim().isEmpty()) {


			String[] words = StringUtils.delimitedListToStringArray(getNoneQuery().trim(), ",");

			if (words.length > 0) {
				for (String word : words) {
					word = "%" + word.trim() + "%";
					if (word.isEmpty()) {
						continue;
					}

					builder
						.and(mold.equipmentCode.notLike(word))
						.and(mold.location.name.notLike(word))
						.and(mold.location.locationCode.notLike(word))
						.and(mold.location.company.name.notLike(word))
						.and(mold.resin.notLike(word))
						.and(mold.id.notIn(
								JPAExpressions
										.select(moldPart.mold.id)
										.from(moldPart)
										.innerJoin(part).on(moldPart.partId.eq(part.id))
										.where(part.partCode.contains(word).and(part.deleted.isNull().or(part.deleted.isFalse())))
						));
				}
			}
		}

		if (getToolId() != null && !getToolId().isEmpty()) {
			builder.and(mold.equipmentCode.contains(getToolId()));
		}

		if (getPartId() != null || (getPartCode() != null && !getPartCode().isEmpty())) {
			BooleanBuilder filter = new BooleanBuilder();
			filter.and(part.deleted.isNull().or(part.deleted.isFalse()));
			if (getPartId() != null) {
				filter.and(part.id.eq(getPartId()));
			}
			if (getPartCode() != null && !getPartCode().isEmpty()) {
				filter.and(part.partCode.contains(getPartCode()));
			}
			builder.and(mold.id.in(JPAExpressions//
					.select(moldPart.mold.id)//
					.from(moldPart)//
					.innerJoin(part)//
					.on(moldPart.partId.eq(part.id))//
					.where(filter)//
			));
		}

		if (getLocationName() != null && !getLocationName().isEmpty()) {
			builder.and(mold.location.name.contains(getLocationName()));
		}
		if (getSupplier() != null && !getSupplier().isEmpty()) {
			builder.and(mold.location.company.name.contains(getSupplier()));
		}
		if (getResin() != null && !getResin().isEmpty()) {
			builder.and(mold.resin.contains(getResin()));
		}
		if (getMadeYear() != null) {
			builder.and(mold.madeYear.eq(getMadeYear()));
		}
		if (getUseageType() != null) {
			builder.and(mold.useageType.eq(getUseageType()));
		}

		if (getToolingCondition() != null) {
			builder.and(mold.toolingCondition.eq(getToolingCondition()));
		}

		if (getUtilizationRate1() != null) {
			builder.and(mold.utilizationRate.goe(getUtilizationRate1()));
		}

		if (getUtilizationRate2() != null) {
			builder.and(mold.utilizationRate.loe(getUtilizationRate2()));
		}

		if (getMaker() != null && !getMaker().isEmpty()) {
			builder.and(mold.toolMaker.name.eq(getMaker()));
		}

		if(getIsMapRedirect() != null && getIsMapRedirect() && !StringUtils.isEmpty(getLocationName())) {
			builder.and(mold.location.name.eq(getLocationName()))
					.and(mold.equipmentStatus.in(EquipmentStatus.INSTALLED, EquipmentStatus.DETACHED));
		}

		if (getTotalCmCount() != null) {
			if (getTotalCmCount().equals(7)) {
				builder.and(mold.totalCmCount.goe(getTotalCmCount()));
			} else {
				builder.and(mold.totalCmCount.eq(getTotalCmCount()));
			}
		}

		if (getToolCondition() != null && !getToolCondition().isEmpty()) {
			if ("GOOD".equals(getToolCondition())) {
				builder.and(mold.toolingCondition.eq(ToolingCondition.GOOD));

			} else if ("FAIR".equals(getToolCondition())) {
				builder.and(mold.toolingCondition.eq(ToolingCondition.FAIR));

			} else if ("POOR".equals(getToolCondition())) {
				builder.and(mold.toolingCondition.eq(ToolingCondition.POOR));

			} else if ("FAILURE".equals(getToolCondition())) {
				builder.and(mold.equipmentStatus.eq(EquipmentStatus.FAILURE));

			} else if ("DISCARDED".equals(getToolCondition())) {
				builder.and(mold.equipmentStatus.eq(EquipmentStatus.DISCARDED));

			}
		}

		if (getTimePeriod() != null && !getTimePeriod().isEmpty()
				&& (getTimePeriod().endsWith("year") || getTimePeriod().endsWith("month"))) {
			String unit = "month";
			if (getTimePeriod().endsWith("year")) {
				unit = "year";
			}
			String data = getTimePeriod().replaceAll(unit, "");
			String year = DateUtils.getToday("yyyy");

			int madeYear = Integer.parseInt(year) - Integer.parseInt(data);

			builder.and(mold.madeYear.goe(madeYear));
		}



//		if(inactiveFrom != null) {
//			BooleanExpression booleanExpression = new CaseBuilder()
//					.when(mold.lastShotAt.isNull().and(mold.createdAt.goe(inactiveFrom))).then(true)
//					.when(mold.lastShotAt.isNotNull().and(mold.lastShotAt.goe(inactiveFrom))).then(true)
//					.otherwise(false);
//			builder.and(booleanExpression.isTrue());
//
//		}
//		if(inactiveTo != null) {
//			BooleanExpression booleanExpression = new CaseBuilder()
//					.when(mold.lastShotAt.isNull().and(mold.createdAt.loe(inactiveTo))).then(true)
//					.when(mold.lastShotAt.isNotNull().and(mold.lastShotAt.loe(inactiveTo))).then(true)
//					.otherwise(false);
//			builder.and(booleanExpression.isTrue());
//		}
//		if (inactiveFrom!=null||inactiveTo!=null){
//			List<Long> filteredIds = BeanUtils.get(MoldService.class).findAllMiniDataByGeneralFilter().stream().map(MiniComponentData::getId).collect(Collectors.toList());
//			builder.and(mold.id.in(filteredIds));
//		}
		if (level1 != null && level2 != null&&DashboardSettingLevel.FIRST_LEVEL.equals(inactiveLevel)) {
			inactiveToolingPredicate(builder,level2,level1);
		}
		if (level2 != null && level3 != null&&DashboardSettingLevel.SECOND_LEVEL.equals(inactiveLevel)) {
			inactiveToolingPredicate(builder,level3,level2);
		}
		if (level3!=null&&DashboardSettingLevel.THIRD_LEVEL.equals(inactiveLevel)){
			inactiveToolingPredicate(builder,null,level3);
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(mold.id.in(getFilteredIds()));
		}

		if(!StringUtils.isEmpty(searchEquipmentCode)) {
			builder.and(mold.equipmentCode.containsIgnoreCase(searchEquipmentCode));
		}
		filterUtilization(builder,utilizationStatus);

		return builder;

	}
	public void inactiveToolingPredicate(BooleanBuilder builder, Instant min,Instant max){

		BooleanBuilder filter = new BooleanBuilder()//
				.or(QueryUtils.gtAndLoe(new BooleanBuilder(), Q.mold.lastShotAt, min, max).and(Q.mold.lastShotAt.isNotNull()))//
				.or(QueryUtils.gtAndLoe(new BooleanBuilder(), Q.mold.activatedAt, min, max).and(Q.mold.activatedAt.isNotNull().and(Q.mold.lastShotAt.isNull())))//
				;
		if (min == null) {
			filter.or(Q.mold.lastShotAt.isNull().and(Q.mold.activatedAt.isNull()));
		}
		builder.and(filter).and(Q.mold.equipmentStatus.ne(EquipmentStatus.DISCARDED));
	}
	// filter direct from Overall Utilization
	public static void filterUtilization(BooleanBuilder builder,ToolingUtilizationStatus utilizationStatus){
		if (utilizationStatus != null) {
			UtilizationConfig config = MoldUtils.getUtilizationConfig();
			BooleanBuilder filter = new BooleanBuilder();
			if (ToolingUtilizationStatus.LOW.equals(utilizationStatus)) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, null, config.getLow());
			} else if (ToolingUtilizationStatus.MEDIUM.equals(utilizationStatus)) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getLow(), config.getMedium());
			} else if (ToolingUtilizationStatus.HIGH.equals(utilizationStatus)) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getMedium(), config.getHigh());
			} else {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getHigh(), null);
			}
			builder.and(filter);
		}
	}

	public static String loadRange(final Map<String, String> range,String word){
		String operation=null;
		if(word.startsWith("gt")){
			String[] values = word.split("gt", 2);
			if(values != null && values.length == 2 && !values[1].equals("")) {
				if((range.containsKey("gt") && operation.equals("or") && Integer.valueOf(range.get("gt")) <= Integer.valueOf(values[1]))
						|| (range.containsKey("gt") && operation.equals("and") && Integer.valueOf(range.get("gt")) >= Integer.valueOf(values[1])))
					return null;
				range.put("gt", values[1]);
			}
		}else if(word.startsWith("lt")){
			String[] values = word.split("lt", 2);
			if(values != null && values.length == 2 && !values[1].equals("")) {
				if((range.containsKey("lt") && operation.equals("or") && Integer.valueOf(range.get("lt")) >= Integer.valueOf(values[1]))
						|| (range.containsKey("lt") && operation.equals("and") && Integer.valueOf(range.get("lt")) <= Integer.valueOf(values[1])))
					return null;
				range.put("lt", values[1]);
			}
		}else if(word.startsWith("eq")){
			String[] values = word.split("eq", 2);
			if(values != null && values.length == 2 && !values[1].equals("")) {
				if(range.containsKey("eq")) range.put("eq2", values[1]);
				else range.put("eq", values[1]);
			}
		}else if(word.startsWith("op")){
			String[] values = word.split("op", 2);
			if(values != null && values.length == 2 && values[1].equals("or")){
				operation = "or";
			}
		}
		return operation;

	}
	public static Predicate[] predicatesMoldPartAndByRange(QMoldPart moldPart, final Map<String, String> range){
		List<Predicate> predicates = new ArrayList<>();
		if(range.containsKey("gt")) predicates.add(moldPart.cavity.sum().gt(Expressions.numberPath(Integer.class, range.get("gt"))));
		if(range.containsKey("lt")) predicates.add(moldPart.cavity.sum().lt(Expressions.numberPath(Integer.class, range.get("lt"))));
		if(range.containsKey("eq")) predicates.add(moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq"))));
		if(range.containsKey("eq2")) predicates.add(moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq2"))));
		Predicate[] predicateArray = predicates.toArray(new Predicate[predicates.size()]);
		return predicateArray;
	}
	public static Predicate predicateMoldPartOrByRange(QMoldPart moldPart, final Map<String, String> range){
		Predicate predicate = null;
		if(range.containsKey("gt")){
			predicate = moldPart.cavity.sum().gt(Expressions.numberPath(Integer.class, range.get("gt")));
			if(range.containsKey("lt"))
				predicate = moldPart.cavity.sum().gt(Expressions.numberPath(Integer.class, range.get("gt")))
						.or(moldPart.cavity.sum().lt(Expressions.numberPath(Integer.class, range.get("lt"))));
			else if(range.containsKey("eq"))
				predicate = moldPart.cavity.sum().gt(Expressions.numberPath(Integer.class, range.get("gt")))
						.or(moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq"))));
		}else if(range.containsKey("lt")){
			predicate = moldPart.cavity.sum().lt(Expressions.numberPath(Integer.class, range.get("lt")));
			if(range.containsKey("eq"))
				predicate = moldPart.cavity.sum().lt(Expressions.numberPath(Integer.class, range.get("lt")))
						.or(moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq"))));
		}else if(range.containsKey("eq")){
			predicate = moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq")));
			if(range.containsKey("eq2")){
				predicate = moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq")))
						.or(moldPart.cavity.sum().eq(Expressions.numberPath(Integer.class, range.get("eq2"))));
			}
		}
		return predicate;
	}


	/**
	 * 금형 위치 정보
	 * @return
	 */
	public Predicate getLocationPredicate() {
		QMoldLocation moldLocation = QMoldLocation.moldLocation;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(moldLocation.mold.counter.isNull()
					.or(moldLocation.mold.counter.equipmentCode.startsWith("NCM")));
		}
*/
		hideCMS(builder,moldLocation.mold);

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldLocation.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if(getId() != null){
			builder.and(moldLocation.moldId.eq(getId()));
		}

		if (getOperatingStatus() != null) {
			builder.and(moldLocation.mold.operatingStatus.eq(getOperatingStatus()));
		}

		if ("alert".equalsIgnoreCase(getStatus())) {
			builder.and(moldLocation.moldLocationStatus.eq(MoldLocationStatus.PENDING));
//			builder.and(moldLocation.moldLocationStatus.eq(MoldLocationStatus.CONFIRMED));
		} else if ("confirmed".equalsIgnoreCase(getStatus())) {
			if (moldLocationStatus==null)
				builder.and(moldLocation.moldLocationStatus.in(MoldLocationStatus.APPROVED, MoldLocationStatus.DISAPPROVED, MoldLocationStatus.UNAPPROVED));
			else if (MoldLocationStatus.DISAPPROVED.equals(moldLocationStatus)){
				builder.and(moldLocation.moldLocationStatus.in(MoldLocationStatus.DISAPPROVED, MoldLocationStatus.UNAPPROVED));
			} else if (MoldLocationStatus.APPROVED.equals(moldLocationStatus)) {
				builder.and(moldLocation.moldLocationStatus.eq(MoldLocationStatus.APPROVED));
			}
//			builder.and(moldLocation.moldLocationStatus.eq(MoldLocationStatus.CHANGED));
//			QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//			JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//					.where(logUserAlert.alertType.eq(AlertType.RELOCATION).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//			builder.and(moldLocation.id.in(query));
		} else if ("DISAPPROVED".equalsIgnoreCase(getStatus()) || "UNAPPROVED".equalsIgnoreCase(getStatus())) {
			builder.and(moldLocation.moldLocationStatus.in(MoldLocationStatus.DISAPPROVED, MoldLocationStatus.UNAPPROVED));
		} else if ("APPROVED".equalsIgnoreCase(getStatus())) {
			builder.and(moldLocation.moldLocationStatus.eq(MoldLocationStatus.APPROVED));
		};

/*
		if(getLastAlert() != null && getLastAlert()){
			builder.and(moldLocation.latest.isTrue());
		}
*/

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFieldsForRelocation(getQuery()));
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldLocation.moldId.in(getFilteredIds()));
		}

		//check deleted
//		checkDeleted(moldLocation.mold, builder);
		QueryUtils.isMold(builder,moldLocation.mold);
		builder.and(moldLocation.relocationType.eq(RelocationType.PLANT));

		return builder;
	}


	/**
	 * 금형 정비 목록
	 * @return
	 */
	public Predicate getMaintenancePredicate() {
		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
		QMold mold = QMold.mold;
		QContinent continent = QContinent.continent;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (!allDataUser)
		{
			if (AccessControlUtils.isAccessFilterRequired()) {
				builder.and(moldMaintenance.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
			}
		}
		if (getOperatingStatus() != null) {
			builder.and(moldMaintenance.mold.operatingStatus.eq(getOperatingStatus()));
		}

		if (getMaintenanceStatus() != null) {
			builder.and(moldMaintenance.maintenanceStatus.eq(getMaintenanceStatus()));
		}

		if (!"done".equals(getStatus()) && (getMaintenanced() != null && getMaintenanced() == true)) {
			builder.and(moldMaintenance.mold.maintenanced.isTrue());
		}

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(moldMaintenance.mold.counter.isNull()
					.or(moldMaintenance.mold.counter.equipmentCode.startsWith("NCM")).or(moldMaintenance.mold.counter.equipmentCode.startsWith("EMA")));
		}
*/
		hideCMS(builder,moldMaintenance.mold);

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFieldsForMaintenance(getQuery()));
		}


		if (!StringUtils.isEmpty(getStatus())) {
			if("done".equalsIgnoreCase(getStatus())){
				builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.DONE));
			}else {
//				if (!allDataUser) {
//					QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//					JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//							.where(logUserAlert.alertType.eq(AlertType.MAINTENANCE).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//					builder.and(moldMaintenance.id.in(query));
//				}
				if ("alert".equalsIgnoreCase(getStatus())) {
//					builder.and(
//							moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.UPCOMING)
//									.or(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE))
//					);
					if (specialAlertType != null){
						if (specialAlertType.equals(SpecialAlertType.OVERDUE)){
							builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE));
						}
						else if (specialAlertType.equals(SpecialAlertType.UPCOMING_OVERDUE)){
							builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.UPCOMING)
									.or(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE)));
						}
					}
					if (routeType != null) {
						if (routeType.equals(SpecialAlertType.OVERDUE)){
							builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE));
						}
						if (routeType.equals(SpecialAlertType.UPCOMING)){
							builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.UPCOMING));
						}
					}
				} else if ("upcoming".equalsIgnoreCase(getStatus())) {
					if (specialAlertType != null && specialAlertType.equals(SpecialAlertType.UPCOMING_OVERDUE)) {
						builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.UPCOMING));
					} else if (specialAlertType != null && specialAlertType.equals(SpecialAlertType.OVERDUE)) {
						builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.UPCOMING).and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE)));
					}
				} else if ("overdue".equalsIgnoreCase(getStatus())) {
					builder.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE));

				}
			}
		}

		if (getId() != null) {
			builder.and(moldMaintenance.moldId.eq(getId()));
		}

		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldMaintenance.latest.isTrue());
		}

		if (getContinentName() != null){
			builder.and(moldMaintenance.id.in(JPAExpressions
					.select(moldMaintenance.id).distinct()
					.from(moldMaintenance)
					.innerJoin(mold).on(mold.id.eq(moldMaintenance.moldId))
					.innerJoin(continent).on(mold.location.countryCode.eq(continent.countryCode))
					.where(continent.continentName.eq(getContinentName()))));
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldMaintenance.moldId.in(getFilteredIds()));
		}

		//check deleted
		checkDeleted(moldMaintenance.mold, builder);

		return builder;
	}

	public Predicate getCycleTimePredicate() {
		QMoldCycleTime moldCycleTime = QMoldCycleTime.moldCycleTime;
		QMold mold = QMold.mold;
		QUserAlert userAlert = QUserAlert.userAlert;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldCycleTime.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (getId() != null) {
			builder.and(moldCycleTime.moldId.eq(getId()));
		}

		if (getOperatingStatus() != null) {
			builder.and(moldCycleTime.mold.operatingStatus.eq(getOperatingStatus()));
		}

		if (getNotificationStatus() != null) {
			builder.and(moldCycleTime.notificationStatus.eq(getNotificationStatus()));
		}

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(moldCycleTime.mold.counter.isNull()
					.or(moldCycleTime.mold.counter.equipmentCode.startsWith("NCM")));
		}
*/
		hideCMS(builder,moldCycleTime.mold);

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFieldsForCycleTime(getQuery()));
		}

		if ("op-status-is-not-null".equals(getExtraStatus())) {
			builder.and(moldCycleTime.mold.operatingStatus.isNotNull());
		}

		if (!StringUtils.isEmpty(getStatus())) {
			if ("confirmed".equalsIgnoreCase(getStatus())) {
				builder.and(moldCycleTime.notificationStatus.eq(NotificationStatus.CONFIRMED));

			}else {
//				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//						.where(logUserAlert.alertType.eq(AlertType.CYCLE_TIME).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//				builder.and(moldCycleTime.id.in(query));
				if ("alert".equalsIgnoreCase(getStatus())) {
					builder.and(moldCycleTime.notificationStatus.eq(NotificationStatus.ALERT));
					if (specialAlertType != null){
						if (specialAlertType.equals(SpecialAlertType.L2)){
							builder.and(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L2));
						}
						else if (specialAlertType.equals(SpecialAlertType.L1L2)){
							builder.and(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L1).or(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L2)));
						}
					}
				} else if ("L1".equalsIgnoreCase(getStatus())) {
					builder.and(moldCycleTime.notificationStatus.eq(NotificationStatus.ALERT));
					if (specialAlertType != null && specialAlertType.equals(SpecialAlertType.L1L2)) {
						builder.and(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L1));
					} else if (specialAlertType != null && specialAlertType.equals(SpecialAlertType.L2)){
						builder.and(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L1).and(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L2)));
					}
				} else if ("L2".equalsIgnoreCase(getStatus())) {
					builder.and(moldCycleTime.notificationStatus.eq(NotificationStatus.ALERT));
					builder.and(moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L2));
				}
			}
		}

		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldCycleTime.latest.isTrue());
		}
//		else{
			builder.and(moldCycleTime.periodType.eq(JPAExpressions.select(userAlert.periodType)
					.from(userAlert)
					.where(userAlert.user.id.eq(SecurityUtils.getUserId()).and(userAlert.alertType.eq(AlertType.CYCLE_TIME)))));
//		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldCycleTime.moldId.in(getFilteredIds()));
		}

		if (!StringUtils.isEmpty(alertDate)){
			Instant start = DateUtils2.toInstant(alertDate, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
			Instant end = start.plus(1, ChronoUnit.DAYS);
			builder.and(moldCycleTime.notificationAt.between(start, end));
		}

		//check deleted
		checkDeleted(moldCycleTime.mold, builder);

		return builder;
	}


	public Predicate getEfficiencyPredicate() {
		QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;
		QMold mold = QMold.mold;
		QUserAlert userAlert = QUserAlert.userAlert;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldEfficiency.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (getId() != null) {
			builder.and(moldEfficiency.moldId.eq(getId()));
		}

		if (getOperatingStatus() != null) {
			builder.and(moldEfficiency.mold.operatingStatus.eq(getOperatingStatus()));
		}

		if (getNotificationStatus() != null) {
			builder.and(moldEfficiency.notificationStatus.eq(getNotificationStatus()));
		}

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(moldEfficiency.mold.counter.isNull()
					.or(moldEfficiency.mold.counter.equipmentCode.startsWith("NCM")));
		}
*/
		hideCMS(builder,moldEfficiency.mold);

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldEfficiency.mold.equipmentCode.contains(getQuery())
							.or(moldEfficiency.mold.location.name.contains(getQuery()))
							.or(moldEfficiency.mold.location.locationCode.contains(getQuery()))
							.or(mold.location.company.name.contains(getQuery()))
							.or(mold.location.company.companyCode.contains(getQuery()))
							.or(moldEfficiency.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
							.or(moldEfficiency.efficiencyStatus.stringValue().containsIgnoreCase(getQuery()))
							.or(moldEfficiency.notificationStatus.stringValue().containsIgnoreCase(getQuery()))
			);
		}

		if ("op-status-is-not-null".equals(getExtraStatus())) {
			builder.and(moldEfficiency.mold.operatingStatus.isNotNull());
		}

		if (!StringUtils.isEmpty(getStatus())) {
			if ("confirmed".equalsIgnoreCase(getStatus())) {
				builder.and(moldEfficiency.notificationStatus.eq(NotificationStatus.CONFIRMED));
			}else {
//				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//						.where(logUserAlert.alertType.eq(AlertType.EFFICIENCY).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//				builder.and(moldEfficiency.id.in(query));
				if ("alert".equalsIgnoreCase(getStatus())) {
					builder.and(moldEfficiency.notificationStatus.eq(NotificationStatus.ALERT));
					if (specialAlertType != null){
						if (specialAlertType.equals(SpecialAlertType.L2)){
							builder.and(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L2));
						}
						else if (specialAlertType.equals(SpecialAlertType.L1L2)){
							builder.and(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L1).or(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L2)));
						}
					}
				} else if ("L1".equalsIgnoreCase(getStatus())) {
					builder.and(moldEfficiency.notificationStatus.eq(NotificationStatus.ALERT));
					if (specialAlertType != null && specialAlertType.equals(SpecialAlertType.L1L2)) {
						builder.and(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L1));
					} else if (specialAlertType != null && specialAlertType.equals(SpecialAlertType.L2)){
						builder.and(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L1).and(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L2)));
					}

				} else if ("L2".equalsIgnoreCase(getStatus())) {
					builder.and(moldEfficiency.notificationStatus.eq(NotificationStatus.ALERT));
					builder.and(moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L2));
				}
			}
		}

		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldEfficiency.latest.isTrue());
		}
		builder.and(moldEfficiency.periodType.eq(JPAExpressions.select(userAlert.periodType)
				.from(userAlert)
				.where(userAlert.user.id.eq(SecurityUtils.getUserId()).and(userAlert.alertType.eq(AlertType.EFFICIENCY)))));


		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldEfficiency.moldId.in(getFilteredIds()));
		}
		//check deleted
		checkDeleted(moldEfficiency.mold, builder);

		return builder;
	}

	public Predicate getDisconnectPredicate() {
		QMoldDisconnect moldDisconnect = QMoldDisconnect.moldDisconnect;
		QMold mold = QMold.mold;
//		QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldDisconnect.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (getId() != null) {
			builder.and(moldDisconnect.moldId.eq(getId()));
		}

		if (getOperatingStatus() != null) {
			builder.and(moldDisconnect.mold.operatingStatus.eq(getOperatingStatus()));
		}

		if (getNotificationStatus() != null) {
			builder.and(moldDisconnect.notificationStatus.eq(getNotificationStatus()));
		}

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(moldDisconnect.mold.counter.isNull()
					.or(moldDisconnect.mold.counter.equipmentCode.startsWith("NCM")));
		}
*/
		hideCMS(builder,moldDisconnect.mold);

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldDisconnect.mold.equipmentCode.contains(getQuery())
							.or(moldDisconnect.mold.location.name.contains(getQuery()))
							.or(moldDisconnect.mold.location.locationCode.contains(getQuery()))
							.or(mold.location.company.companyCode.contains(getQuery()))
							.or(mold.location.company.name.contains(getQuery()))
//							.or(moldDisconnect.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
//							.or(moldDisconnect.mold.equipmentStatus.stringValue().containsIgnoreCase(getQuery()))
			);
		}


		if (!StringUtils.isEmpty(getStatus())) {
			if ("alert".equalsIgnoreCase(getStatus())) {
				builder.and(moldDisconnect.notificationStatus.eq(NotificationStatus.ALERT));
//				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//						.where(logUserAlert.alertType.eq(AlertType.DISCONNECTED).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//				builder.and(moldDisconnect.id.in(query));
			} else if ("confirmed".equalsIgnoreCase(getStatus())) {
				builder.and(moldDisconnect.notificationStatus.eq(NotificationStatus.CONFIRMED)
						.or(moldDisconnect.notificationStatus.eq(NotificationStatus.FIXED)));

			}
		}

		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldDisconnect.latest.isTrue());
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldDisconnect.moldId.in(getFilteredIds()));
		}

		if (CollectionUtils.isNotEmpty(ids)) {
			builder.and(moldDisconnect.moldId.in(ids));
		}

		checkDeleted(moldDisconnect.mold, builder);

		return builder;
	}

	/**
	 * 고장 내역 조회 조건
	 * @return
	 */
	public Predicate getCorrectivePredicate() {
		QMoldCorrective moldCorrective = QMoldCorrective.moldCorrective;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldCorrective.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if(getId() != null){
			builder.and(moldCorrective.mold.id.eq(getId()));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFieldsForCorrective(getQuery()));
		}

//		if (!StringUtils.isEmpty(getStatus())) {
			if (!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("alert")) {
				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
						.where(logUserAlert.alertType.eq(AlertType.CORRECTIVE_MAINTENANCE).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
				builder.and(moldCorrective.id.in(query));
				if(getLastAlert() != null && getLastAlert() == true){
					builder.and(moldCorrective.latest.isTrue());
				}
				builder.and(moldCorrective.correctiveStatus.ne(CorrectiveStatus.COMPLETED));

			} else {
				if(getLastAlert() != null && getLastAlert() == true){
					builder.and(moldCorrective.lastChecked.isTrue());
				}
				builder.and(moldCorrective.correctiveStatus.ne(CorrectiveStatus.REQUESTED)
					.and(moldCorrective.correctiveStatus.ne(CorrectiveStatus.FAILURE)));
			}
//		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldCorrective.mold.id.in(getFilteredIds()));
		}

		//check deleted
		checkDeleted(moldCorrective.mold, builder);

		return builder;
	}

	public Predicate getMisconfigurePredicate() {
		QMoldMisconfigure moldMisconfigure = QMoldMisconfigure.moldMisconfigure;
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldMisconfigure.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (getOperatingStatus() != null) {
			builder.and(moldMisconfigure.mold.operatingStatus.eq(getOperatingStatus()));
		}

		/*if (getNotificationStatus() != null) {
			builder.and(moldMisconfigure.misconfigureStatus.eq(getNotificationStatus()));
		}*/

		// [2021-03-17] Hide CMS for mobile version
/*
		if(getHideCMS() != null && getHideCMS()){
			builder.and(moldMisconfigure.mold.counter.isNull()
					.or(moldMisconfigure.mold.counter.equipmentCode.startsWith("NCM")));
		}
*/
		hideCMS(builder,moldMisconfigure.mold);

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldMisconfigure.mold.equipmentCode.contains(getQuery())
							.or(moldMisconfigure.counterCode.contains(getQuery()))
							.or(moldMisconfigure.mold.location.name.contains(getQuery()))
							.or(moldMisconfigure.mold.location.locationCode.contains(getQuery()))
							.or(mold.location.company.companyCode.contains(getQuery()))
							.or(mold.location.company.name.contains(getQuery()))
//							.or(moldMisconfigure.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
//							.or(moldMisconfigure.misconfigureStatus.stringValue().containsIgnoreCase(getQuery()))
			);
		}

		if ("op-status-is-not-null".equals(getExtraStatus())) {
			builder.and(moldMisconfigure.mold.operatingStatus.isNotNull());
		}

		if (!StringUtils.isEmpty(getStatus())) {

//			Arrays.asList(MisconfigureStatus.values()).stream()
//					.filter(status -> getStatus().equalsIgnoreCase(status.name()))
//					.forEach(status ->
//							builder.and(moldMisconfigure.misconfigureStatus.eq(status))
//					);
			if ("misconfigured".equalsIgnoreCase(getStatus())) {
				builder.and(moldMisconfigure.misconfigureStatus.eq(MisconfigureStatus.MISCONFIGURED));
//				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//						.where(logUserAlert.alertType.eq(AlertType.MISCONFIGURE).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//				builder.and(moldMisconfigure.id.in(query));
			} else if ("confirmed".equalsIgnoreCase(getStatus())) {
				builder.and(moldMisconfigure.misconfigureStatus.eq(MisconfigureStatus.CONFIRMED));

			}
		}

		if (getId() != null) {
			builder.and(moldMisconfigure.counterId.eq(getId()));
		}

		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldMisconfigure.latest.isTrue());
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldMisconfigure.mold.id.in(getFilteredIds()));
		}
		//check deleted
		checkDeleted(moldMisconfigure.mold, builder);

		return builder;
	}

	public Predicate getDataSubmissionPredicate() {
		QMoldDataSubmission moldDataSubmission = QMoldDataSubmission.moldDataSubmission;
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldDataSubmission.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}


		if (getOperatingStatus() != null) {
			builder.and(moldDataSubmission.mold.operatingStatus.eq(getOperatingStatus()));
		}

		if (getNotificationStatus() != null) {
			builder.and(moldDataSubmission.notificationStatus.eq(getNotificationStatus()));
		}


		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldDataSubmission.mold.equipmentCode.contains(getQuery())
							.or(moldDataSubmission.mold.location.name.contains(getQuery()))
							.or(moldDataSubmission.mold.location.locationCode.contains(getQuery()))
							.or(mold.location.company.companyCode.contains(getQuery()))
							.or(mold.location.company.name.contains(getQuery()))
//							.or(moldDataSubmission.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
//							.or(moldDataSubmission.notificationStatus.stringValue().containsIgnoreCase(getQuery()))

			);
		}


		if (!StringUtils.isEmpty(getStatus())) {
			if ("alert".equalsIgnoreCase(getStatus())) {
				if(SecurityUtils.isAdmin()) {
					builder.and(moldDataSubmission.notificationStatus.eq(NotificationStatus.PENDING));
				}else{
					builder.and(moldDataSubmission.notificationStatus.in(Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED)));
					builder.and(moldDataSubmission.confirmed.isFalse()
						.or(moldDataSubmission.confirmed.isNull()));
				}
				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
						.where(logUserAlert.alertType.eq(AlertType.DATA_SUBMISSION).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
				builder.and(moldDataSubmission.id.in(query));
			} else if ("confirmed".equalsIgnoreCase(getStatus())) {
				if(SecurityUtils.isAdmin()){
					builder.and(moldDataSubmission.notificationStatus.in(Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED)));
				}else {
					builder.and(moldDataSubmission.confirmed.isTrue());
				}
			}
		}

		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldDataSubmission.latest.isTrue());
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldDataSubmission.moldId.in(getFilteredIds()));
		}

		//check deleted
		checkDeleted(moldDataSubmission.mold, builder);

		return builder;
	}

	public Predicate getRefurbishmentPredicate() {
		QMoldRefurbishment moldRefurbishment = QMoldRefurbishment.moldRefurbishment;
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldRefurbishment.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		builder.and(moldRefurbishment.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));

		if(getId() != null){
			builder.and(moldRefurbishment.mold.id.eq(getId()));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldRefurbishment.mold.equipmentCode.contains(getQuery())
							.or(moldRefurbishment.mold.location.name.contains(getQuery()))
							.or(moldRefurbishment.mold.location.locationCode.contains(getQuery()))
							.or(mold.location.company.companyCode.contains(getQuery()))
							.or(mold.location.company.name.contains(getQuery()))
//							.or(moldRefurbishment.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
			);
		}

//		if (!StringUtils.isEmpty(getStatus())) {
		if (!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("alert")) {
//			QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//			JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//					.where(logUserAlert.alertType.eq(AlertType.REFURBISHMENT).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//			builder.and(moldRefurbishment.id.in(query));
			builder.and(moldRefurbishment.refurbishmentStatus.in(Arrays.asList(
					RefurbishmentStatus.END_OF_LIFECYCLE,
					RefurbishmentStatus.REQUESTED
			)));
			if(specialAlertType != null){
				if(specialAlertType.equals(SpecialAlertType.MEDIUM_HIGH)){
					builder.and(moldRefurbishment.priority.in(
							Arrays.asList(PriorityType.MEDIUM,PriorityType.HIGH)
					));
				}
				else if(specialAlertType.equals(SpecialAlertType.HIGH)){
					builder.and(moldRefurbishment.priority.eq(PriorityType.HIGH));
				}
			}

			if(getLastAlert() != null && getLastAlert() == true){
				builder.and(moldRefurbishment.latest.isTrue());
			}
		} else if(!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("approved")){

			builder.and(moldRefurbishment.refurbishmentStatus.in(Arrays.asList(
					RefurbishmentStatus.APPROVED
			)));
			if(getLastAlert() != null && getLastAlert() == true){
				builder.and(moldRefurbishment.latest.isTrue());
			}
		} else if(!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("disapproved")){
			builder.and(moldRefurbishment.refurbishmentStatus.in(Arrays.asList(
					RefurbishmentStatus.DISAPPROVED
			)));
			if(getLastAlert() != null && getLastAlert() == true){
				builder.and(moldRefurbishment.latest.isTrue());
			}
		} else if(StringUtils.isEmpty(getStatus()) || getStatus().equalsIgnoreCase("history")){
			if(getLastAlert() != null && getLastAlert() == true){
				builder.and(moldRefurbishment.lastChecked.isTrue());
			}
			builder.and(moldRefurbishment.refurbishmentStatus.notIn(Arrays.asList(
					RefurbishmentStatus.END_OF_LIFECYCLE,
					RefurbishmentStatus.MEDIUM_HIGH,
					RefurbishmentStatus.REQUESTED
			)));
		}
//		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldRefurbishment.moldId.in(getFilteredIds()));
		}

		//check deleted
		checkDeleted(moldRefurbishment.mold, builder);
		return builder;
	}

	public Predicate getDetachmentPredicate() {
		QMoldDetachment moldDetachment = QMoldDetachment.moldDetachment;
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldDetachment.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if(getId() != null){
			builder.and(moldDetachment.mold.id.eq(getId()));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldDetachment.mold.equipmentCode.contains(getQuery())
							.or(moldDetachment.mold.location.name.contains(getQuery()))
							.or(moldDetachment.mold.location.locationCode.contains(getQuery()))
							.or(mold.location.company.companyCode.contains(getQuery()))
							.or(mold.location.company.name.contains(getQuery()))
//							.or(moldDetachment.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
//							.or(moldDetachment.equipmentStatus.stringValue().containsIgnoreCase(getQuery()))
			);
		}

//		if (!StringUtils.isEmpty(getStatus())) {
		if (!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("alert")) {
//			QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//			JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//					.where(logUserAlert.alertType.eq(AlertType.DETACHMENT).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//			builder.and(moldDetachment.id.in(query));
			builder.and(moldDetachment.notificationStatus.in(Arrays.asList(
					NotificationStatus.ALERT
			)));

		}else if(StringUtils.isEmpty(getStatus()) || getStatus().equalsIgnoreCase("history")){
//			if(getLastAlert() != null && getLastAlert() == true){
//				builder.and(moldDetachment.lastChecked.isTrue());
//			}
			builder.and(moldDetachment.notificationStatus.notIn(Arrays.asList(
					NotificationStatus.ALERT
//					NotificationStatus.FIXED
			)));
		}
        if(getLastAlert() != null && getLastAlert() == true){
            builder.and(moldDetachment.latest.isTrue());
        }
//		}


		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldDetachment.moldId.in(getFilteredIds()));
		}
		//check deleted
		checkDeleted(moldDetachment.mold, builder);

		return builder;
	}

	public Predicate getDowntimePredicate(){
		QMoldDowntimeEvent moldDowntimeEvent = QMoldDowntimeEvent.moldDowntimeEvent;
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldDowntimeEvent.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if(getId() != null){
			builder.and(moldDowntimeEvent.mold.id.eq(getId()));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					moldDowntimeEvent.mold.equipmentCode.contains(getQuery())
							.or(moldDowntimeEvent.mold.location.name.contains(getQuery()))
							.or(moldDowntimeEvent.mold.location.locationCode.contains(getQuery()))
							.or(moldDowntimeEvent.mold.location.company.companyCode.contains(getQuery()))
							.or(moldDowntimeEvent.mold.location.company.name.contains(getQuery()))
//							.or(moldDowntimeEvent.mold.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
//							.or(moldDowntimeEvent.downtimeStatus.stringValue().containsIgnoreCase(getQuery()))
			);
		}

//		if (!StringUtils.isEmpty(getStatus())) {
		if (!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("alert")) {
//			QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//			JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//					.where(logUserAlert.alertType.eq(AlertType.DOWNTIME).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//			builder.and(moldDowntimeEvent.id.in(query));
			builder.and(moldDowntimeEvent.downtimeStatus.eq(DowntimeStatus.DOWNTIME));

		} else if (!StringUtils.isEmpty(getStatus()) && getStatus().equalsIgnoreCase("all")) {

		} else if(StringUtils.isEmpty(getStatus()) || getStatus().equalsIgnoreCase("history")){
			builder.and(moldDowntimeEvent.downtimeStatus.eq(DowntimeStatus.CONFIRMED));
		}
		if(getLastAlert() != null && getLastAlert() == true){
			builder.and(moldDowntimeEvent.latest.isTrue());
		}

		if (CollectionUtils.isNotEmpty(getFilteredIds())) {
			builder.and(moldDowntimeEvent.moldId.in(getFilteredIds()));
		}

		//check deleted
		checkDeleted(moldDowntimeEvent.mold, builder);

		return builder;
	}

	private void checkDeleted(QMold mold, BooleanBuilder builder)
	{
		if (getDeleted() == null)
		{
			builder.and(mold.deleted.isFalse().or(mold.deleted.isNull()));
		}
		else if (getDeleted())
		{
			builder.and(mold.deleted.isTrue());
		}
	}

	private void checkDataStatus(QMold mold, BooleanBuilder builder) {
		BooleanBuilder statusBuilder =  new BooleanBuilder();
		QCounter counter = QCounter.counter;
		if(CollectionUtils.isNotEmpty(getToolingStatusList())) {
			builder.and(mold.toolingStatus.in(getToolingStatusList()));
		}

//			for (MoldStatus moldStatus : moldStatusList) {
//				switch (moldStatus) {
//					case IN_PRODUCTION:{
//						statusBuilder.or(new BooleanBuilder().and(
//								mold.operatingStatus.eq(OperatingStatus.WORKING)
//								.and(
//										mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
//										.where(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))))
//								))
//						);
//						break;
//					}
//					case IDLE:{
//						statusBuilder.or(new BooleanBuilder().and(
//								mold.operatingStatus.eq(OperatingStatus.IDLE)
//								.and(
//										mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
//												.where(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))))
//								)
//						));
//						break;
//					}
//					case NOT_WORKING: {
//						statusBuilder.or(new BooleanBuilder().and(
//								mold.operatingStatus.eq(OperatingStatus.NOT_WORKING)
//								.and(
//										mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
//												.where(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))))
//								)
//						));
//						break;
//					}
//					case SENSOR_OFFLINE:{
//						statusBuilder.or(new BooleanBuilder()
//								.and(
//										mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
//												.where(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.eq(OperatingStatus.DISCONNECTED).or(mold.operatingStatus.eq(OperatingStatus.DISCONNECTED)))))
//								)
//						);
//						break;
//					}
//					case SENSOR_DETACHED:{
//						statusBuilder.or(mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
//								.where(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))));
//						break;
//					}
//					case NO_SENSOR:{
//						statusBuilder.or(mold.counterId.isNull());
//						break;
//					}
//					case ON_STANDBY:{
//						statusBuilder.or(mold.operatingStatus.isNull()
//								.and(mold.counterId.in(JPAExpressions.select(counter.id).from(counter).where(counter.operatingStatus.isNull()))));
//					}
//				}
//			}
//		}

		if (CollectionUtils.isNotEmpty(counterStatusList) && counterStatusList.size() < 3) {
			for (CounterStatus counterStatus : counterStatusList) {
				switch (counterStatus) {
					case INSTALLED:{
						statusBuilder.or(mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
								.where(counter.equipmentStatus.eq(EquipmentStatus.INSTALLED))));
						break;
					}
					case DETACHED:{
						statusBuilder.or(mold.counterId.in(JPAExpressions.select(counter.id).from(counter)
								.where(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))));
						break;
					}
					case NOT_INSTALLED: {
						statusBuilder.or(mold.counterId.isNull());
						break;
					}
				}
			}
		}

		if(CollectionUtils.isEmpty(getToolingStatusList()) && CollectionUtils.isEmpty(counterStatusList)
				&& isToolingScreen != null && isToolingScreen) {
			builder.and(mold.id.isNull());
		} else {
			builder.and(statusBuilder);
		}

	}


	public Predicate getPredicateForMatching() {
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(
					mold.equipmentCode.contains(getQuery()));
		}

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFields(String query) {
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QCategory category = new QCategory("category");
		QCategory brand = new QCategory("brand");
		QUser user = QUser.user;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));
		builder.or(mold.id.in(
				JPAExpressions
						.select(moldPart.mold.id)
						.from(moldPart)
						.innerJoin(part).on(moldPart.partId.eq(part.id))
						.where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
		));
		getSelectedFields().forEach(selectedField -> {
			switch (selectedField) {
				case "equipmentStatus":
					builder.or(mold.equipmentStatus.stringValue().containsIgnoreCase(query));
					break;
				case "company":
					builder.or(mold.location.id.in(
							JPAExpressions
									.select(mold.locationId)
									.from(mold)
									.leftJoin(location).on(mold.locationId.eq(location.id))
									.leftJoin(company).on(location.companyId.eq(company.id))
									.where(company.companyCode.contains(query)
											.or(company.name.contains(query)))
					));
					break;
				case "location":
					builder.or(mold.location.id.in(
							JPAExpressions
									.select(mold.locationId)
									.from(mold)
									.leftJoin(location).on(mold.locationId.eq(location.id))
									.leftJoin(company).on(location.companyId.eq(company.id))
									.where(location.name.contains(query)
											.or(location.locationCode.contains(query)))
					));
					break;
				case "op":
					builder.or(mold.operatingStatus.stringValue().containsIgnoreCase(query));
					break;
//				case "engineerInCharge":
//					builder.or(mold.engineers.contains(
//							JPAExpressions
//									.select(user)
//									.from(user)
//									.where(user.name.contains(query))
//					));
//					break;
				case "counterCode":
					builder.or(mold.counterId.in(
							JPAExpressions
									.select(counter.id)
									.from(counter)
									.where(counter.equipmentCode.contains(query))));
					break;
				case "hotRunnerDrop":
					builder.or(mold.hotRunnerDrop.contains(query));
					break;
				case "hotRunnerZone":
					builder.or(mold.hotRunnerZone.contains(query));
					break;
				case "injectionMachineId":
					builder.or(mold.injectionMachineId.contains(query));
					break;
				case "runnerMaker":
					builder.or(mold.runnerMaker.contains(query));
					break;
				case "runnerTypeTitle":
					builder.or(mold.runnerType.contains(query));
					break;
				case "supplierCompanyName":
					builder.or(mold.supplier.name.contains(query));
					break;
				case "toolMakerCompanyName":
					builder.or(mold.toolMaker.name.contains(query));
					break;
//				case "dataSubmission":
//					builder.or(mold.dataSubmission.stringValue().contains(query));
//					break;
				case "toolDescription":
					builder.or(mold.toolDescription.contains(query));
					break;
				case "toolingType":
					builder.or(mold.toolingType.contains(query));
					break;
				case "toolingLetter":
					builder.or(mold.toolingLetter.contains(query));
					break;
				case "toolingComplexity":
					builder.or(mold.toolingComplexity.contains(query));
					break;
				case "productAndCategory":
					builder.or(mold.id.in(
							JPAExpressions
									.select(moldPart.mold.id)
									.from(moldPart)
									.innerJoin(part).on(moldPart.partId.eq(part.id))
									.where(part.category.name.contains(query)
											.or(part.category.parentId.in(JPAExpressions.select(brand.id).from(brand).where(brand.parent.name.contains(query).and(brand.level.eq(2)))))
											.or(part.category.grandParentId.in(JPAExpressions.select(category.id).from(category).where(category.name.contains(query).and(category.level.eq(1)))))
											.and(part.deleted.isNull().or(part.deleted.isFalse())))
					));
					break;
				default:
					break;
			}
		});

		List<Long> customFieldIds = DataUtils.getNumericElements(getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFieldsForRelocation(String query) {
		QMoldLocation moldLocation = QMoldLocation.moldLocation;
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QUser user = QUser.user;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(moldLocation.mold.equipmentCode.contains(getQuery()))
					.or(moldLocation.mold.location.name.contains(getQuery()))
					.or(moldLocation.mold.location.locationCode.contains(getQuery()))
					.or(mold.location.company.name.contains(getQuery()))
					.or(mold.location.company.companyCode.contains(getQuery()));
		} else {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
					case "part":
						builder.or(moldLocation.mold.id.in(
								JPAExpressions
										.select(moldPart.mold.id)
										.from(moldPart)
										.innerJoin(part).on(moldPart.partId.eq(part.id))
										.where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
						));
						break;
//					case "moldLocationStatus":
//						builder.or(moldLocation.moldLocationStatus.stringValue().containsIgnoreCase(query));
//						break;
					case "company":
						builder.or(moldLocation.mold.location.id.in(
								JPAExpressions
										.select(mold.locationId)
										.from(mold)
										.leftJoin(location).on(mold.locationId.eq(location.id))
										.leftJoin(company).on(location.companyId.eq(company.id))
										.where(company.companyCode.contains(query)
												.or(company.name.contains(query)))
						));
						break;
					case "location":
						builder.or(moldLocation.location.id.in(
								JPAExpressions
										.select(location.id)
										.from(location)
										.where(location.name.contains(query)
												.or(location.locationCode.contains(query)))
						));
						break;
					case "previousLocation":
						//todo build query for previous location
						break;
//					case "operatingStatus":
//						builder.or(moldLocation.mold.operatingStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "mold.engineerInCharge":
//						builder.or(moldLocation.mold.engineers.contains(
//								JPAExpressions
//										.select(user)
//										.from(user)
//										.where(user.name.contains(query))
//						));
//						break;
					case "mold.counterCode":
						builder.or(moldLocation.mold.counterId.in(
								JPAExpressions
										.select(counter.id)
										.from(counter)
										.where(counter.equipmentCode.contains(query))));
						break;
					case "mold.hotRunnerDrop":
						builder.or(moldLocation.mold.hotRunnerDrop.contains(query));
						break;
					case "mold.hotRunnerZone":
						builder.or(moldLocation.mold.hotRunnerZone.contains(query));
						break;
					case "mold.injectionMachineId":
						builder.or(moldLocation.mold.injectionMachineId.contains(query));
						break;
					case "mold.runnerMaker":
						builder.or(moldLocation.mold.runnerMaker.contains(query));
						break;
					case "mold.runnerTypeTitle":
						builder.or(moldLocation.mold.runnerType.contains(query));
						break;
					case "mold.supplierCompanyName":
						builder.or(moldLocation.mold.supplier.name.contains(query));
						break;
					case "mold.toolMakerCompanyName":
						builder.or(moldLocation.mold.toolMaker.name.contains(query));
						break;
					case "mold.toolDescription":
						builder.or(moldLocation.mold.toolDescription.contains(query));
						break;
					case "mold.toolingType":
						builder.or(moldLocation.mold.toolingType.contains(query));
						break;
					case "mold.toolingLetter":
						builder.or(moldLocation.mold.toolingLetter.contains(query));
						break;
					case "mold.toolingComplexity":
						builder.or(moldLocation.mold.toolingComplexity.contains(query));
						break;
					default:
						break;
				}
			});
		}

		List<Long> customFieldIds = DataUtils.getRawNumericElements("mold.",getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(moldLocation.mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFieldsForCycleTime(String query) {
		QMoldCycleTime moldCycleTime = QMoldCycleTime.moldCycleTime;
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QUser user = QUser.user;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(moldCycleTime.mold.equipmentCode.contains(query))
					.or(moldCycleTime.mold.location.name.contains(query))
					.or(moldCycleTime.mold.location.locationCode.contains(query))
					.or(mold.location.company.name.contains(query))
					.or(mold.location.company.companyCode.contains(query));
		} else {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
					case "part":
						builder.or(moldCycleTime.mold.id.in(
								JPAExpressions
										.select(moldPart.mold.id)
										.from(moldPart)
										.innerJoin(part).on(moldPart.partId.eq(part.id))
										.where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
						));
						break;
//					case "cycleTimeStatus":
//						builder.or(moldCycleTime.cycleTimeStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "notificationStatus":
//						builder.or(moldCycleTime.mold.equipmentStatus.stringValue().containsIgnoreCase(query));
//						break;
					case "company":
						builder.or(moldCycleTime.mold.location.id.in(
								JPAExpressions
										.select(mold.locationId)
										.from(mold)
										.leftJoin(location).on(mold.locationId.eq(location.id))
										.leftJoin(company).on(location.companyId.eq(company.id))
										.where(company.companyCode.contains(query)
												.or(company.name.contains(query)))
						));
						break;
					case "location":
						builder.or(moldCycleTime.mold.location.id.in(
								JPAExpressions
										.select(location.id)
										.from(location)
										.where(location.name.contains(query)
												.or(location.locationCode.contains(query)))
						));
						break;
//					case "operatingStatus":
//						builder.or(moldCycleTime.mold.operatingStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "mold.engineerInCharge":
//						builder.or(moldCycleTime.mold.engineers.contains(
//								JPAExpressions
//										.select(user)
//										.from(user)
//										.where(user.name.contains(query))
//						));
//						break;
					case "mold.counterCode":
						builder.or(moldCycleTime.mold.counterId.in(
								JPAExpressions
										.select(counter.id)
										.from(counter)
										.where(counter.equipmentCode.contains(query))));
						break;
					case "mold.hotRunnerDrop":
						builder.or(moldCycleTime.mold.hotRunnerDrop.contains(query));
						break;
					case "mold.hotRunnerZone":
						builder.or(moldCycleTime.mold.hotRunnerZone.contains(query));
						break;
					case "mold.injectionMachineId":
						builder.or(moldCycleTime.mold.injectionMachineId.contains(query));
						break;
					case "mold.runnerMaker":
						builder.or(moldCycleTime.mold.runnerMaker.contains(query));
						break;
					case "mold.runnerTypeTitle":
						builder.or(moldCycleTime.mold.runnerType.contains(query));
						break;
					case "mold.supplierCompanyName":
						builder.or(moldCycleTime.mold.supplier.name.contains(query));
						break;
					case "mold.toolMakerCompanyName":
						builder.or(moldCycleTime.mold.toolMaker.name.contains(query));
						break;
					case "mold.toolDescription":
						builder.or(moldCycleTime.mold.toolDescription.contains(query));
						break;
					case "mold.toolingType":
						builder.or(moldCycleTime.mold.toolingType.contains(query));
						break;
					case "mold.toolingLetter":
						builder.or(moldCycleTime.mold.toolingLetter.contains(query));
						break;
					case "mold.toolingComplexity":
						builder.or(moldCycleTime.mold.toolingComplexity.contains(query));
						break;
					default:
						break;
				}
			});
		}

		List<Long> customFieldIds = DataUtils.getRawNumericElements("mold.",getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(moldCycleTime.mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFieldsForMaintenance(String query) {
		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QUser user = QUser.user;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(moldMaintenance.mold.equipmentCode.contains(query))
					.or(moldMaintenance.mold.location.name.contains(query))
					.or(moldMaintenance.mold.location.locationCode.contains(query))
					.or(mold.location.company.name.contains(query))
					.or(mold.location.company.companyCode.contains(query));
		} else {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
					case "part":
						builder.or(moldMaintenance.mold.id.in(
								JPAExpressions
										.select(moldPart.mold.id)
										.from(moldPart)
										.innerJoin(part).on(moldPart.partId.eq(part.id))
										.where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
						));
						break;
//					case "notificationStatus":
//						builder.or(moldMaintenance.mold.equipmentStatus.stringValue().containsIgnoreCase(query));
//						break;
					case "company":
						builder.or(moldMaintenance.mold.location.id.in(
								JPAExpressions
										.select(mold.locationId)
										.from(mold)
										.leftJoin(location).on(mold.locationId.eq(location.id))
										.leftJoin(company).on(location.companyId.eq(company.id))
										.where(company.companyCode.contains(query)
												.or(company.name.contains(query)))
						));
						break;
					case "location":
						builder.or(moldMaintenance.mold.location.id.in(
								JPAExpressions
										.select(location.id)
										.from(location)
										.where(location.name.contains(query)
												.or(location.locationCode.contains(query)))
						));
						break;
//					case "operatingStatus":
//						builder.or(moldMaintenance.mold.operatingStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "mold.engineerInCharge":
//						builder.or(moldMaintenance.mold.engineers.contains(
//								JPAExpressions
//										.select(user)
//										.from(user)
//										.where(user.name.contains(query))
//						));
//						break;
					case "mold.counterCode":
						builder.or(moldMaintenance.mold.counterId.in(
								JPAExpressions
										.select(counter.id)
										.from(counter)
										.where(counter.equipmentCode.contains(query))));
						break;
					case "mold.hotRunnerDrop":
						builder.or(moldMaintenance.mold.hotRunnerDrop.contains(query));
						break;
					case "mold.hotRunnerZone":
						builder.or(moldMaintenance.mold.hotRunnerZone.contains(query));
						break;
					case "mold.injectionMachineId":
						builder.or(moldMaintenance.mold.injectionMachineId.contains(query));
						break;
					case "mold.runnerMaker":
						builder.or(moldMaintenance.mold.runnerMaker.contains(query));
						break;
					case "mold.runnerTypeTitle":
						builder.or(moldMaintenance.mold.runnerType.contains(query));
						break;
					case "mold.supplierCompanyName":
						builder.or(moldMaintenance.mold.supplier.name.contains(query));
						break;
					case "mold.toolMakerCompanyName":
						builder.or(moldMaintenance.mold.toolMaker.name.contains(query));
						break;
					case "mold.toolDescription":
						builder.or(moldMaintenance.mold.toolDescription.contains(query));
						break;
					case "mold.toolingType":
						builder.or(moldMaintenance.mold.toolingType.contains(query));
						break;
					case "mold.toolingLetter":
						builder.or(moldMaintenance.mold.toolingLetter.contains(query));
						break;
					case "mold.toolingComplexity":
						builder.or(moldMaintenance.mold.toolingComplexity.contains(query));
						break;
					default:
						break;
				}
			});
		}

		List<Long> customFieldIds = DataUtils.getRawNumericElements("mold.",getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(moldMaintenance.mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFieldsForEfficiency(String query) {
		QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QUser user = QUser.user;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(moldEfficiency.mold.location.name.contains(query))
					.or(moldEfficiency.mold.location.locationCode.contains(query))
					.or(mold.location.company.name.contains(query))
					.or(mold.location.company.companyCode.contains(query));
		} else {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
					case "part":
						builder.or(moldEfficiency.mold.id.in(
								JPAExpressions
										.select(moldPart.mold.id)
										.from(moldPart)
										.innerJoin(part).on(moldPart.partId.eq(part.id))
										.where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
						));
						break;
//					case "notificationStatus":
//						builder.or(moldEfficiency.mold.equipmentStatus.stringValue().containsIgnoreCase(query));
//						break;
					case "company":
						builder.or(moldEfficiency.mold.location.id.in(
								JPAExpressions
										.select(mold.locationId)
										.from(mold)
										.leftJoin(location).on(mold.locationId.eq(location.id))
										.leftJoin(company).on(location.companyId.eq(company.id))
										.where(company.companyCode.contains(query)
												.or(company.name.contains(query)))
						));
						break;
					case "location":
						builder.or(moldEfficiency.mold.location.id.in(
								JPAExpressions
										.select(location.id)
										.from(location)
										.where(location.name.contains(query)
												.or(location.locationCode.contains(query)))
						));
						break;
//					case "operatingStatus":
//						builder.or(moldEfficiency.mold.operatingStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "mold.engineerInCharge":
//						builder.or(moldEfficiency.mold.engineers.contains(
//								JPAExpressions
//										.select(user)
//										.from(user)
//										.where(user.name.contains(query))
//						));
//						break;
					case "mold.counterCode":
						builder.or(moldEfficiency.mold.counterId.in(
								JPAExpressions
										.select(counter.id)
										.from(counter)
										.where(counter.equipmentCode.contains(query))));
						break;
					case "mold.hotRunnerDrop":
						builder.or(moldEfficiency.mold.hotRunnerDrop.contains(query));
						break;
					case "mold.hotRunnerZone":
						builder.or(moldEfficiency.mold.hotRunnerZone.contains(query));
						break;
					case "mold.injectionMachineId":
						builder.or(moldEfficiency.mold.injectionMachineId.contains(query));
						break;
					case "mold.runnerMaker":
						builder.or(moldEfficiency.mold.runnerMaker.contains(query));
						break;
					case "mold.runnerTypeTitle":
						builder.or(moldEfficiency.mold.runnerType.contains(query));
						break;
					case "mold.supplierCompanyName":
						builder.or(moldEfficiency.mold.supplier.name.contains(query));
						break;
					case "mold.toolMakerCompanyName":
						builder.or(moldEfficiency.mold.toolMaker.name.contains(query));
						break;
					case "mold.toolDescription":
						builder.or(moldEfficiency.mold.toolDescription.contains(query));
						break;
					case "mold.toolingType":
						builder.or(moldEfficiency.mold.toolingType.contains(query));
						break;
					case "mold.toolingLetter":
						builder.or(moldEfficiency.mold.toolingLetter.contains(query));
						break;
					case "mold.toolingComplexity":
						builder.or(moldEfficiency.mold.toolingComplexity.contains(query));
						break;
					default:
						break;
				}
			});
		}

		List<Long> customFieldIds = DataUtils.getRawNumericElements("mold.",getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(moldEfficiency.mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFieldsForCorrective(String query) {
		QMoldCorrective moldCorrective = QMoldCorrective.moldCorrective;
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QUser user = QUser.user;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));

		getSelectedFields().forEach(selectedField -> {
			switch (selectedField) {
				case "part":
					builder.or(moldCorrective.mold.id.in(
							JPAExpressions
									.select(moldPart.mold.id)
									.from(moldPart)
									.innerJoin(part).on(moldPart.partId.eq(part.id))
									.where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
					));
					break;
//				case "notificationStatus":
//					builder.or(moldCorrective.mold.equipmentStatus.stringValue().containsIgnoreCase(query));
//					break;
				case "company":
					builder.or(moldCorrective.mold.location.id.in(
							JPAExpressions
									.select(mold.locationId)
									.from(mold)
									.leftJoin(location).on(mold.locationId.eq(location.id))
									.leftJoin(company).on(location.companyId.eq(company.id))
									.where(company.companyCode.contains(query)
											.or(company.name.contains(query)))
					));
					break;
				case "location":
					builder.or(moldCorrective.mold.location.id.in(
							JPAExpressions
									.select(location.id)
									.from(location)
									.where(location.name.contains(query)
											.or(location.locationCode.contains(query)))
					));
					break;
//				case "operatingStatus":
//					builder.or(moldCorrective.mold.operatingStatus.stringValue().containsIgnoreCase(query));
//					break;
//				case "mold.engineerInCharge":
//					builder.or(moldCorrective.mold.engineers.contains(
//							JPAExpressions
//									.select(user)
//									.from(user)
//									.where(user.name.contains(query))
//					));
//					break;
				case "mold.counterCode":
					builder.or(moldCorrective.mold.counterId.in(
							JPAExpressions
									.select(counter.id)
									.from(counter)
									.where(counter.equipmentCode.contains(query))));
					break;
				case "mold.hotRunnerDrop":
					builder.or(moldCorrective.mold.hotRunnerDrop.contains(query));
					break;
				case "mold.hotRunnerZone":
					builder.or(moldCorrective.mold.hotRunnerZone.contains(query));
					break;
				case "mold.injectionMachineId":
					builder.or(moldCorrective.mold.injectionMachineId.contains(query));
					break;
				case "mold.runnerMaker":
					builder.or(moldCorrective.mold.runnerMaker.contains(query));
					break;
				case "mold.runnerTypeTitle":
					builder.or(moldCorrective.mold.runnerType.contains(query));
					break;
				case "mold.supplierCompanyName":
					builder.or(moldCorrective.mold.supplier.name.contains(query));
					break;
				case "mold.toolMakerCompanyName":
					builder.or(moldCorrective.mold.toolMaker.name.contains(query));
					break;
				case "mold.toolDescription":
					builder.or(moldCorrective.mold.toolDescription.contains(query));
					break;
				case "mold.toolingType":
					builder.or(moldCorrective.mold.toolingType.contains(query));
					break;
				case "mold.toolingLetter":
					builder.or(moldCorrective.mold.toolingLetter.contains(query));
					break;
				case "mold.toolingComplexity":
					builder.or(moldCorrective.mold.toolingComplexity.contains(query));
					break;
				default:
					break;
			}
		});


		List<Long> customFieldIds = DataUtils.getRawNumericElements("mold.",getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(moldCorrective.mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFieldsForDowntime(String query) {
		QMoldDowntimeEvent moldDowntimeEvent = QMoldDowntimeEvent.moldDowntimeEvent;
		QMold mold = QMold.mold;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(mold.equipmentCode.contains(query));

		getSelectedFields().forEach(selectedField -> {
			switch (selectedField) {
				case "company":
					builder.or(moldDowntimeEvent.mold.location.id.in(
							JPAExpressions
									.select(mold.locationId)
									.from(mold)
									.leftJoin(location).on(mold.locationId.eq(location.id))
									.leftJoin(company).on(location.companyId.eq(company.id))
									.where(company.companyCode.contains(query)
											.or(company.name.contains(query)))
					));
					break;
				case "location":
					builder.or(moldDowntimeEvent.mold.location.id.in(
							JPAExpressions
									.select(location.id)
									.from(location)
									.where(location.name.contains(query)
											.or(location.locationCode.contains(query)))
					));
					break;
				default:
					break;
			}
		});


		List<Long> customFieldIds = DataUtils.getRawNumericElements("mold.",getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(moldDowntimeEvent.mold.id.in(
					JPAExpressions
							.select(customFieldValue.objectId)
							.from(customFieldValue)
							.where(customFieldValue.value.contains(query))));
		});

		return builder;
	}

	public String toSize() {
		String size = null;
		String sizeW = this.sizeW != null ? this.sizeW.trim() : "";
		String sizeL = this.sizeL != null ? this.sizeL.trim() : "";
		String sizeH = this.sizeH != null ? this.sizeH.trim() : "";
		if (StringUtils.isEmpty(sizeW + sizeL + sizeH)) return size;
		size = sizeW + " x " + sizeL + " x " + sizeH;
		return size;
	}

	public void populate(final Mold mold){
		Object toTemp = ValueUtils.fromJsonStr(ValueUtils.toJsonStr(mold),MoldPayload.class);
		ValueUtils.map(toTemp,this);

		if (mold.getPoDate() != null)
			this.setPoDate(DateUtils2.format(mold.getPoDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS));
	}

	public boolean isModifyMoldPmPlan(MoldPmPlan moldPmPlan) {
		return moldPmPlan == null
				|| this.pmStrategy != moldPmPlan.getPmStrategy()
				|| this.pmFrequency != moldPmPlan.getPmFrequency()
				|| !org.apache.commons.lang3.StringUtils.equals(this.schedStartDate, moldPmPlan.getSchedStartDate())
				|| !org.apache.commons.lang3.StringUtils.equals(this.recurrDueDate, moldPmPlan.getRecurrDueDate())
				|| !Objects.equals(this.schedInterval, moldPmPlan.getSchedInterval())
				|| !Objects.equals(this.schedOrdinalNum, moldPmPlan.getSchedOrdinalNum())
				|| !Objects.equals(this.schedUpcomingTolerance, moldPmPlan.getSchedUpcomingTolerance())
				|| !Objects.equals(this.recurrNum, moldPmPlan.getRecurrNum())
				;
	}
}
