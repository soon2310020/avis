package saleson.api.mold;

import static saleson.service.util.NumberUtils.assertGtValue;
import static saleson.service.util.NumberUtils.assertGteValue;
import static saleson.service.util.NumberUtils.assertIntegerNumberOrNull;
import static saleson.service.util.NumberUtils.assertLtValue;
import static saleson.service.util.NumberUtils.assertLteValue;
import static saleson.service.util.NumberUtils.assertNumberOrNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.Acceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_FREQUENCY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.RECURR_CONSTRAINT_TYPE;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlan;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlanRepository;
import com.emoldino.api.asset.resource.base.mold.dto.OptimalCycleTime;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.rststp.service.RstStpService;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.masterdata.dto.Currency;
import com.emoldino.api.common.resource.base.masterdata.util.MasterDataUtils;
import com.emoldino.api.common.resource.base.menu.util.MenuUtils;
import com.emoldino.api.common.resource.base.option.dto.InactiveConfig;
import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.base.workorder.enumeration.WorkOrderParticipantType;
import com.emoldino.framework.enumeration.ActiveStatus;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobCall;
import com.emoldino.framework.util.JobUtils.JobCallParam;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;
import saleson.api.accessHierachy.AccessHierarchyRepository;
import saleson.api.accessHierachy.AccessHierarchyService;
import saleson.api.broadcastNotification.BroadcastNotificationService;
import saleson.api.category.CategoryRepository;
import saleson.api.category.payload.CategoryParam;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.company.CompanyRepository;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.counter.CounterRepository;
import saleson.api.counter.CounterService;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.dashboardSetting.DashboardSettingRepository;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataRequest.DataRequestService;
import saleson.api.endLifeCycle.MoldEndLifeCycleRepository;
import saleson.api.endLifeCycle.MoldEndLifeCycleService;
import saleson.api.endLifeCycle.MoldRefurbishmentRepository;
import saleson.api.filestorage.payload.FileStoragePayload;
import saleson.api.location.LocationRepository;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.MachineRepository;
import saleson.api.machine.payload.MachineMoldData;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertRepository;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.api.mold.payload.DataSubmissionConfirmListPayload;
import saleson.api.mold.payload.InactiveToolingCount;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.mold.payload.NoteAndChangeStatusPayLoad;
import saleson.api.mold.payload.PartChangeData;
import saleson.api.mold.payload.PartChangeDataFull;
import saleson.api.mold.payload.PartData;
import saleson.api.mold.payload.PartLiteData;
import saleson.api.mold.payload.SwitchMoldPartPayload;
import saleson.api.notification.NotificationService;
import saleson.api.part.PartProjectProducedRepository;
import saleson.api.part.PartRepository;
import saleson.api.part.PartService;
import saleson.api.part.ResinCodeChangeRepository;
import saleson.api.part.payload.PartPayload;
import saleson.api.preset.PresetRepository;
import saleson.api.preset.PresetService;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.api.role.RoleRepository;
import saleson.api.statistics.StatisticsPartReplacedRepository;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.statistics.StatisticsService;
import saleson.api.statistics.StatisticsWutRepository;
import saleson.api.statistics.StatisticsWutService;
import saleson.api.systemNote.SystemNoteService;
import saleson.api.systemNote.payload.SystemNotePayload;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.tabbedFilter.TabbedOverviewGeneralFilterService;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.api.terminal.TerminalDisconnectRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.api.terminal.payload.TerminalAlertPayload;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.user.UserAlertRepository;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.api.versioning.repositories.MoldVersionRepository;
import saleson.api.versioning.repositories.ReversionHistoryRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.api.workOrder.WorkOrderAssetRepository;
import saleson.api.workOrder.WorkOrderRepository;
import saleson.api.workOrder.WorkOrderService;
import saleson.api.workOrder.WorkOrderUserRepository;
import saleson.api.workOrder.payload.WorkOrderPayload;
import saleson.common.config.Const;
import saleson.common.constant.CommonMessage;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.domain.ListIdPayload;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.ColorCode;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ContinentName;
import saleson.common.enumeration.CorrectiveAction;
import saleson.common.enumeration.CorrectiveStatus;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.DashboardChartType;
import saleson.common.enumeration.DashboardSettingLevel;
import saleson.common.enumeration.DataRangeType;
import saleson.common.enumeration.DataSubmissionAction;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.DayOfWeek;
import saleson.common.enumeration.DowntimeStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.FrequentUsage;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.MaintenanceTimeStatus;
import saleson.common.enumeration.MaintenanceType;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.PeriodType;
import saleson.common.enumeration.PresetStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.RefurbishmentStatus;
import saleson.common.enumeration.ReportType;
import saleson.common.enumeration.RevisionObjectType;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.enumeration.StorageType;
import saleson.common.enumeration.TabType;
import saleson.common.enumeration.WeightUnit;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.enumeration.WorkOrderType;
import saleson.common.enumeration.productivity.CompareType;
import saleson.common.enumeration.productivity.ProductivityVariable;
import saleson.common.exception.BadRequestException;
import saleson.common.notification.MailService;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageRepository;
import saleson.common.service.FileStorageService;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.ExcelUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.PdfUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.common.util.UnitExchange;
import saleson.dto.BatchUpdateDTO;
import saleson.dto.MoldMaintenanceDTO;
import saleson.dto.RestDataList;
import saleson.dto.UserLiteDTO;
import saleson.model.AccessCompanyRelation;
import saleson.model.AccessHierarchy;
import saleson.model.Category;
import saleson.model.Cdata;
import saleson.model.Company;
import saleson.model.Counter;
import saleson.model.DashboardSetting;
import saleson.model.FileStorage;
import saleson.model.Location;
import saleson.model.LogUserAlert;
import saleson.model.Mold;
import saleson.model.MoldCorrective;
import saleson.model.MoldCorrectiveCustomFieldValue;
import saleson.model.MoldCustomFieldValue;
import saleson.model.MoldCycleTime;
import saleson.model.MoldCycleTimeCustomFieldValue;
import saleson.model.MoldDataSubmission;
import saleson.model.MoldDetachment;
import saleson.model.MoldDisconnect;
import saleson.model.MoldDownTime;
import saleson.model.MoldDowntimeEvent;
import saleson.model.MoldEfficiency;
import saleson.model.MoldLocation;
import saleson.model.MoldLocationCustomFieldValue;
import saleson.model.MoldMaintenance;
import saleson.model.MoldMaintenanceCustomFieldValue;
import saleson.model.MoldMisconfigure;
import saleson.model.MoldPart;
import saleson.model.MoldRefurbishment;
import saleson.model.Part;
import saleson.model.PartProjectProduced;
import saleson.model.Preset;
import saleson.model.QLocation;
import saleson.model.QMold;
import saleson.model.QMoldCycleTime;
import saleson.model.QMoldDataSubmission;
import saleson.model.QMoldEfficiency;
import saleson.model.QMoldMaintenance;
import saleson.model.QMoldPart;
import saleson.model.QUserAlert;
import saleson.model.QWorkOrderAsset;
import saleson.model.ResinCodeChange;
import saleson.model.Statistics;
import saleson.model.StatisticsPart;
import saleson.model.StatisticsPartReplaced;
import saleson.model.StatisticsWut;
import saleson.model.TabTable;
import saleson.model.TabTableData;
import saleson.model.TerminalDisconnect;
import saleson.model.User;
import saleson.model.UserAlert;
import saleson.model.WorkOrder;
import saleson.model.WorkOrderAsset;
import saleson.model.WorkOrderUser;
import saleson.model.clone.MoldVersion;
import saleson.model.clone.RevisionHistory;
import saleson.model.customField.CustomFieldValue;
import saleson.model.data.AlertCount;
import saleson.model.data.ChartData;
import saleson.model.data.ChartDataOte;
import saleson.model.data.CompanyLiteData;
import saleson.model.data.ContinentStatisticData;
import saleson.model.data.CountLocationMold;
import saleson.model.data.DashboardChartData;
import saleson.model.data.DashboardChartDataOte;
import saleson.model.data.GoogleMapData;
import saleson.model.data.LocationMoldData;
import saleson.model.data.MaintenanceData;
import saleson.model.data.MaintenanceHistoryData;
import saleson.model.data.MaintenanceLog;
import saleson.model.data.MaintenanceTimeData;
import saleson.model.data.MapChartData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MiniGeneralData;
import saleson.model.data.MoldAccumulatedData;
import saleson.model.data.MoldCapacityReportData;
import saleson.model.data.MoldCorrectiveExtraData;
import saleson.model.data.MoldCorrectivePartExtraData;
import saleson.model.data.MoldCycleTimeExtraData;
import saleson.model.data.MoldCycleTimePartExtraData;
import saleson.model.data.MoldDisconnectExtraData;
import saleson.model.data.MoldEfficiencyExtraData;
import saleson.model.data.MoldExtraData;
import saleson.model.data.MoldLiteData;
import saleson.model.data.MoldLocationExtraData;
import saleson.model.data.MoldMaintenanceExtraData;
import saleson.model.data.MoldMaintenancePartExtraData;
import saleson.model.data.MoldMisconfigureExtraData;
import saleson.model.data.MoldPartYearWeekOrMonth;
import saleson.model.data.MoldRefurbishmentExtraData;
import saleson.model.data.MoldReportData;
import saleson.model.data.MoldReportDataPage;
import saleson.model.data.PartProductionData;
import saleson.model.data.PartStatisticsPartIds;
import saleson.model.data.ProductionQuantityData;
import saleson.model.data.QuickStats;
import saleson.model.data.ResinCodeChangeData;
import saleson.model.data.StatisticsData;
import saleson.model.data.TotalToolingData;
import saleson.model.data.acceleration.AccelerationData;
import saleson.model.data.cycleTime.CycleTimeOverviewDetailData;
import saleson.model.data.cycleTime.ToolingCycleTimeData;
import saleson.model.data.dashboard.DashboardTabData;
import saleson.model.data.dashboard.DigitalizationRateData;
import saleson.model.data.dashboard.EndOfLifeCycleChartData;
import saleson.model.data.dashboard.cost.CostData;
import saleson.model.data.dashboard.maintenance.MaintenanceContinentData;
import saleson.model.data.dashboard.maintenance.MaintenanceMapData;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioData;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioDetails;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioTooling;
import saleson.model.data.productivity.ProductivityOverviewData;
import saleson.model.data.productivity.ToolingProductivityData;
import saleson.model.data.wut.BasedWUTData;
import saleson.model.data.wut.MoldCttTempData;
import saleson.model.data.wut.SectionData;
import saleson.model.data.wut.WUTData;
import saleson.model.data.wut.WUTFullData;
import saleson.model.logs.LogDisconnection;
import saleson.model.logs.QLogDisconnection;
import saleson.restdocs.dto.MoldDtoPDF;
import saleson.service.research.WUTService;
import saleson.service.s3storage.DoLogService;
import saleson.service.supplier.SubTierService;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.LogDisconnectionService;
import saleson.service.transfer.LogUserAlertRepository;
import saleson.service.transfer.TransferService;
import saleson.service.util.DateTimeUtils;
import saleson.service.util.NumberUtils;

@Slf4j
@Service
public class MoldService {

	@Autowired
	private MoldRepository moldRepository;

	@Autowired
	private MoldAuthorityRepository moldAuthorityRepository;

	@Autowired
	private MoldLocationRepository moldLocationRepository;

	@Autowired
	private MoldMaintenanceRepository moldMaintenanceRepository;

	@Autowired
	private MoldDisconnectRepository moldDisconnectRepository;

	@Autowired
	MoldCycleTimeRepository moldCycleTimeRepository;

	@Autowired
	MoldEfficiencyRepository moldEfficiencyRepository;

	@Autowired
	MoldDataSubmissionRepository moldDataSubmissionRepository;

	@Autowired
	TerminalDisconnectRepository terminalDisconnectRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogDisconnectionService logDisconnectionService;

	@Autowired
	CounterRepository counterRepository;

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	PartRepository partRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	MachineRepository machineRepository;

	@Autowired
	MoldPartRepository moldPartRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	MoldCorrectiveRepository moldCorrectiveRepository;

	@Autowired
	FileStorageService fileStorageService;

	@Autowired
	MoldMisconfigureRepository moldMisconfigureRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	TerminalRepository terminalRepository;

	@Autowired
	StatisticsRepository statisticsRepository;

	@Autowired
	StatisticsPartRepository statisticsPartRepository;

	@Autowired
	StatisticsPartReplacedRepository statisticsPartReplacedRepository;

	@Autowired
	UserAlertRepository userAlertRepository;

	@Autowired
	MailService mailService;

	@Autowired
	SubTierService subTierService;

	@Autowired
	LogUserAlertRepository logUserAlertRepository;

	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	PdfUtils pdfUtils;

	@Autowired
	ExcelUtils excelUtils;

	@Autowired
	VersioningService versioningService;

	@Autowired
	MoldDownTimeRepository moldDownTimeRepository;

	@Autowired
	MoldVersionRepository moldVersionRepository;

	@Autowired
	ReversionHistoryRepository reversionHistoryRepository;

	@Autowired
	MoldRefurbishmentRepository moldRefurbishmentRepository;

	@Autowired
	MoldEndLifeCycleService moldEndLifeCycleService;

	@Autowired
	WUTService wutService;

	@Value("${customer.server.name}")
	private String serverName;

	@Autowired
	private StatisticsWutService statisticsWutService;
	@Autowired
	private StatisticsWutRepository statisticsWutRepository;
	@Autowired
	CdataRepository cdataRepository;

	@Autowired
	MoldDetachmentRepository moldDetachmentRepository;
	@Autowired
	PartService partService;

	@Autowired
	StatisticsService statisticsService;

	@Autowired
	AccessHierarchyService accessHierarchyService;

	@Autowired
	BroadcastNotificationService broadcastNotificationService;

	@Autowired
	DoLogService doLogService;

	@Autowired
	CustomFieldValueService customFieldValueService;

	@Autowired
	UserService userService;
	@Autowired
	SystemNoteService systemNoteService;

	@Autowired
	DataCompletionRateService dataCompletionRateService;

	@Autowired
	DataAccelerationRepository dataAccelerationRepository;

	@Autowired
	MoldStandardValueRepository moldStandardValueRepository;

	@Autowired
	MoldDowntimeEventRepository moldDowntimeEventRepository;

	@Autowired
	DashboardSettingRepository dashboardSettingRepository;
	@Autowired
	DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

	@Autowired
	PresetService presetService;

	@Lazy
	@Autowired
	TransferService transferService;

	@Autowired
	CounterService counterService;

	@Autowired
	PartProjectProducedRepository partProjectProducedRepository;

	@Autowired
	PresetRepository presetRepository;

	@Autowired
	private MachineDowntimeAlertRepository machineDowntimeAlertRepository;

	@Autowired
	private MoldEndLifeCycleRepository moldEndLifeCycleRepository;

	@Autowired
	private AccessHierarchyRepository accessHierarchyRepository;

	@Autowired
	private ResinCodeChangeRepository resinCodeChangeRepository;

	@Autowired
	private WorkOrderRepository workOrderRepository;
	@Autowired
	private WorkOrderUserRepository workOrderUserRepository;

	@Autowired
	private WorkOrderService workOrderService;

	@Autowired
	private TabbedOverviewGeneralFilterService tabbedOverviewGeneralFilterService;

	@Autowired
	private DataRequestService dataRequestService;

	@Autowired
	private MoldPmPlanRepository moldPmPlanRepository;

	@Autowired
	private WorkOrderAssetRepository workOrderAssetRepository;

	@Autowired
	private ColumnTableConfigService columnTableConfigService;
	@Autowired
	private TabTableRepository tabTableRepository;
	@Autowired
	private TabTableDataRepository tabTableDataRepository;
	@Autowired
	private FileStorageRepository fileStorageRepository;
	@Autowired
	private NotificationService notificationService;

	public Page<Mold> findAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter, String filterCode, ActiveStatus activeStatus) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(QMold.mold));
		return findAll(builder, pageable, true, accumulatedShotFilter, filterCode, activeStatus);
	}

	public Page<Mold> findAllWithTabbedDashboardFilter(Predicate predicate, Pageable pageable, String accumulatedShotFilter, String filterCode, ActiveStatus activeStatus) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		builder.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getMoldFilter());
		return findAll(builder, pageable, true, accumulatedShotFilter, filterCode,activeStatus);
	}

	public List<Long> findAllIds(MoldPayload payload){
		ActiveStatus activeStatus = ActiveStatus.ENABLED;
		if (!ObjectUtils.isEmpty(payload.getFilterCode())) {
			if (ValueUtils.toBoolean(payload.getDeleted(), false)) {
				payload.setMoldStatusList(null);
				activeStatus = ActiveStatus.DISABLED;
			}else if(TabType.DISPOSED.equals(payload.getTabType())){
				payload.setMoldStatusList(null);
				activeStatus = ActiveStatus.DISPOSED;
			}
			payload.setDeleted(false);
		}
		changeTabPayload(payload);
		BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
		if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected()) {
			builder.and(dashboardGeneralFilterUtils.getMoldFilter(QMold.mold));

		} else if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected()) {
			builder.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getMoldFilter());
		}
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)
				.select(Projections.constructor(Long.class, Q.mold.id))
				.from(Q.mold);
		query.where(builder);
		//todo

		List<Long> results = StringUtils.isEmpty(payload.getFilterCode()) ? query.fetchResults().getResults() : moldRepository.findAllIdsByMasterFilter(builder, activeStatus);
		return results;
	}

	public Page<Mold> findAll(Predicate predicate, Pageable pageable) {
		return findAll(predicate, pageable, true, null);
	}

	public Page<Mold> findAll(Predicate predicate, Pageable pageable, boolean loadCustomProperty, String accumulatedShotFilter) {
		return findAll(predicate, pageable, loadCustomProperty, accumulatedShotFilter, null, ActiveStatus.ENABLED);
	}

	public Page<Mold> findAll(Predicate predicate, Pageable pageable, boolean loadCustomProperty, String accumulatedShotFilter, String filterCode, ActiveStatus activeStatus) {
		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);
		String[] properties = { "" };
		Sort.Direction[] directions = { Sort.Direction.DESC };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		Page<Mold> page;
		if (!ObjectUtils.isEmpty(filterCode)) {
			page = moldRepository.findAllByMasterFilter(predicate, activeStatus, pageable);
		} else {
			if (SpecialSortProperty.moldSortPartProperties.contains(properties[0])) {
				List<MoldExtraData> moldExtraDataList = moldRepository.findMoldExtraData(predicate, pageable);
				List<Mold> molds = moldExtraDataList.stream().map(MoldExtraData::getMold).collect(Collectors.toList());
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadUpperTierCompanies(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.customFieldSort)) {
				List<MoldCustomFieldValue> moldCustomFieldValues = moldRepository.findMoldCustomFieldValue(predicate, pageable);
				List<Mold> molds = moldCustomFieldValues.stream().map(MoldCustomFieldValue::getMold).collect(Collectors.toList());
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadUpperTierCompanies(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.moldInactiveSort)) {
				List<Mold> molds = moldRepository.findMoldOrderByInactivePeriod(predicate, pageable);
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadUpperTierCompanies(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
				List<MoldAccumulatedData> moldAccumulatedData = moldRepository.findMoldOrderByAccumulatedShot(predicate, pageable, null);
				List<Mold> molds = moldAccumulatedData.stream().map(MoldAccumulatedData::getMold).collect(Collectors.toList());
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadUpperTierCompanies(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.operatingStatus)) {
				List<Mold> molds = moldRepository.findMoldOrderByOperatingStatus(predicate, pageable);
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadUpperTierCompanies(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.upperTierCompanies)) {
				List<Mold> molds = moldRepository.findMoldOrderByUpperTierCompanies(predicate, pageable);
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadUpperTierCompanies(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.slDepreciation)) {
				List<Mold> molds = moldRepository.findMoldOrderBySlDepreciation(predicate, pageable);
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (properties[0].startsWith(SpecialSortProperty.upDepreciation)) {
				List<Mold> molds = moldRepository.findMoldOrderByUpDepreciation(predicate, pageable);
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
				List<Mold> molds = moldRepository.findMoldOrderByStatus(predicate, pageable);
				if (loadCustomProperty)
					loadValueCustomField(molds);
				loadInactivePeriod(molds);
				loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
				loadUpDepreciation(molds);
				loadCustomFieldForPart(molds);
				loadWorkOrderDetail(molds);
				loadPM(molds);
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			} else if (Arrays.asList("lastShotCheckpoint", "dueDate", "utilNextPm").contains(properties[0])) {
				List<MoldMaintenanceExtraData> moldMaintenanceExtraDataList = moldRepository.findMoldMaintenanceExtraDataForMoldTable(predicate, pageable);
				List<Mold> molds = moldMaintenanceExtraDataList.stream().map(data -> {
					data.getMold().setMoldMaintenance(new MoldMaintenanceDTO(data.getMoldMaintenance()));
					return data.getMold();
				}).collect(Collectors.toList());
				loadEngineersInfo(molds);
				loadPlantEngineersInfo(molds);
				loadUntilNextPm(molds);
				return new PageImpl<>(molds, pageable, moldRepository.count(predicate));
			}
			
		if("tco".equalsIgnoreCase(properties[0])){
			List<Mold> molds = moldRepository.findMoldOrderByTco(predicate, pageable);
			if(loadCustomProperty) loadValueCustomField(molds);
			loadInactivePeriod(molds);
			loadAccumulatedShot(molds, accumulatedShotFilter, dataFilterEnabled);
			loadUpDepreciation(molds);
			loadCustomFieldForPart(molds);
			loadUpperTierCompanies(molds);
			loadWorkOrderDetail(molds);
			loadPM(molds);
			loadUntilNextPm(molds);
			return new PageImpl<>(molds, pageable, moldRepository.count(predicate));

		}

			page = moldRepository.findAll(predicate, pageable);
		}

		if (loadCustomProperty) {
			loadValueCustomField(page.getContent());
		}
		loadInactivePeriod(page.getContent());
		loadAccumulatedShot(page.getContent(), accumulatedShotFilter, dataFilterEnabled);
		loadUpDepreciation(page.getContent());
		loadCustomFieldForPart(page.getContent());
		loadUpperTierCompanies(page.getContent());
		loadWorkOrderDetail(page.getContent());
		loadPM(page.getContent());
		loadEngineersInfo(page.getContent());
		loadPlantEngineersInfo(page.getContent());
		loadRemainingPartsCount(page.getContent());
//		loadUntilNextPm(page.getContent());
		return page;
	}

	public Page<MoldLiteData> getAllMoldLite(Predicate predicate, Pageable pageable){
		Page<Mold> pageContent = moldRepository.findAll(predicate,pageable);
		List<MoldLiteData> moldLiteDataList =  pageContent.getContent().stream()
						.map(m-> new MoldLiteData(m.getId(),m.getEquipmentCode(),m.getLocationId(),m.getLocationCode(), m.getLocationName(), m.getLastShot()))
						.collect(Collectors.toList());
		loadPmStrategy(moldLiteDataList);
		return new PageImpl<>(moldLiteDataList,pageable,pageContent.getTotalElements());
	}
	private void loadPmStrategy(List<MoldLiteData> molds){
		molds.forEach(mold -> {
			MoldPmPlan pmPlan = BeanUtils.get(MoldPmPlanRepository.class).findByMoldId(mold.getId()).orElse(null);
			if (pmPlan == null) {
				mold.setPmStrategy(PM_STRATEGY.SHOT_BASED);
			} else {
				mold.setPmStrategy(pmPlan.getPmStrategy());
			}
		});
	}
	private void loadCustomFieldForPart(List<Mold> molds) {
		molds.forEach(mold -> {
			Map<Long, Map<Long, List<CustomFieldValue>>> mapValueCustomField = customFieldValueService.mapValueCustomField(ObjectType.PART,
					mold.getParts().stream().map(PartData::getPartId).collect(Collectors.toList()));
			mold.setCustomFieldValueMapForPart(mapValueCustomField);
		});
	}

	private void loadUpperTierCompanies(List<Mold> molds) {
		molds.forEach(mold -> {
			Optional<AccessHierarchy> optional = accessHierarchyRepository.findFirstByCompanyId(mold.getCompanyId());
			if (optional.isPresent()) {
				List<Long> parentIds = optional.get().getAccessCompanyParentRelations().stream().map(AccessCompanyRelation::getCompanyParentId).collect(Collectors.toList());
				mold.setUpperTierCompanies(companyRepository.findByIdIn(parentIds).stream().map(CompanyLiteData::new).collect(Collectors.toList()));
			}
		});
	}

	public void loadAccumulatedShot(List<Mold> molds, String year) {
		loadAccumulatedShot(false, molds, year);
	}

	private void loadAccumulatedShot(boolean isReSumAll, List<Mold> molds, String year) {
		List<Long> ids = molds.stream().map(Mold::getId).collect(Collectors.toList());
		List<MoldAccumulatedData> moldAccumulatedData = moldRepository.findMoldAccumulatedShotByStatistic(isReSumAll, year, ids);
		molds.forEach(mold -> {
			moldAccumulatedData.forEach(mData -> {
				if (mData.getMold().getId().equals(mold.getId())) {
					mold.setAccumulatedShot(mData.getAccumulatedShots());
				}
			});
		});
	}

	private void loadAccumulatedShot(List<Mold> molds, String year, boolean dataFilterEnabled) {
		List<Long> ids = molds.stream().map(Mold::getId).collect(Collectors.toList());
		List<MoldAccumulatedData> moldAccumulatedData = moldRepository.findMoldAccumulatedShot(year, ids);
		molds.forEach(mold -> {
			moldAccumulatedData.forEach(mData -> {
				if (mData.getMold().getId().equals(mold.getId())) {
					mold.setAccumulatedShot(mData.getAccumulatedShots());
				}
			});
		});
	}

	private void loadUpDepreciation(List<Mold> molds) {
		molds.forEach(mold -> {
			String latestDepreciationPoint = DateUtils2.format(mold.getUpLatestDepreciationPoint(), DatePattern.yyyyMMddHHmmss, Zone.SYS);
			MoldAccumulatedData data = moldRepository.findMoldAccumulatedShotByLstLessThan(latestDepreciationPoint, mold.getId());
			if (mold.getLastShot() != null && mold.getLastShot() >= mold.getDesignedShot()) {
				mold.setUpDepreciation(mold.getLastShot() == null ? 0D : mold.getLastShot().doubleValue());
			} else {
				Double upDepreciation = data == null ? 0D : (data.getAccumulatedShots() == null ? 0D : data.getAccumulatedShots().doubleValue());
				mold.setUpDepreciation(mold.getLastShot() == null ? 0D : (upDepreciation > mold.getLastShot() ? mold.getLastShot() : upDepreciation));
			}

			if (mold.getLastShot() != null && mold.getLastShot() >= mold.getDesignedShot()) {
				mold.setUpCurrentBookValue(mold.getSalvageValue());
			} else {
				double assetCost = mold.getCost() != null ? mold.getCost() : 0;
				mold.setUpCurrentBookValue(ValueUtils.max(NumberUtils.roundOffNumber(assetCost - (mold.getUpDepreciationPerShotRaw() * mold.getUpDepreciation())), 0D));
			}
		});
	}

	public void filterInactive(MoldPayload payload) {
		Pair<Integer, Pair<Integer, Integer>> inactiveConfig = getInactiveConfig();
		Integer firstLevelMonthNumber = inactiveConfig.getFirst();
		Integer secondLevelMonthNumber = inactiveConfig.getSecond().getFirst();
		Integer thirdLevelMonthNumber = inactiveConfig.getSecond().getSecond();

		ZonedDateTime now = ZonedDateTime.now();
		Instant firstLevelDate = now.minusMonths(firstLevelMonthNumber).toInstant();
		Instant secondLevelDate = now.minusMonths(secondLevelMonthNumber).toInstant();
		Instant thirdLevelDate = now.minusMonths(thirdLevelMonthNumber).toInstant();

		if (payload.getInactiveLevel().equals(DashboardSettingLevel.FIRST_LEVEL)) {
			payload.setInactiveFrom(secondLevelDate);
			payload.setInactiveTo(firstLevelDate);
		} else if (payload.getInactiveLevel().equals(DashboardSettingLevel.SECOND_LEVEL)) {
			payload.setInactiveFrom(thirdLevelDate);
			payload.setInactiveTo(secondLevelDate);
		} else if (payload.getInactiveLevel().equals(DashboardSettingLevel.THIRD_LEVEL)) {
			payload.setInactiveTo(thirdLevelDate);
		} else if (payload.getInactiveLevel().equals(DashboardSettingLevel.ALL_LEVEL)) {
			payload.setInactiveTo(firstLevelDate);
		}
	}
	public void filterInactiveNew(MoldPayload payload) {
		InactiveConfig config = MoldUtils.getInactiveConfig();
		Instant level1 = minusMonths(config.getLevel1());
		payload.setLevel1(level1);
		Instant level2 = minusMonths(config.getLevel2());
		payload.setLevel2(level2);
		Instant level3 = minusMonths(config.getLevel3());
		payload.setLevel3(level3);

	}
	private Instant minusMonths(Long value) {
		if (value == null) {
			return null;
		}
		return DateUtils2.plusMonths(DateUtils2.getInstant(), -value.intValue(), Zone.GMT);
	}

	public Pair<Integer, Pair<Integer, Integer>> getInactiveConfig() {
		List<DashboardSetting> dashboardSettingList = dashboardSettingRepository.findAllByUserId(SecurityUtils.getUserId());
		Optional<DashboardSetting> firstLevelSettingOptional = dashboardSettingList.stream()
				.filter(dashboardSetting -> dashboardSetting.getLevel() == DashboardSettingLevel.FIRST_LEVEL).findFirst();

		Optional<DashboardSetting> secondLevelSettingOptional = dashboardSettingList.stream()
				.filter(dashboardSetting -> dashboardSetting.getLevel() == DashboardSettingLevel.SECOND_LEVEL).findFirst();

		Optional<DashboardSetting> thirdLevelSettingOptional = dashboardSettingList.stream()
				.filter(dashboardSetting -> dashboardSetting.getLevel() == DashboardSettingLevel.THIRD_LEVEL).findFirst();

		Integer firstLevelMonthNumber = firstLevelSettingOptional.isPresent() ? firstLevelSettingOptional.get().getMonthNumber() : Const.DASHBOARD_SETTING_DEFAULT.FIRST_LEVEL;

		Integer secondLevelMonthNumber = secondLevelSettingOptional.isPresent() ? secondLevelSettingOptional.get().getMonthNumber() : Const.DASHBOARD_SETTING_DEFAULT.SECOND_LEVEL;

		Integer thirdLevelMonthNumber = thirdLevelSettingOptional.isPresent() ? thirdLevelSettingOptional.get().getMonthNumber() : Const.DASHBOARD_SETTING_DEFAULT.THIRD_LEVEL;

		return Pair.of(firstLevelMonthNumber, Pair.of(secondLevelMonthNumber, thirdLevelMonthNumber));
	}

	private void loadInactivePeriod(List<Mold> molds) {
		molds.stream().forEach(Mold::setInactivePeriod);
	}

	private void loadValueCustomField(List<Mold> molds) {
		//get data for custom field
		Map<Long, Map<Long, List<CustomFieldValue>>> mapValueCustomField = customFieldValueService.mapValueCustomField(ObjectType.TOOLING,
				molds.stream().map(m -> m.getId()).collect(Collectors.toList()));
		molds.stream().forEach(mold -> {
			if (mapValueCustomField.containsKey(mold.getId())) {
				mold.setCustomFieldValueMap(mapValueCustomField.get(mold.getId()));
			}
		});
	}

	public Mold findById(Long id) {
		if (id == null)
			return null;

		Optional<Mold> moldOptional = moldRepository.findById(id);
		if (moldOptional.isPresent()) {
			Mold mold = moldOptional.get();
			loadEngineersInfo(Collections.singletonList(mold));
			loadPlantEngineersInfo(Collections.singletonList(mold));
			loadWorkOrderDetail(mold);
			loadRemainingPartsCount(mold);
			return mold;
		} else {
			return null;
		}
	}


	public Mold findByIdWithoutAdditionalInfo(Long id) {
		if (id == null)
			return null;

		Optional<Mold> category = moldRepository.findById(id);
		if (category.isPresent()) {
			return category.get();
		} else {
			return null;
		}
	}

	public List<Mold> findByIdIn(List<Long> ids) {
		return moldRepository.findByIdInOrderByIdDesc(ids);
	}

	@Transactional
	public Mold save(Mold mold, MoldPayload payload) {
		/* M01
			if (mold.getPartId() != null) {
			Part part = new Part(mold.getPartId());
			mold.setPart(part);
		}*/
		if (payload == null) {
			moldRepository.save(mold);
			return mold;
		}

//		MoldLocation moldLocation = new MoldLocation();
		// 신규 등록 시 로케이션 정보를 등록한다.
		if (payload.getLocationId() != null || payload.getLocationCode() != null) {
			Location curLocation = mold.getLocation();
			Location location = null;
			if (payload.getLocationId() != null) {
				location = locationRepository.findById(payload.getLocationId()).orElse(null);
			} else {
				List<Location> locations = locationRepository.findByLocationCodeAndEnabledIsTrue(StringUtils.trimWhitespace(payload.getLocationCode()));
				if (locations != null && locations.size() > 0) {
					location = locations.get(0);
				}
			}

			if (location != null && (curLocation == null || !curLocation.equals(location))) {
//				if (mold.getId() != null) {
//					List<MoldLocation> lastMoldLocations = moldLocationRepository
//							.findByMoldIdAndMoldLocationStatusAndLatest(mold.getId(), MoldLocationStatus.CHANGED, true);
//					if (lastMoldLocations != null && lastMoldLocations.size() > 0) {
//						lastMoldLocations.forEach(x -> x.setLatest(false));
//						moldLocationRepository.saveAll(lastMoldLocations);
//					}
//					moldLocation.setMold(mold);
//				}

				mold.setLocation(location);
				mold.setLocationChanged(false);
				mold.setCompanyId(location.getCompanyId());

//				moldLocation.setLatest(true);
//				moldLocation.setLocation(location);
//				moldLocation.setMoldLocationStatus(MoldLocationStatus.CHANGED);
//				moldLocation.setNotificationAt(Instant.now());
			}

			// Tool Maker 등록
			if (payload.getToolMakerCompanyId() != null || payload.getToolMakerCompanyCode() != null) {
				Optional<Company> optionalToolMaker = payload.getToolMakerCompanyId() != null ? companyRepository.findById(payload.getToolMakerCompanyId())
						: companyRepository.findByCompanyCode(StringUtils.trimWhitespace(payload.getToolMakerCompanyCode()));

				if (optionalToolMaker.isPresent()) {
					mold.setToolMaker(optionalToolMaker.get());
				}
			}

			// Supplier 등록
			if (payload.getSupplierCompanyId() != null || payload.getSupplierCompanyCode() != null) {
				Optional<Company> optionalSupplier = payload.getSupplierCompanyId() != null ? companyRepository.findById(payload.getSupplierCompanyId())
						: companyRepository.findByCompanyCode(StringUtils.trimWhitespace(payload.getSupplierCompanyCode()));

				if (optionalSupplier.isPresent()) {
					mold.setSupplier(optionalSupplier.get());
				}
			}
		}

		if (mold.getMaintenanceCount() == null) {
			mold.setMaintenanceCount(0);
		}

		if (mold.getEquipmentStatus() == null)
			mold.setEquipmentStatus(EquipmentStatus.AVAILABLE);

		if (mold.getEquipmentStatus() != EquipmentStatus.INSTALLED && mold.getEquipmentStatus() != EquipmentStatus.DETACHED) {
			if (mold.getCounter() != null) {
				Counter counter = mold.getCounter();
				counter.setEquipmentStatus(EquipmentStatus.AVAILABLE);
				counter.setPresetStatus(null);

				counterRepository.save(counter);
			}

			mold.setCounter(null);

		}

		List<String> engineerEmails = payload.getEngineerEmails();
		if (engineerEmails != null && engineerEmails.size() > 0) {
			List<User> engineers = userRepository.findByEmailInAndDeletedIsFalse(engineerEmails);
//			mold.setEngineers(new HashSet<>(engineers));
			mold.setEngineersInCharge(engineers);
		}

		boolean newObj = mold.getId() == null;

		boolean isDyson = serverName.equalsIgnoreCase("dyson") ? true : false;

		Mold moldNew = moldRepository.save(mold);

		if (isDyson) {
//			if (mold.getDataSubmission() == null || !mold.getDataSubmission().equals(NotificationStatus.APPROVED)) {
				mold.setDataSubmission(NotificationStatus.PENDING);
				MoldDataSubmission lastPending = moldDataSubmissionRepository.findByMoldIdAndNotificationStatusAndLatest(mold.getId(), NotificationStatus.PENDING, true)
						.orElse(null);
				if (lastPending == null) {
					MoldDataSubmission moldDataSubmission = new MoldDataSubmission();
					moldDataSubmission.setMold(mold);
					moldDataSubmission.setNotificationStatus(NotificationStatus.PENDING);
					moldDataSubmission.setNotificationAt(Instant.now());
					moldDataSubmission.setLatest(true);
					lastPending = moldDataSubmissionRepository.save(moldDataSubmission);
					List<User> engineers = mold.getEngineersInCharge();
					if (engineers != null && engineers.size() > 0) {
						List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(new ArrayList<>(engineers), AlertType.DATA_SUBMISSION);
						Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
						List<LogUserAlert> logUserAlerts = buildLogUserAlert(userAlertMap, null, null, null, null, null, null, null, Arrays.asList(moldDataSubmission), null, true);
						logUserAlertRepository.saveAll(logUserAlerts);

						List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail())
								.collect(Collectors.toList());
						Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
						mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, null, null, null, null, null, Arrays.asList(moldDataSubmission),
								null, null);
					}
				} else {
					lastPending.setUpdatedAt(Instant.now());
					List<User> engineers = mold.getEngineersInCharge();
					if (engineers != null && engineers.size() > 0) {
						List<Long> engineerIds = engineers.stream().map(x -> x.getId()).collect(Collectors.toList());
						List<LogUserAlert> existLogUserAlertList = logUserAlertRepository.findByAlertTypeAndAlertId(AlertType.DATA_SUBMISSION, lastPending.getId());

						// Removed engineers from list will not see alert anymore
						List<LogUserAlert> toRemove = existLogUserAlertList.stream().filter(x -> !engineerIds.contains(x.getUserId())).collect(Collectors.toList());
						logUserAlertRepository.deleteAll(toRemove);

						List<Long> newEngineerIds = engineerIds;
						if (existLogUserAlertList != null && existLogUserAlertList.size() > 0) {
							List<Long> sentAlertEngineerIds = existLogUserAlertList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
							newEngineerIds = engineerIds.stream().filter(x -> !sentAlertEngineerIds.contains(x)).collect(Collectors.toList());
						}

						if (newEngineerIds.size() > 0) {
							List<User> newEngineers = userRepository.findByIdInAndDeletedIsFalse(newEngineerIds);
							List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(new ArrayList<>(newEngineers), AlertType.DATA_SUBMISSION);
							Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
							List<LogUserAlert> logUserAlerts = buildLogUserAlert(userAlertMap, null, null, null, null, null, null, null, Arrays.asList(lastPending), null, true);
							logUserAlertRepository.saveAll(logUserAlerts);

							List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail() == true)
									.collect(Collectors.toList());
							Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
							mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, null, null, null, null, null, Arrays.asList(lastPending),
									null, null);
						}
					}
				}
//			}
		}

//		if(moldLocation.getLocation() != null){
//			if(moldLocation.getMold() == null) {
//				moldLocation.setMold(mold);
//			}
//			moldLocationRepository.save(moldLocation);
//		}
		moldAuthorityRepository.deleteAllByMoldId(null);

		// MoldPart 등록
		if (payload != null && payload.getMoldParts() != null) {

			//
			List<MoldPart> moldParts = moldPartRepository.findAllByMoldIdOrderById(mold.getId());

			// 이미 등록된 part 정보 중 새로게 등록할 part 정보에 포함되지 않은 part는 삭제.
			for (int i = moldParts.size() - 1; i >= 0; i--) {
				MoldPart moldPart = moldParts.get(i);

				boolean hasPart = false;
				if (payload.getPartIdsFromMoldParts() != null && payload.getPartIdsFromMoldParts().size() > 0) {
					hasPart = payload.getPartIdsFromMoldParts().stream().anyMatch(x -> moldPart.getPartId().equals(x));
				} else if (payload.getPartCodesFromMoldParts() != null && payload.getPartCodesFromMoldParts().size() > 0) {
					hasPart = payload.getPartCodesFromMoldParts().stream().anyMatch(x -> moldPart.getPart().getPartCode().trim().equals(x.trim()));
				}
//				for (Long partId : payload.getPartIdsFromMoldParts()) {
//					if (moldPart.getPartId().equals(partId)) {
//						hasPart = true;
//						break;
//					}
//				}

				if (!hasPart) {
					moldPartRepository.deleteById(moldPart.getId());
					moldParts.remove(i);
				}
				//moldParts.remove(i);
			}

			AtomicInteger index = new AtomicInteger();

			List<Long> partIds = payload.getPartIdsFromMoldParts();

			List<Part> parts = partRepository.findAllById(partIds);

			if (parts == null || parts.size() == 0) {
				List<String> partCodes = payload.getPartCodesFromMoldParts();
				parts = partRepository.findByPartCodeIn(partCodes);
			}

			parts.stream().forEach(part -> {
				// 일치하는 cavity조회
				/*
										int cavity = Arrays.stream(payload.getMoldParts())
												.filter(p -> p.getPartId() != null ? p.getPartId().equals(part.getId()) : p.getPartCode().equals(part.getPartCode()))
												.map(m -> m.getCavity())
												.findFirst()
												.orElse(1);
				*/

				MoldPart moldPart = Arrays.stream(payload.getMoldParts())
						.filter(p -> p.getPartId() != null ? p.getPartId().equals(part.getId()) : p.getPartCode().trim().equalsIgnoreCase(part.getPartCode().trim())).findFirst()
						.orElse(null);
				int cavity = moldPart != null && moldPart.getCavity() != null ? moldPart.getCavity() : 1;
				Integer totalCavities = moldPart != null ? moldPart.getTotalCavities() : null;
				boolean hasPart = false;
				for (MoldPart mp : moldParts) {
					if (mp.getMold().getId().equals(mold.getId()) && mp.getPart().getId().equals(part.getId())) {
						mp.setCavity(cavity);
						mp.setTotalCavities(totalCavities);
						hasPart = true;
						break;
					}
				}

				/*MoldPart moldPart = moldParts.stream()
						.filter(mp -> mp.getMoldId().equals(mold.getId()) && mp.getPartId().equals(part.getId()))
						.findFirst().orElse(new MoldPart(mold, part));
				moldPart.setCavity(cavity);*/

				if (!hasPart) {
					MoldPart newMP = new MoldPart(mold, part, cavity, totalCavities);
					newMP.setSwitchedTime(Instant.now());
					moldParts.add(newMP);
				}

			});

			/*// 기존 등록 정보 삭제.
			moldPartRepository.deleteByMoldId(mold.getId());

			Iterable<Long> partIds = payload.getPartIdsFromMoldParts();

			AtomicInteger index = new AtomicInteger();
			List<MoldPart> moldParts = new ArrayList<>();
			partRepository.findAllById(partIds).stream()
			.forEach(part -> {
				// 일치하는 cavity조회
				int cavity = Arrays.stream(payload.getMoldParts())
						.filter(p -> p.getPartId().equals(part.getId()))
						.map(m -> m.getCavity())
						.findFirst()
						.orElse(1);

				moldParts.add(new MoldPart(mold, part, cavity));
			});*/

			moldPartRepository.saveAll(moldParts);
			mold.setMoldParts(new ArrayList<>(moldParts));
		}

		if (newObj) {
			versioningService.writeHistory(moldNew);
		} else {
			dataRequestService.completeDataCompletion(mold.getId(), ObjectType.TOOLING);
		}


		// Calculate daily max capacity
		setMoldDailyMaxCapacity(mold);

		if (mold.getProductionDays() == null)
			mold.setProductionDays(payload.getProductionDays() != null ? payload.getProductionDays() : "7");

		// Calculate max capacity per week
		if (payload.getMaxCapacityPerWeek() == null) {
			mold.setMaxCapacityPerWeek(mold.getDailyMaxCapacity() * Integer.valueOf(mold.getProductionDays()));
		}

		// Set passed days
		if (mold.getCreatedAt() == null)
			mold.setPassedDays(0);
		else {
			long diffSeconds = Instant.now().getEpochSecond() - mold.getCreatedAt().getEpochSecond();
			long passedDays = diffSeconds / (3600 * 24);
			mold.setPassedDays((int) passedDays);
		}

		// Calculate shot weight
		if (payload.getShotSize() == null) {
			try {

				Double shotWeight = mold.getMoldParts().stream()
						.map(x -> UnitExchange.weightUnitExchange(
								(x.getPart().getWeight() != null && !x.getPart().getWeight().isEmpty() ? Double.valueOf(x.getPart().getWeight()) : 0.0),
								x.getPart().getWeightUnit() != null ? x.getPart().getWeightUnit() : WeightUnit.GRAMS, WeightUnit.GRAMS) * x.getCavity())
						.reduce(0.0, Double::sum);
				Integer numberOfCavities = mold.getMoldParts().stream().map(x -> x.getCavity()).reduce(0, Integer::sum);
				shotWeight += (mold.getWeightRunner() != null ? mold.getWeightRunner() : 0.0) * numberOfCavities;
				mold.setShotSize(shotWeight);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Integer originLastShot = mold.getLastShot();
		if (payload.getAccumulatedShots() != null) {
			mold.setLastShot(payload.getAccumulatedShots());
		}
		moldRepository.save(mold);

		if (payload.getCounterId() != null || !StringUtils.isEmpty(payload.getCounterCode())) {
			Optional<Counter> counterOptional = payload.getCounterId() != null ? counterRepository.findById(payload.getCounterId())
					: counterRepository.findByEquipmentCode(payload.getCounterCode());
			if (counterOptional.isPresent()) {
				Counter oldCounter = mold.getCounter();
				Counter counter = counterOptional.get();
				if (oldCounter == null || !mold.getCounter().getId().equals(counter.getId())) {
					if (EquipmentStatus.AVAILABLE != counter.getEquipmentStatus()) {
						counter.setEquipmentStatus(EquipmentStatus.AVAILABLE);
						counterService.save(counter);
					}
					if (oldCounter != null) {
						oldCounter.setEquipmentStatus(EquipmentStatus.AVAILABLE);
						counterService.save(oldCounter);
					}
					counter.setMold(mold);
					counter.setEquipmentStatus(EquipmentStatus.INSTALLED);
					counter.setPresetCount(mold.getLastShot());
					counterService.save(counter);

					// 2023.08.31 Mickey Park
					// If the counter of the mold is changed, reset to the shot count of the mold.

//					Preset preset = new Preset(counter.getEquipmentCode());
//					preset.setPreset(mold.getLastShot().toString());
//					Integer forecastedMaxShots = 0;
//					if (mold.getLastShot() > 0) {
//						forecastedMaxShots = (int) 1e6 * (int) ((mold.getLastShot() / 1e6) + 1);
//					}
//					preset.setForecastedMaxShots(forecastedMaxShots);
//					Preset newPreset = BeanUtils.get(PresetService.class).save(preset);
//					BeanUtils.get(RstStpService.class).applyReset(newPreset);

					// Delete
//					if ( counterService.checkChangeShot(counter, counter.getMold(), counter.getPresetCount())) {
//
//						if (counter.getPresetStatus() == PresetStatus.APPLIED || (counter.getPresetCount() == null && counter.getPresetStatus() == PresetStatus.READY)) {
//							presetService.autoCreatePreset(counter);
//							transferService.makePresetMisconfigured(mold, counter);
//						} else if (counter.getPresetCount() != null && counter.getPresetStatus() == PresetStatus.READY) {
//							Preset preset = new Preset(counter.getEquipmentCode());
//							preset.setPreset(mold.getLastShot().toString());
//							Integer forecastedMaxShots = 0;
//							if (mold.getLastShot() > 0) {
//								forecastedMaxShots = (int) 1e6 * (int) ((mold.getLastShot() / 1e6) + 1);
//							}
//							preset.setForecastedMaxShots(forecastedMaxShots);
//							presetService.save(preset);
//							transferService.makePresetMisconfigured(mold, counter);
//						}
//
//					}

				}
			}
		}
		/*
		if (mold.getPlantEngineersInCharge()!=null&&CollectionUtils.isNotEmpty(mold.getPlantEngineersInCharge())){
			QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
			JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldNew.getId()));
			List<WorkOrderAsset> workOrderAssets = query.fetch();
			List<WorkOrderUser> workOrderUserAddList = new ArrayList<>();
			List<WorkOrderUser> workOrderUserNoticeList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(workOrderAssets)){
				 workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(wo-> !Arrays.asList(WorkOrderStatus.COMPLETED,WorkOrderStatus.DECLINED,WorkOrderStatus.CANCELLED).contains(wo.getStatus())).forEach(
						wo ->  {

							mold.getPlantEngineersInCharge().forEach(
									u -> {
										WorkOrderUser workOrderUser = new WorkOrderUser();
										workOrderUser.setUser(u);
										workOrderUser.setUserId(u.getId());
										workOrderUser.setWorkOrderId(wo.getId());
										workOrderUser.setWorkOrder(wo);
										workOrderUser.setParticipantType(WorkOrderParticipantType.ASSIGNEE);
										if(!workOrderUserRepository.existsByWorkOrderAndUserAndParticipantType(wo, u, WorkOrderParticipantType.ASSIGNEE)){
											workOrderUserAddList.add(workOrderUser);
											workOrderUserNoticeList.add(workOrderUser);
										}
									}
							);
						}
				);
				workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(wo-> Arrays.asList(WorkOrderStatus.COMPLETED,WorkOrderStatus.DECLINED,WorkOrderStatus.CANCELLED).contains(wo.getStatus())).forEach(
						wo ->  {

							mold.getPlantEngineersInCharge().forEach(
									u -> {
										if(workOrderUserRepository.existsByWorkOrderAndUserAndParticipantType(wo, u, WorkOrderParticipantType.ASSIGNEE)) {
											WorkOrderUser workOrderUser = new WorkOrderUser();
											workOrderUser.setUser(u);
											workOrderUser.setUserId(u.getId());
											workOrderUser.setWorkOrderId(wo.getId());
											workOrderUser.setWorkOrder(wo);
											workOrderUser.setParticipantType(WorkOrderParticipantType.ASSIGNEE);
											workOrderUserAddList.add(workOrderUser) ;
										}
									}
							);
							workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(wo.getId(),WorkOrderParticipantType.ASSIGNEE);
						}
				);

			}
			if (CollectionUtils.isNotEmpty(workOrderUserAddList)) {
				workOrderUserRepository.saveAll(workOrderUserAddList);
				workOrderUserNoticeList.forEach(wu -> BeanUtils.get(NotificationService.class).createWorkOrderNotification(wu.getWorkOrder(), Collections.singletonList(wu.getUserId())));
			}
		}
		else{
			QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
			JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldNew.getId()));
			List<WorkOrderAsset> workOrderAssets = query.fetch();
			List<WorkOrder> workOrders = workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(wo-> Arrays.asList(WorkOrderStatus.COMPLETED,WorkOrderStatus.DECLINED,WorkOrderStatus.CANCELLED).contains(wo.getStatus())).collect(Collectors.toList());
			workOrders.forEach(workOrder -> workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(workOrder.getId(),WorkOrderParticipantType.ASSIGNEE));

		}
		if(mold.getEngineersInCharge()!=null&&CollectionUtils.isNotEmpty(mold.getEngineersInCharge())){
			QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
			JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldNew.getId()));
			List<WorkOrderAsset> workOrderAssets = query.fetch();
			List<WorkOrderUser> workOrderUserAddList = new ArrayList<>();
			workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(workOrder -> WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(workOrder.getOrderType())).forEach(
					workOrder -> {
						mold.getEngineersInCharge().forEach(
								u -> {
										WorkOrderUser workOrderUser = new WorkOrderUser();
										workOrderUser.setUser(u);
										workOrderUser.setUserId(u.getId());
										workOrderUser.setWorkOrderId(workOrder.getId());
										workOrderUser.setWorkOrder(workOrder);
										workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
										workOrderUserAddList.add(workOrderUser) ;
								}
						);
						if (workOrder.getCreatedBy()!=null) {
							User user = userRepository.getOne(workOrder.getCreatedBy());
							WorkOrderUser workOrderUser = new WorkOrderUser();
							workOrderUser.setUserId(user.getId());
							workOrderUser.setUser(user);
							workOrderUser.setWorkOrderId(workOrder.getId());
							workOrderUser.setWorkOrder(workOrder);
							workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
							workOrderUserAddList.add(workOrderUser);
						}
						workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(workOrder.getId(),WorkOrderParticipantType.CREATOR);
					}

			);
			if (CollectionUtils.isNotEmpty(workOrderUserAddList)) {
				workOrderUserRepository.saveAll(workOrderUserAddList);
			}
		} else{
			QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
			JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldNew.getId()));
			List<WorkOrderAsset> workOrderAssets = query.fetch();
			workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(workOrder -> WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(workOrder.getOrderType()))
					.forEach(workOrder -> workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(workOrder.getId(),WorkOrderParticipantType.CREATOR));

		}
*/
		// 첨부파일 등록
		if (payload.getFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_MAINTENANCE_DOCUMENT, mold.getId(), payload.getFiles()));
		}
		if (payload.getSecondFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_INSTRUCTION_VIDEO, mold.getId(), payload.getSecondFiles()));
		}
		if (payload.getThirdFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_CONDITION, mold.getId(), payload.getThirdFiles()));
		}
		if (payload.getForthFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_PO_DOCUMENT, mold.getId(), payload.getForthFiles()));
		}

		if (payload.getDataRequestId() != null) {
			dataRequestService.saveDataRequestObject(payload.getDataRequestId(), mold.getId(), ObjectType.TOOLING);
		}

		createWorkOrderWhenAddPlant(moldNew.getId());
		//update data completion rate
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.TOOLING, mold.getLocation().getCompanyId());
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, mold.getLocation().getCompanyId());
		//update data completion order
		dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.TOOLING, mold.getId());
 		saveMoldPmPlan(payload, mold);
		scheduleMaintainMoldByTime(mold.getId(), payload);
		return mold;
	}

	public void createWorkOrderWhenAddPlant(Long moldId) {
		final Authentication auth = SecurityUtils.getAuthentication();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			log.info("Start createWorkOrderWhenAddPlant");
			SecurityUtils.setAuthentication(auth);

			Mold mold = moldRepository.findById(moldId).orElseThrow();
			if (mold.getPlantEngineersInCharge() != null && CollectionUtils.isNotEmpty(mold.getPlantEngineersInCharge())) {
				QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
				JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldId));
				List<WorkOrderAsset> workOrderAssets = query.fetch();
				List<WorkOrderUser> workOrderUserAddList = new ArrayList<>();
				List<WorkOrderUser> workOrderUserNoticeList = new ArrayList<>();
				if (CollectionUtils.isNotEmpty(workOrderAssets)) {
					workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder)
							.filter(wo -> !Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.DECLINED, WorkOrderStatus.CANCELLED).contains(wo.getStatus())).forEach(wo -> {

								mold.getPlantEngineersInCharge().forEach(u -> {
									WorkOrderUser workOrderUser = new WorkOrderUser();
									workOrderUser.setUser(u);
									workOrderUser.setUserId(u.getId());
									workOrderUser.setWorkOrderId(wo.getId());
									workOrderUser.setWorkOrder(wo);
									workOrderUser.setParticipantType(WorkOrderParticipantType.ASSIGNEE);
									if (!workOrderUserRepository.existsByWorkOrderAndUserAndParticipantType(wo, u, WorkOrderParticipantType.ASSIGNEE)) {
										workOrderUserAddList.add(workOrderUser);
										workOrderUserNoticeList.add(workOrderUser);
									}
								});
							});
					workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder)
							.filter(wo -> Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.DECLINED, WorkOrderStatus.CANCELLED).contains(wo.getStatus())).forEach(wo -> {

								mold.getPlantEngineersInCharge().forEach(u -> {
									if (workOrderUserRepository.existsByWorkOrderAndUserAndParticipantType(wo, u, WorkOrderParticipantType.ASSIGNEE)) {
										WorkOrderUser workOrderUser = new WorkOrderUser();
										workOrderUser.setUser(u);
										workOrderUser.setUserId(u.getId());
										workOrderUser.setWorkOrderId(wo.getId());
										workOrderUser.setWorkOrder(wo);
										workOrderUser.setParticipantType(WorkOrderParticipantType.ASSIGNEE);
										workOrderUserAddList.add(workOrderUser);
									}
								});
								workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(wo.getId(), WorkOrderParticipantType.ASSIGNEE);
							});

				}
				if (CollectionUtils.isNotEmpty(workOrderUserAddList)) {
					workOrderUserRepository.saveAll(workOrderUserAddList);
					workOrderUserNoticeList.forEach(
							wu -> BeanUtils.get(NotificationService.class).createWorkOrderNotification(wu.getWorkOrder(), Collections.singletonList(wu.getUserId())));
				}
			} else {
				QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
				JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldId));
				List<WorkOrderAsset> workOrderAssets = query.fetch();
				List<WorkOrder> workOrders = workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder)
						.filter(wo -> Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.DECLINED, WorkOrderStatus.CANCELLED).contains(wo.getStatus()))
						.collect(Collectors.toList());
				workOrders.forEach(workOrder -> workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(workOrder.getId(), WorkOrderParticipantType.ASSIGNEE));

			}
			if (mold.getEngineersInCharge() != null && CollectionUtils.isNotEmpty(mold.getEngineersInCharge())) {
				QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
				JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldId));
				List<WorkOrderAsset> workOrderAssets = query.fetch();
				List<WorkOrderUser> workOrderUserAddList = new ArrayList<>();
				workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(workOrder -> WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(workOrder.getOrderType()))
						.forEach(workOrder -> {
									mold.getEngineersInCharge().forEach(u -> {
										WorkOrderUser workOrderUser = new WorkOrderUser();
										workOrderUser.setUser(u);
										workOrderUser.setUserId(u.getId());
										workOrderUser.setWorkOrderId(workOrder.getId());
										workOrderUser.setWorkOrder(workOrder);
										workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
										workOrderUserAddList.add(workOrderUser);
									});
									if (workOrder.getCreatedBy() != null) {
										User user = userRepository.getOne(workOrder.getCreatedBy());
										WorkOrderUser workOrderUser = new WorkOrderUser();
										workOrderUser.setUserId(user.getId());
										workOrderUser.setUser(user);
										workOrderUser.setWorkOrderId(workOrder.getId());
										workOrderUser.setWorkOrder(workOrder);
										workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
										workOrderUserAddList.add(workOrderUser);
									}
									workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(workOrder.getId(), WorkOrderParticipantType.CREATOR);
								}

						);
				if (CollectionUtils.isNotEmpty(workOrderUserAddList)) {
					workOrderUserRepository.saveAll(workOrderUserAddList);
				}
			} else {
				QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
				JPQLQuery<WorkOrderAsset> query = BeanUtils.get(JPAQueryFactory.class).select(workOrderAsset).from(workOrderAsset).where(workOrderAsset.assetId.eq(moldId));
				List<WorkOrderAsset> workOrderAssets = query.fetch();
				workOrderAssets.stream().map(WorkOrderAsset::getWorkOrder).filter(workOrder -> WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(workOrder.getOrderType()))
						.forEach(workOrder -> workOrderUserRepository.deleteAllByWorkOrderIdAndParticipantType(workOrder.getId(), WorkOrderParticipantType.CREATOR));

			}
			log.info("End createWorkOrderWhenAddPlant");
		});
		executor.shutdown();
	}

	@Transactional
	public void save(Mold mold) {
		save(mold, null);
	}

	@Transactional
	public void deleteById(Long id) {
		moldRepository.deleteById(id);
		customFieldValueService.deleteCustomFieldValueByObjectId(id);
	}

	public List<MiniComponentData> getExistsMoldCodes(List<String> moldCodes) {
		if (moldCodes != null) {
			moldCodes = moldCodes.stream().map(c -> StringUtils.trimWhitespace(c)).collect(Collectors.toList());
		}
		return moldRepository.findExistsMoldCodes(moldCodes);
	}

	public boolean existsByEquipmentCode(String equipmentCode, Long id) {
		if (id != null)
			return moldRepository.existsByEquipmentCodeAndIdNot(equipmentCode, id);
		return moldRepository.existsByEquipmentCode(equipmentCode);
	}

	public List<ChartData> findCycleTimeData(ChartPayload payload) {
		//Mold moldData = moldRepository.findByEquipmentCode(payload.getMoldCode());
		Mold moldData = moldRepository.findById(payload.getMoldId()).orElse(moldRepository.findByEquipmentCode(payload.getMoldCode()));

		return moldRepository.findCycleTimeData(payload, moldData);
	}

	public List<ChartData> buildHourStatisticsDetails(String compressedData) {
		List<ChartData> result = new ArrayList<>();
		compressedData = compressedData != null ? compressedData : "";
		String[] elements = compressedData.split("/");
		for (int i = 0; i < elements.length; i += 2) {
			if (elements[i] != "") {
				Double cycleTime = Double.valueOf(elements[i]) / 10;
				ChartData chartData = new ChartData(cycleTime.toString(), elements[i + 1] != "" ? Integer.valueOf(elements[i + 1]) : 0);
				chartData.setCycleTime(cycleTime);

				result.add(chartData);
			}
		}
		return result;
	}

	public List<ChartData> buildHourStatisticsTempDetails(List<ChartData> rawData, String date) {
		List<ChartData> result = new ArrayList<>();

		rawData.forEach(anHour -> {
			String compressedData = anHour.getCompressedData();
			String[] elements = compressedData.split("/");
			Instant firstRecord = DateUtils.getInstant(date.substring(0, 8) + "000000", "yyyyMMddHHmmss");
			if (anHour.getTitle().length() == 14) {
				String dateHour = anHour.getTitle().substring(0, 10);
				firstRecord = DateUtils.getInstant(dateHour + "0000", "yyyyMMddHHmmss");
			}
			for (int i = 0; i < elements.length; i++) {
				if (i != 5 && !elements[i].equalsIgnoreCase("")) {
					String title = DateUtils.getDateTime(firstRecord);
					result.add(new ChartData(title, Integer.valueOf(elements[i])));
					firstRecord = firstRecord.plus(10, ChronoUnit.MINUTES);
				}
			}
		});
		return result;
	}

	public List<ChartData> findHourStatisticsDetails(ChartPayload payload) {
		List<ChartData> result = new ArrayList<>();
		List<ChartData> rawData = moldRepository.findHourDetailsStatistics(payload, null);
		if (rawData != null && rawData.size() > 0) {
			if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_ANALYSIS)) {
				String compressedData = rawData.get(0).getCompressedData();
				/*
								compressedData = compressedData != null ? compressedData : "";
								String[] elements = compressedData.split("/");
								for(int i = 0; i < elements.length; i += 2){
									if(elements[i] != "") {
										Double cycleTime = Double.valueOf(elements[i]) / 10;

										result.add(new ChartData(cycleTime.toString(), elements[i + 1] != "" ? Integer.valueOf(elements[i + 1]) : 0));
									}
								}
				*/
				result.addAll(buildHourStatisticsDetails(compressedData));
			} else if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.TEMPERATURE_ANALYSIS)) {
				/*
								rawData.forEach(anHour -> {
									String compressedData = anHour.getCompressedData();
									String[] elements = compressedData.split("/");
									Instant firstRecord = DateUtils.getInstant(payload.getDate() + "000000", "yyyyMMddHHmmss");
									if(anHour.getTitle().length() == 14){
										String dateHour = anHour.getTitle().substring(0, 10);
										firstRecord = DateUtils.getInstant(dateHour + "0000", "yyyyMMddHHmmss");
									}
									for(int i = 0; i < elements.length; i++){
										if(i != 5 && !elements[i].equalsIgnoreCase("")){
											String title = DateUtils.getDateTime(firstRecord);
											result.add(new ChartData(title, Integer.valueOf(elements[i])));
											firstRecord = firstRecord.plus(10, ChronoUnit.MINUTES);
										}
									}
								});
				*/
				result.addAll(buildHourStatisticsTempDetails(rawData, payload.getDate()));
			}
		}
		return result;
	}

	/**
	 * 금형 정비 완료 처리.
	 * @param payload
	 */
	public void completeMoldMaintenance(MoldPayload payload) {

		// MoldMaintenance 기준 처리..
		MoldMaintenance moldMaintenance = moldMaintenanceRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));

		MoldMaintenance lastMoldMaintenance = moldMaintenanceRepository.findByMoldIdAndMaintenanceStatusAndLatest(moldMaintenance.getMoldId(), MaintenanceStatus.DONE, true)
				.orElse(null);
//		if(lastMoldMaintenance != null){
//			lastMoldMaintenance.setLatest(false);
//			moldMaintenanceRepository.save(lastMoldMaintenance);
//		}

		moldMaintenance.setMaintenanceStatus(MaintenanceStatus.DONE);
//		moldMaintenance.setShotCount(moldMaintenance.getMold().getLastShot()); 	// 완료 시점 lastshot
		moldMaintenance.setChecklist(payload.getMessage()); // Checklist
		moldMaintenance.setMaintenancedAt(Instant.now());
		moldMaintenance.setMaintenanceBy(SecurityUtils.getName());
		moldMaintenance.setStartTime(payload.getMaintenanceStartTime());
		moldMaintenance.setEndTime(payload.getMaintenanceEndTime());
		moldMaintenance.setLatest(true);
		// new log for last shot
		Integer lastShot = moldMaintenance.getMold().getLastShot();
		if (moldMaintenance.getStartTime() != null) {
			String startTime = DateUtils.getDate(moldMaintenance.getStartTime(), DateUtils.DEFAULT_DATE_FORMAT);
			Statistics statisticsLast = statisticsRepository.findFirstByMoldIdAndLstLessThanEqualOrderByScDesc(moldMaintenance.getMoldId(), startTime).orElse(null);
			if (statisticsLast != null)
				lastShot = statisticsLast.getSc();
		}
		moldMaintenance.setShotCount(lastShot); // 완료 시점 lastshot
		if (lastMoldMaintenance != null) {
			if (lastMoldMaintenance.getShotCount() != null && lastMoldMaintenance.getShotCount() > moldMaintenance.getShotCount()) {
				moldMaintenance.setLatest(false);
			} else {
				lastMoldMaintenance.setLatest(false);
				moldMaintenanceRepository.save(lastMoldMaintenance);
			}
		}

		moldMaintenanceRepository.save(moldMaintenance);

		// Mold 데이터 업데이트
		Mold mold = moldMaintenance.getMold();
		mold.setMaintenanced(false);
		mold.setMaintenanceCount(mold.getMaintenanceCount() + 1);

		Instant currentMaintenanceDate = moldMaintenance.getEndTime() != null ? moldMaintenance.getEndTime() : Instant.now();
		if (mold.getLastMaintenanceDate() != null) {
			if (currentMaintenanceDate.compareTo(mold.getLastMaintenanceDate()) > 0) {
				mold.setLastMaintenanceDate(currentMaintenanceDate);
			}
		} else {
			mold.setLastMaintenanceDate(currentMaintenanceDate);
		}

		if (payload.getFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_MAINTENANCE, mold.getId(), payload.getFiles()));
		}

		moldRepository.save(mold);
	}

	public void registerMoldMaintenance(MoldPayload payload) {
		Mold mold = findById(payload.getId());
		if (mold == null)
			new RuntimeException("ERROR: DATA (id)");

		MoldMaintenance lastMoldMaintenance = moldMaintenanceRepository.findByMoldIdAndMaintenanceStatusAndLatest(payload.getId(), MaintenanceStatus.DONE, true).orElse(null);

		MoldMaintenance moldMaintenance = new MoldMaintenance();

		moldMaintenance.setMold(mold);
		moldMaintenance.setMaintenanceStatus(MaintenanceStatus.DONE);
		moldMaintenance.setChecklist(payload.getMessage()); // Checklist
		moldMaintenance.setMaintenancedAt(Instant.now());
		moldMaintenance.setMaintenanceBy(SecurityUtils.getName());
		moldMaintenance.setStartTime(payload.getMaintenanceStartTime());
		moldMaintenance.setEndTime(payload.getMaintenanceEndTime());
		// new log for last shot
		Integer lastShot = mold.getLastShot();
		if (moldMaintenance.getStartTime() != null) {
			String startTime = DateUtils.getDate(moldMaintenance.getStartTime(), DateUtils.DEFAULT_DATE_FORMAT);
			Statistics statisticsLast = statisticsRepository.findFirstByMoldIdAndLstLessThanEqualOrderByScDesc(mold.getId(), startTime).orElse(null);
			if (statisticsLast != null)
				lastShot = statisticsLast.getSc();
		}
		moldMaintenance.setShotCount(lastShot);

		moldMaintenance.setLatest(true);
		moldMaintenance.setRegistered(true);

		if (lastMoldMaintenance != null) {
			if (lastMoldMaintenance.getShotCount() != null && lastMoldMaintenance.getShotCount() > moldMaintenance.getShotCount()) {
				moldMaintenance.setLatest(false);
			} else {
				lastMoldMaintenance.setLatest(false);
				moldMaintenanceRepository.save(lastMoldMaintenance);
			}
		}

		moldMaintenanceRepository.save(moldMaintenance);

//		mold.setMaintenanced(false);
		mold.setMaintenanceCount(mold.getMaintenanceCount() + 1);
		Instant currentMaintenanceDate = moldMaintenance.getEndTime() != null ? moldMaintenance.getEndTime() : Instant.now();
		if (mold.getLastMaintenanceDate() != null) {
			if (currentMaintenanceDate.compareTo(mold.getLastMaintenanceDate()) > 0) {
				mold.setLastMaintenanceDate(currentMaintenanceDate);
			}
		} else {
			mold.setLastMaintenanceDate(currentMaintenanceDate);
		}

		if (payload.getFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_MAINTENANCE, mold.getId(), payload.getFiles()));
		}

		moldRepository.save(mold);
	}

	public void completeMoldMaintenance(List<MoldPayload> moldPayloadList) {
		List<Long> ids = moldPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<MoldMaintenance> moldMaintenanceList = moldMaintenanceRepository.findByIdIsIn(ids);
		if (moldMaintenanceList == null || moldMaintenanceList.size() == 0)
			return;

		List<Long> moldIdsExist = moldMaintenanceList.stream().map(x -> x.getMoldId()).collect(Collectors.toList());
		List<MoldMaintenance> moldMaintenanceExistList = moldMaintenanceRepository.findByMoldIdIsInAndMaintenanceStatusAndLatest(moldIdsExist, MaintenanceStatus.DONE, true);
		moldMaintenanceExistList.forEach(moldMaintenance -> moldMaintenance.setLatest(false));
		moldMaintenanceRepository.saveAll(moldMaintenanceExistList);

		List<Mold> moldList = new ArrayList<>();
		moldMaintenanceList.forEach(moldMaintenance -> {
			MoldPayload payload = moldPayloadList.stream().filter(x -> x.getId().equals(moldMaintenance.getId())).findAny().orElse(null);
			if (payload != null) {
				moldMaintenance.setMaintenanceStatus(MaintenanceStatus.DONE);
				moldMaintenance.setShotCount(moldMaintenance.getMold().getLastShot()); // 완료 시점 lastshot
				moldMaintenance.setChecklist(payload.getMessage()); // Checklist
				moldMaintenance.setMaintenancedAt(Instant.now());
				moldMaintenance.setMaintenanceBy(SecurityUtils.getName());
				moldMaintenance.setStartTime(payload.getMaintenanceStartTime());
				moldMaintenance.setEndTime(payload.getMaintenanceEndTime());
				moldMaintenance.setLatest(true);
			}
			Mold mold = moldMaintenance.getMold();
			mold.setMaintenanced(false);
			mold.setMaintenanceCount(mold.getMaintenanceCount() + 1);
			moldList.add(mold);
		});
		moldMaintenanceRepository.saveAll(moldMaintenanceList);
		moldRepository.saveAll(moldList);
	}

	public void saveMoldDisconnect(MoldPayload payload) {

		// MoldMaintenance 기준 처리..
		MoldDisconnect moldDisconnect = moldDisconnectRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));

//		List<MoldDisconnect> moldDisconnectExistList = moldDisconnectRepository.findByMoldIdAndNotificationStatus(moldDisconnect.getMoldId(), NotificationStatus.CONFIRMED);
//		if(moldDisconnectExistList != null && moldDisconnectExistList.size() > 0) moldDisconnectRepository.deleteAll(moldDisconnectExistList);

		MoldDisconnect lastMoldDisconnect = moldDisconnectRepository.findByMoldIdAndNotificationStatusAndLatest(moldDisconnect.getMoldId(), NotificationStatus.CONFIRMED, true)
				.orElse(null);
		if (lastMoldDisconnect != null) {
			lastMoldDisconnect.setLatest(false);
			moldDisconnectRepository.save(lastMoldDisconnect);
		}

		moldDisconnect.setLatest(true);
		moldDisconnect.setNotificationStatus(NotificationStatus.CONFIRMED);
		moldDisconnect.setMessage(payload.getMessage());
		moldDisconnect.setConfirmedAt(Instant.now());
		moldDisconnect.setConfirmedBy(SecurityUtils.getName());

		moldDisconnectRepository.save(moldDisconnect);
	}

	public void saveMoldDisconnect(List<MoldPayload> moldPayloadList) {
		List<Long> ids = moldPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<MoldDisconnect> moldDisconnectList = moldDisconnectRepository.findByIdIsIn(ids);

		List<Long> moldIdsExist = moldDisconnectList.stream().map(x -> x.getMoldId()).collect(Collectors.toList());
		List<MoldDisconnect> moldDisconnectExistList = moldDisconnectRepository.findByMoldIdIsInAndNotificationStatusAndLatest(moldIdsExist, NotificationStatus.CONFIRMED, true);
		moldDisconnectExistList.forEach(moldDisconnect -> moldDisconnect.setLatest(false));
		moldDisconnectRepository.saveAll(moldDisconnectExistList);
//		if(moldDisconnectExistList != null && moldDisconnectExistList.size() > 0) moldDisconnectRepository.deleteAll(moldDisconnectExistList);

		if (moldDisconnectList == null || moldDisconnectList.size() == 0)
			return;
		moldDisconnectList.forEach(moldDisconnect -> {
			MoldPayload payload = moldPayloadList.stream().filter(x -> x.getId().equals(moldDisconnect.getId())).findAny().orElse(null);
			if (payload != null) {
				moldDisconnect.setLatest(true);
				moldDisconnect.setNotificationStatus(NotificationStatus.CONFIRMED);
				moldDisconnect.setMessage(payload.getMessage());
				moldDisconnect.setConfirmedAt(Instant.now());
				moldDisconnect.setConfirmedBy(SecurityUtils.getName());
			}
		});
		moldDisconnectRepository.saveAll(moldDisconnectList);
	}

	public void saveMoldCycleTime(MoldPayload payload) {
		// MoldMaintenance 기준 처리..
		MoldCycleTime moldCycleTime = moldCycleTimeRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));

		List<MoldCycleTime> lastMoldCycleTime = moldCycleTimeRepository.findByMoldIdAndNotificationStatusAndLatestAndPeriodType(moldCycleTime.getMoldId(),
				NotificationStatus.CONFIRMED, true, moldCycleTime.getPeriodType());
		if (lastMoldCycleTime != null && lastMoldCycleTime.size() > 0) {
			lastMoldCycleTime.forEach(mct -> {
				mct.setLatest(false);
			});
			moldCycleTimeRepository.saveAll(lastMoldCycleTime);
		}

		moldCycleTime.setLatest(true);
		moldCycleTime.setNotificationStatus(NotificationStatus.CONFIRMED);
		moldCycleTime.setMessage(payload.getMessage());
		moldCycleTime.setConfirmedAt(Instant.now());
		moldCycleTime.setConfirmedBy(SecurityUtils.getName());

		moldCycleTimeRepository.save(moldCycleTime);

		Mold mold = moldCycleTime.getMold();
		mold.setCycleTimeStatus(CycleTimeStatus.WITHIN_TOLERANCE);
		moldRepository.save(mold);

		// Mold 데이터 업데이트
		/*Mold mold = moldCycleTime.getMold();
		mold.setMaintenanced(false);
		mold.setMaintenanceCount(mold.getMaintenanceCount() + 1);

		moldRepository.save(mold);*/

	}

	public void saveMoldCycleTime(List<MoldPayload> moldPayloadList) {
		List<Long> ids = moldPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<MoldCycleTime> moldCycleTimeList = moldCycleTimeRepository.findByIdIsIn(ids);
		if (moldCycleTimeList == null || moldCycleTimeList.size() == 0)
			return;

		List<Long> moldIdsExist = moldCycleTimeList.stream().map(x -> x.getMoldId()).collect(Collectors.toList());
		List<MoldCycleTime> moldCycleTimeExistList = moldCycleTimeRepository.findByMoldIdIsInAndNotificationStatusAndLatest(moldIdsExist, NotificationStatus.CONFIRMED, true);
		moldCycleTimeExistList.forEach(moldDisconnect -> moldDisconnect.setLatest(false));
		moldCycleTimeRepository.saveAll(moldCycleTimeExistList);

		moldCycleTimeList.forEach(moldCycleTime -> {
			MoldPayload payload = moldPayloadList.stream().filter(x -> x.getId().equals(moldCycleTime.getId())).findAny().orElse(null);
			if (payload != null) {
				moldCycleTime.setLatest(true);
				moldCycleTime.setNotificationStatus(NotificationStatus.CONFIRMED);
				moldCycleTime.setMessage(payload.getMessage());
				moldCycleTime.setConfirmedAt(Instant.now());
				moldCycleTime.setConfirmedBy(SecurityUtils.getName());
			}
		});
		moldCycleTimeRepository.saveAll(moldCycleTimeList);
	}

	/**
	 * 변경된 위치 정보 확인
	 * @param payload
	 */
	@Deprecated
	public void saveMoldLocation(MoldPayload payload) {
		// MoldLocation 기준 처리..
		MoldLocation moldLocation = moldLocationRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));
		List<MoldLocation> lastMoldLocations = moldLocationRepository.findByMoldIdAndMoldLocationStatusAndLatest(moldLocation.getMoldId(), MoldLocationStatus.CONFIRMED, true);
		if (lastMoldLocations != null && lastMoldLocations.size() > 0) {
			MoldLocation lastMoldLocation = lastMoldLocations.get(0);
			lastMoldLocation.setLatest(false);
			moldLocationRepository.save(lastMoldLocation);
		}

		moldLocation.setLatest(true);
		moldLocation.setMoldLocationStatus(MoldLocationStatus.CONFIRMED);
		moldLocation.setMessage(payload.getMessage());
		moldLocation.setConfirmedAt(Instant.now());
		moldLocation.setConfirmedBy(SecurityUtils.getName());

		moldLocationRepository.save(moldLocation);

		// Mold
		boolean existsChangedLocation = moldLocationRepository.existsByMoldIdAndMoldLocationStatus(moldLocation.getMold().getId(), MoldLocationStatus.CHANGED);

		if (!existsChangedLocation) {
			Mold mold = moldLocation.getMold();
			mold.setLocationChanged(false);
			moldRepository.save(mold);
		}

		/*
		mold 기준 처리...

		Mold mold = findById(payload.getId());
		mold.setLocationChanged(payload.getLocationChanged());

		BooleanBuilder predicate = new BooleanBuilder();
		QMoldLocation moldLocation = QMoldLocation.moldLocation;

		predicate.and(moldLocation.moldId.eq(payload.getId()));
		predicate.and(moldLocation.moldLocationStatus.eq(MoldLocationStatus.CHANGED));
		Iterable<MoldLocation> moldLocationList = moldLocationRepository.findAll(predicate);


		for (MoldLocation ml : moldLocationList) {
			if (payload.getLocationChanged() == false) {
				ml.setMoldLocationStatus(MoldLocationStatus.CONFIRMED);
			} else {
				ml.setMoldLocationStatus(MoldLocationStatus.CHANGED);
			}
			moldLocationRepository.save(ml);
		}
		moldRepository.save(mold);*/
	}

	@Deprecated
	public void saveMoldLocation(List<MoldPayload> moldPayloadList) {
		List<Long> ids = moldPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<MoldLocation> moldLocationList = moldLocationRepository.findByIdIsIn(ids);
		if (moldLocationList == null || moldLocationList.size() == 0)
			return;

		List<MoldLocation> lastMoldLocation = moldLocationRepository.findByMoldIdIsInAndMoldLocationStatusAndLatest(ids, MoldLocationStatus.CONFIRMED, true);
		lastMoldLocation.forEach(moldLocation -> moldLocation.setLatest(false));
		moldLocationRepository.saveAll(lastMoldLocation);

		moldLocationList.forEach(moldLocation -> {
			MoldPayload payload = moldPayloadList.stream().filter(x -> x.getId().equals(moldLocation.getId())).findAny().orElse(null);
			if (payload != null) {
				moldLocation.setMoldLocationStatus(MoldLocationStatus.CONFIRMED);
				moldLocation.setMessage(payload.getMessage());
				moldLocation.setLatest(true);
				moldLocation.setConfirmedAt(Instant.now());
				moldLocation.setConfirmedBy(SecurityUtils.getName());
			}
		});
		moldLocationList = moldLocationRepository.saveAll(moldLocationList);

		List<Mold> moldList = new ArrayList<>();
		moldLocationList.forEach(moldLocation -> {
			// Mold
			boolean existsChangedLocation = moldLocationRepository.existsByMoldIdAndMoldLocationStatus(moldLocation.getMold().getId(), MoldLocationStatus.CHANGED);

			if (!existsChangedLocation) {
				Mold mold = moldLocation.getMold();
				mold.setLocationChanged(false);
				moldList.add(mold);
			}
		});
		moldRepository.saveAll(moldList);
	}
	@Transactional
	public void changeMoldLocationStatus(NoteAndChangeStatusPayLoad payLoad){
		List<MoldPayload> moldPayloads = payLoad.getMoldPayloadList();
		SystemNotePayload systemNotePayload = payLoad.getSystemNotePayload();
		List<Long> ids = moldPayloads.stream().map(SearchParam::getId).collect(Collectors.toList());
		List<MoldLocation> moldLocationList = moldLocationRepository.findByIdIsIn(ids);


		if (moldLocationList == null || moldLocationList.size() == 0)
			return;
		moldLocationList.forEach(moldLocation -> {
			MoldPayload payload = moldPayloads.stream().filter(x -> x.getId().equals(moldLocation.getId())).findAny().orElse(null);
			MoldLocationStatus moldLocationStatusOld = moldLocation.getMoldLocationStatus();
			if (payload != null) {
				try {
					moldLocation.setMoldLocationStatus(payload.getMoldLocationStatus());
				}
				catch (IllegalArgumentException ex){
					throw new BadRequestException(ex.getMessage());
				}

				moldLocation.setConfirmedAt(Instant.now());
				moldLocation.setConfirmedBy(SecurityUtils.getName());
				if (moldLocation.getMoldLocationStatus().equals(MoldLocationStatus.APPROVED)) {
					Company previousCompany = moldLocation.getPreviousLocation() != null ? moldLocation.getPreviousLocation().getCompany() : null;
					Company presentCompany = moldLocation.getLocation().getCompany();
					List<User> engineers = moldLocation.getMold().getEngineersInCharge().stream().filter(User::isEnabled).collect(Collectors.toList());
					if (previousCompany == null || !presentCompany.getId().equals(previousCompany.getId())) {
						List<User> userRemove = new ArrayList<>();
						engineers.forEach(e -> {
							List<Long> companyIds = new ArrayList<>();
							addAllTierCompanyIds(companyIds, e.getCompanyId());
							if (!(e.isAdmin() || (e.getCompany().getCompanyType().equals(CompanyType.IN_HOUSE)
									&& e.getRoles().stream().anyMatch(r -> r.getAuthority().equalsIgnoreCase("ROLE_ADMIN"))))
									&& !companyIds.contains(presentCompany.getId())) {
								userRemove.add(e);
							}
						});
						engineers.removeAll(userRemove);
					}
					// Assuming you have a list of MoldLocation objects from the repository
					// Find the latest created MoldLocation where LocationId is not equal to PreviousLocationId
					List<MoldLocation> latestCreatedMoldLocationList = moldLocationRepository.findApprovedChangeLocationByMoldOrderByCreatedAtDesc(moldLocation.getMoldId(), PageRequest.of(0,1));
					MoldLocation latestCreatedMoldLocation = !latestCreatedMoldLocationList.isEmpty()?latestCreatedMoldLocationList.get(0):null;

					if(MoldLocationStatus.PENDING.equals(moldLocationStatusOld)
						&& (latestCreatedMoldLocation!=null&&latestCreatedMoldLocation.getCreatedAt().isBefore(moldLocation.getCreatedAt()))) {
						moldLocation.getMold().setSupplierCompanyId(moldLocation.getLocation().getCompanyId());
						moldLocation.getMold().setSupplier(moldLocation.getLocation().getCompany());
					}
				}
/*
				moldLocation.setLatest(true);
*/
			}
		});

		List<Mold> moldList = new ArrayList<>();
		moldLocationList.forEach(moldLocation -> {
			// Mold
			boolean existsChangedLocation = moldLocationRepository.existsByMoldIdAndMoldLocationStatus(moldLocation.getMold().getId(), MoldLocationStatus.PENDING);

			if (!existsChangedLocation) {
				Mold mold = moldLocation.getMold();
				mold.setLocationChanged(false);
				moldList.add(mold);
			}
		});
		moldRepository.saveAll(moldList);
		if (Objects.nonNull(systemNotePayload) && systemNotePayload.getMessage()!=null && !systemNotePayload.getMessage().isEmpty()) {
			systemNoteService.save(systemNotePayload.getModel(), systemNotePayload);
		}
		moldLocationRepository.saveAll(moldLocationList);

		//notification
//		moldLocationList.forEach(ml -> notificationService.createAlertNotification(ml, AlertType.RELOCATION));
	}
	private void addAllTierCompanyIds(List<Long> companyIds, Long companyId) {
		if (companyIds.contains(companyId)||companyId==null) {
			return;
		}
		companyIds.add(companyId);
		BeanUtils.get(AccessHierarchyRepository.class).findFirstByCompanyId(companyId).ifPresent(data -> {
			if (!ObjectUtils.isEmpty(data.getAccessCompanyChildRelations())) {
				data.getAccessCompanyChildRelations().forEach(item -> {
					addAllTierCompanyIds(companyIds, item.getCompanyId());
				});
			}
		});
	}
	public Page<MoldLocation> findLocations(Predicate locationPredicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		if (SpecialSortProperty.moldSortProperties.contains(properties[0])) {
			List<MoldLocationExtraData> moldExtraDataList = moldLocationRepository.findMoldLocationExtraData(locationPredicate, pageable);
			List<MoldLocation> molds = moldExtraDataList.stream().map(x -> x.getMoldLocation()).collect(Collectors.toList());
			loadValueCustomField(molds.stream().map(mc -> mc.getMold()).filter(m -> m != null).collect(Collectors.toList()));
			loadAccumulatedShot(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
			loadWorkOrderDetail(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadEngineersInfo(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(molds, pageable, moldLocationRepository.count(locationPredicate));
		}
		if (properties[0].startsWith(SpecialSortProperty.alertCustomFieldSort)) {
			List<MoldLocationCustomFieldValue> moldLocationCustomFieldValue = moldLocationRepository.findMoldLocationCustomFieldValue(locationPredicate, pageable);
			List<MoldLocation> molds = moldLocationCustomFieldValue.stream().map(MoldLocationCustomFieldValue::getMoldLocation).collect(Collectors.toList());
			loadValueCustomField(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadAccumulatedShot(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
			loadWorkOrderDetail(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadEngineersInfo(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(molds, pageable, moldLocationRepository.count(locationPredicate));
		}
		if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldLocationExtraData> moldAccumulatedData = moldLocationRepository.findMoldLocationOrderByAccumulatedShot(locationPredicate, pageable, null);
			List<MoldLocation> molds = moldAccumulatedData.stream().map(MoldLocationExtraData::getMoldLocation).collect(Collectors.toList());
			loadValueCustomField(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadAccumulatedShot(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
			loadWorkOrderDetail(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadEngineersInfo(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(molds, pageable, moldLocationRepository.countMoldLocationOrderByAccumulatedShot(locationPredicate, pageable, null));
		}
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldLocation> molds = moldLocationRepository.findMoldLocationOrderByOperatingStatus(locationPredicate, pageable);
			loadValueCustomField(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadAccumulatedShot(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
			loadWorkOrderDetail(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(molds, pageable, moldLocationRepository.countMoldLocationOrderByAccumulatedShot(locationPredicate, pageable, null));
		}
		if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldLocation> molds = moldLocationRepository.findMoldLocationOrderByStatus(locationPredicate, pageable);
			loadValueCustomField(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadAccumulatedShot(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
			loadWorkOrderDetail(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			loadEngineersInfo(molds.stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(molds, pageable, moldLocationRepository.countMoldLocationOrderByAccumulatedShot(locationPredicate, pageable, null));
		}

		Page<MoldLocation> page = moldLocationRepository.findAll(locationPredicate, pageable);
		loadValueCustomField(page.getContent().stream().map(mc -> mc.getMold()).filter(m -> m != null).collect(Collectors.toList()));
		loadAccumulatedShot(page.getContent().stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		loadWorkOrderDetail(page.getContent().stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		loadEngineersInfo(page.getContent().stream().map(MoldLocation::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return page;
	}

//	public void addPreviousLocations(Page<MoldLocation> pageContent) {
//		pageContent.getContent().forEach(currentMoldLocation -> {
//			if (currentMoldLocation.getPre)
//			List<MoldLocation> previousMoldLocations = moldLocationRepository.findTop2ByMoldIdOrderByIdDesc(currentMoldLocation.getMoldId());
//			if (previousMoldLocations == null || previousMoldLocations.size() < 2)
//				return;
//			currentMoldLocation.setPreviousLocation(previousMoldLocations.get(1).getLocation());
//		});
//	}

	public Page<MoldMaintenance> findMaintenanceAll(Predicate predicate, Pageable pageable) {
		return moldMaintenanceRepository.findAll(predicate, pageable);
	}

	public Page<MoldCycleTime> findCycleTimeAllFiltered(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(QMoldCycleTime.moldCycleTime.mold));
		return findCycleTimeAll(builder, pageable, accumulatedShotFilter);
	}

	public Page<MoldCycleTime> findCycleTimeAllFiltered_New(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		builder.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getMoldFilter(QMoldCycleTime.moldCycleTime.mold));
		return findCycleTimeAll(builder, pageable, accumulatedShotFilter);
	}

	public Page<MoldCycleTime> findCycleTimeAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldCycleTime> page;
		if (SpecialSortProperty.moldCycleTimeSortProperties.contains(properties[0])) {
			List<MoldCycleTimeExtraData> moldCycleTimeExtraDataList = moldRepository.findMoldCycleTimeExtraData(predicate, pageable);
			List<MoldCycleTime> moldCycleTimes = moldCycleTimeExtraDataList.stream().map(x -> x.getMoldCycleTime()).collect(Collectors.toList());
			page = new PageImpl<>(moldCycleTimes, pageable, moldCycleTimeRepository.count(predicate));
		} else if (SpecialSortProperty.moldSortProperties.contains(properties[0])) {
			List<MoldCycleTimePartExtraData> moldExtraDataList = moldCycleTimeRepository.findMoldCycleTimeExtraData(predicate, pageable);
			List<MoldCycleTime> molds = moldExtraDataList.stream().map(x -> x.getMoldCycleTime()).collect(Collectors.toList());
			page = new PageImpl<>(molds, pageable, moldCycleTimeRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.alertCustomFieldSort)) {
			List<MoldCycleTimeCustomFieldValue> moldCycleTimeCustomFieldValue = moldCycleTimeRepository.findMoldCycleTimeCustomFieldValue(predicate, pageable);
			List<MoldCycleTime> molds = moldCycleTimeCustomFieldValue.stream().map(MoldCycleTimeCustomFieldValue::getMoldCycleTime).collect(Collectors.toList());
			page = new PageImpl<>(molds, pageable, moldCycleTimeRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldCycleTimeExtraData> moldAccumulatedData = moldCycleTimeRepository.findMoldCycleTimeOrderByAccumulatedShot(predicate, pageable, null);
			List<MoldCycleTime> molds = moldAccumulatedData.stream().map(MoldCycleTimeExtraData::getMoldCycleTime).collect(Collectors.toList());
			page = new PageImpl<>(molds, pageable, moldCycleTimeRepository.count(predicate));
		}
		else if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldCycleTime> molds = moldCycleTimeRepository.findMoldCycleTimeOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(molds, pageable, moldCycleTimeRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldCycleTime> molds = moldCycleTimeRepository.findMoldCycleTimeOrderByStatus(predicate, pageable);
			page = new PageImpl<>(molds, pageable, moldCycleTimeRepository.count(predicate));
		} else
			page = moldCycleTimeRepository.findAll(predicate, pageable);
		loadValueCustomField(page.getContent().stream().map(mc -> mc.getMold()).filter(m -> m != null).collect(Collectors.toList()));
		loadAccumulatedShot(page.getContent().stream().map(MoldCycleTime::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		loadWorkOrderDetail(page.getContent().stream().map(MoldCycleTime::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		loadEngineersInfo(page.getContent().stream().map(MoldCycleTime::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return page;
	}

	public List<MoldCycleTime> findCycleTimeByMoldId(Long moldId) {
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		return moldCycleTimeRepository.findByMoldId(moldId, sort);
	}

	public Page<MoldDisconnect> findDisconnectAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});

		Page<MoldDisconnect> moldDisconnectPage;
		if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldDisconnectExtraData> moldAccumulatedData = moldDisconnectRepository.findMoldDisconnectOrderByAccumulatedShot(predicate, pageable, null);
			List<MoldDisconnect> moldDisconnects = moldAccumulatedData.stream().map(MoldDisconnectExtraData::getMoldDisconnect).collect(Collectors.toList());
			moldDisconnectPage = new PageImpl<>(moldDisconnects, pageable, moldDisconnectRepository.count(predicate));
		} else if(properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldDisconnect> moldDisconnects = moldDisconnectRepository.findMoldDisconnectOrderByOperatingStatus(predicate, pageable);
			moldDisconnectPage = new PageImpl<>(moldDisconnects, pageable, moldDisconnectRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldDisconnect> moldDisconnects = moldDisconnectRepository.findMoldDisconnectOrderByStatus(predicate, pageable);
			moldDisconnectPage = new PageImpl<>(moldDisconnects, pageable, moldDisconnectRepository.count(predicate));
		} else {
			moldDisconnectPage = moldDisconnectRepository.findAll(predicate, pageable);
		}
		loadAccumulatedShot(moldDisconnectPage.getContent().stream().map(MoldDisconnect::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		loadWorkOrderDetail(moldDisconnectPage.getContent().stream().map(MoldDisconnect::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return moldDisconnectPage;
	}

	public Page<MaintenanceData> findAllMaintenanceData(MoldPayload payload, Pageable pageable) {
		Page<MaintenanceData> page;
		if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
			page = findMaintenanceDataAllFilter(payload.getMaintenancePredicate(), pageable, payload.getAccumulatedShotFilter());
		else if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected())
			page = findMaintenanceDataAllFilter_New(payload.getMaintenancePredicate(), pageable, payload.getAccumulatedShotFilter());
		else
			page = findMaintenanceDataAll(payload.getMaintenancePredicate(), pageable, payload.getAccumulatedShotFilter());

		return page;
	}

	public Page<MaintenanceData> findMaintenanceDataAllFilter(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(QMoldMaintenance.moldMaintenance.mold));
		return findMaintenanceDataAll(builder, pageable, accumulatedShotFilter);
	}

	public Page<MaintenanceData> findMaintenanceDataAllFilter_New(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		TabbedOverviewGeneralFilterPayload tabbedOverviewGeneralFilterPayload = tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter();
		builder.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getMoldFilter(QMoldMaintenance.moldMaintenance.mold));
//		Pair<Instant, Instant> startEnd = tabbedOverviewGeneralFilterPayload.getStartEndByDuration(true);
//		builder.and(QMoldMaintenance.moldMaintenance.createdAt.goe(startEnd.getFirst()).and(QMoldMaintenance.moldMaintenance.createdAt.loe(startEnd.getSecond())));
 		return findMaintenanceDataAll(builder, pageable, accumulatedShotFilter);
	}

	public Page<MaintenanceData> findMaintenanceDataAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldMaintenance> moldMaintenancePage;
		if (SpecialSortProperty.moldMaintenanceSortProperties.contains(properties[0])) {
			List<MoldMaintenanceExtraData> moldMaintenanceExtraDataList = moldRepository.findMoldMaintenanceExtraData(predicate, pageable);
			List<MoldMaintenance> moldMaintenances = moldMaintenanceExtraDataList.stream().map(x -> x.getMoldMaintenance()).collect(Collectors.toList());
			moldMaintenancePage = new PageImpl<>(moldMaintenances, pageable, moldMaintenanceRepository.count(predicate));
		} else if (SpecialSortProperty.moldSortProperties.contains(properties[0])) {
			List<MoldMaintenancePartExtraData> moldExtraDataList = moldMaintenanceRepository.findMoldMaintenanceExtraData(predicate, pageable);
			List<MoldMaintenance> moldMaintenances = moldExtraDataList.stream().map(x -> x.getMoldMaintenance()).collect(Collectors.toList());
			moldMaintenancePage = new PageImpl<>(moldMaintenances, pageable, moldMaintenanceRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.alertCustomFieldSort)) {
			List<MoldMaintenanceCustomFieldValue> moldMaintenanceCustomFieldValue = moldMaintenanceRepository.findMoldMaintenanceCustomFieldValue(predicate, pageable);
			List<MoldMaintenance> moldMaintenances = moldMaintenanceCustomFieldValue.stream().map(MoldMaintenanceCustomFieldValue::getMoldMaintenance).collect(Collectors.toList());
			moldMaintenancePage = new PageImpl<>(moldMaintenances, pageable, moldMaintenanceRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldMaintenanceExtraData> moldAccumulatedData = moldMaintenanceRepository.findMoldMaintenanceOrderByAccumulatedShot(predicate, pageable, null);
			List<MoldMaintenance> moldMaintenances = moldAccumulatedData.stream().map(MoldMaintenanceExtraData::getMoldMaintenance).collect(Collectors.toList());
			moldMaintenancePage = new PageImpl<>(moldMaintenances, pageable, moldMaintenanceRepository.count(predicate));
		} 	else if(properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldMaintenance> moldMaintenances = moldMaintenanceRepository.findMoldMaintenanceOrderByOperatingStatus(predicate, pageable);
			moldMaintenancePage = new PageImpl<>(moldMaintenances, pageable, moldMaintenanceRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldMaintenance> moldMaintenances = moldMaintenanceRepository.findMoldMaintenanceOrderByStatus(predicate, pageable);
			moldMaintenancePage = new PageImpl<>(moldMaintenances, pageable, moldMaintenanceRepository.count(predicate));
		} else {
			moldMaintenancePage = moldMaintenanceRepository.findAll(predicate, pageable);
		}
		List<MaintenanceData> maintenanceDataList = new ArrayList<>();
		moldMaintenancePage.forEach(moldMaintenance -> {

//			Double executionRate = Double.valueOf(moldMaintenance.getMold().getMaintenanceCount()) * 100 / Double.valueOf(moldMaintenanceRepository.countByMoldId(moldMaintenance.getMoldId()));
			Integer numMaintenaced = moldMaintenanceRepository.countByMoldIdAndMaintenanceStatusIn(moldMaintenance.getMoldId(),
					Arrays.asList(MaintenanceStatus.DONE, MaintenanceStatus.OVERDUE));
			Integer numDone = moldMaintenanceRepository.countByMoldIdAndMaintenanceStatusIn(moldMaintenance.getMoldId(), Arrays.asList(MaintenanceStatus.DONE));
			Double executionRate = numMaintenaced > 0 ? numDone * 100.0 / numMaintenaced : 0;
			MaintenanceData maintenanceData = new MaintenanceData(moldMaintenance, executionRate);
			try {
				if (moldMaintenance.getPeriodStart() != null && moldMaintenance.getMold().getPreventUpcoming() != null)
					maintenanceData.setPeriod(moldMaintenance.getPeriodStart() + moldMaintenance.getMold().getPreventUpcoming());
				else if (moldMaintenance.getPeriodEnd() != null && moldMaintenance.getMold().getPreventOverdue() != null)
					maintenanceData.setPeriod(moldMaintenance.getPeriodEnd() - moldMaintenance.getMold().getPreventOverdue());

				//Calculate shot gap from last maintenance
//				if(maintenanceData.getMold().getLastShot() != null && maintenanceData.getPeriod() != null){
//					if(maintenanceData.getMold().getLastShot() < maintenanceData.getPeriod()){
//						maintenanceData.setLastShotMade(
//								maintenanceData.getMold().getLastShot() - (maintenanceData.getPeriod() - maintenanceData.getMold().getPreventCycle()));
//					}else{
//						maintenanceData.setLastShotMade(maintenanceData.getMold().getLastShot() - maintenanceData.getPeriod());
//					}
//				}else{
//					maintenanceData.setLastShotMade(0);
//				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			maintenanceDataList.add(maintenanceData);

		});
		loadValueCustomField(maintenanceDataList.stream().map(mc -> mc.getMold()).filter(m -> m != null).collect(Collectors.toList()));
		loadAccumulatedShot(maintenanceDataList.stream().map(MaintenanceData::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		loadWorkOrderDetail(maintenanceDataList.stream().map(MaintenanceData::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return new PageImpl<>(maintenanceDataList, pageable, moldMaintenancePage.getTotalElements());
	}

	public Page<MoldDataSubmission> findDataSubmissionAll(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldDataSubmission> page;
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldDataSubmission> list = moldDataSubmissionRepository.findAllOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, moldDataSubmissionRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldDataSubmission> list = moldDataSubmissionRepository.findAllOrderByStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, moldDataSubmissionRepository.count(predicate));
		} else {
			page = moldDataSubmissionRepository.findAll(predicate, pageable);
		}

		loadWorkOrderDetail(page.getContent().stream().map(MoldDataSubmission::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return page;
	}

	public Page<MoldDataSubmission> findDataSubmissionByMoldId(Long moldId, Pageable pageable) {
		QMoldDataSubmission moldDataSubmission = QMoldDataSubmission.moldDataSubmission;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(moldDataSubmission.moldId.eq(moldId));
		builder.and(moldDataSubmission.notificationStatus.in(Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED)));
		return moldDataSubmissionRepository.findAll(builder, pageable);
	}

	public MoldDataSubmission findLastDisapprovalByMoldId(Long moldId) {
		return moldDataSubmissionRepository.findByMoldIdAndNotificationStatusAndLatest(moldId, NotificationStatus.DISAPPROVED, true).orElse(null);
	}

	public MoldDataSubmission findLastDataSubmissionByMoldId(Long moldId,NotificationStatus dataSubmission) {
		return moldDataSubmissionRepository.findByMoldIdAndNotificationStatusAndLatest(moldId, dataSubmission, true).orElse(null);
	}

	public List<MoldDataSubmission> confirmDataSubmission(DataSubmissionConfirmListPayload payload) {
		if (payload.getMoldIds() == null || payload.getAction() == null)
			return null;
		if (payload.getAction().equals(DataSubmissionAction.APPROVE) || payload.getAction().equals(DataSubmissionAction.DISAPPROVE)) {
			List<MoldDataSubmission> moldDataSubmissions = moldDataSubmissionRepository.findByMoldIdInAndNotificationStatusInAndLatest(payload.getMoldIds(),
					Arrays.asList(NotificationStatus.PENDING), true);
			List<Mold> molds = moldRepository.findByIdInOrderByIdDesc(payload.getMoldIds());
			if (molds == null || molds.size() == 0 || moldDataSubmissions == null || moldDataSubmissions.size() == 0)
				return null;

			List<MoldDataSubmission> lastStatus = moldDataSubmissionRepository.findByMoldIdInAndNotificationStatusInAndLatest(payload.getMoldIds(),
					Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED), true);
			if (lastStatus != null && lastStatus.size() > 0) {
				lastStatus.forEach(item -> {
					item.setLatest(false);
				});
				moldDataSubmissionRepository.saveAll(lastStatus);
			}

			if (payload.getAction().equals(DataSubmissionAction.APPROVE)) {
				moldDataSubmissions.forEach(moldDataSubmission -> moldDataSubmission.setNotificationStatus(NotificationStatus.APPROVED));
				molds.forEach(mold -> mold.setDataSubmission(NotificationStatus.APPROVED));
			} else {
				moldDataSubmissions.forEach(moldDataSubmission -> {
					moldDataSubmission.setNotificationStatus(NotificationStatus.DISAPPROVED);
//					moldDataSubmission.setReason(payload.getMessage());
				});
				molds.forEach(mold -> mold.setDataSubmission(NotificationStatus.DISAPPROVED));
			}
			moldDataSubmissions.forEach(moldDataSubmission -> {
				moldDataSubmission.setReason(payload.getMessage());
				moldDataSubmission.setApprovedAt(Instant.now());
				moldDataSubmission.setApprovedBy(SecurityUtils.getName());
			});

			moldDataSubmissionRepository.saveAll(moldDataSubmissions);

			// TODO: to change supplier hierarchy
			molds.forEach(mold -> {
				MoldDataSubmission moldDataSubmission = moldDataSubmissions.stream().filter(x -> x.getMoldId().equals(mold.getId())).findAny().orElse(null);
				if (moldDataSubmission != null) {
					List<User> supplierList = subTierService.getTierSuppliers(mold);
					List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(supplierList, AlertType.DATA_SUBMISSION);
					Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
					List<LogUserAlert> logUserAlerts = buildLogUserAlert(userAlertMap, null, null, null, null, null, null, null, Arrays.asList(moldDataSubmission), null, true);
					logUserAlertRepository.saveAll(logUserAlerts);

					List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail() == true)
							.collect(Collectors.toList());
					Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
					mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, null, null, null, null, null, Arrays.asList(moldDataSubmission), null,
							null);

					save(mold);
				}
			});
			return lastStatus;
		} else if (payload.getAction().equals(DataSubmissionAction.CONFIRM)) {
			List<MoldDataSubmission> moldDataSubmission = moldDataSubmissionRepository.findByMoldIdInAndNotificationStatusInAndLatest(payload.getMoldIds(),
					Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED), true);
			if (moldDataSubmission == null || moldDataSubmission.size() == 0)
				return null;
			moldDataSubmission.forEach(mds -> {
				mds.setConfirmed(true);
				mds.setConfirmedAt(Instant.now());
				mds.setConfirmedBy(SecurityUtils.getName());
				mds.setMessage(payload.getMessage());
			});
			return moldDataSubmissionRepository.saveAll(moldDataSubmission);
		}
		return null;
	}

	public Page<MoldDowntimeEvent> findDowntimeEventAll(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldDowntimeEvent> page;
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldDowntimeEvent> list = moldDowntimeEventRepository.findMoldDownTimeEventOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, moldDowntimeEventRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldDowntimeEvent> list = moldDowntimeEventRepository.findMoldDownTimeEventOrderByStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, moldDowntimeEventRepository.count(predicate));
		} else
		page = moldDowntimeEventRepository.findAll(predicate, pageable);

		loadWorkOrderDetail(page.getContent().stream().map(MoldDowntimeEvent::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return page;
	}

	public List<DashboardChartData> findPreventiveMaintenance(DashboardFilterPayload payload) {
		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;

		List<DashboardChartData> result = new ArrayList<>();

		// 1 예방정비 alert 데이터 기준으로 변경 - 10/25
		BooleanBuilder predicate = new BooleanBuilder();

		// DONE이 아닌 경우
		predicate.and(moldMaintenance.maintenanceStatus.ne(MaintenanceStatus.DONE));
//		predicate.and(moldMaintenance.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));

		// Last alert
		predicate.and(moldMaintenance.latest.isTrue());

		predicate.and(dashboardGeneralFilterUtils.getMoldFilter(moldMaintenance.mold));
		if (payload != null) {
			if (payload.getStartTime() != null && payload.getEndTime() != null)
				predicate.and(moldMaintenance.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(moldMaintenance.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

//		QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//		JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//				.where(logUserAlert.alertType.eq(AlertType.MAINTENANCE).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//		predicate.and(moldMaintenance.id.in(query));

		predicate.and(moldMaintenance.mold.maintenanced.eq(true));

		Iterable<MoldMaintenance> list = moldMaintenanceRepository.findAll(predicate);

		long scheduledConunt = getBaseMoldCount(payload); // INSTALL, CompanyId 기준
		long upcomingCount = 0;
		long overdueCount = 0;

		for (MoldMaintenance m : list) {
			if (m.getMaintenanceStatus() == MaintenanceStatus.UPCOMING) {
				upcomingCount++;
			} else if (m.getMaintenanceStatus() == MaintenanceStatus.OVERDUE) {
				overdueCount++;
			}
		}

		result.add(new DashboardChartData("On Scheduled", scheduledConunt - upcomingCount - overdueCount));
		result.add(new DashboardChartData("Upcoming", upcomingCount));
		result.add(new DashboardChartData("Overdue", overdueCount));

		/*// 1. On Scheduled
		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		predicate.and(mold.operatingStatus.isNotNull());
		predicate.and(mold.maintenanced.isFalse());

		result.add(new DashboardChartData("On Scheduled", moldRepository.count(predicate)));

		// 2. Upcoming, Overdue;
		predicate = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		predicate.and(mold.operatingStatus.isNotNull());
		predicate.and(mold.maintenanced.isTrue());

		long upcomingCount = 0;
		long overdueCount = 0;
		Iterable<Mold> list = moldRepository.findAll(predicate);

		for (Mold m : list) {
			int lastShot = m.getLastShot();
			int upcommingMax = (m.getPreventCycle() * (m.getMaintenanceCount() + 1)) + m.getPreventUpcoming();

			if (lastShot > upcommingMax) {
				overdueCount++;
			} else {
				upcomingCount++;
			}
		}

		result.add(new DashboardChartData("Upcoming", upcomingCount));
		result.add(new DashboardChartData("Overdue", overdueCount));*/

		return result;
	}

	/**
	 * getBaseMoldConunt();		// INSTALL, CompanyId 기준 Mold 수 (기준 에서 이슈있는 걸 차감함)
	 * @return
	 */
	private long getBaseMoldConunt() {
		long baseMoldCount;
		if (SecurityUtils.isAdmin()) { // admin이 아니면 companyId 기준
			baseMoldCount = moldRepository.countByEquipmentStatus(EquipmentStatus.INSTALLED);
		} else {
			// admin이 아니면 companyId 기준 (Supplier / Tool Maker인 경우는 어떻게 ??)
			baseMoldCount = moldRepository.countByEquipmentStatusAndCompanyId(EquipmentStatus.INSTALLED, SecurityUtils.getCompanyId());
		}
		return baseMoldCount;
	}

	private long getBaseMoldCount(DashboardFilterPayload payload) {
		return moldRepository.getBaseMoldCount(payload);
	}

	public List<DashboardChartData> findCycleTimeStatus(DashboardFilterPayload payload) {

		QMoldCycleTime moldCycleTime = QMoldCycleTime.moldCycleTime;

		List<DashboardChartData> result = new ArrayList<>();

		// 1 사이클 타임 alert 데이터 기준으로 변경 - 10/25
		BooleanBuilder predicate = new BooleanBuilder();

		// DONE이 아닌 경우
		predicate.and(moldCycleTime.cycleTimeStatus.ne(CycleTimeStatus.WITHIN_TOLERANCE));
		predicate.and(moldCycleTime.notificationStatus.eq(NotificationStatus.ALERT));
//		predicate.and(moldCycleTime.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
		predicate.and(moldCycleTime.latest.isTrue());

		predicate.and(dashboardGeneralFilterUtils.getMoldFilter(moldCycleTime.mold));
		if (payload != null) {
//			if(payload.getId() != null)
//				predicate.and(moldCycleTime.mold.supplier.id.eq(payload.getId()));
//			if(payload.getToolMakerId() != null)
//				predicate.and(moldCycleTime.mold.toolMaker.id.eq(payload.getToolMakerId()));
//			if(payload.getLocationId() != null)
//				predicate.and(moldCycleTime.mold.location.id.eq(payload.getLocationId()));
			if (payload.getStartTime() != null && payload.getEndTime() != null)
				predicate.and(moldCycleTime.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(moldCycleTime.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

//		QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//		JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//				.where(logUserAlert.alertType.eq(AlertType.CYCLE_TIME).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//		predicate.and(moldCycleTime.id.in(query));

		QUserAlert userAlert = QUserAlert.userAlert;
		predicate.and(moldCycleTime.periodType.eq(JPAExpressions.select(userAlert.periodType).from(userAlert)
				.where(userAlert.user.id.eq(SecurityUtils.getUserId()).and(userAlert.alertType.eq(AlertType.CYCLE_TIME)))));

		Iterable<MoldCycleTime> list = moldCycleTimeRepository.findAll(predicate);

		long within = getBaseMoldCount(payload);
		long outsideL1 = 0;
		long outsideL2 = 0;

		for (MoldCycleTime m : list) {
			if (m.getCycleTimeStatus() == CycleTimeStatus.OUTSIDE_L1) {
				outsideL1++;
			} else if (m.getCycleTimeStatus() == CycleTimeStatus.OUTSIDE_L2) {
				outsideL2++;
			}
		}

		result.add(new DashboardChartData("Within Tolerance", within - outsideL1 - outsideL2));
		result.add(new DashboardChartData("Outside L1", outsideL1));
		result.add(new DashboardChartData("Outside L2", outsideL2));

		return result;

		/*QMold mold = QMold.mold;
		BooleanBuilder predicate = new BooleanBuilder();

		List<DashboardChartData> result = new ArrayList<>();

		// 1. 동작 중(WORKING)인 Mold만 조회
		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		predicate.and(mold.operatingStatus.eq(OperatingStatus.WORKING));
		predicate.and(mold.maintenanced.isFalse());

		Iterable<Mold> list = moldRepository.findAll(predicate);

		Stream<Mold> stream1 = StreamSupport.stream(list.spliterator(), false);
		Stream<Mold> stream2 = StreamSupport.stream(list.spliterator(), false);
		Stream<Mold> stream3 = StreamSupport.stream(list.spliterator(), false);

		long within = stream1.count();
		long outsideL1 = stream2.filter(m -> m.getCycleTimeStatus() == CycleTimeStatus.OUTSIDE_L1).count();
		long outsideL2 = stream3.filter(m -> m.getCycleTimeStatus() == CycleTimeStatus.OUTSIDE_L2).count();



		result.add(new DashboardChartData("Within Tolerance", within - outsideL1 - outsideL2));
		result.add(new DashboardChartData("Outside L1", outsideL1));
		result.add(new DashboardChartData("Outside L2", outsideL2));

		return result;*/
	}

	public List<DashboardChartData> findEfficiencyStatus(DashboardFilterPayload payload) {
		QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;

		List<DashboardChartData> result = new ArrayList<>();

		// 1 사이클 타임 alert 데이터 기준으로 변경 - 10/25
		BooleanBuilder predicate = new BooleanBuilder();

		// DONE이 아닌 경우
		predicate.and(moldEfficiency.efficiencyStatus.ne(EfficiencyStatus.WITHIN_TOLERANCE));
		predicate.and(moldEfficiency.notificationStatus.eq(NotificationStatus.ALERT));
//		predicate.and(moldEfficiency.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
		predicate.and(moldEfficiency.latest.isTrue());

		/*
				QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
				JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
						.where(logUserAlert.alertType.eq(AlertType.EFFICIENCY).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
				predicate.and(moldEfficiency.id.in(query));
		*/

		QUserAlert userAlert = QUserAlert.userAlert;
		predicate.and(moldEfficiency.periodType.eq(JPAExpressions.select(userAlert.periodType).from(userAlert)
				.where(userAlert.user.id.eq(SecurityUtils.getUserId()).and(userAlert.alertType.eq(AlertType.EFFICIENCY)))));

		predicate.and(dashboardGeneralFilterUtils.getMoldFilter(moldEfficiency.mold));
		if (payload != null) {
//			if(payload.getId() != null)
//				predicate.and(moldEfficiency.mold.supplier.id.eq(payload.getId()));
//			if(payload.getToolMakerId() != null)
//				predicate.and(moldEfficiency.mold.toolMaker.id.eq(payload.getToolMakerId()));
//			if(payload.getLocationId() != null)
//				predicate.and(moldEfficiency.mold.location.id.eq(payload.getLocationId()));
			if (payload.getStartTime() != null && payload.getEndTime() != null)
				predicate.and(moldEfficiency.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(moldEfficiency.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		Iterable<MoldEfficiency> list = moldEfficiencyRepository.findAll(predicate);

		long within = getBaseMoldCount(payload);
		long outsideL1 = 0;
		long outsideL2 = 0;

		for (MoldEfficiency m : list) {
			if (m.getEfficiencyStatus() == EfficiencyStatus.OUTSIDE_L1) {
				outsideL1++;
			} else if (m.getEfficiencyStatus() == EfficiencyStatus.OUTSIDE_L2) {
				outsideL2++;
			}
		}

		result.add(new DashboardChartData("Within Tolerance", within - outsideL1 - outsideL2));
		result.add(new DashboardChartData("Outside L1", outsideL1));
		result.add(new DashboardChartData("Outside L2", outsideL2));

		return result;

		/*QMold mold = QMold.mold;
		BooleanBuilder predicate = new BooleanBuilder();

		List<DashboardChartData> result = new ArrayList<>();

		// 1. 동작 중(WORKING)인 Mold만 조회
		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		predicate.and(mold.operatingStatus.eq(OperatingStatus.WORKING));
		predicate.and(mold.maintenanced.isFalse());

		Iterable<Mold> list = moldRepository.findAll(predicate);

		Stream<Mold> stream1 = StreamSupport.stream(list.spliterator(), false);
		Stream<Mold> stream2 = StreamSupport.stream(list.spliterator(), false);
		Stream<Mold> stream3 = StreamSupport.stream(list.spliterator(), false);

		long within = stream1.count();
		long outsideL1 = stream2.filter(m -> m.getEfficiencyStatus() == EfficiencyStatus.OUTSIDE_L1).count();
		long outsideL2 = stream3.filter(m -> m.getEfficiencyStatus() == EfficiencyStatus.OUTSIDE_L2).count();



		result.add(new DashboardChartData("Within Tolerance", within - outsideL1 - outsideL2));
		result.add(new DashboardChartData("Outside L1", outsideL1));
		result.add(new DashboardChartData("Outside L2", outsideL2));

		return result;*/
	}

	public List<DashboardChartData> findCapacityUtilization(DashboardFilterPayload payload) {

		List<StatisticsData> statisticsData = moldRepository.getMoldUptimeData(payload);
		if (payload != null && payload.getStartTime() != null && payload.getEndTime() != null) {
			statisticsData.forEach(x -> {
				if (x.getMoldCreatedAt() < payload.getStartTime()) {
					x.setAliveTime(payload.getEndTime() - payload.getStartTime());
				} else {
					x.setAliveTime(payload.getEndTime() - x.getMoldCreatedAt());
				}
			});
		}

		long frequentlyUsed = statisticsData.stream().filter(statistics -> statistics.getUptime() * 100 / statistics.getAliveTime() >= 50).count();
		long occasionallyUsed = statisticsData.stream()
				.filter(statistics -> statistics.getUptime() * 100 / statistics.getAliveTime() < 50 && statistics.getUptime() * 100 / statistics.getAliveTime() >= 20).count();
		long rarelyUsed = statisticsData.stream()
				.filter(statistics -> statistics.getUptime() * 100 / statistics.getAliveTime() < 20 && statistics.getUptime() * 100 / statistics.getAliveTime() > 0).count();
		long neverUsed = statisticsData.size() - frequentlyUsed - occasionallyUsed - rarelyUsed;

		List<DashboardChartData> result = new ArrayList<>();
		result.add(new DashboardChartData("Frequently used", frequentlyUsed));
		result.add(new DashboardChartData("Occasionally used", occasionallyUsed));
		result.add(new DashboardChartData("Rarely used", rarelyUsed));
		result.add(new DashboardChartData("Never used", neverUsed));

		return result;
	}

	public List<DashboardChartData> findUtilizationRate(DashboardFilterPayload payload) {
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if (payload != null && payload.getStartTime() != null && payload.getEndTime() != null) {
			builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		Iterable<Mold> iterable = moldRepository.findAll(builder);
		List<Mold> molds = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
		long low = molds.stream().filter(aMold -> aMold.getLastShot() == null || aMold.getDesignedShot() == null || aMold.getDesignedShot() == 0 || getUtilizationRate(aMold) <= 30)
				.count();
		long medium = molds.stream()
				.filter(aMold -> aMold.getLastShot() != null && aMold.getDesignedShot() != null && getUtilizationRate(aMold) > 30 && getUtilizationRate(aMold) <= 80).count();
		long high = molds.stream()
				.filter(aMold -> aMold.getLastShot() != null && aMold.getDesignedShot() != null && getUtilizationRate(aMold) > 80 && getUtilizationRate(aMold) <= 100).count();
		long over = molds.stream().filter(aMold -> aMold.getLastShot() != null && aMold.getDesignedShot() != null && getUtilizationRate(aMold) > 100).count();

		List<DashboardChartData> result = new ArrayList<>();
		result.add(new DashboardChartData("Total", (long) molds.size()));
		result.add(new DashboardChartData("Low", low));
		result.add(new DashboardChartData("Medium", medium));
		result.add(new DashboardChartData("High", high));
		result.add(new DashboardChartData("Over-utilized", over));

		return result;
	}

	private Integer getUtilizationRate(Mold mold) {
		double utilizationRate = ((double) mold.getLastShot() / (double) mold.getDesignedShot()) * 100;
		return (int) Math.round(utilizationRate * 10) / 10;
	}

//	public static class DashboardChartDataList extends ListOut<DashboardChartData> {
//
//	}

	public List<DashboardChartData> findMoldDowntime(DashboardFilterPayload payload) {
//		QMoldMaintenance QMoldCorrective AccessCompanyRelation
//		CacheDataUtils.get("MOLD_SERVICE_FIND_MOLD_DOWNTIME", DashboardChartDataList.class, new CacheDataOptions(), null, () -> {
//			return new DashboardChartDataList();
//		}, () -> ValueUtils.min(() -> {
//			Page<Mold> page = moldRepository.findAll(PageRequest.of(0, 1, Sort.by(Direction.DESC, "updatedAt")));
//			return page.isEmpty() ? null : page.getContent().get(0).getUpdatedAt();
//		}));
		List<MaintenanceTimeData> maintenanceTimeDataList = moldRepository.findMaintenanceTimeData(payload);
		final Long[] plannedDowntime = { 0L };
		maintenanceTimeDataList.forEach(maintenanceTimeData -> {
			plannedDowntime[0] += maintenanceTimeData.getEndTime().getEpochSecond() - maintenanceTimeData.getStartTime().getEpochSecond();
		});

		final Long[] totalDowntime = { 0L };
		List<ChartData> uptimeDataList = statisticsRepository.findChartData(payload);
		Long totalUptime = uptimeDataList.stream().map(x -> x.getUptime()).reduce(0L, Long::sum);

		Map<String, Instant> map = new HashMap<>();
		uptimeDataList.forEach(uptimeData -> {
			if (!map.containsKey(uptimeData.getMoldCode())) {
				map.put(uptimeData.getMoldCode(), uptimeData.getMoldCreatedAt());
			}
		});

		if (payload == null || payload.getStartTime() == null) {
			map.forEach((k, v) -> {
				totalDowntime[0] += v.until(Instant.now(), ChronoUnit.SECONDS);
			});
		} else if (payload.getStartTime() != null && payload.getEndTime() != null) {
			totalDowntime[0] += payload.getEndTime() - payload.getStartTime();
		}
		totalDowntime[0] -= totalUptime;

//		List<LogDisconnectionData> logDisconnectionDataList = logDisconnectionService.findLogDisconnectionData(payload);
//		final Long[] totalDowntime = {0L};
//		if(logDisconnectionDataList != null && logDisconnectionDataList.size() > 0) {
//			Map<Long, List<LogDisconnectionData>> logDisconnectionMap = new HashMap<>();
//			logDisconnectionDataList.forEach(logDisconnectionData -> {
//				if (!logDisconnectionMap.containsKey(logDisconnectionData.getEquipmentId())) {
//					logDisconnectionMap.put(logDisconnectionData.getEquipmentId(), new ArrayList<>());
//				}
//				logDisconnectionMap.get(logDisconnectionData.getEquipmentId()).add(logDisconnectionData);
//			});
//
//			logDisconnectionMap.forEach((k, v) -> {
//				for(int i = 0; i < v.size() - 1; i++){
//					if(i == 0 && v.get(i).getEvent().equals(Event.DISCONNECT)){
//						totalDowntime[0] += Instant.now().getEpochSecond() - v.get(i).getCreatedAt().getEpochSecond();
//					}else
//					if(v.get(i).getEvent().equals(Event.RECONNECT) && v.get(i+1).getEvent().equals(Event.DISCONNECT)){
//						totalDowntime[0] += v.get(i).getCreatedAt().getEpochSecond() - v.get(i+1).getCreatedAt().getEpochSecond();
//					}
//				}
//			});
//		}

		List<DashboardChartData> result = new ArrayList<>();
		if (totalDowntime[0] == 0L) {
			result.add(new DashboardChartData("Planned Downtime", 0L));
			result.add(new DashboardChartData("Unplanned Downtime", 0L));
			return result;
		}
		Long plannedDowntimeRate = plannedDowntime[0] / totalDowntime[0];
		result.add(new DashboardChartData("Planned Downtime", plannedDowntimeRate));
		result.add(new DashboardChartData("Unplanned Downtime", 100 - plannedDowntimeRate));
		return result;
	}

	public List<DashboardChartData> findOee(DashboardFilterPayload payload) {
		List<ChartData> statisticsDataList = statisticsRepository.findChartData(payload);
		Map<String, List<ChartData>> map = new HashMap<>();
		statisticsDataList.forEach(statisticsData -> {
			if (!map.containsKey(statisticsData.getMoldCode())) {
				map.put(statisticsData.getMoldCode(), new ArrayList<>());
			}
			map.get(statisticsData.getMoldCode()).add(statisticsData);
		});

		Long totalUptime = statisticsDataList.stream().map(x -> x.getUptime()).reduce(0L, Long::sum);
		Double totalCycleTime = statisticsDataList.stream().map(x -> x.getCycleTime()).reduce(0.0, Double::sum);

		Double[] approvedUptime = { 0.0 };
		Double[] approvedCycleTime = { 0.0 };
		if (payload == null || payload.getStartTime() == null) {
			map.forEach((k, v) -> {
				if (v != null && v.size() > 0) {
					if (v.get(0).getApprovedUptime() != null) {
						approvedUptime[0] += v.get(0).getMoldCreatedAt().until(Instant.now(), ChronoUnit.SECONDS) * v.get(0).getApprovedUptime() / 100;
					} else
						approvedUptime[0] += v.get(0).getMoldCreatedAt().until(Instant.now(), ChronoUnit.SECONDS) * 0.9;
					if (v.get(0).getApprovedCycleTime() != null) {
						approvedCycleTime[0] += v.get(0).getMoldCreatedAt().until(Instant.now(), ChronoUnit.SECONDS) * v.get(0).getApprovedCycleTime() / 100;
					} else
						approvedCycleTime[0] += v.get(0).getMoldCreatedAt().until(Instant.now(), ChronoUnit.SECONDS) * 0.9;
				}
			});
		} else if (payload.getStartTime() != null && payload.getEndTime() != null) {
			map.forEach((k, v) -> {
				if (v != null && v.size() > 0) {
					if (v.get(0).getApprovedUptime() != null) {
						approvedUptime[0] += (payload.getEndTime() - payload.getStartTime()) * v.get(0).getApprovedUptime() / 100;
					} else
						approvedUptime[0] += (payload.getEndTime() - payload.getStartTime()) * 0.9;
					if (v.get(0).getApprovedCycleTime() != null) {
						approvedCycleTime[0] += (payload.getEndTime() - payload.getStartTime()) * v.get(0).getApprovedCycleTime() / 100;
					} else
						approvedCycleTime[0] += (payload.getEndTime() - payload.getStartTime()) * 0.9;
				}
			});
		}

		Long oee = 0L;
		if (approvedUptime[0] > 0 && approvedCycleTime[0] > 0) {
			Double doubleOee = (totalUptime * totalCycleTime * 100) / (approvedUptime[0] * approvedCycleTime[0]);
//			oee = (totalUptime * totalCycleTime.longValue() * 100) / (approvedUptime[0].longValue() * approvedCycleTime[0].longValue());
			oee = doubleOee.longValue();
		}
		List<DashboardChartData> result = new ArrayList<>();
		result.add(new DashboardChartData("oee", oee));
		return result;
	}

	public List<ChartData> findCycleTimeDashboard(Long moldId, DateViewType dateViewType, Long limit) {
//		Mold mold = findById(moldId);
//		if(mold == null) throw new RuntimeException("Mold does not exist.");

		Statistics lastStatistics = statisticsRepository.findFirstByMoldIdAndCtGreaterThanOrderByDayDesc(moldId, 0.0).orElse(null);
		if (lastStatistics == null)
			return new ArrayList<>();
		String endMoment = lastStatistics.getDay();
		if (dateViewType.equals(DateViewType.WEEK))
			endMoment = lastStatistics.getWeek();
		else if (dateViewType.equals(DateViewType.MONTH))
			endMoment = lastStatistics.getMonth();
		String startMoment = DateUtils.getReducedMoment(endMoment, dateViewType, limit != null ? limit : 10);

		ChartPayload payload = new ChartPayload();
		payload.setDateViewType(dateViewType);
		payload.setStartDate(startMoment);
		payload.setEndDate(endMoment);
		payload.setMoldId(moldId);
		payload.addChartDataType(ChartDataType.CYCLE_TIME_DASHBOARD);

		return findCycleTimeData(payload);
	}

	public Map<Long, List<MoldPartYearWeekOrMonth>> findProductionPart(DashboardFilterPayload payload, String type, List<Long> ids, Frequent frequent) {
		Map<Long, List<MoldPartYearWeekOrMonth>> result = new HashMap<>();
		if (type == null || type.equalsIgnoreCase("part")) {
			List<PartStatisticsPartIds> partStatisticsPartIds = moldRepository.findMoldIdsByDashboardPayloadAndPartIds(payload, ids, frequent);

			if (partStatisticsPartIds != null && partStatisticsPartIds.size() > 0) {
				partStatisticsPartIds.forEach(partStatisticsPartId -> {
					List<ProductionQuantityData> productionQuantityData = statisticsRepository.findProductionQuantityData(partStatisticsPartId.getStatisticsPartIds(), frequent);
					List<MoldPartYearWeekOrMonth> moldPartYearWeekOrMonths = new ArrayList<>();
					productionQuantityData.forEach(x -> moldPartYearWeekOrMonths.add(x.convert(frequent)));
					result.put(partStatisticsPartId.getPartId(), moldPartYearWeekOrMonths);
				});
			}
		} else {
			List<ProductionQuantityData> productionQuantityData = statisticsRepository.findProductionQuantityOfTooling(payload, ids, frequent);
			productionQuantityData.forEach(x -> {
				MoldPartYearWeekOrMonth moldPartYearWeekOrMonth = x.convert(frequent);
				if (ids != null && ids.size() > 0) {
					if (!result.containsKey(x.getId())) {
						result.put(x.getId(), new ArrayList<>());
					}
					result.get(x.getId()).add(moldPartYearWeekOrMonth);
				} else {
					if (!result.containsKey(0L)) {
						result.put(0L, new ArrayList<>());
					}
					result.get(0L).add(moldPartYearWeekOrMonth);
				}
			});

		}

		// Add same data point to all element, if it's null then count is 0
		result.forEach((id, list) -> {
			result.forEach((secondPart, secondList) -> {
				if (secondPart != id) {
					list.forEach(a -> {
						MoldPartYearWeekOrMonth diffList = secondList.stream()
								.filter(x -> (x.getYear().equals(a.getYear())
										&& ((x.getWeekOrMonth() == null && a.getWeekOrMonth() == null) || x.getWeekOrMonth().equals(a.getWeekOrMonth())))
										&& ((x.getDay() == null && a.getDay() == null) || x.getDay().equals(a.getDay())))
								.findAny().orElse(null);
						if (diffList == null) {
							secondList.add(new MoldPartYearWeekOrMonth(0L, a.getYear(), a.getWeekOrMonth(), a.getDay()));
						}
					});
				}
			});
		});

		// Add blank data
		result.forEach((id, list) -> {
			addBlankDataMoldPartYearWeekOrMonth(list, frequent);
		});
		return result;
	}

	public DashboardTabData getPartProducedTabData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			Long currentPartProduced = statisticsRepository.countProductionQuantityOfTooling(payload, true);
			Long previousPartProduced = statisticsRepository.countProductionQuantityOfTooling(payload, false);
			Double trend = null;
			if (previousPartProduced != 0) {
				trend = NumberUtils.roundOffNumber(currentPartProduced / ((double) previousPartProduced / 1000)) - 100;
			}
			return new DashboardTabData(DashboardChartType.PRODUCTION_QUANTITY, currentPartProduced.doubleValue(), trend);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return new DashboardTabData(DashboardChartType.PRODUCTION_QUANTITY);
		}
	}

	public Map<Long, List<MoldPartYearWeekOrMonth>> getPartProduced(TabbedOverviewGeneralFilterPayload payload) {
		Map<Long, List<MoldPartYearWeekOrMonth>> result = new HashMap<>();
		Frequent frequent = payload.getFrequentFromDuration();
		List<ProductionQuantityData> productionQuantityData = statisticsRepository.findProductionQuantityOfTooling(payload, frequent);

		//default data when there is no result
		if (CollectionUtils.isEmpty(productionQuantityData)) {
			switch (frequent) {
				case DAILY:
					productionQuantityData = Collections.singletonList(new ProductionQuantityData(0, DateUtils2.format(Instant.now(), DatePattern.yyyyMMdd, Zone.SYS)));
					break;
				case WEEKLY:
					productionQuantityData = Collections.singletonList(new ProductionQuantityData(0, DateUtils2.format(Instant.now(), DatePattern.YYYYww, Zone.SYS)));
					break;
				case MONTHLY:
					productionQuantityData = Collections.singletonList(new ProductionQuantityData(0, DateUtils2.format(Instant.now(), DatePattern.yyyyMM, Zone.SYS)));
					break;
				default:
			}
		}

		productionQuantityData.forEach(x -> {
			MoldPartYearWeekOrMonth moldPartYearWeekOrMonth = x.convert(frequent);
			if (!result.containsKey(0L)) {
				result.put(0L, new ArrayList<>());
			}
			result.get(0L).add(moldPartYearWeekOrMonth);
		});

		// Add same data point to all element, if it's null then count is 0
		result.forEach((id, list) -> {
			result.forEach((secondPart, secondList) -> {
				if (secondPart != id) {
					list.forEach(a -> {
						MoldPartYearWeekOrMonth diffList = secondList.stream()
								.filter(x -> (x.getYear().equals(a.getYear())
										&& ((x.getWeekOrMonth() == null && a.getWeekOrMonth() == null) || x.getWeekOrMonth().equals(a.getWeekOrMonth())))
										&& ((x.getDay() == null && a.getDay() == null) || x.getDay().equals(a.getDay())))
								.findAny().orElse(null);
						if (diffList == null) {
							secondList.add(new MoldPartYearWeekOrMonth(0L, a.getYear(), a.getWeekOrMonth(), a.getDay()));
						}
					});
				}
			});
		});

		// Add blank data
		result.forEach((id, list) -> {
			addBlankDataMoldPartYearWeekOrMonth_New(list, frequent);
		});

		result.forEach((id, list) -> {
			list.forEach(item -> {
				if (Frequent.DAILY.equals(frequent)) {
					item.setTitle(item.getYear() + DateTimeUtils.getDayOrWeekOrMonthString(item.getWeekOrMonth()) + DateTimeUtils.getDayOrWeekOrMonthString(item.getDay()));
				} else {
					item.setTitle(item.getYear() + DateTimeUtils.getDayOrWeekOrMonthString(item.getWeekOrMonth()));
				}
			});

			list.sort(Comparator.comparing(MoldPartYearWeekOrMonth::getTitle));
		});

		return result;
	}

	private void addBlankDataMoldPartYearWeekOrMonth(List<MoldPartYearWeekOrMonth> data, Frequent frequent) {
		String endYear = DateUtils.getYear(Instant.now());
		Integer endYearInt = Integer.valueOf(endYear);

		if (frequent.equals(Frequent.YEARLY)) {
			for (int year = endYearInt; year > endYearInt - 12; year--) {
				int finalYear = year;
				MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getYear().equals(finalYear)).findAny().orElse(null);
				if (aData == null) {
					data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(finalYear).build());
				}
			}
		} else {
			if (frequent.equals(Frequent.DAILY)) {
				for (int sub = 0; sub < 12; sub++) {
					String previousDay = DateUtils.getPreviousDay(sub);
					if (previousDay.length() < 8)
						return;
					String year = previousDay.substring(0, 4);
					String month = previousDay.substring(4, 6);
					String day = previousDay.substring(6, 8);
					Integer yearInt = Integer.valueOf(year);
					Integer monthInt = Integer.valueOf(month);
					Integer dayInt = Integer.valueOf(day);
					MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getYear().equals(yearInt) && x.getWeekOrMonth().equals(monthInt) && x.getDay().equals(dayInt))
							.findAny().orElse(null);
					if (aData == null) {
						data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(yearInt).weekOrMonth(monthInt).day(dayInt).build());
					}
				}
			} else {
				String endYearWeekOrMonth = DateUtils.getYearMonth(Instant.now(), "yyyyMMddHHmmss");
				if (frequent.equals(Frequent.WEEKLY))
					endYearWeekOrMonth = DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss");
				if (endYearWeekOrMonth.length() < 6)
					return;
				String endWeekOrMonth = endYearWeekOrMonth.substring(4, 6);
				Integer endWeekOrMonthInt = Integer.valueOf(endWeekOrMonth);
				if (endWeekOrMonthInt >= 12) {
					for (int i = 11; i >= 0; i--) {
						int weekOrMonth = endWeekOrMonthInt - i;
						MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getWeekOrMonth().equals(weekOrMonth)).findAny().orElse(null);
						if (aData == null) {
							data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(endYearInt).weekOrMonth(weekOrMonth).build());
						}
					}
				} else {
					Integer lastWeekOrMonthOfYear = 12;
					if (frequent.equals(Frequent.WEEKLY)) {
						lastWeekOrMonthOfYear = DateUtils.getLastWeekOfYear(endYearInt - 1);
					}
					for (int weekOrMonth = 1; weekOrMonth <= endWeekOrMonthInt; weekOrMonth++) {
						int finalMonth = weekOrMonth;
						MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getYear().equals(endYearInt) && x.getWeekOrMonth().equals(finalMonth)).findAny().orElse(null);
						if (aData == null) {
							data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(endYearInt).weekOrMonth(finalMonth).build());
						}
					}
					for (int weekOrMonth = lastWeekOrMonthOfYear + endWeekOrMonthInt - 12 + 1; weekOrMonth <= lastWeekOrMonthOfYear; weekOrMonth++) {
						int finalMonth = weekOrMonth;
						MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getYear().equals(endYearInt - 1) && x.getWeekOrMonth().equals(finalMonth)).findAny()
								.orElse(null);
						if (aData == null) {
							data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(endYearInt - 1).weekOrMonth(finalMonth).build());
						}
					}
				}
			}
		}
	}

	private void addBlankDataMoldPartYearWeekOrMonth_New(List<MoldPartYearWeekOrMonth> data, Frequent frequent) {
		String endYear = DateUtils.getYear(Instant.now());
		Integer endYearInt = Integer.valueOf(endYear);
		if (frequent.equals(Frequent.DAILY)) {
			for (int sub = 0; sub < 7; sub++) {
				String previousDay = DateUtils.getPreviousDay(sub);
				if (previousDay.length() < 8)
					return;
				String year = previousDay.substring(0, 4);
				String month = previousDay.substring(4, 6);
				String day = previousDay.substring(6, 8);
				Integer yearInt = Integer.valueOf(year);
				Integer monthInt = Integer.valueOf(month);
				Integer dayInt = Integer.valueOf(day);
				MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getYear().equals(yearInt) && x.getWeekOrMonth().equals(monthInt) && x.getDay().equals(dayInt))
						.findAny().orElse(null);
				if (aData == null) {
					data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(yearInt).weekOrMonth(monthInt).day(dayInt).build());
				}
			}
		} else if (frequent.equals(Frequent.WEEKLY)) {
			for (int i = 0; i <= 3 ; i++) {
				String currentWeek = DateUtils.getYearWeek(Instant.now().minus(i* 7L, ChronoUnit.DAYS), DatePattern.yyyyMMddHHmmss);
				String currentWeekNumber = currentWeek.substring(4, 6);
				Integer currentWeekInt = Integer.valueOf(currentWeekNumber);
				MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getWeekOrMonth().equals(currentWeekInt)).findAny().orElse(null);
				if (aData == null) {
					data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(endYearInt).weekOrMonth(currentWeekInt).build());
				}
			}
		} else {
			Instant current = Instant.now();
			for (int i = 0; i <= 5; i++) {
				String currentMonth = DateUtils.getYearMonth(current, DatePattern.yyyyMMddHHmmss);
				String currentDate = DateUtils2.format(current, DatePattern.yyyyMMdd, Zone.SYS);
				if (i > 0) {
					currentMonth = DateUtils.getPreviousMonth(currentDate);
					String previousDateStr = currentMonth + "01";
					current = DateUtils2.toInstant(previousDateStr, DatePattern.yyyyMMdd, Zone.SYS);
				}
				String currentMonthNumber = currentMonth.substring(4, 6);
				Integer currentMonthInt = Integer.valueOf(currentMonthNumber);
				MoldPartYearWeekOrMonth aData = data.stream().filter(x -> x.getWeekOrMonth().equals(currentMonthInt)).findAny().orElse(null);
				if (aData == null) {
					data.add(MoldPartYearWeekOrMonth.builder().count(0L).year(endYearInt).weekOrMonth(currentMonthInt).build());
				}
			}
		}
	}

	/**
	 * Get Alert Count
	 * @return
	 */
	public List<AlertCount> getAlertCount(String adminPage) throws ExecutionException, InterruptedException {
		User user = userService.findById(SecurityUtils.getUserId());
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		executorService = new DelegatingSecurityContextExecutorService(executorService, SecurityContextHolder.getContext());

		// 1. Relocation
		Future<Long> locationCountFuture = !MenuUtils.isPermitted("ALERT-RELOCATION") || !isAlertOn(user, AlertType.RELOCATION) ? null : executorService.submit(() -> {
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			long count = moldLocationRepository.count(payload.getLocationPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1210", "Relocation", locationCount));

		// 2. Disconnected
		Future<Long> disconntedCountFuture = !MenuUtils.isPermitted("ALERT-DISCONNECTED") || !isAlertOn(user, AlertType.DISCONNECTED) ? null : executorService.submit(() -> {
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			long count = moldDisconnectRepository.count(payload.getDisconnectPredicate());

			TerminalAlertPayload payloadTerminal = new TerminalAlertPayload();
			payloadTerminal.setStatus("alert");
			payloadTerminal.setLastAlert(true);
			count += terminalDisconnectRepository.count(payloadTerminal.getDisconnectPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1220", "Disconnected", disconntedCount));

		// 3. Cycle Time
		Future<Long> cycleTimeCountFuture = !MenuUtils.isPermitted("ALERT-CYCLE_TIME") || !isAlertOn(user, AlertType.CYCLE_TIME) ? null : executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.CYCLE_TIME).orElse(null);
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			if (userAlert != null) {
				payload.setSpecialAlertType(userAlert.getSpecialAlertType() != null ? userAlert.getSpecialAlertType() : SpecialAlertType.L1L2);
			}
			long count = moldCycleTimeRepository.count(payload.getCycleTimePredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1230", "Cycle Time", cycleTimeCount));

		// 4. Maintenance
		Future<Long> maintenanceCountFuture = !MenuUtils.isPermitted("ALERT-MAINTENANCE") || !isAlertOn(user, AlertType.MAINTENANCE) ? null : executorService.submit(() -> {
			UserAlert userAlertPM = userAlertRepository.findByUserAndAlertType(user, AlertType.MAINTENANCE).orElse(null);
			UserAlert userAlertCM = userAlertRepository.findByUserAndAlertType(user, AlertType.CORRECTIVE_MAINTENANCE).orElse(null);

			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setMaintenanced(true);
			payload.setLastAlert(true);
			if (userAlertPM != null) {
				payload.setSpecialAlertType(userAlertPM.getSpecialAlertType() != null ? userAlertPM.getSpecialAlertType() : SpecialAlertType.UPCOMING_OVERDUE);
			}
			long count = moldMaintenanceRepository.count(payload.getMaintenancePredicate());

			payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			count += moldCorrectiveRepository.count(payload.getCorrectivePredicate());

			if (userAlertPM != null && userAlertCM != null) {
				if (userAlertPM.getAlertOn() != null && userAlertCM.getAlertOn() != null) {
					if (!userAlertPM.getAlertOn() && !userAlertCM.getAlertOn())
						return 0L;
					else if (!userAlertPM.getAlertOn() && userAlertCM.getAlertOn()) {
						payload = new MoldPayload();
						payload.setStatus("alert");
						payload.setLastAlert(true);
						return moldCorrectiveRepository.count(payload.getCorrectivePredicate());
					} else if (userAlertPM.getAlertOn() && !userAlertCM.getAlertOn()) {
						payload = new MoldPayload();
						payload.setStatus("alert");
						payload.setMaintenanced(true);
						payload.setLastAlert(true);
						if (userAlertPM != null) {
							payload.setSpecialAlertType(userAlertPM.getSpecialAlertType() != null ? userAlertPM.getSpecialAlertType() : SpecialAlertType.UPCOMING_OVERDUE);
						}
						return moldMaintenanceRepository.count(payload.getMaintenancePredicate());
					}
				}
			}

			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1240", "Maintenance", maintenanceCount));

		// 5. Efficiency
		Future<Long> efficiencyCountFuture = !MenuUtils.isPermitted("ALERT-EFFICIENCY") || !isAlertOn(user, AlertType.EFFICIENCY) ? null : executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.EFFICIENCY).orElse(null);
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			if (userAlert != null) {
				payload.setSpecialAlertType(userAlert.getSpecialAlertType() != null ? userAlert.getSpecialAlertType() : SpecialAlertType.L1L2);
			}
			long count = moldEfficiencyRepository.count(payload.getEfficiencyPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1250", "Efficiency", efficiencyCount));

		// 6. Misconfigure
		Future<Long> misconfigureCountFuture = !MenuUtils.isPermitted("ALERT-MISCONFIGURE") || !isAlertOn(user, AlertType.MISCONFIGURE) ? null : executorService.submit(() -> {
			MoldPayload payload = new MoldPayload();
			payload.setStatus("misconfigured");
			payload.setLastAlert(true);
			long count = moldMisconfigureRepository.count(payload.getMisconfigurePredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1260", "Misconfigure", misconfigureCount));

		// 7. Data Submission
		Future<Long> dataSubmissionCountFuture = !MenuUtils.isPermitted("ALERT-DATA_SUBMISSION") || !isAlertOn(user, AlertType.DATA_SUBMISSION) ? null
				: executorService.submit(() -> {
					MoldPayload payload = new MoldPayload();
					payload.setStatus("alert");
					payload.setLastAlert(true);
					long count = moldDataSubmissionRepository.count(payload.getDataSubmissionPredicate());
					return count;
				});
//		alerts.add(new AlertCount("badge-alert-1270", "Data Submission", dataSubmissionCount));

		// 8. Refurbishment
		Future<Long> refurbishmentCountFuture = !MenuUtils.isPermitted("ALERT-REFURBISHMENT") || !isAlertOn(user, AlertType.REFURBISHMENT) ? null : executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.REFURBISHMENT).orElse(null);
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			if (userAlert != null) {
				payload.setSpecialAlertType(userAlert.getSpecialAlertType() != null ? userAlert.getSpecialAlertType() : SpecialAlertType.MEDIUM_HIGH);
			}
			long count = moldRefurbishmentRepository.count(payload.getRefurbishmentPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1280", "Refurbishment", refurbishmentCount));

		// 9. Detachment
		Future<Long> detachmentCountFuture = !MenuUtils.isPermitted("ALERT-DETACHMENT") || !isAlertOn(user, AlertType.DETACHMENT) ? null : executorService.submit(() -> {
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			long count = moldDetachmentRepository.count(payload.getDetachmentPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-1290", "Detachment", detachmentCount));

		// TODO: update
		// 10. Downtime
		Future<Long> downtimeCountFuture = !MenuUtils.isPermitted("ALERT-DOWNTIME") || !isAlertOn(user, AlertType.DOWNTIME) ? null : executorService.submit(() -> {
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			long count = moldDowntimeEventRepository.count(payload.getDowntimePredicate());
			return count;
		});

		Future<Long> machineDowntimeFuture = !MenuUtils.isPermitted("ALERT-DOWNTIME_MACHINE") || !isAlertOn(user, AlertType.DOWNTIME_MACHINE) ? null
				: executorService.submit(() -> {
					Pageable pageable = PageRequest.of(0, 1);
					SearchMachineDowntimePayload payload = new SearchMachineDowntimePayload();
					payload.setQuery(org.apache.commons.lang3.StringUtils.EMPTY);
					payload.setTab("ALERT");
					payload.setLastAlert(true);
					long count = machineDowntimeAlertRepository.countMachineDowntime(payload, pageable);
					return count;
				});

		// ADMIN ALERT
		boolean admin = ValueUtils.toBoolean(adminPage, false) && SecurityUtils.isAdmin();

		// 1. Terminal
		Future<Long> terminalCountFuture = !admin ? null : executorService.submit(() -> {
			TerminalPayload payload = new TerminalPayload();
			payload.setOperatingStatus(OperatingStatus.NOT_WORKING);
			payload.setEquipmentStatus(EquipmentStatus.INSTALLED);
			long count = terminalRepository.count(payload.getPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-2510", "Terminal", terminalCount));

		// 2. Counter
		Future<Long> counterCountFuture = !admin ? null : executorService.submit(() -> {
			CounterPayload payload = new CounterPayload();
			long count = moldRepository.findCdataCountersCount(payload);
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-2520", "Counter", counterCount));

		// 3. tooling
		Future<Long> moldCountFuture = !admin ? null : executorService.submit(() -> {
			MoldPayload payload = new MoldPayload();
			payload.setEquipmentStatus(EquipmentStatus.INSTALLED);
			payload.setExtraStatus("op-status-is-null");
			long count = moldRepository.count(payload.getPredicate());
			return count;
		});
//		alerts.add(new AlertCount("badge-alert-2530", "Tooling", moldCount));

		long locationCount = locationCountFuture == null ? 0 : locationCountFuture.get();
		long disconntedCount = disconntedCountFuture == null ? 0 : disconntedCountFuture.get();
		long cycleTimeCount = cycleTimeCountFuture == null ? 0 : cycleTimeCountFuture.get();
		long maintenanceCount = maintenanceCountFuture == null ? 0 : maintenanceCountFuture.get();
		long efficiencyCount = efficiencyCountFuture == null ? 0 : efficiencyCountFuture.get();
		long misconfigureCount = misconfigureCountFuture == null ? 0 : misconfigureCountFuture.get();
		long dataSubmissionCount = dataSubmissionCountFuture == null ? 0 : dataSubmissionCountFuture.get();
		long refurbishmentCount = refurbishmentCountFuture == null ? 0 : refurbishmentCountFuture.get();
		long detachmentCount = detachmentCountFuture == null ? 0 : detachmentCountFuture.get();
		long downtimeCount = downtimeCountFuture == null ? 0 : downtimeCountFuture.get();
		long machineDowntimeCount = machineDowntimeFuture == null ? 0 : machineDowntimeFuture.get();
		long terminalCount = terminalCountFuture == null ? 0 : terminalCountFuture.get();
		long counterCount = counterCountFuture == null ? 0 : counterCountFuture.get();
		long moldCount = moldCountFuture == null ? 0 : moldCountFuture.get();
		executorService.shutdown();

		List<AlertCount> alerts = new ArrayList<>();
		alerts.add(new AlertCount("badge-alert-1210", "Relocation", locationCount));
		alerts.add(new AlertCount("badge-alert-1220", "Disconnected", disconntedCount));
		alerts.add(new AlertCount("badge-alert-1230", "Cycle Time", cycleTimeCount));
		alerts.add(new AlertCount("badge-alert-1240", "Maintenance", maintenanceCount));
		alerts.add(new AlertCount("badge-alert-1250", "Efficiency", efficiencyCount));
		alerts.add(new AlertCount("badge-alert-1260", "Misconfigure", misconfigureCount));
		alerts.add(new AlertCount("badge-alert-1270", "Data Submission", dataSubmissionCount));
		alerts.add(new AlertCount("badge-alert-1280", "Refurbishment", refurbishmentCount));
		alerts.add(new AlertCount("badge-alert-1290", "Detachment", detachmentCount));
		alerts.add(new AlertCount("badge-alert-1211", "Downtime", downtimeCount));
		alerts.add(new AlertCount("badge-alert-1291", "Machine Downtime", machineDowntimeCount));
		long total = locationCount + disconntedCount + cycleTimeCount + maintenanceCount + efficiencyCount + misconfigureCount + dataSubmissionCount + refurbishmentCount
				+ detachmentCount + downtimeCount + machineDowntimeCount;
		alerts.add(new AlertCount("badge-alert-total", "Total(Front)", total));

		alerts.add(new AlertCount("badge-alert-2510", "Terminal", terminalCount));
		alerts.add(new AlertCount("badge-alert-2520", "Counter", counterCount));
		alerts.add(new AlertCount("badge-alert-2530", "Tooling", moldCount));
		long totalAdmin = terminalCount + counterCount + moldCount;
		alerts.add(new AlertCount("badge-alert-total-admin", "Total(Admin)", totalAdmin));

		return alerts;
	}

	private boolean isAlertOn(User user, AlertType type) {
		UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, type).orElse(null);
		return userAlert == null || (userAlert.getAlertOn() != null && userAlert.getAlertOn());
	}

	/**
	 * Get Alert Count by Ids
	 * @return
	 */
	public List<AlertCount> getAlertCountByIds(ListIdPayload ids) throws ExecutionException, InterruptedException {
		List<AlertCount> alerts = new ArrayList<>();
		User user = userService.findById(SecurityUtils.getUserId());
		ExecutorService executorService = Executors.newFixedThreadPool(10);

		executorService = new DelegatingSecurityContextExecutorService(executorService, SecurityContextHolder.getContext());
		// Alert Relocation
		Future<Long> locationCountFuture = executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.RELOCATION).orElse(null);
			if (userAlert != null) {
				if (userAlert.getAlertOn() != null && !userAlert.getAlertOn()) {
					return 0L;
				}
			}
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			payload.setFilteredIds(ids.getIds());
			long locationCount = moldLocationRepository.count(payload.getLocationPredicate());
			return locationCount;
		});

		// Alert Cycle Time
		Future<Long> cycleTimeCountFuture = executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.CYCLE_TIME).orElse(null);
			if (userAlert != null) {
				if (userAlert.getAlertOn() != null && !userAlert.getAlertOn()) {
					return 0L;
				}
			}
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			payload.setFilteredIds(ids.getIds());
			if (userAlert != null) {
				payload.setSpecialAlertType(userAlert.getSpecialAlertType() != null ? userAlert.getSpecialAlertType() : SpecialAlertType.L1L2);
			}
			long cycleTimeCount = moldCycleTimeRepository.count(payload.getCycleTimePredicate());
			return cycleTimeCount;
		});

		// Alert Cycle Time
		Future<ColorCode> cycleTimeColorFuture = executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.CYCLE_TIME).orElse(null);
			if (userAlert != null) {
				if (userAlert.getAlertOn() != null && !userAlert.getAlertOn()) {
					return ColorCode.NONE;
				}
			}
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			payload.setFilteredIds(ids.getIds());
			if (userAlert != null) {
				payload.setSpecialAlertType(userAlert.getSpecialAlertType() != null ? userAlert.getSpecialAlertType() : SpecialAlertType.L1L2);
			}
			long cycleTimeCount = moldCycleTimeRepository.count(payload.getCycleTimePredicate());

			if (cycleTimeCount == 0) {
				return ColorCode.NONE;
			} else {
				payload.setStatus(SpecialAlertType.L2.getCode());
				long l2Count = moldCycleTimeRepository.count(payload.getCycleTimePredicate());
				if (l2Count > 0) {
					return ColorCode.RED;
				} else {
					return ColorCode.YELLOW;
				}
			}
		});

		// Alert Downtime
		Future<Long> downtimeCountFuture = executorService.submit(() -> {
			UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.DOWNTIME).orElse(null);
			if (userAlert != null) {
				if (userAlert.getAlertOn() != null && !userAlert.getAlertOn()) {
					return 0L;
				}
			}
			MoldPayload payload = new MoldPayload();
			payload.setStatus("alert");
			payload.setLastAlert(true);
			payload.setFilteredIds(ids.getIds());
			long downtimeCount = moldDowntimeEventRepository.count(payload.getDowntimePredicate());
			return downtimeCount;
		});

		// Tooling
		Future<Long> moldCountFuture = executorService.submit(() -> {

			MoldPayload moldPayload = new MoldPayload();
			moldPayload.setFilteredIds(ids.getIds());
			long moldCount = moldRepository.count(moldPayload.getPredicate());
			return moldCount;
		});

		long locationCount = locationCountFuture.get();
		long cycleTimeCount = cycleTimeCountFuture.get();
		long downtimeCount = downtimeCountFuture.get();
		long moldCount = moldCountFuture.get();
		ColorCode cycleTimeColor = cycleTimeColorFuture.get();
		executorService.shutdown();

		alerts.add(new AlertCount("badge-alert-1210", "Relocation", locationCount, locationCount > 0 ? ColorCode.RED : ColorCode.NONE));
		alerts.add(new AlertCount("badge-alert-1230", "Cycle Time", cycleTimeCount, cycleTimeColor));
		alerts.add(new AlertCount("badge-alert-1211", "Downtime", downtimeCount, downtimeCount > 0 ? ColorCode.RED : ColorCode.NONE));
		alerts.add(new AlertCount("badge-alert-2530", "Tooling", moldCount, ColorCode.NONE));

		return alerts;
	}

	/**
	 * Alert Count 조회 (Admin)
	 * @return
	 */
	public List<AlertCount> getAlertCountForAdmin() {
		List<AlertCount> alerts = new ArrayList<>();

		// 1. Terminal
		TerminalPayload payload = new TerminalPayload();
		payload.setOperatingStatus(OperatingStatus.NOT_WORKING);
		payload.setEquipmentStatus(EquipmentStatus.INSTALLED);

		long terminalCount = terminalRepository.count(payload.getPredicate());

		alerts.add(new AlertCount("badge-alert-2510", "Terminal", terminalCount));

		// 2. Counter
		CounterPayload counterPayload = new CounterPayload();
		long counterCount = moldRepository.findCdataCountersCount(counterPayload);
		alerts.add(new AlertCount("badge-alert-2520", "Counter", counterCount));

		// 3. tooling
		MoldPayload moldPayload = new MoldPayload();
		moldPayload.setEquipmentStatus(EquipmentStatus.INSTALLED);
		moldPayload.setExtraStatus("op-status-is-null");
//		loadTreeCompanyForPayLoad(moldPayload);
		long moldCount = moldRepository.count(moldPayload.getPredicate());

		alerts.add(new AlertCount("badge-alert-2530", "Tooling", moldCount));

		// 4. Total
		long total = terminalCount + counterCount + moldCount;
		alerts.add(new AlertCount("badge-alert-total-admin", "Total", total));

		return alerts;
	}

	public Page<MoldEfficiency> findEfficiencyAllFiltered(Predicate predicate, Pageable pageable) {
		BooleanBuilder builder = new BooleanBuilder(predicate);
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(QMoldEfficiency.moldEfficiency.mold));
		return findEfficiencyAll(builder, pageable);
	}

	public Page<MoldEfficiency> findEfficiencyAll(Predicate predicate, Pageable pageable) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		if (SpecialSortProperty.moldEfficiencySortProperties.contains(properties[0])) {
			List<MoldEfficiencyExtraData> moldEfficiencyExtraDataList = moldRepository.findMoldEfficiencyExtraData(predicate, pageable);
			List<MoldEfficiency> moldEfficiencies = moldEfficiencyExtraDataList.stream().map(x -> x.getMoldEfficiency()).collect(Collectors.toList());
			loadWorkOrderDetail(moldEfficiencies.stream().map(MoldEfficiency::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(moldEfficiencies, pageable, moldEfficiencyRepository.count(predicate));
		}
		if(properties[0].contains(SpecialSortProperty.operatingStatus)){
			List<MoldEfficiency> moldEfficiencies = moldRepository.findMoldEfficiencyOrderByOperatingStatus(predicate, pageable);
			loadWorkOrderDetail(moldEfficiencies.stream().map(MoldEfficiency::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(moldEfficiencies, pageable, moldEfficiencyRepository.count(predicate));
		}
		if(SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])){
			List<MoldEfficiency> moldEfficiencies = moldRepository.findMoldEfficiencyOrderByStatus(predicate, pageable);
			loadWorkOrderDetail(moldEfficiencies.stream().map(MoldEfficiency::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
			return new PageImpl<>(moldEfficiencies, pageable, moldEfficiencyRepository.count(predicate));
		}
		return moldEfficiencyRepository.findAll(predicate, pageable);
	}

	public void saveMoldEfficiency(MoldPayload payload) {
		// MoldMaintenance 기준 처리..
		MoldEfficiency moldEfficiency = moldEfficiencyRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));
		List<MoldEfficiency> lastMoldEfficiency = moldEfficiencyRepository.findByMoldIdAndNotificationStatusAndLatestAndPeriodType(moldEfficiency.getMoldId(),
				NotificationStatus.CONFIRMED, true, moldEfficiency.getPeriodType());
		if (lastMoldEfficiency != null && lastMoldEfficiency.size() > 0) {
			lastMoldEfficiency.forEach(me -> {
				me.setLatest(false);
			});
			moldEfficiencyRepository.saveAll(lastMoldEfficiency);
		}

		moldEfficiency.setNotificationStatus(NotificationStatus.CONFIRMED);
		moldEfficiency.setMessage(payload.getMessage());
		moldEfficiency.setConfirmedAt(Instant.now());
		moldEfficiency.setConfirmedBy(SecurityUtils.getName());
		moldEfficiency.setLatest(true);

		moldEfficiencyRepository.save(moldEfficiency);

		Mold mold = moldEfficiency.getMold();
		mold.setEfficiencyStatus(EfficiencyStatus.WITHIN_TOLERANCE);
		moldRepository.save(mold);
	}

	public void saveMoldEfficiency(List<MoldPayload> moldAlertPayloadList) {
		List<Long> ids = moldAlertPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<MoldEfficiency> moldEfficiencyList = moldEfficiencyRepository.findByIdIsIn(ids);

		if (moldEfficiencyList == null || moldEfficiencyList.size() == 0)
			return;

		List<Long> moldIdsExist = moldEfficiencyList.stream().map(x -> x.getMoldId()).collect(Collectors.toList());
		List<MoldEfficiency> moldEfficiencyExistList = moldEfficiencyRepository.findByMoldIdIsInAndNotificationStatusAndLatest(moldIdsExist, NotificationStatus.CONFIRMED, true);
		moldEfficiencyExistList.forEach(moldEfficiency -> moldEfficiency.setLatest(false));
		moldEfficiencyRepository.saveAll(moldEfficiencyExistList);
//		List<MoldEfficiency> moldEfficiencyExistList = moldEfficiencyRepository.findByMoldIdIsInAndNotificationStatus(moldIdsExist, NotificationStatus.CONFIRMED);
//		if(moldEfficiencyExistList != null && moldEfficiencyExistList.size() > 0) moldEfficiencyRepository.deleteAll(moldEfficiencyExistList);

		moldEfficiencyList.forEach(moldEfficiency -> {
			MoldPayload payload = moldAlertPayloadList.stream().filter(x -> x.getId().equals(moldEfficiency.getId())).findAny().orElse(null);
			if (payload != null) {
				moldEfficiency.setNotificationStatus(NotificationStatus.CONFIRMED);
				moldEfficiency.setMessage(payload.getMessage());
				moldEfficiency.setConfirmedAt(Instant.now());
				moldEfficiency.setConfirmedBy(SecurityUtils.getName());
				moldEfficiency.setLatest(true);
			}
		});
		moldEfficiencyRepository.saveAll(moldEfficiencyList);
	}

	public List<MoldPart> findMoldPartsById(Long moldId) {
		return moldPartRepository.findAllByMoldIdOrderById(moldId);
		/*QMoldPart moldPart = QMoldPart.moldPart;
		BooleanBuilder builer = new BooleanBuilder();
		builer.and(moldPart.mold.id.eq(moldId));

		return moldPartRepository.findAll(builer, PageRequest.of(0, 100));*/
	}

	public List<MoldPart> findMoldPartAll(MoldPayload moldPayload, DashboardFilterPayload payload) {

		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder builder = new BooleanBuilder();

		QueryUtils.isMold(builder, moldPart.mold);

		if ("ACTIVE".equalsIgnoreCase(moldPayload.getMoldPartWhereCondition())) {
			builder.and(moldPart.mold.operatingStatus.isNotNull());
			builder.and(moldPart.part.enabled.isTrue());
		}

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldPart.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (moldPayload.getCategoryId() != null) {
			builder.and(moldPart.part.category.parentId.eq(moldPayload.getCategoryId()));
		}

		if (moldPayload.getProjectId() != null) {
			builder.and(moldPart.part.categoryId.eq(moldPayload.getProjectId()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(moldPart.mold));
		if (payload != null) {
//			if(payload.getId() != null)
//				builder.and(moldPart.mold.supplier.id.eq(payload.getId()));
//			if(payload.getToolMakerId() != null)
//				builder.and(moldPart.mold.toolMaker.id.eq(payload.getToolMakerId()));
//			if(payload.getLocationId() != null)
//				builder.and(moldPart.mold.location.id.eq(payload.getLocationId()));
			if (payload.getStartTime() != null && payload.getEndTime() != null)
				builder.and(moldPart.mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		Iterable<MoldPart> iterable = moldPartRepository.findAll(builder);

		return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
	}

	/**
	 * 고장 목록 조회
	 * @param predicate
	 * @param pageable
	 * @return
	 */
	public Page<MoldCorrective> findCorrectiveAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldCorrective> moldCorrectivePage;
		if (SpecialSortProperty.moldSortProperties.contains(properties[0])) {
			List<MoldCorrectivePartExtraData> moldExtraDataList = moldCorrectiveRepository.findMoldCorrectivePartExtraData(predicate, pageable);
			List<MoldCorrective> moldCorrectives = moldExtraDataList.stream().map(MoldCorrectivePartExtraData::getMoldCorrective).collect(Collectors.toList());
			moldCorrectivePage = new PageImpl<>(moldCorrectives, pageable, moldCorrectiveRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.alertCustomFieldSort)) {
			List<MoldCorrectiveCustomFieldValue> moldCorrectiveCustomFieldValue = moldCorrectiveRepository.findMoldCorrectiveCustomFieldValue(predicate, pageable);
			List<MoldCorrective> moldCorrectives = moldCorrectiveCustomFieldValue.stream().map(MoldCorrectiveCustomFieldValue::getMoldCorrective).collect(Collectors.toList());
			moldCorrectivePage = new PageImpl<>(moldCorrectives, pageable, moldCorrectiveRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldCorrectiveExtraData> moldAccumulatedData = moldCorrectiveRepository.findMoldCorrectiveOrderByAccumulatedShot(predicate, pageable, null);
			List<MoldCorrective> moldCorrectives = moldAccumulatedData.stream().map(MoldCorrectiveExtraData::getMoldCorrective).collect(Collectors.toList());
			moldCorrectivePage = new PageImpl<>(moldCorrectives, pageable, moldCorrectiveRepository.count(predicate));
		}else if(properties[0].contains(SpecialSortProperty.operatingStatus)){
			List<MoldCorrective> moldCorrectives = moldCorrectiveRepository.findMoldCorrectiveOrderByOperatingStatus(predicate, pageable);
			moldCorrectivePage = new PageImpl<>(moldCorrectives, pageable, moldCorrectiveRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldCorrective> moldCorrectives = moldCorrectiveRepository.findMoldCorrectiveOrderByStatus(predicate, pageable);
			moldCorrectivePage = new PageImpl<>(moldCorrectives, pageable, moldCorrectiveRepository.count(predicate));
		} else
		{
			moldCorrectivePage = moldCorrectiveRepository.findAll(predicate, pageable);
		}
		loadValueCustomField(moldCorrectivePage.getContent().stream().map(mc -> mc.getMold()).filter(m -> m != null).collect(Collectors.toList()));
		loadAccumulatedShot(moldCorrectivePage.getContent().stream().map(MoldCorrective::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		loadWorkOrderDetail(moldCorrectivePage.getContent().stream().map(MoldCorrective::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return moldCorrectivePage;
	}

	/**
	 * 고장 내역 등록
	 * @param corrective
	 */
	@Transactional
	public void saveCorrective(MoldCorrective corrective) {
		// 1. MoldCorrective 데이터 저장
		corrective.convertStringToInstant();

		Mold mold = moldRepository.findById(corrective.getMoldId()).orElseThrow(() -> new RuntimeException("Mold is null"));

		List<MoldCorrective> existing = moldCorrectiveRepository.findByMoldAndCorrectiveStatusInAndLatest(mold, Arrays.asList(CorrectiveStatus.FAILURE), true);

		if (existing != null && existing.size() > 0) {
			existing.forEach(x -> x.setLatest(false));
			moldCorrectiveRepository.saveAll(existing);
		}

		corrective.setMold(mold);
		corrective.setLocation(mold.getLocation());

//		if (corrective.getCurrencyType() == null) {
//			corrective.setCurrencyType(CurrencyType.USD);
//		}

		corrective.setCorrectiveStatus(CorrectiveStatus.FAILURE);
		corrective.setLatest(true);
		moldCorrectiveRepository.save(corrective);

		// 2. Mold EquipmentStatus 'FAILUER'로 변경.
		mold.setEquipmentStatus(EquipmentStatus.FAILURE);
		moldRepository.save(mold);

		// 3. Generate log user alert
		/*
				List<User> supplierList = subTierService.getTierSuppliers(mold);

				if(serverName.equalsIgnoreCase("dyson")){
					List<User> engineers = mold.getEngineers();
					engineers.forEach(engineer -> {
						if(!supplierList.contains(engineer)) supplierList.add(engineer);
					});
				}else {
					List<User> oemList = userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
					oemList.forEach(oem -> {
						if (!supplierList.contains(oem)) supplierList.add(oem);
					});
				}
		*/
		List<User> supplierList = getSupplierListOfMold(mold);

		List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(supplierList, AlertType.CORRECTIVE_MAINTENANCE);
		Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
		List<LogUserAlert> logUserAlerts = buildLogUserAlert(userAlertMap, null, null, null, null, null, null, null, null, Arrays.asList(corrective), true);
		logUserAlertRepository.saveAll(logUserAlerts);

		List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail() == true)
				.collect(Collectors.toList());
		Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
		mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, null, null, null, null, null, null, Arrays.asList(corrective), null);
	}

	public List<User> getSupplierListOfMold(Mold mold) {
		List<User> supplierList = subTierService.getTierSuppliers(mold);

		if (serverName.equalsIgnoreCase("dyson")) {
			List<User> engineers = mold.getEngineersInCharge();
			engineers.forEach(engineer -> {
				if (!supplierList.contains(engineer))
					supplierList.add(engineer);
			});
		} else {
			List<User> oemList = userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
			oemList.forEach(oem -> {
				if (!supplierList.contains(oem))
					supplierList.add(oem);
			});
		}
		return supplierList;
	}

	public MoldCorrective findMoldCorrectiveById(Long id) {
		return moldCorrectiveRepository.findById(id).orElseGet(() -> new MoldCorrective());
	}

	public void resisterRepair(MoldCorrective corrective, CorrectiveAction correctiveAction) {
		// 1. MoldCorrective 데이터 저장
//		corrective.convertStringToInstant();

		MoldCorrective moldCorrective = moldCorrectiveRepository.findById(corrective.getId()).orElseThrow(() -> new RuntimeException("MoldCorrective is null."));

		List<FileStorage> copyOfDocuments = new ArrayList<>();
		boolean isNew = false;
		if (correctiveAction.equals(CorrectiveAction.REQUEST)) {
			if (!moldCorrective.getCorrectiveStatus().equals(CorrectiveStatus.FAILURE)) {
				moldCorrective.setLatest(false);
				moldCorrectiveRepository.save(moldCorrective);

				MoldCorrective newRequest = new MoldCorrective();
				newRequest.setMold(moldCorrective.getMold());
				newRequest.setLocation(moldCorrective.getMold().getLocation());
				newRequest.setMemo(moldCorrective.getMemo());
				newRequest.setFailureTime(moldCorrective.getFailureTime());
				newRequest.setFailureReason(moldCorrective.getFailureReason());

				FileStoragePayload fileStoragePayload = new FileStoragePayload();
				fileStoragePayload.setStorageType(StorageType.MOLD_CORRECTIVE);
				fileStoragePayload.setRefId(moldCorrective.getId());
				Iterable<FileStorage> fileStorageIterable = fileStorageService.findAll(fileStoragePayload.getPredicate());
				if (fileStorageIterable != null) {
					fileStorageIterable.forEach(fileStorage -> {
						copyOfDocuments.add(fileStorageService.copyDocument(fileStorage));
					});
				}

				moldCorrective = newRequest;
				isNew = true;
			}
			moldCorrective.setCorrectiveStatus(CorrectiveStatus.REQUESTED);
		} else {
			List<MoldCorrective> existing = moldCorrectiveRepository.findByMoldAndCorrectiveStatusInAndLastChecked(moldCorrective.getMold(),
					Arrays.asList(CorrectiveStatus.APPROVED, CorrectiveStatus.DISAPPROVED, CorrectiveStatus.COMPLETED), true);
			existing.forEach(mc -> mc.setLastChecked(false));
			moldCorrectiveRepository.saveAll(existing);

			if (correctiveAction.equals(CorrectiveAction.APPROVE)) {
				moldCorrective.setCorrectiveStatus(CorrectiveStatus.APPROVED);
			} else if (correctiveAction.equals(CorrectiveAction.DISAPPROVE)) {
				moldCorrective.setCorrectiveStatus(CorrectiveStatus.DISAPPROVED);
				moldCorrective.setDisapprovedBy(SecurityUtils.getName());
				moldCorrective.setDisapprovedAt(Instant.now());
			} else if (correctiveAction.equals(CorrectiveAction.COMPLETE)) {
				moldCorrective.setCorrectiveStatus(CorrectiveStatus.COMPLETED);

				Mold mold = moldCorrective.getMold();
				if (mold.getCounter() == null) {
					mold.setEquipmentStatus(EquipmentStatus.AVAILABLE);
				} else {
					mold.setEquipmentStatus(EquipmentStatus.INSTALLED);
				}

				if (corrective.getCost() != null) {
					mold.setAccumulatedMaintenanceCost(
							mold.getAccumulatedMaintenanceCost() == null ? corrective.getCost() : mold.getAccumulatedMaintenanceCost() + corrective.getCost());
				}

				Instant currentMaintenanceDate = moldCorrective.getEndTime() != null ? moldCorrective.getEndTime() : Instant.now();
				if (mold.getLastMaintenanceDate() != null) {
					if (currentMaintenanceDate.compareTo(mold.getLastMaintenanceDate()) > 0) {
						mold.setLastMaintenanceDate(currentMaintenanceDate);
					}
				} else {
					mold.setLastMaintenanceDate(currentMaintenanceDate);
				}

				moldRepository.save(mold);
			}
			moldCorrective.setLastChecked(true);
		}
		moldCorrective.setLatest(true);
		if (corrective.getFailureReason() != null)
			moldCorrective.setFailureReason(corrective.getFailureReason());
		if (corrective.getNumberOfBackups() != null)
			moldCorrective.setNumberOfBackups(corrective.getNumberOfBackups());
		if (corrective.getExpectedEndTime() != null)
			moldCorrective.setExpectedEndTime(corrective.getExpectedEndTime());
		if (corrective.getDisapprovedMessage() != null)
			moldCorrective.setDisapprovedMessage(corrective.getDisapprovedMessage());
		if (corrective.getMemo() != null && !corrective.getMemo().equals("")) {
			moldCorrective.setMemo(
					moldCorrective.getMemo() != null && moldCorrective.getMemo() != "" ? corrective.getMemo() + " - " + SecurityUtils.getName() + "\n" + moldCorrective.getMemo()
							: corrective.getMemo() + " - " + SecurityUtils.getName());
		}
		if (corrective.getStartTime() != null)
			moldCorrective.setStartTime(corrective.getStartTime());
		if (corrective.getEndTime() != null)
			moldCorrective.setEndTime(corrective.getEndTime());
		if (corrective.getCost() != null) {
			moldCorrective.setCost(corrective.getCost());
			moldCorrective.setCurrencyType(corrective.getCurrencyType() != null ? corrective.getCurrencyType() : CurrencyType.USD);
		}

		moldCorrectiveRepository.save(moldCorrective);
		createUpdateMoldDownTime(moldCorrective);

		if (isNew) {
			List<User> supplierList = subTierService.getTierSuppliers(moldCorrective.getMold());
			List<User> oemList = userRepository.findByAdminIsTrueAndDeletedIsFalse();
			oemList.forEach(oem -> {
				if (!supplierList.contains(oem))
					supplierList.add(oem);
			});
			List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(supplierList, AlertType.CORRECTIVE_MAINTENANCE);
			Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
			List<LogUserAlert> logUserAlerts = buildLogUserAlert(userAlertMap, null, null, null, null, null, null, null, null, Arrays.asList(moldCorrective), true);
			logUserAlertRepository.saveAll(logUserAlerts);
		}

		Long refId = moldCorrective.getId();
		if (copyOfDocuments.size() > 0) {
			copyOfDocuments.forEach(aCopy -> aCopy.setRefId(refId));
			fileStorageService.saveAll(copyOfDocuments);
		}
		if (corrective.getFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_CORRECTIVE, moldCorrective.getId(), corrective.getFiles()));
		}

//		moldCorrective.setCost(corrective.getCost());
//		moldCorrective.setCurrencyType(corrective.getCurrencyType());
//		moldCorrective.setMemo(corrective.getMemo());
//		moldCorrective.setRepairTime(corrective.getRepairTime());
//
//		// 시간 계산
//		try {
//			long timeToRepair = ChronoUnit.HOURS.between(moldCorrective.getFailureTime(), moldCorrective.getRepairTime());
//			moldCorrective.setTimeToRepair((int) timeToRepair);
//		} catch(Exception e) {
//			log.warn("timeToRepair ERROR");
//			moldCorrective.setTimeToRepair(0);
//		}
//
//		// 수리 시간이 고장 시간 보다 작은 경우
//		long secondsToRepair = ChronoUnit.SECONDS.between(moldCorrective.getFailureTime(), moldCorrective.getRepairTime());
//		if (secondsToRepair <= 0L) {
//			throw new AppException("Repair time is wrong.");
//		}
//
//		moldCorrective.setCorrectiveStatus(CorrectiveStatus.REPAIRED);
//		moldCorrectiveRepository.save(moldCorrective);

		// 2. Mold EquipmentStatus 'AVAILABLE' or 'INSTALLED' 로 변경.
//		Mold mold = moldCorrective.getMold();
//		if (mold.getCounter() == null) {
//			mold.setEquipmentStatus(EquipmentStatus.AVAILABLE);
//		} else {
//			mold.setEquipmentStatus(EquipmentStatus.INSTALLED);
//		}
//
//		moldRepository.save(mold);

		// 파일등록
		// 첨부파일 등록
//		if (corrective.getFiles() != null) {
//			fileStorageService.save(new FileInfo(StorageType.MOLD_CORRECTIVE, moldCorrective.getId(), corrective.getFiles()));
//		}

	}

	private void createUpdateMoldDownTime(MoldCorrective corrective) {
		try {

			List<MoldDownTime> moldDownTimes = moldDownTimeRepository.getByMoldCorrective(corrective);
			//remove old
			if (moldDownTimes != null && !moldDownTimes.isEmpty()) {
				moldDownTimeRepository.deleteAll(moldDownTimes);
			}
			if (CorrectiveStatus.COMPLETED.equals(corrective.getCorrectiveStatus()) && corrective.getStartTime() != null && corrective.getEndTime() != null) {
				moldDownTimes = new ArrayList<>();
				Instant instantTime = corrective.getStartTime();
				while (instantTime.getEpochSecond() < corrective.getEndTime().getEpochSecond()) {
					Instant nextDay = instantTime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
					Instant nextTime = nextDay.getEpochSecond() < corrective.getEndTime().getEpochSecond() ? nextDay : corrective.getEndTime();

					MoldDownTime moldDownTime = new MoldDownTime(corrective, nextTime.toEpochMilli() - instantTime.toEpochMilli(), instantTime);
					moldDownTimes.add(moldDownTime);

					instantTime = nextDay;
				}
				moldDownTimeRepository.saveAll(moldDownTimes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancelRepair(MoldCorrective corrective) {
		// 1. MoldCorrective 데이터 저장
		MoldCorrective moldCorrective = moldCorrectiveRepository.findById(corrective.getId()).orElseThrow(() -> new RuntimeException("MoldCorrective is null."));

		moldCorrective.setCost(null);
		moldCorrective.setCurrencyType(CurrencyType.USD); // 기본값
		moldCorrective.setMemo(null);
		moldCorrective.setRepairTime(null);
		moldCorrective.setRepairTime(null);
		moldCorrective.setTimeToRepair(null);
		moldCorrective.setCorrectiveStatus(CorrectiveStatus.FAILURE);

		moldCorrectiveRepository.save(moldCorrective);
		createUpdateMoldDownTime(moldCorrective);

		// 2. Mold EquipmentStatus 'AVAILABLE' or 'INSTALLED' 로 변경.
		Mold mold = moldCorrective.getMold();
		mold.setEquipmentStatus(EquipmentStatus.FAILURE);

		moldRepository.save(mold);
	}

	public Page<MoldMisconfigure> findMisconfigureAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldMisconfigure> page;
		if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldMisconfigureExtraData> moldAccumulatedData = moldMisconfigureRepository.findMoldMisconfigureOrderByAccumulatedShot(predicate, pageable, null);
			List<MoldMisconfigure> molds = moldAccumulatedData.stream().map(MoldMisconfigureExtraData::getMoldMisconfigure).collect(Collectors.toList());
			page = new PageImpl<>(molds, pageable, moldMisconfigureRepository.count(predicate));
		} else if (properties[0].startsWith(SpecialSortProperty.preset)) {
			page =  moldMisconfigureRepository.findAllSortByPreset(predicate, pageable);
		} else if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldMisconfigure> moldMisconfigures = moldMisconfigureRepository.findAllOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(moldMisconfigures, pageable, moldMisconfigureRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldMisconfigure> moldMisconfigures = moldMisconfigureRepository.findAllOrderByStatus(predicate, pageable);
			page = new PageImpl<>(moldMisconfigures, pageable, moldMisconfigureRepository.count(predicate));
		} else page = moldMisconfigureRepository.findAll(predicate, pageable);
		loadAccumulatedShot(page.getContent().stream().map(MoldMisconfigure::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		loadWorkOrderDetail(page.getContent().stream().map(MoldMisconfigure::getMold).filter(Objects::nonNull).collect(Collectors.toList()));
		return page;
	}

	public void saveMoldMisconfigure(MoldPayload payload) {
		// MoldMisconfigure 기준 처리..
		MoldMisconfigure moldMisconfigure = moldMisconfigureRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));

		moldMisconfigure.setMisconfigureStatus(MisconfigureStatus.CONFIRMED);
		moldMisconfigure.setMessage(payload.getMessage());
		moldMisconfigure.setConfirmedAt(Instant.now());
		moldMisconfigure.setConfirmedBy(SecurityUtils.getName());

		moldMisconfigureRepository.save(moldMisconfigure);

	}

	public Page<LogDisconnection> getDisconnectHistoryByMoldId(Long moldId, NotificationStatus notificationStatus, Pageable pageable) {
		QLogDisconnection logDisconnection = QLogDisconnection.logDisconnection;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(logDisconnection.equipmentType.eq(EquipmentType.MOLD));
		if (moldId != null)
			builder.and(logDisconnection.equipmentId.eq(moldId));
		if (notificationStatus != null)
			builder.and(logDisconnection.notificationStatus.eq(notificationStatus));
		return logDisconnectionService.findLogDisconnection(builder, pageable);
	}

	@Transactional(propagation = Propagation.NEVER)
	public void procEfficiencyAndCycleTimeAlerts() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(MoldService.class, "ALERTS"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			Instant instant = DateUtils2.getInstant().minus(Duration.ofDays(1));
			String date = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);
			Double percentageThreshold = getPercentageThreshold();

			// Daily
			{
				StatMaps maps = new StatMaps();
				String period = date;
				populate(maps, period, PeriodType.DAILY);
				generateEfficiencyAlert(maps, date, PeriodType.DAILY);
				generateCycleTimeAlert(maps, PeriodType.DAILY, percentageThreshold);
			}

			// Weekly
			if (DateTimeUtils.todayIsMonday()) {
				StatMaps maps = new StatMaps();
				String period = DateUtils2.format(instant, DatePattern.YYYYww, Zone.SYS);
				populate(maps, period, PeriodType.WEEKLY);
				generateEfficiencyAlert(maps, date, PeriodType.WEEKLY);
				generateCycleTimeAlert(maps, PeriodType.WEEKLY, percentageThreshold);
			}

			// Montly
			if (DateTimeUtils.todayIsFirstDayOfMonth()) {
				StatMaps maps = new StatMaps();
				String period = DateUtils2.format(instant, DatePattern.yyyyMM, Zone.SYS);
				populate(maps, period, PeriodType.MONTHLY);
				generateEfficiencyAlert(maps, date, PeriodType.MONTHLY);
				generateCycleTimeAlert(maps, PeriodType.MONTHLY, percentageThreshold);
			}
		});
	}

	private Double getPercentageThreshold() {
		return 1d;
	}

	private static void populate(StatMaps maps, String period, PeriodType periodType) {
		LogicUtils.assertNotNull(maps, "maps");
		LogicUtils.assertNotNull(period, "period");
		LogicUtils.assertNotNull(periodType, "periodType");

		TranUtils.doNewTran(() -> {
			int i = 0;
			Long lastId = 0L;
			Pageable pageable = PageRequest.of(0, 1000, Direction.ASC, "id");
			do {
				List<Statistics> list = null;
				if (PeriodType.DAILY.equals(periodType)) {
					list = BeanUtils.get(StatisticsRepository.class).findAllByDayAndIdIsGreaterThan(period, lastId, pageable).getContent();
				} else if (PeriodType.WEEKLY.equals(periodType)) {
					list = BeanUtils.get(StatisticsRepository.class).findAllByWeekAndIdIsGreaterThan(period, lastId, pageable).getContent();
				} else if (PeriodType.MONTHLY.equals(periodType)) {
					list = BeanUtils.get(StatisticsRepository.class).findAllByMonthAndIdIsGreaterThan(period, lastId, pageable).getContent();
				}
				if (ObjectUtils.isEmpty(list)) {
					break;
				}
				lastId = list.get(list.size() - 1).getId();
				populate(maps, list);
			} while (i++ < 10000);
		});
	}

	private static class StatMaps {
		Map<Long, Long> uptimeMap = new HashMap<>();
		Map<Long, Double> cycleTimeMap = new HashMap<>();
		Map<Long, Integer> shotMap = new HashMap<>();
		Map<Long, Integer> fullShotMap = new HashMap<>();
		Map<Long, Integer> partProdMap = new HashMap<>();
	}

	private static void populate(StatMaps maps, List<Statistics> statList) {
		if (ObjectUtils.isEmpty(statList)) {
			return;
		}

		statList.forEach(stat -> {
			if (stat.getMoldId() == null) {
				return;
			}

			if (stat.getCt() > 0) {
				Double totalCt = stat.getCt() * stat.getShotCount();
				if (!maps.cycleTimeMap.containsKey(stat.getMoldId())) {
					maps.cycleTimeMap.put(stat.getMoldId(), totalCt);
				} else {
					maps.cycleTimeMap.put(stat.getMoldId(), maps.cycleTimeMap.get(stat.getMoldId()) + totalCt);
				}

				if (!maps.uptimeMap.containsKey(stat.getMoldId())) {
					maps.uptimeMap.put(stat.getMoldId(), stat.getUptimeSeconds());
				} else {
					maps.uptimeMap.put(stat.getMoldId(), maps.uptimeMap.get(stat.getMoldId()) + stat.getUptimeSeconds());
				}

				if (!maps.shotMap.containsKey(stat.getMoldId())) {
					maps.shotMap.put(stat.getMoldId(), stat.getShotCount());
				} else {
					maps.shotMap.put(stat.getMoldId(), maps.shotMap.get(stat.getMoldId()) + stat.getShotCount());
				}

				if (!maps.fullShotMap.containsKey(stat.getMoldId())) {
					maps.fullShotMap.put(stat.getMoldId(), stat.getShotCount());
				} else {
					maps.fullShotMap.put(stat.getMoldId(), maps.fullShotMap.get(stat.getMoldId()) + stat.getShotCount());
				}

				List<StatisticsPart> statPartList = BeanUtils.get(StatisticsPartRepository.class).findByStatisticsId(stat.getId());
				int producedQuantity = 0;
				for (StatisticsPart statisticsPart : statPartList) {
					if (stat.getShotCount() != null && statisticsPart.getCavity() != null) {
						producedQuantity += stat.getShotCount() * statisticsPart.getCavity();
					}
				}
				if (!maps.partProdMap.containsKey(stat.getMoldId())) {
					maps.partProdMap.put(stat.getMoldId(), producedQuantity);
				} else {
					maps.partProdMap.put(stat.getMoldId(), maps.partProdMap.get(stat.getMoldId()) + producedQuantity);
				}

			} else if (stat.getCt() == 0 && stat.getFirstData()) {
				if (!maps.fullShotMap.containsKey(stat.getMoldId())) {
					maps.fullShotMap.put(stat.getMoldId(), stat.getShotCount());
				} else {
					maps.fullShotMap.put(stat.getMoldId(), maps.fullShotMap.get(stat.getMoldId()) + stat.getShotCount());
				}
			}
		});
	}

	private void generateEfficiencyAlert(StatMaps maps, String date, PeriodType periodType) {
		if (ObjectUtils.isEmpty(maps.uptimeMap)) {
			return;
		}

		maps.uptimeMap.forEach((moldId, uptimeSeconds) -> {
			if (uptimeSeconds <= 0) {
				return;
			}

			TranUtils.doNewTran(() -> {
				Mold mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(moldId).orElse(null);
				if (mold == null || mold.isDeleted()) {
					return;
				}

				double baseEffciency = mold.getUptimeTarget() != null ? mold.getUptimeTarget() : 90; // default 90%
				if (baseEffciency == 0) {
					return;
				}

				Pair<EfficiencyStatus, Pair<Long, Double>> totalStatus = getEfficiencyStatus(mold, uptimeSeconds, date, periodType);
				EfficiencyStatus status = totalStatus.getFirst();

				/**
				 * Save Mold
				 */
				mold.setEfficiencyStatus(status);
				BeanUtils.get(MoldRepository.class).save(mold);

				/**
				 * Save MoldEfficiency
				 */
				if (status != EfficiencyStatus.OUTSIDE_L1 && status != EfficiencyStatus.OUTSIDE_L2) {
					return;
				}

				// 3-2. 사이클타임 Alert (기존에 Alert 정보가 있는 경우 UPDATE)
				List<MoldEfficiency> oldList = moldEfficiencyRepository.findByMoldIdAndNotificationStatusAndLatestAndPeriodType(mold.getId(), NotificationStatus.ALERT, true,
						periodType);
				if (!ObjectUtils.isEmpty(oldList)) {
					oldList.forEach(me -> me.setLatest(false));
					moldEfficiencyRepository.saveAll(oldList);
				}

				Long totalTimeSeconds = totalStatus.getSecond().getFirst();
				double efficiency = totalStatus.getSecond().getSecond();

				MoldEfficiency data = new MoldEfficiency();
				data.setMold(mold);
				data.setBaseEfficiency(baseEffciency);
				data.setEfficiencyLimit1(mold.getCycleTimeLimit1());
				data.setEfficiencyLimit2(mold.getCycleTimeLimit2());
				data.setCurrentSeconds(totalTimeSeconds);
				data.setUptimeSeconds(uptimeSeconds);
				data.setEfficiency(efficiency);
				data.setEfficiencyStatus(status);
				data.setNotificationStatus(NotificationStatus.ALERT);
				data.setNotificationAt(Instant.now());
				if (maps.fullShotMap.containsKey(moldId)) {
					data.setShots(maps.fullShotMap.get(moldId));
				}
				data.setLatest(true);
				data.setPeriodType(periodType);

				// [AlertEevent] 메세지 발송 (신규 등록시 마다)
//				if (shouldSendAlertMessage) {
//					AlertEvent event = new EfficiencyAlertEvent(data);
//					applicationEventPublisher.publishEvent(event);
//					log.info("[ALERT EVENT] MoldEfficiency Alert!");
//				}
				BeanUtils.get(MoldEfficiencyRepository.class).save(data);

				//notification
//				notificationService.createAlertNotification(data, AlertType.EFFICIENCY);
			});
		});
	}

	private void generateCycleTimeAlert(StatMaps maps, PeriodType periodType, Double percentageThreshold) {
		if (ObjectUtils.isEmpty(maps.cycleTimeMap)) {
			return;
		}

		maps.cycleTimeMap.forEach((moldId, cycleTime) -> {
			if (maps.shotMap.get(moldId) != null && maps.shotMap.get(moldId) > 0)
				maps.cycleTimeMap.put(moldId, cycleTime / maps.shotMap.get(moldId));
		});

		maps.cycleTimeMap.forEach((moldId, cycleTime) -> {
			if (cycleTime <= 0) {
				return;
			}

			TranUtils.doNewTran(() -> {
				Mold mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(moldId).orElse(null);
				if (mold == null || mold.isDeleted()) {
					return;
				}

				OptimalCycleTime oct = MoldUtils.getOptimalCycleTime(mold.getId(), mold.getContractedCycleTimeToCalculation(), null);
				double baseCycleTime = oct.getValue(); // Both baseCycleTime & weight (total cycle time) is in 100ms
				int limit1 = mold.getCycleTimeLimit1();
				int limit2 = mold.getCycleTimeLimit2();
				double cycleTimeL1 = mold.getCycleTimeLimit1Unit() == null || mold.getCycleTimeLimit1Unit().equals(OutsideUnit.PERCENTAGE) ? baseCycleTime * limit1 * 0.01
						: (double) mold.getCycleTimeLimit1() * 10;
				double cycleTimeL2 = mold.getCycleTimeLimit2Unit() == null || mold.getCycleTimeLimit2Unit().equals(OutsideUnit.PERCENTAGE) ? baseCycleTime * limit2 * 0.01
						: (double) mold.getCycleTimeLimit2() * 10;

				// 3-1. 기준 범위  ===== -L2 === -L1 === base === +L1 === +L2 =====
				double minusL2 = baseCycleTime - cycleTimeL2;
				double minusL1 = baseCycleTime - cycleTimeL1;
				double plusL1 = baseCycleTime + cycleTimeL1;
				double plusL2 = baseCycleTime + cycleTimeL2;

				CycleTimeStatus status = CycleTimeStatus.WITHIN_TOLERANCE;
				if (cycleTime <= minusL2 || plusL2 <= cycleTime) {
					status = CycleTimeStatus.OUTSIDE_L2;
				} else if ((minusL2 < cycleTime && cycleTime <= minusL1) || (plusL1 <= cycleTime && cycleTime < plusL2)) {
					status = CycleTimeStatus.OUTSIDE_L1;
				}

				/**
				 * Save Mold
				 */
				mold.setCycleTimeStatus(status);
				if (mold.getDailyMaxCapacity() == null) {
					setMoldDailyMaxCapacity(mold);
				}
				BeanUtils.get(MoldRepository.class).save(mold);

				/**
				 * Save MoldCycleTime
				 */
				if (status != CycleTimeStatus.OUTSIDE_L1 && status != CycleTimeStatus.OUTSIDE_L2) {
					return;
				}

				int maxCapacity = 0;
				{
					int productionDays = StringUtils.isEmpty(mold.getProductionDays()) ? 7 : Integer.parseInt(mold.getProductionDays());
					if (PeriodType.DAILY.equals(periodType)) {
						maxCapacity = mold.getDailyMaxCapacity();
					} else if (PeriodType.WEEKLY.equals(periodType)) {
						maxCapacity = mold.getDailyMaxCapacity() * productionDays;
					} else if (PeriodType.MONTHLY.equals(periodType)) {
						maxCapacity = mold.getDailyMaxCapacity() * productionDays * 4;
					}
				}
				// percentage of the daily maximum capacity
				double percentageOfDailyMaximumCapacity = 100.0 * maps.partProdMap.get(moldId) / maxCapacity;
				if (percentageOfDailyMaximumCapacity < percentageThreshold) {
					return;
				}
				if (maps.fullShotMap.containsKey(moldId) && maps.fullShotMap.get(moldId) < 10) {
					try {
						String header = String.format("generateCycleTimeAlert mold %s %s", moldId, mold.getEquipmentCode());
						log.info(header);
						StringBuffer content = new StringBuffer();
						content.append(String.format("generateCycleTimeAlert shot: %s\n", maps.fullShotMap.get(moldId)));
						content.append(String.format("type: %s\n", periodType));
						content.append(String.format("partsProducedMap.get(moldId) %s\n", maps.partProdMap.get(moldId)));
						content.append(String.format("maxCapacity %s\n", maxCapacity));
						content.append(String.format("percentageOfDailyMaximumCapacity %s\n", percentageOfDailyMaximumCapacity));
						content.append(String.format("percentageThreshold %s\n", percentageThreshold));
						log.info(content.toString());
						doLogService.doLog(header, content.toString());
					} catch (Exception e) {
						LogUtils.saveErrorQuietly(ErrorType.LOGIC, "LOGGING_FAIL", HttpStatus.EXPECTATION_FAILED, "Logging Failed at CycleTime Alert", e);
					}
				}

				List<MoldCycleTime> oldList = moldCycleTimeRepository.findByMoldIdAndNotificationStatusAndLatestAndPeriodType(mold.getId(), NotificationStatus.ALERT, true,
						periodType);
				if (oldList != null && oldList.size() > 0) {
					oldList.forEach(mct -> mct.setLatest(false));
					moldCycleTimeRepository.saveAll(oldList);
				}

				MoldCycleTime data = new MoldCycleTime();
				data.setMold(mold);
				data.setContractedCycleTime(((Double) baseCycleTime).intValue());
				data.setCycleTime(cycleTime);
				data.setCycleTimeLimit1(mold.getCycleTimeLimit1());
				data.setCycleTimeLimit2(mold.getCycleTimeLimit2());
				data.setCycleTimeStatus(status);
				data.setNotificationStatus(NotificationStatus.ALERT);
				data.setNotificationAt(Instant.now());
				if (maps.fullShotMap.containsKey(moldId))
					data.setShots(maps.fullShotMap.get(moldId));
				data.setLatest(true);
				data.setPeriodType(periodType);
				moldCycleTimeRepository.save(data);

				//notification
//				notificationService.createAlertNotification(data, AlertType.CYCLE_TIME);

				// [AlertEevent] 메세지 발송 (신규 등록 시 마다)
//				if (shouldSendAlertMessage) {
//					AlertEvent event = new CycleTimeAlertEvent(data);
//					applicationEventPublisher.publishEvent(event);
//					log.info("[ALERT EVENT] Cycle time!");
//				}
			});
		});
	}

	public static Pair<EfficiencyStatus, Pair<Long, Double>> getEfficiencyStatus(Mold mold, Long uptimeSeconds, String date, PeriodType type) {
		double baseEffciency = mold.getUptimeTarget() != null ? mold.getUptimeTarget() : 90; // default 90%

//		if (baseEffciency == 0) return EfficiencyStatus.WITHIN_TOLERANCE;

		Long totalTimeSeconds = 86400L;
		if (type.equals(PeriodType.WEEKLY)) {
			totalTimeSeconds = 7 * 86400L;
		} else if (type.equals(PeriodType.MONTHLY)) {
			totalTimeSeconds = DateUtils.getLengthOfMonth(date, "yyyyMMdd") * 86400L;
		}
		double efficiency = ((double) uptimeSeconds / (double) totalTimeSeconds) * 100;

		int limit1 = mold.getUptimeLimitL1() != null ? mold.getUptimeLimitL1() : 5; // default 5%
		int limit2 = mold.getUptimeLimitL2() != null ? mold.getUptimeLimitL2() : 10; // default 10%

		double cycleTimeL1 = baseEffciency * limit1 * 0.01;
		double cycleTimeL2 = baseEffciency * limit2 * 0.01;

		// 4. 기준 범위  ===== -L2 === -L1 === base === +L1 === +L2 =====
		double minusL2 = baseEffciency - cycleTimeL2;
		double minusL1 = baseEffciency - cycleTimeL1;
		double plusL1 = baseEffciency + cycleTimeL1;
		double plusL2 = baseEffciency + cycleTimeL2;

		EfficiencyStatus efficiencyStatus = EfficiencyStatus.WITHIN_TOLERANCE;
		if (efficiency <= minusL2 || plusL2 <= efficiency) {
			efficiencyStatus = EfficiencyStatus.OUTSIDE_L2;
		} else if ((minusL2 < efficiency && efficiency <= minusL1) || (plusL1 <= efficiency && efficiency < plusL2)) {
			efficiencyStatus = EfficiencyStatus.OUTSIDE_L1;
		}

		return Pair.of(efficiencyStatus, Pair.of(totalTimeSeconds, efficiency));
	}

	@Transactional(propagation = Propagation.NEVER)
	public void procAlertsToUsers() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(MoldService.class, "USER_ALERTS"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			List<UserAlert> userAlertList = userAlertRepository.findAll();

			Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

			// Daily
			TranUtils.doNewTran(() -> {
				List<LogUserAlert> logUserAlertList = new ArrayList<>();
/*
				List<MoldLocation> moldLocationDailyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfYesterday(),
						DateTimeUtils.getStartOfToday(), MoldLocationStatus.CHANGED, true, sort);
*/
				List<MoldLocation> moldLocationDailyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfYesterday(),
						DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
				List<MoldCycleTime> moldCycleTimeDailyList = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(),
						DateTimeUtils.getStartOfTomorrow(), true, PeriodType.DAILY, sort);
				List<MoldMaintenance> moldMaintenanceDailyList = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(
						DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
				List<MoldEfficiency> moldEfficiencyDailyList = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(),
						DateTimeUtils.getStartOfTomorrow(), true, PeriodType.DAILY, sort);
//				List<MoldDataSubmission> moldDataSubmissionDailyList = moldDataSubmissionRepository.findAllByCreatedAtBetweenAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), true, sort);
				List<MoldDowntimeEvent> moldDowntimeDailyList = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(
						DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
				List<UserAlert> userAlertDailyList = userAlertList.stream().filter(x -> x.getPeriodType() != null && x.getPeriodType().equals(PeriodType.DAILY))
						.collect(Collectors.toList());
				Map<User, List<AlertType>> userAlertDailyMap = mailService.getUserAlertListMap(userAlertDailyList);
				logUserAlertList.addAll(buildLogUserAlert(userAlertDailyMap, moldLocationDailyList, null, null, moldCycleTimeDailyList, moldMaintenanceDailyList, null,
						moldEfficiencyDailyList, null, null, true));
				logUserAlertList.addAll(generateLogUserDowntimeAlert(userAlertDailyMap, moldDowntimeDailyList, true));
				logUserAlertRepository.saveAll(logUserAlertList);
			});

			// Weekly
			if (DateTimeUtils.todayIsMonday()) {
				TranUtils.doNewTran(() -> {
					List<LogUserAlert> logUserAlertList = new ArrayList<>();
/*
					List<MoldLocation> moldLocationWeeklyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(),
							DateTimeUtils.getStartOfToday(), MoldLocationStatus.CHANGED, true, sort);
*/
					List<MoldLocation> moldLocationWeeklyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(),
							DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
					List<MoldCycleTime> moldCycleTimeWeeklyList = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(),
							DateTimeUtils.getStartOfTomorrow(), true, PeriodType.WEEKLY, sort);
					List<MoldMaintenance> moldMaintenanceWeeklyList = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(
							DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
					List<MoldEfficiency> moldEfficiencyWeeklyList = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(),
							DateTimeUtils.getStartOfTomorrow(), true, PeriodType.WEEKLY, sort);
//					List<MoldDataSubmission> moldDataSubmissionWeeklyList = moldDataSubmissionRepository.findAllByCreatedAtBetweenAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), true, sort);
					List<MoldDowntimeEvent> moldDowntimeWeeklyList = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(
							DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
					List<UserAlert> userAlertWeeklyList = userAlertList.stream().filter(x -> x.getPeriodType() != null && x.getPeriodType().equals(PeriodType.WEEKLY))
							.collect(Collectors.toList());
					Map<User, List<AlertType>> userAlertWeeklyMap = mailService.getUserAlertListMap(userAlertWeeklyList);
					logUserAlertList.addAll(buildLogUserAlert(userAlertWeeklyMap, moldLocationWeeklyList, null, null, moldCycleTimeWeeklyList, moldMaintenanceWeeklyList, null,
							moldEfficiencyWeeklyList, null, null, true));
					logUserAlertList.addAll(generateLogUserDowntimeAlert(userAlertWeeklyMap, moldDowntimeWeeklyList, true));
					logUserAlertRepository.saveAll(logUserAlertList);
				});
			}

			// Monthly
			if (DateTimeUtils.todayIsFirstDayOfMonth()) {
				TranUtils.doNewTran(() -> {
					List<LogUserAlert> logUserAlertList = new ArrayList<>();
					List<MoldLocation> moldLocationMonthlyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(),
							DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
					List<MoldCycleTime> moldCycleTimeMonthlyList = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(),
							DateTimeUtils.getStartOfTomorrow(), true, PeriodType.MONTHLY, sort);
					List<MoldMaintenance> moldMaintenanceMonthlyList = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(
							DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
					List<MoldEfficiency> moldEfficiencyMonthlyList = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(),
							DateTimeUtils.getStartOfTomorrow(), true, PeriodType.MONTHLY, sort);
//					List<MoldDataSubmission> moldDataSubmissionMonthlyList = moldDataSubmissionRepository.findAllByCreatedAtBetweenAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), true, sort);
					List<MoldDowntimeEvent> moldDowntimeMonthlyList = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(
							DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
					List<UserAlert> userAlertMonthlyList = userAlertList.stream().filter(x -> x.getPeriodType() != null && x.getPeriodType().equals(PeriodType.MONTHLY))
							.collect(Collectors.toList());
					Map<User, List<AlertType>> userAlertMonthlyMap = mailService.getUserAlertListMap(userAlertMonthlyList);
					logUserAlertList.addAll(buildLogUserAlert(userAlertMonthlyMap, moldLocationMonthlyList, null, null, moldCycleTimeMonthlyList, moldMaintenanceMonthlyList, null,
							moldEfficiencyMonthlyList, null, null, true));
					logUserAlertList.addAll(generateLogUserDowntimeAlert(userAlertMonthlyMap, moldDowntimeMonthlyList, true));
					logUserAlertRepository.saveAll(logUserAlertList);
				});
			}
		});
	}

	/*
		public List<LogUserAlert> generateLogUserAlertOld(Map<User, List<AlertType>> userAlertMap, List<MoldLocation> moldLocationList, List<MoldDisconnect> moldDisconnectList,
													   List<TerminalDisconnect> terminalDisconnectList, List<MoldCycleTime> moldCycleTimeList, List<MoldMaintenance> moldMaintenanceList,
													   List<MoldMisconfigure> moldMisconfigureList, List<MoldEfficiency> moldEfficiencyList, List<MoldDataSubmission> moldDataSubmissionList,
													   List<MoldCorrective> moldCorrectiveList){
			List<LogUserAlert> logUserAlertList = new ArrayList<>();
			userAlertMap.forEach((user, alertTypes) -> {
				if(alertTypes.contains(AlertType.RELOCATION) && moldLocationList != null && moldLocationList.size() > 0){
					List<MoldLocation> moldLocationSubList = new ArrayList<>();
					if(user.isAdmin()) {
						moldLocationSubList = moldLocationList;
					}else{
	//					if(serverName.equalsIgnoreCase("dyson")){
	//						List<MoldLocation> finalMoldLocationSubList = moldLocationSubList;
	//						moldLocationList.forEach(moldLocation -> {
	//							List<User> supplierList = subTierService.getTierSuppliers(moldLocation.getMold());
	//							if(supplierList.contains(user))
	//								finalMoldLocationSubList.add(moldLocation);
	//						});
	//						moldLocationSubList.addAll(finalMoldLocationSubList);
	//					}else {
						moldLocationSubList = moldLocationList.stream().filter(x -> x.getMold().getCompanyId() != null
								&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
	//					}
					}
					moldLocationSubList.forEach(moldLocation -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.RELOCATION, moldLocation.getId(), false));
					});
				}
				if(alertTypes.contains(AlertType.DISCONNECTED) || alertTypes.contains(AlertType.TERMINAL_DISCONNECTED)) {
					if (moldDisconnectList != null && moldDisconnectList.size() > 0) {
						List<MoldDisconnect> moldDisconnectSubList = new ArrayList<>();
						if(user.isAdmin()){
							moldDisconnectSubList = moldDisconnectList;
						}else{
	//						if(serverName.equalsIgnoreCase("dyson")){
	//							List<MoldDisconnect> finalMoldDisconnectSubList = moldDisconnectSubList;
	//							moldDisconnectList.forEach(moldDisconnect -> {
	//								List<User> supplierList = subTierService.getTierSuppliers(moldDisconnect.getMold());
	//								if(supplierList.contains(user))
	//									finalMoldDisconnectSubList.add(moldDisconnect);
	//							});
	//							moldDisconnectSubList.addAll(finalMoldDisconnectSubList);
	//						}else {
							moldDisconnectSubList = moldDisconnectList.stream().filter(x -> x.getMold().getCompanyId() != null
									&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
	//						}
						}
						moldDisconnectSubList.forEach(moldDisconnect -> {
							logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DISCONNECTED, moldDisconnect.getId(), false));
						});
					}
					if (terminalDisconnectList != null && terminalDisconnectList.size() > 0) {
						List<TerminalDisconnect> terminalDisconnectSubList;
						if(user.isAdmin()){
							terminalDisconnectSubList = terminalDisconnectList;
						}else{
							terminalDisconnectSubList = terminalDisconnectList.stream().filter(x -> x.getTerminal().getCompanyId() != null
									&& x.getTerminal().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
						}
						terminalDisconnectSubList.forEach(terminalDisconnect -> {
							logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.TERMINAL_DISCONNECTED, terminalDisconnect.getId(), false));
						});
					}
				}
				if(alertTypes.contains(AlertType.CYCLE_TIME) && moldCycleTimeList != null && moldCycleTimeList.size() > 0){
					List<MoldCycleTime> moldCycleTimeSubList = new ArrayList<>();
					if(user.isAdmin()){
						moldCycleTimeSubList = moldCycleTimeList;
					}else{
	//					if(serverName.equalsIgnoreCase("dyson")){
	//						List<MoldCycleTime> finalMoldCycleTimeSubList = moldCycleTimeSubList;
	//						moldCycleTimeList.forEach(moldCycleTime -> {
	//							List<User> supplierList = subTierService.getTierSuppliers(moldCycleTime.getMold());
	//							if(supplierList.contains(user))
	//								finalMoldCycleTimeSubList.add(moldCycleTime);
	//						});
	//						moldCycleTimeSubList.addAll(finalMoldCycleTimeSubList);
	//					}else {
						moldCycleTimeSubList = moldCycleTimeList.stream().filter(x -> x.getMold().getCompanyId() != null
								&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
	//					}
					}
					moldCycleTimeSubList.forEach(moldCycleTime -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.CYCLE_TIME, moldCycleTime.getId(), false));
					});
				}
				if(alertTypes.contains(AlertType.MAINTENANCE) && moldMaintenanceList != null && moldMaintenanceList.size() > 0){
					List<MoldMaintenance> moldMaintenanceSubList = new ArrayList<>();
					if(user.isAdmin()){
						moldMaintenanceSubList = moldMaintenanceList;
					}else{
	//					if(serverName.equalsIgnoreCase("dyson")){
	//						List<MoldMaintenance> finalMoldMaintenanceList = moldMaintenanceSubList;
	//						moldMaintenanceList.forEach(moldMaintenance -> {
	//							List<User> supplierList = subTierService.getTierSuppliers(moldMaintenance.getMold());
	//							if(supplierList.contains(user))
	//								finalMoldMaintenanceList.add(moldMaintenance);
	//						});
	//						moldMaintenanceSubList.addAll(finalMoldMaintenanceList);
	//					}else {
						moldMaintenanceSubList = moldMaintenanceList.stream().filter(x -> x.getMold().getCompanyId() != null
								&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
	//					}
					}
					moldMaintenanceSubList.forEach(moldMaintenance -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.MAINTENANCE, moldMaintenance.getId(), false));
					});
				}
				if(alertTypes.contains(AlertType.CORRECTIVE_MAINTENANCE) && moldCorrectiveList != null && moldCorrectiveList.size() > 0){
					List<MoldCorrective> moldCorrectiveSubList = new ArrayList<>();
	*/
	/*
					if(user.isAdmin()){
						moldCorrectiveSubList = moldCorrectiveList;
					}else{
						if(serverName.equalsIgnoreCase("dyson")){
							List<MoldCorrective> finalMoldCorrectiveSubList = moldCorrectiveSubList;
							moldCorrectiveList.forEach(cm -> {
								List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
								if(supplierList.contains(user))
									finalMoldCorrectiveSubList.add(cm);
							});
							moldCorrectiveSubList.addAll(finalMoldCorrectiveSubList);
						}else {
							moldCorrectiveSubList = moldCorrectiveList.stream().filter(x -> x.getMold().getCompanyId() != null
									&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
						}
					}
	*//*

					if (serverName.equalsIgnoreCase("dyson")) {
						List<MoldCorrective> finalMoldCorrectiveSubList = moldCorrectiveSubList;
						moldCorrectiveList.forEach(cm -> {
							List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
							if (supplierList.contains(user) || cm.getMold() != null && cm.getMold().getEngineers() != null && cm.getMold().getEngineers().contains(user))
								finalMoldCorrectiveSubList.add(cm);
						});
						moldCorrectiveSubList.addAll(finalMoldCorrectiveSubList);
					} else {
						if (user.isAdmin()) {
							moldCorrectiveSubList = moldCorrectiveList;
						} else {
							moldCorrectiveSubList = moldCorrectiveList.stream().filter(x -> {
								List<User> supplierList = subTierService.getTierSuppliers(x.getMold());

								return x.getMold().getCompanyId() != null
										&& x.getMold().getCompanyId().equals(user.getCompanyId()) || supplierList.contains(user);
							}).collect(Collectors.toList());
						}
					}
					moldCorrectiveSubList.forEach(moldCorrective -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.CORRECTIVE_MAINTENANCE, moldCorrective.getId(), false));
					});
				}
				if(alertTypes.contains(AlertType.MISCONFIGURE) && moldMisconfigureList != null && moldMisconfigureList.size() > 0){
					List<MoldMisconfigure> moldMisconfigureSubList = new ArrayList<>();
					if(user.isAdmin()){
						moldMisconfigureSubList = moldMisconfigureList;
					}else{
		//					if(serverName.equalsIgnoreCase("dyson")){
		//						List<MoldMisconfigure> finalMoldMisconfigureSubList = moldMisconfigureSubList;
		//						moldMisconfigureList.forEach(moldMisconfigure -> {
		//							List<User> supplierList = subTierService.getTierSuppliers(moldMisconfigure.getMold());
		//							if(supplierList.contains(user))
		//								finalMoldMisconfigureSubList.add(moldMisconfigure);
		//						});
		//						moldMisconfigureSubList.addAll(finalMoldMisconfigureSubList);
		//					}else {
						moldMisconfigureSubList = moldMisconfigureList.stream().filter(x -> x.getMold().getCompanyId() != null
								&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
		//					}
					}
					moldMisconfigureSubList.forEach(moldMisconfigure -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.MISCONFIGURE, moldMisconfigure.getId(), false));
					});
				}
				if(alertTypes.contains(AlertType.EFFICIENCY) && moldEfficiencyList != null && moldEfficiencyList.size() > 0){
					List<MoldEfficiency> moldEfficiencySubList = new ArrayList<>();
					if(user.isAdmin()){
						moldEfficiencySubList = moldEfficiencyList;
					}else{
		//					if(serverName.equalsIgnoreCase("dyson")){
		//						List<MoldEfficiency> finalMoldEfficiencySubList = moldEfficiencySubList;
		//						moldEfficiencyList.forEach(moldEfficiency -> {
		//							List<User> supplierList = subTierService.getTierSuppliers(moldEfficiency.getMold());
		//							if(supplierList.contains(user))
		//								finalMoldEfficiencySubList.add(moldEfficiency);
		//						});
		//						moldEfficiencySubList.addAll(finalMoldEfficiencySubList);
		//					}else {
						moldEfficiencySubList = moldEfficiencyList.stream().filter(x -> x.getMold().getCompanyId() != null
								&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
		//					}
					}
					moldEfficiencySubList.forEach(moldEfficiency -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.EFFICIENCY, moldEfficiency.getId(), false));
					});
				}
				if(alertTypes.contains(AlertType.DATA_SUBMISSION) && moldDataSubmissionList != null && moldDataSubmissionList.size() > 0){
					List<MoldDataSubmission> moldDataSubmissionSubList = moldDataSubmissionList.stream()
							.filter(x -> x.getNotificationStatus().equals(NotificationStatus.PENDING)).collect(Collectors.toList());
					moldDataSubmissionSubList.forEach(moldDataSubmission -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DATA_SUBMISSION, moldDataSubmission.getId(), false));
					});

					moldDataSubmissionSubList = moldDataSubmissionList.stream()
							.filter(x -> !x.getNotificationStatus().equals(NotificationStatus.PENDING)
									&& x.getMold().getCompanyId() != null
									&& (x.getMold().getCompanyId().equals(user.getCompanyId())
									|| (user.getCompany().getName().equals("FLEXTRONICS") && Arrays.asList("MCTRONIC", "Skreen Fabric").contains(x.getMold().getCompanyName()))
									|| (user.getCompany().getName().equals("SKP") && Arrays.asList("MyTech", "CGH", "TF", "GOLDPAR", "GOODHART", "SPI", "SKP BP").contains(x.getMold().getCompanyName()))))
							.collect(Collectors.toList());
					moldDataSubmissionSubList.forEach(moldDataSubmission -> {
						if(!user.isAdmin()){
							logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DATA_SUBMISSION, moldDataSubmission.getId(), false));
						}
					});
				}
			});
			return logUserAlertList;
		}

		*/
	public List<LogUserAlert> generateLogUserAlert(Map<User, List<AlertType>> userAlertMap, List<MoldLocation> moldLocationList, List<MoldDisconnect> moldDisconnectList,
			List<TerminalDisconnect> terminalDisconnectList, List<MoldCycleTime> moldCycleTimeList, List<MoldMaintenance> moldMaintenanceList,
			List<MoldMisconfigure> moldMisconfigureList, List<MoldEfficiency> moldEfficiencyList, List<MoldDataSubmission> moldDataSubmissionList,
			List<MoldCorrective> moldCorrectiveList) {
		return buildLogUserAlert(userAlertMap, moldLocationList, moldDisconnectList, terminalDisconnectList, moldCycleTimeList, moldMaintenanceList, moldMisconfigureList,
				moldEfficiencyList, moldDataSubmissionList, moldCorrectiveList, false);
	}

	private boolean checkAlertOn(UserAlert userAlert) {
		if (userAlert != null) {
			if (userAlert.getAlertOn())
				return true;
			else
				return false;
		} else
			return true;
	}

	public List<LogUserAlert> buildLogUserAlert(Map<User, List<AlertType>> userAlertMap, List<MoldLocation> moldLocationList, List<MoldDisconnect> moldDisconnectList,
			List<TerminalDisconnect> terminalDisconnectList, List<MoldCycleTime> moldCycleTimeList, List<MoldMaintenance> moldMaintenanceList,
			List<MoldMisconfigure> moldMisconfigureList, List<MoldEfficiency> moldEfficiencyList, List<MoldDataSubmission> moldDataSubmissionList,
			List<MoldCorrective> moldCorrectiveList, boolean sendNoti) {
		List<LogUserAlert> logUserAlertList = new ArrayList<>();
		userAlertMap.forEach((user, alertTypes) -> {
			final List<Long> accessCompanyIds = new ArrayList<>();
			final List<Long> accessMoldIds = new ArrayList<>();
			if (!user.isAdmin() || (moldDataSubmissionList != null && moldDataSubmissionList.stream().anyMatch(x -> !x.getNotificationStatus().equals(NotificationStatus.PENDING)))
					|| (moldCorrectiveList != null && !moldCorrectiveList.isEmpty() && serverName.equalsIgnoreCase("dyson"))) {
				loadTreeCompanyForPayLoad(user.getCompanyId(), accessCompanyIds, accessMoldIds);
			}

			if (alertTypes.contains(AlertType.RELOCATION) && moldLocationList != null && moldLocationList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.RELOCATION).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldLocation> moldLocationSubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldLocationSubList = moldLocationList;
				} else {
//					if(serverName.equalsIgnoreCase("dyson")){
//						List<MoldLocation> finalMoldLocationSubList = moldLocationSubList;
//						moldLocationList.forEach(moldLocation -> {
//							List<User> supplierList = subTierService.getTierSuppliers(moldLocation.getMold());
//							if(supplierList.contains(user))
//								finalMoldLocationSubList.add(moldLocation);
//						});
//						moldLocationSubList.addAll(finalMoldLocationSubList);
//					}else {
					/*
											moldLocationSubList = moldLocationList.stream().filter(x -> x.getMold().getCompanyId() != null
													&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
					*/
					moldLocationSubList = moldLocationList.stream().filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
							|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()))).collect(Collectors.toList());
//					}
				}
				moldLocationSubList.forEach(moldLocation -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.RELOCATION, moldLocation.getId(), false));
				});
				if (sendNoti && alertOn)
					broadcastNotificationService.notificationMoldLocation(moldLocationSubList, user);

			}
			if (alertTypes.contains(AlertType.DISCONNECTED) || alertTypes.contains(AlertType.TERMINAL_DISCONNECTED)) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.DISCONNECTED)
						.orElse(userAlertRepository.findByUserAndAlertType(user, AlertType.TERMINAL_DISCONNECTED).orElse(null));
				boolean alertOn = checkAlertOn(userAlert);
				if (moldDisconnectList != null && moldDisconnectList.size() > 0) {
					List<MoldDisconnect> moldDisconnectSubList = new ArrayList<>();
					if (user.isAdmin()) {
						moldDisconnectSubList = moldDisconnectList;
					} else {
//						if(serverName.equalsIgnoreCase("dyson")){
//							List<MoldDisconnect> finalMoldDisconnectSubList = moldDisconnectSubList;
//							moldDisconnectList.forEach(moldDisconnect -> {
//								List<User> supplierList = subTierService.getTierSuppliers(moldDisconnect.getMold());
//								if(supplierList.contains(user))
//									finalMoldDisconnectSubList.add(moldDisconnect);
//							});
//							moldDisconnectSubList.addAll(finalMoldDisconnectSubList);
//						}else {
						/*
													moldDisconnectSubList = moldDisconnectList.stream().filter(x -> x.getMold().getCompanyId() != null
															&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
						*/
						moldDisconnectSubList = moldDisconnectList.stream()
								.filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
										|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId())))
								.collect(Collectors.toList());
//						}
					}
					moldDisconnectSubList.forEach(moldDisconnect -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DISCONNECTED, moldDisconnect.getId(), false));
					});

					if (sendNoti && alertOn)
						broadcastNotificationService.notificationMoldDisconnect(moldDisconnectSubList, user);

				}
				if (terminalDisconnectList != null && terminalDisconnectList.size() > 0) {
					List<TerminalDisconnect> terminalDisconnectSubList;
					if (user.isAdmin()) {
						terminalDisconnectSubList = terminalDisconnectList;
					} else {
						/*
												terminalDisconnectSubList = terminalDisconnectList.stream().filter(x -> x.getTerminal().getCompanyId() != null
														&& x.getTerminal().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
						*/
						terminalDisconnectSubList = terminalDisconnectList.stream()
								.filter(x -> x.getTerminal().getCompanyId() != null
										&& (x.getTerminal().getCompanyId().equals(user.getCompanyId()) || accessCompanyIds.contains(x.getTerminal().getCompanyId())))
								.collect(Collectors.toList());
					}
					terminalDisconnectSubList.forEach(terminalDisconnect -> {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.TERMINAL_DISCONNECTED, terminalDisconnect.getId(), false));
					});
					if (sendNoti && alertOn)
						broadcastNotificationService.notificationTerminalDisconnect(terminalDisconnectSubList, user);

				}
			}
			if (alertTypes.contains(AlertType.CYCLE_TIME) && moldCycleTimeList != null && moldCycleTimeList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.CYCLE_TIME).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldCycleTime> moldCycleTimeSubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldCycleTimeSubList = moldCycleTimeList;
				} else {
//					if(serverName.equalsIgnoreCase("dyson")){
//						List<MoldCycleTime> finalMoldCycleTimeSubList = moldCycleTimeSubList;
//						moldCycleTimeList.forEach(moldCycleTime -> {
//							List<User> supplierList = subTierService.getTierSuppliers(moldCycleTime.getMold());
//							if(supplierList.contains(user))
//								finalMoldCycleTimeSubList.add(moldCycleTime);
//						});
//						moldCycleTimeSubList.addAll(finalMoldCycleTimeSubList);
//					}else {
					/*
											moldCycleTimeSubList = moldCycleTimeList.stream().filter(x -> x.getMold().getCompanyId() != null
													&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
					*/
					moldCycleTimeSubList = moldCycleTimeList.stream().filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
							|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()))).collect(Collectors.toList());
//					}
				}
				moldCycleTimeSubList.forEach(moldCycleTime -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.CYCLE_TIME, moldCycleTime.getId(), false));
				});
				if (sendNoti && alertOn)
					broadcastNotificationService.notificationMoldCycleTime(moldCycleTimeSubList, user);

			}
			if (alertTypes.contains(AlertType.MAINTENANCE) && moldMaintenanceList != null && moldMaintenanceList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.MAINTENANCE).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldMaintenance> moldMaintenanceSubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldMaintenanceSubList = moldMaintenanceList;
				} else {
//					if(serverName.equalsIgnoreCase("dyson")){
//						List<MoldMaintenance> finalMoldMaintenanceList = moldMaintenanceSubList;
//						moldMaintenanceList.forEach(moldMaintenance -> {
//							List<User> supplierList = subTierService.getTierSuppliers(moldMaintenance.getMold());
//							if(supplierList.contains(user))
//								finalMoldMaintenanceList.add(moldMaintenance);
//						});
//						moldMaintenanceSubList.addAll(finalMoldMaintenanceList);
//					}else {
					/*
											moldMaintenanceSubList = moldMaintenanceList.stream().filter(x -> x.getMold().getCompanyId() != null
													&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
					*/
					moldMaintenanceSubList = moldMaintenanceList.stream().filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
							|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()))).collect(Collectors.toList());
//					}
				}
				moldMaintenanceSubList.forEach(moldMaintenance -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.MAINTENANCE, moldMaintenance.getId(), false));
				});
				if (sendNoti && alertOn)
					broadcastNotificationService.notificationMoldMaintenance(moldMaintenanceSubList, user);

			}
			if (alertTypes.contains(AlertType.CORRECTIVE_MAINTENANCE) && moldCorrectiveList != null && moldCorrectiveList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.CORRECTIVE_MAINTENANCE).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldCorrective> moldCorrectiveSubList = new ArrayList<>();
				/*
								if(user.isAdmin()){
									moldCorrectiveSubList = moldCorrectiveList;
								}else{
									if(serverName.equalsIgnoreCase("dyson")){
										List<MoldCorrective> finalMoldCorrectiveSubList = moldCorrectiveSubList;
										moldCorrectiveList.forEach(cm -> {
											List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
											if(supplierList.contains(user))
												finalMoldCorrectiveSubList.add(cm);
										});
										moldCorrectiveSubList.addAll(finalMoldCorrectiveSubList);
									}else {
										moldCorrectiveSubList = moldCorrectiveList.stream().filter(x -> x.getMold().getCompanyId() != null
												&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
									}
								}
				*/
				if (serverName.equalsIgnoreCase("dyson")) {
					List<MoldCorrective> finalMoldCorrectiveSubList = moldCorrectiveSubList;
					/*
										moldCorrectiveList.forEach(cm -> {
											List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
											if (supplierList.contains(user) || cm.getMold() != null && cm.getMold().getEngineers() != null && cm.getMold().getEngineers().contains(user))
												finalMoldCorrectiveSubList.add(cm);
										});
					*/
					moldCorrectiveList.forEach(cm -> {
//						List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
						if ((cm.getMold().getCompanyId().equals(user.getCompanyId()) || accessCompanyIds.contains(cm.getMold().getCompanyId())
								|| accessMoldIds.contains(cm.getMold().getId()))
								|| cm.getMold() != null && cm.getMold().getEngineersInCharge() != null && cm.getMold().getEngineersInCharge().contains(user))
							finalMoldCorrectiveSubList.add(cm);
					});

					moldCorrectiveSubList.addAll(finalMoldCorrectiveSubList);
				} else {
					if (user.isAdmin()) {
						moldCorrectiveSubList = moldCorrectiveList;
					} else {
						moldCorrectiveSubList = moldCorrectiveList.stream().filter(x -> {
							List<User> supplierList = subTierService.getTierSuppliers(x.getMold());

							/*
														return x.getMold().getCompanyId() != null
																&& x.getMold().getCompanyId().equals(user.getCompanyId()) || supplierList.contains(user);
							*/
							return x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
									|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMold().getId())) || supplierList.contains(user);
						}).collect(Collectors.toList());
					}
				}
				moldCorrectiveSubList.forEach(moldCorrective -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.CORRECTIVE_MAINTENANCE, moldCorrective.getId(), false));
				});
//				if(sendNoti)
//					broadcastNotificationService.notificationMold
			}
			if (alertTypes.contains(AlertType.MISCONFIGURE) && moldMisconfigureList != null && moldMisconfigureList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.MISCONFIGURE).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldMisconfigure> moldMisconfigureSubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldMisconfigureSubList = moldMisconfigureList;
				} else {
//					if(serverName.equalsIgnoreCase("dyson")){
//						List<MoldMisconfigure> finalMoldMisconfigureSubList = moldMisconfigureSubList;
//						moldMisconfigureList.forEach(moldMisconfigure -> {
//							List<User> supplierList = subTierService.getTierSuppliers(moldMisconfigure.getMold());
//							if(supplierList.contains(user))
//								finalMoldMisconfigureSubList.add(moldMisconfigure);
//						});
//						moldMisconfigureSubList.addAll(finalMoldMisconfigureSubList);
//					}else {
					/*
											moldMisconfigureSubList = moldMisconfigureList.stream().filter(x -> x.getMold().getCompanyId() != null
													&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
					*/
					moldMisconfigureSubList = moldMisconfigureList.stream()
							.filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
									|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMold().getId())))
							.collect(Collectors.toList());
//					}
				}
				moldMisconfigureSubList.forEach(moldMisconfigure -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.MISCONFIGURE, moldMisconfigure.getId(), false));
				});
				if (sendNoti && alertOn)
					broadcastNotificationService.notificationMoldMisconfigure(moldMisconfigureSubList, user);
			}
			if (alertTypes.contains(AlertType.EFFICIENCY) && moldEfficiencyList != null && moldEfficiencyList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.EFFICIENCY).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldEfficiency> moldEfficiencySubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldEfficiencySubList = moldEfficiencyList;
				} else {
//					if(serverName.equalsIgnoreCase("dyson")){
//						List<MoldEfficiency> finalMoldEfficiencySubList = moldEfficiencySubList;
//						moldEfficiencyList.forEach(moldEfficiency -> {
//							List<User> supplierList = subTierService.getTierSuppliers(moldEfficiency.getMold());
//							if(supplierList.contains(user))
//								finalMoldEfficiencySubList.add(moldEfficiency);
//						});
//						moldEfficiencySubList.addAll(finalMoldEfficiencySubList);
//					}else {
					/*
											moldEfficiencySubList = moldEfficiencyList.stream().filter(x -> x.getMold().getCompanyId() != null
													&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
					*/
					moldEfficiencySubList = moldEfficiencyList.stream().filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
							|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()))).collect(Collectors.toList());
//					}
				}
				moldEfficiencySubList.forEach(moldEfficiency -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.EFFICIENCY, moldEfficiency.getId(), false));
				});
				if (sendNoti && alertOn)
					broadcastNotificationService.notificationMoldEfficiency(moldEfficiencySubList, user);
			}
			if (alertTypes.contains(AlertType.DATA_SUBMISSION) && moldDataSubmissionList != null && moldDataSubmissionList.size() > 0) {
				UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, AlertType.DATA_SUBMISSION).orElse(null);
				boolean alertOn = checkAlertOn(userAlert);
				List<MoldDataSubmission> moldDataSubmissionSubList = moldDataSubmissionList.stream().filter(x -> x.getNotificationStatus().equals(NotificationStatus.PENDING))
						.collect(Collectors.toList());
				moldDataSubmissionSubList.forEach(moldDataSubmission -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DATA_SUBMISSION, moldDataSubmission.getId(), false));
				});
				/*

								moldDataSubmissionSubList = moldDataSubmissionList.stream()
										.filter(x -> !x.getNotificationStatus().equals(NotificationStatus.PENDING)
												&& x.getMold().getCompanyId() != null
												&& (x.getMold().getCompanyId().equals(user.getCompanyId())
													|| (user.getCompany().getName().equals("FLEXTRONICS") && Arrays.asList("MCTRONIC", "Skreen Fabric").contains(x.getMold().getCompanyName()))
													|| (user.getCompany().getName().equals("SKP") && Arrays.asList("MyTech", "CGH", "TF", "GOLDPAR", "GOODHART", "SPI", "SKP BP").contains(x.getMold().getCompanyName()))))
										.collect(Collectors.toList());
				*/

				moldDataSubmissionSubList = moldDataSubmissionList.stream()
						.filter(x -> !x.getNotificationStatus().equals(NotificationStatus.PENDING) && x.getMold().getCompanyId() != null
								&& (x.getMold().getCompanyId().equals(user.getCompanyId()) || accessCompanyIds.contains(x.getMold().getCompanyId())
										|| accessMoldIds.contains(x.getMoldId())))
						.collect(Collectors.toList());
				moldDataSubmissionSubList.forEach(moldDataSubmission -> {
					if (!user.isAdmin()) {
						logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DATA_SUBMISSION, moldDataSubmission.getId(), false));
					}
				});
			}
		});
		return logUserAlertList;
	}

	public List<LogUserAlert> generateLogUserAlertOld(Map<User, List<AlertType>> userAlertMap, List<MoldRefurbishment> moldEndLifeCycleList) {
		List<LogUserAlert> logUserAlertList = new ArrayList<>();
		userAlertMap.forEach((user, alertTypes) -> {
			if (alertTypes.contains(AlertType.REFURBISHMENT) && moldEndLifeCycleList != null && moldEndLifeCycleList.size() > 0) {
				List<MoldRefurbishment> moldEndLifeCycleSubList = new ArrayList<>();
				/*
								if (user.isAdmin()) {
									moldEndLifeCycleSubList = moldEndLifeCycleList;
								} else {
									if (serverName.equalsIgnoreCase("dyson")) {
										List<MoldRefurbishment> finalMoldEndLifeCycleSubList = moldEndLifeCycleSubList;
										moldEndLifeCycleList.forEach(cm -> {
											List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
											if (supplierList.contains(user))
												finalMoldEndLifeCycleSubList.add(cm);
										});
										moldEndLifeCycleSubList.addAll(finalMoldEndLifeCycleSubList);
									} else {
										moldEndLifeCycleSubList = moldEndLifeCycleList.stream().filter(x -> x.getMold().getCompanyId() != null
												&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
									}
								}
				*/
				if (serverName.equalsIgnoreCase("dyson")) {
					List<MoldRefurbishment> finalMoldEndLifeCycleSubList = new ArrayList<>();
					moldEndLifeCycleList.forEach(cm -> {
						List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
						if (supplierList.contains(user) || cm.getMold() != null && cm.getMold().getEngineersInCharge() != null && cm.getMold().getEngineersInCharge().contains(user))
							finalMoldEndLifeCycleSubList.add(cm);
					});
					moldEndLifeCycleSubList.addAll(finalMoldEndLifeCycleSubList);
				} else {
					if (user.isAdmin()) {
						moldEndLifeCycleSubList = moldEndLifeCycleList;
					} else {
						moldEndLifeCycleSubList = moldEndLifeCycleList.stream().filter(x -> {
							List<User> supplierList = subTierService.getTierSuppliers(x.getMold());
							return x.getMold().getCompanyId() != null && x.getMold().getCompanyId().equals(user.getCompanyId()) || supplierList.contains(user);
						}).collect(Collectors.toList());
					}
				}

				moldEndLifeCycleSubList.forEach(moldCorrective -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.REFURBISHMENT, moldCorrective.getId(), false));
				});
			}
		});
		return logUserAlertList;

	}

	public List<LogUserAlert> generateLogUserAlert(Map<User, List<AlertType>> userAlertMap, List<MoldRefurbishment> moldEndLifeCycleList, boolean sendNoti) {
		List<LogUserAlert> logUserAlertList = new ArrayList<>();
		userAlertMap.forEach((user, alertTypes) -> {
			final List<Long> accessCompanyIds = new ArrayList<>();
			final List<Long> accessMoldIds = new ArrayList<>();

			if (alertTypes.contains(AlertType.REFURBISHMENT) && moldEndLifeCycleList != null && moldEndLifeCycleList.size() > 0) {
				if (!user.isAdmin() || (serverName.equalsIgnoreCase("dyson"))) {
					loadTreeCompanyForPayLoad(user.getCompanyId(), accessCompanyIds, accessMoldIds);
				}

				List<MoldRefurbishment> moldEndLifeCycleSubList = new ArrayList<>();
				/*
								if (user.isAdmin()) {
									moldEndLifeCycleSubList = moldEndLifeCycleList;
								} else {
									if (serverName.equalsIgnoreCase("dyson")) {
										List<MoldRefurbishment> finalMoldEndLifeCycleSubList = moldEndLifeCycleSubList;
										moldEndLifeCycleList.forEach(cm -> {
											List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
											if (supplierList.contains(user))
												finalMoldEndLifeCycleSubList.add(cm);
										});
										moldEndLifeCycleSubList.addAll(finalMoldEndLifeCycleSubList);
									} else {
										moldEndLifeCycleSubList = moldEndLifeCycleList.stream().filter(x -> x.getMold().getCompanyId() != null
												&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
									}
								}
				*/
				if (serverName.equalsIgnoreCase("dyson")) {
					List<MoldRefurbishment> finalMoldEndLifeCycleSubList = new ArrayList<>();
					/*
										moldEndLifeCycleList.forEach(cm -> {
											List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
											if (supplierList.contains(user) || cm.getMold()!=null && cm.getMold().getEngineers()!=null && cm.getMold().getEngineers().contains(user))
												finalMoldEndLifeCycleSubList.add(cm);
										});
					*/
					moldEndLifeCycleList.forEach(cm -> {
//						List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
						if ((cm.getMold().getCompanyId().equals(user.getCompanyId()) || accessCompanyIds.contains(cm.getMold().getCompanyId())
								|| accessMoldIds.contains(cm.getMold().getId()))
								|| cm.getMold() != null && cm.getMold().getEngineersInCharge() != null && cm.getMold().getEngineersInCharge().contains(user))
							finalMoldEndLifeCycleSubList.add(cm);
					});
					moldEndLifeCycleSubList.addAll(finalMoldEndLifeCycleSubList);
				} else {
					if (user.isAdmin()) {
						moldEndLifeCycleSubList = moldEndLifeCycleList;
					} else {
						/*
												moldEndLifeCycleSubList = moldEndLifeCycleList.stream().filter(x -> {
															List<User> supplierList = subTierService.getTierSuppliers(x.getMold());
															return x.getMold().getCompanyId() != null
																	&& x.getMold().getCompanyId().equals(user.getCompanyId()) || supplierList.contains(user);
														}
												).collect(Collectors.toList());
						*/

						moldEndLifeCycleSubList = moldEndLifeCycleList.stream().filter(x -> {
//									List<User> supplierList = subTierService.getTierSuppliers(x.getMold());
							return x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
									|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()));
						}).collect(Collectors.toList());
					}
				}

				moldEndLifeCycleSubList.forEach(moldCorrective -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.REFURBISHMENT, moldCorrective.getId(), false));
				});
				if (sendNoti)
					broadcastNotificationService.notificationMoldRefurbishment(moldEndLifeCycleSubList, user);
			}
		});
		return logUserAlertList;

	}

	public List<LogUserAlert> generateLogUserDowntimeAlert(Map<User, List<AlertType>> userAlertMap, List<MoldDowntimeEvent> moldDowntimeEventList, boolean sendNoti) {
		List<LogUserAlert> logUserAlertList = new ArrayList<>();
		userAlertMap.forEach((user, alertTypes) -> {
			final List<Long> accessCompanyIds = new ArrayList<>();
			final List<Long> accessMoldIds = new ArrayList<>();

			if (alertTypes.contains(AlertType.DOWNTIME) && moldDowntimeEventList != null && moldDowntimeEventList.size() > 0) {
				if (!user.isAdmin() || (serverName.equalsIgnoreCase("dyson"))) {
					loadTreeCompanyForPayLoad(user.getCompanyId(), accessCompanyIds, accessMoldIds);
				}

				List<MoldDowntimeEvent> moldDowntimeEventSubList = new ArrayList<>();
//				if (serverName.equalsIgnoreCase("dyson")) {
//					List<MoldDowntimeEvent> finalMoldDowntimeEventSubList = new ArrayList<>();
//					moldDowntimeEventList.forEach(moldDowntimeEvent -> {
//						if ((moldDowntimeEvent.getMold().getCompanyId().equals(user.getCompanyId())
//								|| accessCompanyIds.contains(moldDowntimeEvent.getMold().getCompanyId()) || accessMoldIds.contains(moldDowntimeEvent.getMold().getId()))
//								|| moldDowntimeEvent.getMold()!=null && moldDowntimeEvent.getMold().getEngineers()!=null && moldDowntimeEvent.getMold().getEngineers().contains(user))
//							finalMoldDowntimeEventSubList.add(moldDowntimeEvent);
//					});
//					moldDowntimeEventSubList.addAll(finalMoldDowntimeEventSubList);
//				} else {
				if (user.isAdmin()) {
					moldDowntimeEventSubList = moldDowntimeEventList;
				} else {
					moldDowntimeEventSubList = moldDowntimeEventList.stream().filter(x -> {
						return x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
								|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()));
					}).collect(Collectors.toList());
				}
//				}

				moldDowntimeEventSubList.forEach(moldCorrective -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DOWNTIME, moldCorrective.getId(), false));
				});
				if (sendNoti)
					broadcastNotificationService.notificationMoldDowntimeEvent(moldDowntimeEventSubList, user);
			}
		});
		return logUserAlertList;

	}

	public List<LogUserAlert> generateLogUserAlertDetachmentOld(Map<User, List<AlertType>> userAlertMap, List<MoldDetachment> moldAlertList) {
		List<LogUserAlert> logUserAlertList = new ArrayList<>();
		userAlertMap.forEach((user, alertTypes) -> {
			if (alertTypes.contains(AlertType.DETACHMENT) && moldAlertList != null && moldAlertList.size() > 0) {
				List<MoldDetachment> moldAlertSubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldAlertSubList = moldAlertList;
				} else {
					if (serverName.equalsIgnoreCase("dyson")) {
						List<MoldDetachment> finalMoldEndLifeCycleSubList = moldAlertSubList;
						moldAlertList.forEach(cm -> {
							List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
							if (supplierList.contains(user))
								finalMoldEndLifeCycleSubList.add(cm);
						});
						moldAlertSubList.addAll(finalMoldEndLifeCycleSubList);
					} else {
						moldAlertSubList = moldAlertList.stream().filter(x -> x.getMold().getCompanyId() != null && x.getMold().getCompanyId().equals(user.getCompanyId()))
								.collect(Collectors.toList());
					}
				}
				moldAlertSubList.forEach(moldCorrective -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DETACHMENT, moldCorrective.getId(), false));
				});
			}
		});
		return logUserAlertList;

	}

	public List<LogUserAlert> generateLogUserAlertDetachment(Map<User, List<AlertType>> userAlertMap, List<MoldDetachment> moldAlertList, boolean sendNoti) {
		List<LogUserAlert> logUserAlertList = new ArrayList<>();
		userAlertMap.forEach((user, alertTypes) -> {
			final List<Long> accessCompanyIds = new ArrayList<>();
			final List<Long> accessMoldIds = new ArrayList<>();

			if (alertTypes.contains(AlertType.DETACHMENT) && moldAlertList != null && moldAlertList.size() > 0) {

				if (!user.isAdmin()
//						|| (serverName.equalsIgnoreCase("dyson"))
				) {
					loadTreeCompanyForPayLoad(user.getCompanyId(), accessCompanyIds, accessMoldIds);
				}

				List<MoldDetachment> moldAlertSubList = new ArrayList<>();
				if (user.isAdmin()) {
					moldAlertSubList = moldAlertList;
				} else {
					/*
										if (serverName.equalsIgnoreCase("dyson")) {
											List<MoldDetachment> finalMoldEndLifeCycleSubList = moldAlertSubList;
											moldAlertList.forEach(cm -> {
												List<User> supplierList = subTierService.getTierSuppliers(cm.getMold());
												if (supplierList.contains(user))
													finalMoldEndLifeCycleSubList.add(cm);
											});
											moldAlertSubList.addAll(finalMoldEndLifeCycleSubList);
										} else {
											moldAlertSubList = moldAlertList.stream().filter(x -> x.getMold().getCompanyId() != null
													&& x.getMold().getCompanyId().equals(user.getCompanyId())).collect(Collectors.toList());
										}
					*/
					moldAlertSubList = moldAlertList.stream().filter(x -> x.getMold().getCompanyId() != null && (x.getMold().getCompanyId().equals(user.getCompanyId())
							|| accessCompanyIds.contains(x.getMold().getCompanyId()) || accessMoldIds.contains(x.getMoldId()))).collect(Collectors.toList());
				}
				moldAlertSubList.forEach(moldCorrective -> {
					logUserAlertList.add(new LogUserAlert(user.getId(), AlertType.DETACHMENT, moldCorrective.getId(), false));
				});
				//TODO: noti mold detactment
				if (sendNoti)
					broadcastNotificationService.notificationMoldDetachment(moldAlertSubList, user);
			}
		});
		return logUserAlertList;

	}

	public MaintenanceLog findMaintenanceLogByMoldId(Long moldId) {
		if (moldId == null)
			return null;
		Mold mold = findById(moldId);
		MaintenanceLog maintenanceLog = new MaintenanceLog();
		maintenanceLog.setMoldId(moldId);
		maintenanceLog.setEquipmentCode(mold.getEquipmentCode());
		if (mold.getToolMaker() != null && mold.getToolMaker().getName() != null)
			maintenanceLog.setToolMaker(mold.getToolMaker().getName());
		maintenanceLog.setEngineer(mold.getEngineer());

		List<MoldMaintenance> moldMaintenanceList = moldMaintenanceRepository.findByMoldIdOrderByIdDesc(moldId);
		if (moldMaintenanceList != null && moldMaintenanceList.size() > 0) {
			moldMaintenanceList.forEach(moldMaintenance -> {
				MaintenanceHistoryData maintenanceHistoryData = new MaintenanceHistoryData();
				maintenanceHistoryData.setShotCount(moldMaintenance.getShotCount());
				if (moldMaintenance.getMaintenanceStatus().equals(MaintenanceStatus.DONE)) {
					maintenanceHistoryData.setConfirmed(true);
					if (moldMaintenance.getEndTime() != null) {
						if (moldMaintenance.getOverdueTime() == null || moldMaintenance.getOverdueTime().compareTo(moldMaintenance.getEndTime()) > 0)
							maintenanceHistoryData.setOnTimeStatus(MaintenanceTimeStatus.ON_TIME);
						else
							maintenanceHistoryData.setOnTimeStatus(MaintenanceTimeStatus.OVERDUE);
					}
				} else
					maintenanceHistoryData.setConfirmed(false);
				maintenanceHistoryData.setMaintenanceType(MaintenanceType.PREVENTIVE);
				maintenanceHistoryData.setStartTime(moldMaintenance.getStartTime());
				maintenanceHistoryData.setEndTime(moldMaintenance.getEndTime());
				maintenanceHistoryData.setCheckList(moldMaintenance.getChecklist());
				maintenanceHistoryData.setLatest(moldMaintenance.getLatest());
				if (Arrays.asList(MaintenanceStatus.DONE, MaintenanceStatus.OVERDUE).contains(moldMaintenance.getMaintenanceStatus())) {

					maintenanceLog.getHistories().add(maintenanceHistoryData);
				}
			});
			long numMaintenaced = moldMaintenanceList.stream().filter(m -> Arrays.asList(MaintenanceStatus.DONE, MaintenanceStatus.OVERDUE).contains(m.getMaintenanceStatus()))
					.count();
			long numDone = moldMaintenanceList.stream().filter(m -> Arrays.asList(MaintenanceStatus.DONE).contains(m.getMaintenanceStatus())).count();

			maintenanceLog.setExecutionRate(numMaintenaced > 0 ? numDone * 100.0 / numMaintenaced : 0);

		}
		return maintenanceLog;
	}

	public List<MiniComponentData> findMoldsForPMRegistering() {
		return moldRepository.findMoldsForPMRegistering();
	}

	public List<LocationMoldData> getLocationMoldData() {
		MoldPayload payload = new MoldPayload();
//		loadTreeCompanyForPayLoad(payload);
		List<LocationMoldData> locationMoldDataList = moldRepository.getLocationMoldData(payload);
		Long total = locationMoldDataList.stream().map(x -> x.getToolingCount()).reduce(0L, Long::sum);
		locationMoldDataList.forEach(locationMoldData -> {
			locationMoldData.setPercentage(Double.valueOf((locationMoldData.getToolingCount() * 100)) / total);
		});
		return locationMoldDataList;
	}

	public List<ContinentStatisticData> getContinentStatisticData() {
		List<ContinentStatisticData> continentStatisticDataList = new ArrayList<>();
		List<LocationMoldData> locationMoldDataList = getLocationMoldData();
		locationMoldDataList.forEach(locationMoldData -> {
			ContinentName continentName = locationMoldData.getContinent();
			ContinentStatisticData continentStatisticData = continentStatisticDataList.stream().filter(x -> x.getContinent().equals(continentName.getTitle())).findAny()
					.orElse(null);
			if (continentStatisticData == null) {
				continentStatisticData = new ContinentStatisticData(continentName);
				continentStatisticDataList.add(continentStatisticData);
			}
			continentStatisticData.getLocationMoldData().add(locationMoldData);
			continentStatisticData.setPercentage(continentStatisticData.getPercentage() + locationMoldData.getPercentage());
		});
		return continentStatisticDataList;
	}

	public List<GoogleMapData.MapData> getMapDataList(List<MapChartData> mapData) {
		GoogleMapData googleMapData = new GoogleMapData(mapData);
		googleMapData.getMapDataList().forEach(mapData1 -> {
			QLocation qLocation = QLocation.location;
			Predicate cnt = qLocation.name.eq(mapData1.getLocationName());
			Iterable<Location> locationList = locationRepository.findAll(cnt);
			Iterator<Location> locationIterator = locationList.iterator();
			if (locationIterator.hasNext()) {
				Location location = locationIterator.next();
				mapData1.setLatitude(location.getLatitude());
				mapData1.setLongitude(location.getLongitude());
				mapData1.setCompanyName(location.getCompanyName());
			}
		});

		return googleMapData.getMapDataList();
	}

	public List<Long> standardizeData() {
		List<MoldDisconnect> moldDisconnectList = moldDisconnectRepository
				.findByNotificationStatusIsInAndLatest(Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true);
		Map<Long, List<MoldDisconnect>> moldDisconnectMap = new HashMap<>();
		moldDisconnectList.forEach(moldDisconnect -> {
			if (!moldDisconnectMap.containsKey(moldDisconnect.getMoldId()))
				moldDisconnectMap.put(moldDisconnect.getMoldId(), new ArrayList<>());
			moldDisconnectMap.get(moldDisconnect.getMoldId()).add(moldDisconnect);
		});
		moldDisconnectMap.forEach((k, v) -> {
			if (moldDisconnectMap.get(k) != null && moldDisconnectMap.get(k).size() > 1) {
				for (MoldDisconnect md : moldDisconnectMap.get(k)) {
					if (moldDisconnectMap.get(k).indexOf(md) == moldDisconnectMap.get(k).size() - 1)
						break;
					md.setLatest(false);
				}
				moldDisconnectRepository.saveAll(moldDisconnectMap.get(k));
			}
		});
		return moldDisconnectRepository.findByNotificationStatusIsInAndLatest(Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true).stream()
				.map(x -> x.getMoldId()).collect(Collectors.toList());
	}

	public QuickStats getQuickStats(DashboardFilterPayload payload) {
		QuickStats quickStats = new QuickStats();
		quickStats.setMoldCount(moldRepository.getTotalMoldCount(payload));
		if (quickStats.getMoldCount() > 0)
			quickStats.setInstalledMoldPercentage(moldRepository.getInstalledMoldCount(payload) * 100 / quickStats.getMoldCount());
		else
			quickStats.setInstalledMoldPercentage(0L);

		Currency currency = MasterDataUtils.getMainCurrency();
		quickStats.setSymbol(currency.getCurrencyType().getTitle());
		payload.setMainRate(currency.getExchangeRate());
		quickStats.setCost(Long.valueOf(Math.round(moldRepository.getTotalCost(payload))).intValue());
		quickStats.setPartCount(moldRepository.getPartCount(payload));
		return quickStats;
	}

	public DashboardTabData getTotalCostTabData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			DashboardTabData data = new DashboardTabData(DashboardChartType.TOTAL_COST);
			Currency currency = MasterDataUtils.getMainCurrency();
			data.setSymbol(currency.getCurrencyType().getTitle());
			payload.setMainRate(currency.getExchangeRate());
			Double currentPeriodCost = moldRepository.getTotalCost(payload, true);
			Double previousPeriodCost = moldRepository.getTotalCost(payload, false);
			data.setKpi(currentPeriodCost);
			data.setTrend(currentPeriodCost - previousPeriodCost);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return new DashboardTabData(DashboardChartType.TOTAL_COST);
		}
	}

	public ApiResponse getTotalCost(TabbedOverviewGeneralFilterPayload payload) {
		try {
			Currency currency = MasterDataUtils.getMainCurrency();
			payload.setMainRate(currency.getExchangeRate());
			List<CostData> data = moldRepository.getTotalCostGroupByFrequent(payload);
			data.forEach(d -> {
				d.setToolingCost(d.getToolingCost() != null ? NumberUtils.roundOffNumber(d.getToolingCost() * payload.getMainRate()) : 0d);
			});
			List<String> existedTitles = data.stream().map(CostData::getTitle).collect(Collectors.toList());
			List<String> titles = getListTitleByDuration(payload.getDuration());
			titles.forEach(t -> {
				if (!existedTitles.contains(t)) {
					data.add(new CostData(t));
				}
			});

			data.sort(Comparator.comparing(CostData::getTitle));
			return ApiResponse.success(CommonMessage.OK, data);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	private List<String> getListTitleByDuration(Integer duration) {
		List<String> result = new ArrayList<>();

		if (duration == 7) {
			result.add(DateUtils.getDate(Instant.now(), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			result.add(DateUtils.getDate(Instant.now().minus(1, ChronoUnit.DAYS), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			result.add(DateUtils.getDate(Instant.now().minus(2, ChronoUnit.DAYS), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			result.add(DateUtils.getDate(Instant.now().minus(3, ChronoUnit.DAYS), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			result.add(DateUtils.getDate(Instant.now().minus(4, ChronoUnit.DAYS), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			result.add(DateUtils.getDate(Instant.now().minus(5, ChronoUnit.DAYS), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			result.add(DateUtils.getDate(Instant.now().minus(6, ChronoUnit.DAYS), DateUtils.YYYY_MM_DD_DATE_FORMAT));
		} else if (duration == 30) {
			result.add(DateUtils.getYearWeek(Instant.now(), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearWeek(Instant.now().minus(7, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearWeek(Instant.now().minus(14, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearWeek(Instant.now().minus(21, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
		} else {
			result.add(DateUtils.getYearMonth(Instant.now(), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearMonth(Instant.now().minus(30, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearMonth(Instant.now().minus(60, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearMonth(Instant.now().minus(90, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearMonth(Instant.now().minus(120, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
			result.add(DateUtils.getYearMonth(Instant.now().minus(150, ChronoUnit.DAYS), DateUtils.DEFAULT_DATE_FORMAT));
		}

		return result;
	}

	public Page<MoldReportData> findReportOeeOrCapacityUtilization(DashboardFilterPayload payload, Pageable pageable, ReportType type) {
		if (type.equals(ReportType.CAPACITY_UTILIZATION)) {
//            Page<MoldReportData> dataPage = findReportDataWithRateCapacity(payload, pageable);
			Page<MoldReportData> dataPage = moldRepository.findReportDataWithRateCapacity(payload, pageable);
			return dataPage;
		}
		MoldReportDataPage moldReportDataPage = moldRepository.findReportData(payload, pageable, type);
		if (type.equals(ReportType.CAPACITY_UTILIZATION)) {
			moldReportDataPage.getContent().forEach(data -> {
				data.setPercentage(data.getValue() != null ? data.getValue() * 100 / data.getAliveTime() : 0);
			});
		} else {
			moldReportDataPage.getContent().forEach(data -> {
				data.setPercentage(data.getValue() != null ? data.getValue() * 100 / (data.getAliveTime() * data.getAliveTime()) : 0);
			});
		}
		/*
				if(!StringUtils.isEmpty(payload.getRateCapacity()) && !payload.getRateCapacity().equals(RateCapacityType.ALL.getCode())){
					List<MoldReportData> moldReportDataList = moldReportDataPage.getContent();
					if(payload.getRateCapacity().equals(RateCapacityType.Frequently.getCode())){
						moldReportDataList = moldReportDataPage.getContent().stream().filter(data->data.getPercentage()>=50).collect(Collectors.toList());
					}else
					if(payload.getRateCapacity().equals(RateCapacityType.Occasionally.getCode())){
						moldReportDataList = moldReportDataPage.getContent().stream().filter(data->data.getPercentage()>=20
						&& data.getPercentage()<50).collect(Collectors.toList());
					}else
					if(payload.getRateCapacity().equals(RateCapacityType.Rarely.getCode())){
						moldReportDataList = moldReportDataPage.getContent().stream().filter(data->data.getPercentage()>0
						&& data.getPercentage()<20).collect(Collectors.toList());
					}else
					if(payload.getRateCapacity().equals(RateCapacityType.Never.getCode())){
						moldReportDataList = moldReportDataPage.getContent().stream().filter(data->data.getPercentage().longValue()==0).collect(Collectors.toList());
					}
					moldReportDataPage.setContent(moldReportDataList);
				}
				*/
		Page<MoldReportData> result = new PageImpl<>(moldReportDataPage.getContent(), pageable, moldReportDataPage.getTotal());
		return result;
	}

	/*	public Page<MoldReportData> findReportDataWithRateCapacity(DashboardFilterPayload payload, Pageable pageable) {
			String startDateStr = null;
			String endDateStr = null;
			Long duaTime = null;
			List<String> ops = null;
			Double rateFrom = null;
			Double rateTo = null;
			Double rarelyFrom = null;
			Boolean isNever = false;

			Integer pageFrom = pageable.getPageNumber() * pageable.getPageSize();
			Integer pageSize = pageable.getPageSize();

			if (!StringUtils.isEmpty(payload.getRateCapacity()) && !payload.getRateCapacity().equals(RateCapacityType.ALL.getCode())) {
				if (payload.getRateCapacity().equals(RateCapacityType.Frequently.getCode())) {
					rateFrom = 50d;
				} else if (payload.getRateCapacity().equals(RateCapacityType.Occasionally.getCode())) {
					rateFrom = 20d;
					rateTo = 50d;
				} else if (payload.getRateCapacity().equals(RateCapacityType.Rarely.getCode())) {
					rarelyFrom = 0.5d;
				} else if (payload.getRateCapacity().equals(RateCapacityType.Never.getCode())) {
					isNever = true;
				}
			}
			if(payload.getStartTime() != null && payload.getEndTime() != null){
				startDateStr = DateUtils.getDateTime(Instant.ofEpochSecond(payload.getStartTime()));
				endDateStr = DateUtils.getDateTime(Instant.ofEpochSecond(payload.getEndTime()));
	//			startDate = Instant.ofEpochMilli(payload.getStartTime());
	//			endDate = Instant.ofEpochMilli(payload.getEndTime());
	//			builder.and(statistics.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
			}
	//		if (payload.getOps() != null && !payload.getOps().isEmpty()) {
			ops = payload.getOps().stream().map(p->p.getCode()).collect(Collectors.toList());
	//		}
	//		Pageable pageable1 = PageRequest.of(pageable.getPageNumber(),pageable.getPageSize());
			List<Object[]> moldReportData = moldRepository.findReportDataWithRateCapacity(
					startDateStr, endDateStr, duaTime, ops, rateFrom, rateTo, rarelyFrom, isNever,
					pageFrom,pageSize
			);
			List<MoldReportData> moldReportDataPage = moldReportData.stream().map(m -> new MoldReportData(m)).collect(Collectors.toList());
			long total =  moldRepository.countReportDataWithRateCapacity(
					startDateStr, endDateStr, duaTime, ops, rateFrom, rateTo, rarelyFrom, isNever
			);
			return  new PageImpl<>(moldReportDataPage, pageable, total);

		}
		*/
	public Map<AlertType, Object> getAllLatestAlert() {
		Map<AlertType, Object> result = new HashMap<>();

		// 1. Relocation
		result.put(AlertType.RELOCATION, moldLocationRepository.findByLatest(true));

		// 2. Disconnected
		result.put(AlertType.DISCONNECTED, moldDisconnectRepository.findByLatest(true));

		result.put(AlertType.TERMINAL_DISCONNECTED, terminalDisconnectRepository.findByLatest(true));

		// 3. Cycle Time
		result.put(AlertType.CYCLE_TIME, moldCycleTimeRepository.findByLatest(true));

		// 4. Maintenance
		result.put(AlertType.MAINTENANCE, moldMaintenanceRepository.findByLatest(true));

		// 5. Corrective Maintenance
		result.put(AlertType.CORRECTIVE_MAINTENANCE, moldCorrectiveRepository.findByLatest(true));

		// 6. Efficiency
		result.put(AlertType.EFFICIENCY, moldEfficiencyRepository.findByLatest(true));

		// 7. Misconfigure
		result.put(AlertType.MISCONFIGURE, moldMisconfigureRepository.findByLatest(true));

		// 8. Data Submission
		result.put(AlertType.DATA_SUBMISSION,
				moldDataSubmissionRepository.findByLatestAndNotificationStatusIn(true, Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED)));
		// 9. REFURBISHMENT
		result.put(AlertType.REFURBISHMENT, moldRefurbishmentRepository.findByLatest(true));

		// 9. DETACHMENT
		result.put(AlertType.DETACHMENT, moldDetachmentRepository.findByLatest(true));

		// 10. DOWNTIME
		result.put(AlertType.DOWNTIME, moldDowntimeEventRepository.findByLatest(true));

		return result;
	}

	private List<MoldDtoPDF> convertToMoldDtoPDF(List<Mold> molds) {
		List<MoldDtoPDF> moldDtoPDFList = new ArrayList<>();
		if (molds != null && molds.size() > 0) {
			molds.forEach(mold -> {
				MoldDtoPDF moldDTOPDF = new MoldDtoPDF();
				moldDTOPDF.setToolingID(mold.getEquipmentCode());
				moldDTOPDF.setCounter(mold.getCounter() != null ? mold.getCounter().getEquipmentCode() : "");
				moldDTOPDF.setLocation(mold.getLocation() != null ? mold.getLocation().getName() : "");
				moldDTOPDF.setCycleTime(String.valueOf(mold.getContractedCycleTime()));
				moldDTOPDF.setLastShot(mold.getLastShot() != null ? String.valueOf(mold.getLastShot()) : "");
				moldDTOPDF.setOp(mold.getOperatingStatus() != null ? mold.getOperatingStatus().getTitle() : "");
				moldDTOPDF.setStatus(mold.getEquipmentStatus() != null ? mold.getEquipmentStatus().getTitle() : "");
				moldDtoPDFList.add(moldDTOPDF);
			});
		}
		return moldDtoPDFList;
	}

	public byte[] exportPDFDataMolds(MoldPayload payload) {
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
//		loadTreeCompanyForPayLoad(payload);
		Iterable<Mold> molds = moldRepository.findAll(payload.getPredicate(), sort);
		List<Mold> moldList = StreamSupport.stream(molds.spliterator(), false).collect(Collectors.toList());
		List<MoldDtoPDF> moldDtoPDFS = convertToMoldDtoPDF(moldList);
		return pdfUtils.exportPDFDataMold(moldDtoPDFS);
	}

	public ByteArrayOutputStream exportExcelDataMolds(List<Long> ids, Integer timezoneOffsetClient, Pageable pageable) throws IOException {
		List<Mold> moldList;
		if (ids == null || ids.size() == 0) {
			moldList = moldRepository.findAllByOrderByIdDesc();
		} else {
			if (pageable != null && pageable.getSort() != null) {
				Pageable pageableNew = Pageable.unpaged();
				if (pageable.getSort() != null) {
					pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
				}
				MoldPayload payload = new MoldPayload();
				payload.setIds(ids);
				QueryUtils.includeDisabled(Q.mold);
				Page<Mold> pageContent = findAll(payload.getPredicate(), pageableNew, true, null, "COMMON", ActiveStatus.ENABLED);
				moldList = pageContent.getContent();
			} else
				moldList = moldRepository.findByIdInOrderByIdDesc(ids);
		}
		loadRemainingPartsCount(moldList);


		return excelUtils.exportExcelToolingDetailNew(moldList, timezoneOffsetClient);
	}

	public ByteArrayOutputStream exportExcelImportTemplate() {
		return excelUtils.exportExcelToolingImportTemplate();
	}

	public byte[] exportPdfDetailTooling(List<Long> ids, String fileName) {
		List<Mold> molds;
		if (ids == null || ids.size() == 0)
			molds = moldRepository.findAllByOrderByIdDesc();
		else
			molds = moldRepository.findByIdInOrderByIdDesc(ids);
		return pdfUtils.exportListMoldDetail(molds, fileName + ".pdf");
	}

	public List<MoldPart> findMoldPartByIdIn(List<Long> ids) {
		return moldPartRepository.findByIdIn(ids);
	}

	public List<MiniComponentData> findAllMiniData() {
		return moldRepository.findAllMoldIdMoldCodes(null);
	}

	public ApiResponse findMoldLiteData(String code, Pageable pageable) {
		try {
			return ApiResponse.success(CommonMessage.OK, moldRepository.findMoldLiteData(code, pageable));
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	public List<MiniComponentData> findAllMiniDataByGeneralFilter() {
		return moldRepository.findAllMoldIdMoldCodesByGeneralFilter(null);
	}

	public Map<Long, List<MoldCapacityReportData>> findMoldMaxCapacity(DashboardFilterPayload payload, List<Long> ids, Frequent frequent) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson");
		Map<Long, List<MoldCapacityReportData>> result = new HashMap<>();
		List<MoldCapacityReportData> productionQuantityData = statisticsRepository.findMoldCapacityOfTooling(payload, ids, frequent, isDyson);
		productionQuantityData.forEach(x -> {
			MoldCapacityReportData m = x.convert(frequent);
			if (ids != null && ids.size() > 0) {
				if (!result.containsKey(x.getId())) {
					result.put(x.getId(), new ArrayList<>());
				}
				result.get(x.getId()).add(m);
			} else {
				if (!result.containsKey(0L)) {
					result.put(0L, new ArrayList<>());
				}
				result.get(0L).add(m);
			}
		});

		// Add same data point to all element, if it's null then count is 0
		result.forEach((id, list) -> {
			result.forEach((secondPart, secondList) -> {
				if (secondPart != id) {
					list.forEach(a -> {
						MoldCapacityReportData diffList = secondList.stream()
								.filter(x -> (x.getYear().equals(a.getYear())
										&& ((x.getWeekOrMonth() == null && a.getWeekOrMonth() == null) || x.getWeekOrMonth().equals(a.getWeekOrMonth())))
										&& ((x.getDay() == null && a.getDay() == null) || x.getDay().equals(a.getDay())))
								.findAny().orElse(null);
						if (diffList == null) {
							MoldCapacityReportData moldCapacityReportData = new MoldCapacityReportData();
							moldCapacityReportData.setTitle(a.getTitle());
							moldCapacityReportData.setYear(a.getYear());
							moldCapacityReportData.setWeekOrMonth(a.getWeekOrMonth());
							moldCapacityReportData.setDay(a.getDay());
							secondList.add(moldCapacityReportData);
						}
					});
				}
			});
		});
		return result;
	}

	public Map<Long, List<MoldCapacityReportData>> findProductionCapacity(TabbedOverviewGeneralFilterPayload payload) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson");
		Map<Long, List<MoldCapacityReportData>> result = new HashMap<>();
		Frequent frequent = payload.getFrequentFromDuration();
		List<MoldCapacityReportData> productionQuantityData = statisticsRepository.findProductionCapacityOfTooling(payload, frequent, isDyson);
		productionQuantityData.forEach(x -> {
			MoldCapacityReportData m = x.convert(frequent);
			if (!result.containsKey(0L)) {
				result.put(0L, new ArrayList<>());
			}
			result.get(0L).add(m);
		});

		// Add same data point to all element, if it's null then count is 0
		result.forEach((id, list) -> {
			result.forEach((secondPart, secondList) -> {
				if (secondPart != id) {
					list.forEach(a -> {
						MoldCapacityReportData diffList = secondList.stream()
								.filter(x -> (x.getYear().equals(a.getYear())
										&& ((x.getWeekOrMonth() == null && a.getWeekOrMonth() == null) || x.getWeekOrMonth().equals(a.getWeekOrMonth())))
										&& ((x.getDay() == null && a.getDay() == null) || x.getDay().equals(a.getDay())))
								.findAny().orElse(null);
						if (diffList == null) {
							MoldCapacityReportData moldCapacityReportData = new MoldCapacityReportData();
							moldCapacityReportData.setTitle(a.getTitle());
							moldCapacityReportData.setYear(a.getYear());
							moldCapacityReportData.setWeekOrMonth(a.getWeekOrMonth());
							moldCapacityReportData.setDay(a.getDay());
							secondList.add(moldCapacityReportData);
						}
					});
				}
			});
		});
		return result;
	}

	public DashboardTabData getProductionCapacityTabData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			DashboardTabData data = new DashboardTabData(DashboardChartType.PRODUCTION_CAPACITY);
			Map<Long, List<MoldCapacityReportData>> capacityData = findProductionCapacity(payload);
			data.setKpi(capacityData.get(0L).stream().mapToDouble(MoldCapacityReportData::getOutputCapacityPercent).average().getAsDouble());
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return new DashboardTabData(DashboardChartType.PRODUCTION_CAPACITY);
		}
	}

	/**
	 * 고장 목록 조회
	 * @param predicate
	 * @param pageable
	 * @return
	 */
	public Page<MoldRefurbishment> findMoldRefurbishmentAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<MoldRefurbishment> page;
		if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldRefurbishmentExtraData> moldAccumulatedData = moldRefurbishmentRepository.findMoldRefurbishmentOrderByAccumulatedShot(predicate, pageable, null);
			List<MoldRefurbishment> molds = moldAccumulatedData.stream().map(MoldRefurbishmentExtraData::getMoldRefurbishment).collect(Collectors.toList());
			page = new PageImpl<>(molds, pageable, moldRefurbishmentRepository.count(predicate));
		} else if (properties[0].contains(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldRefurbishment> molds = moldRefurbishmentRepository.findMoldRefurbishmentOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(molds, pageable, moldRefurbishmentRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldRefurbishment> molds = moldRefurbishmentRepository.findMoldRefurbishmentOrderByStatus(predicate, pageable);
			page = new PageImpl<>(molds, pageable, moldRefurbishmentRepository.count(predicate));
		} else page = moldRefurbishmentRepository.findAll(predicate, pageable);
		loadAccumulatedShot(page.getContent().stream().map(MoldRefurbishment::getMold).filter(Objects::nonNull).collect(Collectors.toList()), accumulatedShotFilter);
		return page;
	}

	/**
	 * 고장 내역 등록
	 * @param
	 */
	/*
		@Transactional
		public void saveRefurbishment(MoldRefurbishment refurbishment) {
			// 1. MoldRefurbishment 데이터 저장
	*/
	/*
			corrective.convertStringToInstant();
	*//*


			Mold mold = moldRepository.findById(refurbishment.getMoldId()).orElseThrow(() -> new RuntimeException("Mold is null"));

			refurbishment.setMold(mold);
			refurbishment.setLocation(mold.getLocation());

		//		if (corrective.getCurrencyType() == null) {
		//			corrective.setCurrencyType(CurrencyType.USD);
		//		}

			refurbishment.setRefurbishmentStatus(CorrectiveStatus.FAILURE);
			refurbishment.setLatest(true);
			moldRefurbishmentRepository.save(refurbishment);

			// 2. Mold EquipmentStatus 'FAILUER'로 변경.
			mold.setEquipmentStatus(EquipmentStatus.FAILURE);
			moldRepository.save(mold);

			// 3. Generate log user alert
			List<User> supplierList = subTierService.getTierSuppliers(mold);

			if(serverName.equalsIgnoreCase("dyson")){
				Set<User> engineers = mold.getEngineers();
				engineers.forEach(engineer -> {
					if(!supplierList.contains(engineer)) supplierList.add(engineer);
				});
			}else {
				List<User> oemList = userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
				oemList.forEach(oem -> {
					if (!supplierList.contains(oem)) supplierList.add(oem);
				});
			}
			List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(supplierList, AlertType.CORRECTIVE_MAINTENANCE);
			Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
			List<LogUserAlert> logUserAlerts = generateLogUserAlert(userAlertMap, null, null, null, null, null, null, null, null, Arrays.asList(refurbishment));
			logUserAlertRepository.saveAll(logUserAlerts);

			List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> x.getEmail() == true).collect(Collectors.toList());
			Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
			mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, null, null,
					null, null, null, null, Arrays.asList(refurbishment));
		}

		*/

	public MoldRefurbishment findMoldRefurbishmentById(Long id) {
		return moldRefurbishmentRepository.findById(id).orElseGet(() -> new MoldRefurbishment());
	}

	public void resisterRepairRefurbishment(MoldRefurbishment refurbishment, CorrectiveAction refurbishmentAction) {
		// 1. MoldRefurbishment 데이터 저장
//		corrective.convertStringToInstant();

		MoldRefurbishment moldRefurbishment = moldRefurbishmentRepository.findById(refurbishment.getId()).orElseThrow(() -> new RuntimeException("MoldRefurbishment is null."));

		List<FileStorage> copyOfDocuments = new ArrayList<>();
		boolean isNew = false;
		if (refurbishmentAction.equals(CorrectiveAction.REQUEST)) {
			if (!moldRefurbishment.getRefurbishmentStatus().equals(RefurbishmentStatus.END_OF_LIFECYCLE)) {
				moldRefurbishment.setLatest(false);
				moldRefurbishmentRepository.save(moldRefurbishment);

				MoldRefurbishment newRequest = new MoldRefurbishment();
				newRequest.setMold(moldRefurbishment.getMold());
				newRequest.setLocation(moldRefurbishment.getMold().getLocation());
				newRequest.setMemo(moldRefurbishment.getMemo());
				newRequest.setFailureTime(moldRefurbishment.getFailureTime());
				newRequest.setFailureReason(moldRefurbishment.getFailureReason());
				newRequest.setMoldEndLifeCycle(moldRefurbishment.getMoldEndLifeCycle());
				newRequest.setCost(moldRefurbishment.getCost());
				newRequest.setCurrencyType(moldRefurbishment.getCurrencyType());

				FileStoragePayload fileStoragePayload = new FileStoragePayload();
				fileStoragePayload.setStorageType(StorageType.MOLD_REFURBISHMENT);
				fileStoragePayload.setRefId(moldRefurbishment.getId());
				Iterable<FileStorage> fileStorageIterable = fileStorageService.findAll(fileStoragePayload.getPredicate());
				if (fileStorageIterable != null) {
					fileStorageIterable.forEach(fileStorage -> {
						copyOfDocuments.add(fileStorageService.copyDocument(fileStorage));
					});
				}

				moldRefurbishment = newRequest;
				isNew = true;
			}
			moldRefurbishment.setRefurbishmentStatus(RefurbishmentStatus.REQUESTED);
		} else {
			List<MoldRefurbishment> existing = moldRefurbishmentRepository.findByMoldAndRefurbishmentStatusInAndLastChecked(moldRefurbishment.getMold(),
					Arrays.asList(RefurbishmentStatus.APPROVED, RefurbishmentStatus.DISAPPROVED, RefurbishmentStatus.COMPLETED), true);
			existing.forEach(mc -> mc.setLastChecked(false));
			moldRefurbishmentRepository.saveAll(existing);

			if (refurbishmentAction.equals(CorrectiveAction.APPROVE)) {
				moldRefurbishment.setRefurbishmentStatus(RefurbishmentStatus.APPROVED);
			} else if (refurbishmentAction.equals(CorrectiveAction.DISAPPROVE)) {
				moldRefurbishment.setRefurbishmentStatus(RefurbishmentStatus.DISAPPROVED);
				moldRefurbishment.setDisapprovedBy(SecurityUtils.getName());
				moldRefurbishment.setDisapprovedAt(Instant.now());
			} else if (refurbishmentAction.equals(CorrectiveAction.COMPLETE)) {
				moldRefurbishment.setRefurbishmentStatus(RefurbishmentStatus.COMPLETED);

				Mold mold = moldRefurbishment.getMold();
				/*
								if (mold.getCounter() == null) {
									mold.setEquipmentStatus(EquipmentStatus.AVAILABLE);
								} else {
									mold.setEquipmentStatus(EquipmentStatus.INSTALLED);
								}
				*/
// process change design short
				if (refurbishment.getEstimateExtendedLife() != null) {
					mold.setDesignedShot(mold.getDesignedShot() + refurbishment.getEstimateExtendedLife());
				}

				if (refurbishment.getCost() != null) {
					mold.setAccumulatedMaintenanceCost(
							mold.getAccumulatedMaintenanceCost() == null ? refurbishment.getCost() : mold.getAccumulatedMaintenanceCost() + refurbishment.getCost());
				}

				mold.setLastRefurbishmentDate(Instant.now());

				moldRepository.save(mold);
				// update lifecycle
				moldEndLifeCycleService.createOrUpdateMoldEndLifeCycle(mold, false);
			}
			moldRefurbishment.setLastChecked(true);
		}
		moldRefurbishment.setLatest(true);
		if (refurbishment.getFailureReason() != null)
			moldRefurbishment.setFailureReason(refurbishment.getFailureReason());
		/*
				if(refurbishment.getNumberOfBackups() != null) moldRefurbishment.setNumberOfBackups(refurbishment.getNumberOfBackups());
		*/
		if (refurbishment.getExpectedEndTime() != null)
			moldRefurbishment.setExpectedEndTime(refurbishment.getExpectedEndTime());
		if (refurbishment.getEstimateExtendedLife() != null)
			moldRefurbishment.setEstimateExtendedLife(refurbishment.getEstimateExtendedLife());
		if (refurbishment.getNumberOfWorkingCavity() != null)
			moldRefurbishment.setNumberOfWorkingCavity(refurbishment.getNumberOfWorkingCavity());

		if (refurbishment.getDisapprovedMessage() != null)
			moldRefurbishment.setDisapprovedMessage(refurbishment.getDisapprovedMessage());
		if (refurbishment.getMemo() != null && !refurbishment.getMemo().equals("")) {
			moldRefurbishment.setMemo(moldRefurbishment.getMemo() != null && moldRefurbishment.getMemo() != ""
					? refurbishment.getMemo() + " - " + SecurityUtils.getName() + "\n" + moldRefurbishment.getMemo()
					: refurbishment.getMemo() + " - " + SecurityUtils.getName());
		}
		if (refurbishment.getStartTime() != null)
			moldRefurbishment.setStartTime(refurbishment.getStartTime());
		if (refurbishment.getEndTime() != null)
			moldRefurbishment.setEndTime(refurbishment.getEndTime());
		if (refurbishment.getCost() != null) {
			moldRefurbishment.setCost(refurbishment.getCost());
			moldRefurbishment.setCurrencyType(refurbishment.getCurrencyType() != null ? refurbishment.getCurrencyType() : CurrencyType.USD);
		}

		moldRefurbishmentRepository.save(moldRefurbishment);

		if (isNew) {
			List<User> supplierList = subTierService.getTierSuppliers(moldRefurbishment.getMold());
			List<User> oemList = userRepository.findByAdminIsTrueAndDeletedIsFalse();
			oemList.forEach(oem -> {
				if (!supplierList.contains(oem))
					supplierList.add(oem);
			});
			List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(supplierList, AlertType.REFURBISHMENT);
			Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
			List<LogUserAlert> logUserAlerts = generateLogUserAlert(userAlertMap, Arrays.asList(moldRefurbishment), true);
			logUserAlertRepository.saveAll(logUserAlerts);
		}

		Long refId = moldRefurbishment.getId();
		if (copyOfDocuments.size() > 0) {
			copyOfDocuments.forEach(aCopy -> aCopy.setRefId(refId));
			fileStorageService.saveAll(copyOfDocuments);
		}
		if (refurbishment.getFiles() != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_REFURBISHMENT, moldRefurbishment.getId(), refurbishment.getFiles()));
		}
	}

	public void discardRefurbishment(MoldRefurbishment refurbishment) {
		//close Refurbishment
		List<MoldRefurbishment> existing = moldRefurbishmentRepository.findByLatestAndMoldEndLifeCycle(true, refurbishment.getMoldEndLifeCycle());
		existing.forEach(mc -> {
			mc.setLastChecked(false);

		});
		moldRefurbishmentRepository.saveAll(existing);

		refurbishment.setLastChecked(true);
		refurbishment.setRefurbishmentStatus(RefurbishmentStatus.DISCARDED);
		moldRefurbishmentRepository.save(refurbishment);
		Mold mold = refurbishment.getMold();
		if (mold != null && Arrays.asList(EquipmentStatus.AVAILABLE, EquipmentStatus.INSTALLED).contains(mold.getEquipmentStatus())) {
			mold.setEquipmentStatus(EquipmentStatus.DISCARDED);
			moldRepository.save(mold);
		}

	}

	/*
	public void cancelRepair(MoldRefurbishment corrective) {
		// 1. MoldRefurbishment 데이터 저장
		MoldRefurbishment moldCorrective = moldRefurbishmentRepository.findById(corrective.getId())
				.orElseThrow(() -> new RuntimeException("MoldRefurbishment is null."));

		moldCorrective.setCost(null);
		moldCorrective.setCurrencyType(CurrencyType.USD);		// 기본값
		moldCorrective.setMemo(null);
		moldCorrective.setRepairTime(null);
		moldCorrective.setRepairTime(null);
		moldCorrective.setTimeToRepair(null);
		moldCorrective.setRefurbishmentStatus(RefurbishmentStatus.END_OF_LIFECYCLE);

		moldRefurbishmentRepository.save(moldCorrective);

		// 2. Mold EquipmentStatus 'AVAILABLE' or 'INSTALLED' 로 변경.
		Mold mold = moldCorrective.getMold();
		mold.setEquipmentStatus(EquipmentStatus.FAILURE);

		moldRepository.save(mold);
	}
	*/
	@Transactional(propagation = Propagation.NEVER)
	public void procMaintenanceDueDateAll() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(MoldService.class, "MAINT_DUE_DATE"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			MoldPayload payload = new MoldPayload();
			payload.setMaintenanced(true);
			payload.setStatus("alert");
			payload.setLastAlert(true);
			payload.setAllDataUser(true);
			Page<MoldMaintenance> page;
			int pageNo[] = { 0 };
			int pageSize = 50;
			while (pageNo[0] < 10000
					&& !(page = TranUtils.doNewTran(() -> moldMaintenanceRepository.findAll(payload.getMaintenancePredicate(), PageRequest.of(pageNo[0]++, pageSize)))).isEmpty()) {
				page.forEach(m -> {
					TranUtils.doNewTran(() -> {
						Integer scLastMaintenance = getScLastMaintenance(m.getMoldId());
						procMaintenanceDueDate(m, scLastMaintenance);
					});
				});
			}
		});
	}

	@Deprecated
	public Boolean calDueDateMaintenanceOld(MoldMaintenance m) {
		try {

			if (m.getMold() == null)
				return null;
			Integer pm = m.getMold().getPreventCycle();
			Integer cs = m.getMold().getLastShot();
			Instant firstShotCountAt = null;

			Statistics first = statisticsRepository.findFirstByMoldIdOrderByDayAsc(m.getMold().getId()).orElse(null);
			if (first != null && !StringUtils.isEmpty(first.getHour())) {
				firstShotCountAt = DateUtils.getInstant(first.getHour() + "0000", DateUtils.DEFAULT_DATE_FORMAT);
			}
			if (firstShotCountAt == null) {
				log.error("[jobCalculationDueDateOfMaintenance] " + m.getMold().getEquipmentCode() + " firstShotCountAt null");
				return false;
			}
			Integer t = Long.valueOf(Math.round(Double.valueOf(Instant.now().toEpochMilli() - firstShotCountAt.toEpochMilli()) / (24 * 3600 * 1000))).intValue();
			if (pm == null || pm == 0) {
				log.error("[jobCalculationDueDateOfMaintenance] " + m.getMold().getEquipmentCode() + " pm zero");
				return false;
			}
			if (t.equals(0) || cs == null || cs <= 0) {
				log.error("[jobCalculationDueDateOfMaintenance] " + m.getMold().getEquipmentCode() + " t " + t + " cs " + cs);
				return false;
			}
			Integer tms = cs;

			MoldMaintenance last = moldMaintenanceRepository.findFirstByMoldAndMaintenanceStatusOrderByUpdatedAtDesc(m.getMold(), MaintenanceStatus.DONE).orElse(null);
			if (last != null) {
				tms = cs - last.getShotCount();
			}
			Integer due = 0;
			//daysRemaining
			if (tms < pm) {
				due = (((cs / pm + 1) * pm) - cs) * t / cs;
			}
			//daysOver
			if (tms > pm) {
				due = (((cs / pm) * pm) - cs) * t / cs;
			}

			m.setDueDate(due);
			moldMaintenanceRepository.save(m);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Integer getScLastMaintenance(Long moldId) {
		MoldMaintenance lastDone = moldMaintenanceRepository.findFirstByMoldIdAndMaintenanceStatusIsInOrderByShotCountDesc(moldId, Arrays.asList(MaintenanceStatus.DONE))
				.orElse(null);
		Integer scLastMaintenance = lastDone != null ? (lastDone.getShotCount() != null ? lastDone.getShotCount() : 0) : 0;
//		scLastMaintenance = scLastMaintenance != null ? scLastMaintenance : 0;

		return scLastMaintenance;
	}

	public Boolean procMaintenanceDueDate(MoldMaintenance data, Integer scLastMaintenance) {
		try {
			if (data.getMold() == null) {
				return null;
			}

			if (scLastMaintenance >= data.getMold().getLastShot()) {
				log.error("[jobCalculationDueDateOfMaintenance] " + data.getMold().getEquipmentCode() + " scLastMaintenance invalid " + scLastMaintenance);
				return false;
			}

			Integer mi = data.getMold().getPreventCycle();
			mi = mi != null ? mi : 50000;
			Integer umt = data.getMold().getPreventUpcoming();
			umt = umt != null ? umt : 10000;
//			Integer omt = data.getMold().getPreventOverdue();
//			omt = omt != null ? omt : 5000;
			Integer omt = 0;

			Integer csc = data.getMold().getLastShot();
			Instant firstShotCountAt = null;

			/*
			Statistics first = statisticsRepository.findFirstByMoldIdAndScGreaterThanOrderByScAscIdAsc(m.getMold().getId(), 0).orElse(null);
			if (first == null)
				first = statisticsRepository.findFirstByMoldIdOrderByLstAsc(m.getMold().getId()).orElse(null);
			*/
			Statistics first = statisticsService.firstStatistics(data.getMoldId());
			if (first != null && !StringUtils.isEmpty(first.getHour())) {
				firstShotCountAt = DateUtils.getInstant(first.getHour() + "0000", DateUtils.DEFAULT_DATE_FORMAT);
			}
			if (firstShotCountAt == null) {
				log.error("[jobCalculationDueDateOfMaintenance] " + data.getMold().getEquipmentCode() + " firstShotCountAt null");
				return false;
			}
			Integer t = Long.valueOf(Math.round(Double.valueOf(Instant.now().toEpochMilli() - firstShotCountAt.toEpochMilli()) / (24 * 3600 * 1000))).intValue();
			if (mi == null || mi == 0) {
				log.error("[jobCalculationDueDateOfMaintenance] " + data.getMold().getEquipmentCode() + " pm zero");
				return false;
			}
			if (t.equals(0) || csc == null || csc <= 0) {
				log.error("[jobCalculationDueDateOfMaintenance] " + data.getMold().getEquipmentCode() + " t " + t + " cs " + csc);
				return false;
			}
			if (MaintenanceStatus.UPCOMING.equals(data.getMaintenanceStatus())) {
				int maxModCs = csc - omt - scLastMaintenance;
				maxModCs = maxModCs > 0 ? maxModCs : 0;

				int nextMiOmt = ((maxModCs / mi + 1) * mi + omt) + scLastMaintenance;

				Double dua = (nextMiOmt - csc) / (Double.valueOf(csc) / t);
				data.setDueDate(dua.intValue());
			} else if (MaintenanceStatus.OVERDUE.equals(data.getMaintenanceStatus())) {
				int numShotFromMaintenance = (csc - scLastMaintenance);

				int cscOverDue = (numShotFromMaintenance / mi) * mi + omt + scLastMaintenance;
				Statistics statisticsOverDue = statisticsRepository.findFirstByMoldIdAndScGreaterThanOrderByScAscIdAsc(data.getMold().getId(), cscOverDue).orElse(null);
				if (statisticsOverDue == null) {
					log.info("[jobCalculationDueDateOfMaintenance] null statistics for cal over due Date");
					data.setDueDate(0);
				} else {
					Instant fromDate = DateUtils.getInstant(statisticsOverDue.getLst(), DateUtils.DEFAULT_DATE_FORMAT);
					Integer due = Long.valueOf((Instant.now().toEpochMilli() - fromDate.toEpochMilli()) / (24 * 3600 * 1000)).intValue();
					data.setDueDate(due);
				}
			}

			/*
			Integer tms = csc;

			MoldMaintenance last = moldMaintenanceRepository.findFirstByMoldAndMaintenanceStatusOrderByUpdatedAtDesc(m.getMold(), MaintenanceStatus.DONE).orElse(null);
			if (last != null) {
				tms = csc - last.getShotCount();
			}
			Integer due = 0;
			//daysRemaining
			if (tms < mi) {
				due = (((csc / mi + 1) * mi) - csc) * t / csc;
			}
			//daysOver
			if (tms > mi) {
				due = (((csc / mi) * mi) - csc) * t / csc;
			}

			m.setDueDate(due);
			*/
			moldMaintenanceRepository.save(data);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Long> findMoldIdsFromProductivitySearchPayload(ProductivitySearchPayload payload) {
		return moldRepository.findMoldIdsFromProductivitySearchPayload(payload);
	}

	public ProductivityOverviewData findMaxProductivity(List<Long> moldIds, ProductivitySearchPayload payload) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson") ? true : false;
		return moldRepository.findMaxProductivity(moldIds, payload, isDyson);
	}

	public RestDataList<ToolingProductivityData> findToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable, boolean hasCount) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson") ? true : false;
		ProductivityOverviewData producedPartQuantity = partService.findProductivityOverviewData(moldIds, payload);

		List<ToolingProductivityData> result = moldRepository.findToolingProductivity(moldIds, payload, isDyson, pageable);
		Long total = 0l;
		if (hasCount)
			total = moldRepository.countToolingProductivity(moldIds, payload, isDyson);

		List<ToolingProductivityData> resultByTooling = null;
		if (Arrays.asList(CompareType.SUPPLIER, CompareType.TOOLMAKER).contains(payload.getCompareBy())) {
			ProductivitySearchPayload subPayload = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
			subPayload.setCompareBy(CompareType.TOOL);
			resultByTooling = moldRepository.findToolingProductivity(moldIds, subPayload, isDyson, null);
		}
		List<ChartData> allChartData = moldRepository.findDailyProducedQuantity(moldIds, payload, isDyson);

		if (payload.getDuration() != null) {
			calculateTrend(result, moldIds, payload, isDyson, true);
		}

		Integer totalProducedQuantity = result.stream().map(x -> x.getProducedQuantity()).reduce(0, Integer::sum);
		for (ToolingProductivityData tooling : result) {

//		result.forEach(tooling -> {
//            ToolingProductivityData data = maxToolingProductivity.stream().filter(x -> x.getMoldId().equals(tooling.getMoldId())).findAny().orElse(null);
//			if(data != null){
//				tooling.setMaxCapacity(data.getMaxCapacity());
//				tooling.setDailyMaxCapacity(data.getDailyMaxCapacity());
//			}
//
//			Mold mold = molds.stream().filter(x -> x.getId().equals(tooling.getMoldId())).findAny().orElse(null);
//			tooling.setMold(mold);
			/*
						if(producedPartQuantity.getTotalProductivity()!=null && producedPartQuantity.getTotalProductivity()>0){
							tooling.setPercentageProductivity(Double.valueOf(tooling.getProducedQuantity()) * 100 / producedPartQuantity.getTotalProductivity());
						}
			*/
			if (tooling.getProducedQuantity() != null && tooling.getMaxCapacity() != null && tooling.getMaxCapacity() != 0) {
				tooling.setPercentageProductivity(Double.valueOf(tooling.getProducedQuantity()) * 100 / tooling.getMaxCapacity());
			}
			if (producedPartQuantity.getTotalProductivity() != null && producedPartQuantity.getTotalProductivity() > 0) {
				tooling.setPercentageTotalProductivity(Double.valueOf(tooling.getProducedQuantity()) * 100 / producedPartQuantity.getTotalProductivity());
			} else if (totalProducedQuantity.intValue() != 0)
				tooling.setPercentageTotalProductivity(Double.valueOf(tooling.getProducedQuantity()) * 100 / totalProducedQuantity);

			if (Arrays.asList(CompareType.SUPPLIER, CompareType.TOOLMAKER).contains(payload.getCompareBy())) {
				List<ChartData> toolingChartData = allChartData.stream().filter(x -> tooling.getId() == x.getCompanyId() || tooling.getId().equals(x.getCompanyId()))
						.collect(Collectors.toList());
				Long meetTargetDay = 0l;
				Long lowProductivityDay = 0l;
				Long numTool = toolingChartData.stream().map(x -> x.getMoldCode()).distinct().count();
				for (ToolingProductivityData toolingProductivityData : resultByTooling) {
					meetTargetDay += toolingChartData.stream().filter(x -> x.getMoldCode().equalsIgnoreCase(toolingProductivityData.getId().toString()) && x.getData() != null
							&& toolingProductivityData.getDailyMaxCapacity() != null && x.getData() >= toolingProductivityData.getDailyMaxCapacity()).count();
					lowProductivityDay += toolingChartData.stream()
							.filter(x -> x.getMoldCode().equalsIgnoreCase(toolingProductivityData.getId().toString()) && x.getData() != null
									&& toolingProductivityData.getDailyMaxCapacity() != null && x.getData() > 0 && x.getData() < toolingProductivityData.getDailyMaxCapacity())
							.count();
				}
				if (numTool > 0) {
					tooling.setMeetTargetDay(Double.valueOf(meetTargetDay) / numTool);
					tooling.setLowProductivityDay(Double.valueOf(lowProductivityDay) / numTool);
				} else {
					tooling.setMeetTargetDay(0d);
					tooling.setLowProductivityDay(0d);
				}
			} else {

				List<ChartData> toolingChartData = allChartData.stream().filter(x -> x.getMoldCode().equalsIgnoreCase(tooling.getId().toString())).collect(Collectors.toList());
//			tooling.setChartData(toolingChartData);

				tooling.setMeetTargetDay(Double.valueOf(toolingChartData.stream()
						.filter(x -> x.getData() != null && tooling.getDailyMaxCapacity() != null && x.getData() >= tooling.getDailyMaxCapacity()).count()));
				tooling.setLowProductivityDay(Double.valueOf(toolingChartData.stream()
						.filter(x -> x.getData() != null && tooling.getDailyMaxCapacity() != null && x.getData() > 0 && x.getData() < tooling.getDailyMaxCapacity()).count()));
			}

			if (payload.getDuration() != null) {
				tooling.setNoOperationDay(payload.getDuration() - tooling.getMeetTargetDay() - tooling.getLowProductivityDay());
			} else {

			}
//		});
		}
		// part production
		List<Long> toolingIdList = moldIds;
		List<Long> companyIds = null;
		if (Arrays.asList(CompareType.TOOL).contains(payload.getCompareBy())) {
			toolingIdList = result.stream().map(r -> r.getMoldId()).collect(Collectors.toList());
		} else {
			companyIds = result.stream().map(r -> r.getId()).collect(Collectors.toList());
		}
		List<PartProductionData> partProductionDataList = moldRepository.findPartProduction(toolingIdList, payload.getPartId(), payload.getStartDate(), payload.getEndDate(),
				payload.getCompareBy(), companyIds, null);
		Map<Long, List<PartProductionData>> mapPartProduction = partProductionDataList.stream()
				.collect(Collectors.groupingBy(PartProductionData::getGroupId, HashMap::new, Collectors.toCollection(ArrayList::new)));
		result.stream().forEach(tooling -> {
			if (mapPartProduction.containsKey(tooling.getId())) {
				tooling.setPartProductionList(mapPartProduction.get(tooling.getId()));
				tooling.setNumberPart(mapPartProduction.get(tooling.getId()) != null ? mapPartProduction.get(tooling.getId()).size() : null);
			}
		});

		if (pageable != null) {
			Sort.Direction[] directions = { Sort.Direction.DESC };
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
			});
			if (directions[0].isAscending())
				result = result.stream()
						.sorted(Comparator.nullsFirst(Comparator.comparing(ToolingProductivityData::getProducedQuantity, Comparator.nullsFirst(Comparator.naturalOrder()))))
						.collect(Collectors.toList());
			else
				result = result.stream()
						.sorted(Comparator
								.nullsLast(Comparator.comparing(ToolingProductivityData::getProducedQuantity, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()))
						.collect(Collectors.toList());
		}

		return new RestDataList<>(total, result);
	}

	public List<ToolingProductivityData> findToolingDetailsListProductivity(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson") ? true : false;
		List<ToolingProductivityData> result = moldRepository.findToolingProductivity(moldIds, payload, isDyson, null);
		List<ToolingProductivityData> resultByTooling = new ArrayList<>();
		if (Arrays.asList(CompareType.SUPPLIER, CompareType.TOOLMAKER).contains(payload.getCompareBy())
				&& payload.getVariable().equals(ProductivityVariable.AVAILABLE_PRODUCTIVITY)) {
			ProductivitySearchPayload subPayload = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
			subPayload.setCompareBy(CompareType.TOOL);
			resultByTooling = moldRepository.findToolingProductivity(moldIds, subPayload, isDyson, null);
			//convert to available production
			resultByTooling.stream().forEach(
					tooling -> tooling.setMaxCapacity(tooling.getMaxCapacity() > tooling.getProducedQuantity() ? tooling.getMaxCapacity() - tooling.getProducedQuantity() : 0));
		}
		final List<ToolingProductivityData> resultByToolingFinal = resultByTooling;

		Sort.Direction[] directions = { Sort.Direction.DESC };
		if (pageable != null) {
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
			});
		}

		List<ToolingProductivityData> sortedResult = new ArrayList<>();
		if (payload.getVariable() == null || payload.getVariable().equals(ProductivityVariable.PRODUCTIVITY)
				|| payload.getVariable().equals(ProductivityVariable.AVAILABLE_PRODUCTIVITY)) {
			if (payload.getVariable() == null || payload.getVariable().equals(ProductivityVariable.PRODUCTIVITY)) {
				result.forEach(tooling -> tooling.setPercentageProductivity(Double.valueOf(tooling.getProducedQuantity()) * 100 / tooling.getMaxCapacity()));
				if (directions[0].isAscending())
					sortedResult = result.stream()
							.sorted(Comparator
									.nullsFirst(Comparator.comparing(ToolingProductivityData::getPercentageProductivity, Comparator.nullsFirst(Comparator.naturalOrder()))))
							.collect(Collectors.toList());
				else
					sortedResult = result.stream()
							.sorted(Comparator.nullsLast(
									Comparator.comparing(ToolingProductivityData::getPercentageProductivity, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()))
							.collect(Collectors.toList());
			} else {

				if (CompareType.SUPPLIER.equals(payload.getCompareBy()))
					result.forEach(tooling -> {
						Integer maxCapacity = resultByToolingFinal.stream().filter(r -> r.getMold() != null && r.getMold().getLocation() != null
								&& r.getMold().getLocation().getCompanyId() != null && r.getMold().getLocation().getCompanyId().equals(tooling.getId()))
								.map(r -> r.getMaxCapacity()).reduce(0, Integer::sum);
						tooling.setMaxCapacity(maxCapacity);
					});
				else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
					result.forEach(tooling -> {
						Integer maxCapacity = resultByToolingFinal.stream()
								.filter(r -> r.getMold() != null && r.getMold().getToolMakerCompanyId() != null && r.getMold().getToolMakerCompanyId().equals(tooling.getId()))
								.map(r -> r.getMaxCapacity()).reduce(0, Integer::sum);
						tooling.setMaxCapacity(maxCapacity);
					});
				} else
					result.forEach(tooling -> tooling
							.setMaxCapacity(tooling.getMaxCapacity() != null && tooling.getProducedQuantity() != null && tooling.getMaxCapacity() > tooling.getProducedQuantity()
									? tooling.getMaxCapacity() - tooling.getProducedQuantity()
									: 0));

				if (directions[0].isAscending())
					sortedResult = result.stream()
							.sorted(Comparator.nullsFirst(Comparator.comparing(ToolingProductivityData::getMaxCapacity, Comparator.nullsFirst(Comparator.naturalOrder()))))
							.collect(Collectors.toList());
				else
					sortedResult = result.stream()
							.sorted(Comparator
									.nullsLast(Comparator.comparing(ToolingProductivityData::getMaxCapacity, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()))
							.collect(Collectors.toList());
			}
			return sortedResult;
		} else if (payload.getVariable().equals(ProductivityVariable.PRODUCED_QUANTITY)) {
			if (directions[0].isAscending())
				sortedResult = result.stream()
						.sorted(Comparator.nullsFirst(Comparator.comparing(ToolingProductivityData::getProducedQuantity, Comparator.nullsFirst(Comparator.naturalOrder()))))
						.collect(Collectors.toList());
			else
				sortedResult = result.stream()
						.sorted(Comparator
								.nullsLast(Comparator.comparing(ToolingProductivityData::getProducedQuantity, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()))
						.collect(Collectors.toList());
			return sortedResult;
		} else if (payload.getDuration() != null) {
			calculateTrend(result, moldIds, payload, isDyson, false);
			if (directions[0].isAscending())
				sortedResult = result.stream()
						.sorted(Comparator.nullsFirst(Comparator.comparing(ToolingProductivityData::getTrend, Comparator.nullsFirst(Comparator.naturalOrder()))))
						.collect(Collectors.toList());
			else
				sortedResult = result.stream()
						.sorted(Comparator.nullsLast(Comparator.comparing(ToolingProductivityData::getTrend, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()))
						.collect(Collectors.toList());
			return sortedResult;
		}
		return result;
	}

	private void calculateTrend(List<ToolingProductivityData> result, List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson, boolean getChart) {
		ProductivitySearchPayload subPayload = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
		subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2));
		subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1));
		/*
				ProductivitySearchPayload subPayload = ProductivitySearchPayload.builder()
						.partId(payload.getPartId())
						.startDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2))
						.endDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1))
						.compareBy(payload.getCompareBy())
						.build();
		*/
		List<ToolingProductivityData> fifthList = moldRepository.findToolingProductivity(moldIds, payload, isDyson, null);
		List<ToolingProductivityData> forthList = moldRepository.findToolingProductivity(moldIds, subPayload, isDyson, null);
//        if(!getAll){
		result.forEach(tooling -> {
			ToolingProductivityData fifth = fifthList.stream().filter(x -> x.getId().equals(tooling.getId())).findAny().orElse(null);
			ToolingProductivityData forth = forthList.stream().filter(x -> x.getId().equals(tooling.getId())).findAny().orElse(null);
			Double forthPer = forth!=null && forth.getPercentageProductivity() != null ? forth.getPercentageProductivity() : 0;
			if (fifth != null ) {
//					Double trend = forth.getProducedQuantity() == 0 ? -999999 :
//							(fifth.getProducedQuantity() - forth.getProducedQuantity()) * 100.0 / forth.getProducedQuantity();
				Double fifthPer = fifth.getPercentageProductivity() != null ? fifth.getPercentageProductivity() : 0;
				Double trend = fifthPer - forthPer;
				tooling.setTrend(trend);
			}else tooling.setTrend(0.0);
		});
		if (getChart) {
			//todo: load data capacity
			List<ToolingProductivityData>[] listDataPerPeriodic = new ArrayList[4];
			listDataPerPeriodic[0] = fifthList;
			listDataPerPeriodic[1] = forthList;
			for (int lstT = 2; lstT < listDataPerPeriodic.length; lstT++) {
				ProductivitySearchPayload subPayload2 = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
				subPayload2.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * (lstT + 1)));
				subPayload2.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * lstT + 1));
				listDataPerPeriodic[lstT] = moldRepository.findToolingProductivity(moldIds, subPayload2, isDyson, null);
			}
			List<String> titlesListPeriodic = new ArrayList<>();
			for (int lstT = 0; lstT < listDataPerPeriodic.length; lstT++) {
				titlesListPeriodic.add(DateTimeUtils.getTitleRangDate(Instant.now().minus(payload.getDuration() * (lstT + 1), ChronoUnit.DAYS),
						Instant.now().minus(payload.getDuration() * lstT + 1, ChronoUnit.DAYS)));
			}
			result.forEach(p -> {
				List<MoldCapacityReportData> capacityReportDataList = new ArrayList<>();
				for (int i = listDataPerPeriodic.length - 1; i >= 0; i--) {
					ToolingProductivityData productivityData = listDataPerPeriodic[i].stream().filter(x -> x.getId().equals(p.getId())).findAny().orElse(null);
					if (productivityData != null) {
						MoldCapacityReportData chartItem = MoldCapacityReportData.builder().title(titlesListPeriodic.get(i)).maxCapacity(productivityData.getMaxCapacity())
								.outputCapacity(productivityData.getProducedQuantity().longValue()).availableDowntime(productivityData.getAvailableDowntime().longValue()).build();
						Long availableOutput = chartItem.getMaxCapacity() - chartItem.getOutputCapacity() - chartItem.getAvailableDowntime();
						chartItem.setAvailableOutput(availableOutput > 0 ? availableOutput : 0);
						Long overCapacity = chartItem.getOutputCapacity() > chartItem.getMaxCapacity() ? chartItem.getOutputCapacity() - chartItem.getMaxCapacity() : 0;
						chartItem.setOverCapacity(overCapacity);
						chartItem.makePercent();
						capacityReportDataList.add(chartItem);
					} else if (capacityReportDataList.size() > 0) {
						MoldCapacityReportData chartItem = MoldCapacityReportData.builder().title(titlesListPeriodic.get(i))
								.maxCapacity(capacityReportDataList.get(0).getMaxCapacity()).availableOutput(capacityReportDataList.get(0).getMaxCapacity().longValue())
								.availableOutputPercent(100.0).outputCapacity(0L).overCapacity(0L).availableDowntime(0L).build();
						chartItem.makePercent();
						capacityReportDataList.add(chartItem);
					} else {
						MoldCapacityReportData chartItem = MoldCapacityReportData.builder().title(titlesListPeriodic.get(i))
								.maxCapacity(p.getMaxCapacity()).availableOutput(p.getMaxCapacity().longValue())
								.availableOutputPercent(0.0).outputCapacity(0L).overCapacity(0L).availableDowntime(0L).build();
						chartItem.makePercent();
						capacityReportDataList.add(chartItem);
					}
				}
				p.setCapacityReportDataList(capacityReportDataList);
			});
		}
	}

	public List<WUTFullData> calculateWUT(Long moldId, String startTime, String endTime) throws JSONException {
		List<MoldCttTempData> rawData = moldRepository.findMoldCttTempData(moldId, startTime, endTime);
		return calculateWUT(rawData);
	}

	public List<WUTFullData> calculateWUT(List<MoldCttTempData> rawData) throws JSONException {
		Map<Integer, List<MoldCttTempData>> separatedRawData = new HashMap<>();
		int index = -1;
		for (int i = 0; i < rawData.size(); i++) {
			if (i < rawData.size() - 1 && rawData.get(i).getUptimeSeconds() == 0 && rawData.get(i + 1).getUptimeSeconds() > 0) {
				index++;
				List<MoldCttTempData> element = new ArrayList<>();
				separatedRawData.put(index, element);
			} else if (rawData.get(i).getUptimeSeconds() > 0) {
				if (separatedRawData.get(index) == null) {
					index++;
					separatedRawData.put(index, new ArrayList<>());
				}
				separatedRawData.get(index).add(rawData.get(i));
			}
		}

		Map<Integer, Double> downTimeMap = new HashMap<>();
		for (int i = 0; i < separatedRawData.size() - 1; i++) {
			List<MoldCttTempData> firstList = separatedRawData.get(i);
			List<MoldCttTempData> secondList = separatedRawData.get(i + 1);
			Instant startDownTime = DateUtils.getInstant(firstList.get(firstList.size() - 1).getLst(), "yyyyMMddHHmmss");
			String endDownTimeString = DateUtils.getReducedTime(secondList.get(0).getLst(), "yyyyMMddHHmmss", 1l, ChronoUnit.HOURS);
			Instant endDownTime = DateUtils.getInstant(endDownTimeString, "yyyyMMddHHmmss");
			double gap = (double) ((endDownTime.getEpochSecond() - startDownTime.getEpochSecond()) / 3600);
			downTimeMap.put(i, gap);
		}

		Map<Integer, BasedWUTData> wutByTempData = wutService.calculateWUTByTemp(separatedRawData);

		List<WUTData> wutData = new ArrayList<>();
		separatedRawData.forEach((k, v) -> {
			Long totalUptime = v.stream().map(x -> wutService.getUptimeSecondsByCtt(x)).reduce(0L, Long::sum);
			// nullpointexception 에러 수정
			//Integer totalShot = v.stream().map(x -> x.getShotCount()).reduce(0, Integer::sum);
			Integer totalShot = v.stream().map(x -> x.getShotCount() == null ? 0 : x.getShotCount()).reduce(0, Integer::sum);
			Double wut = 0.0;
			int warmUpShotCount = 0;
			double normalTimeSecond = 0.0;
			int indexStartNormal = 0;
			double firstNormalTimePeriod = 0.0;
			int firstNormalTimeShotCount = 0;
			String startWarmUpTime = "";
			String endSection = v.get(v.size() - 1).getLst();
			boolean isValid = false;
			if (wutByTempData.get(k) != null && wutByTempData.get(k).getWut() >= 15 && wutByTempData.get(k).getWut() <= (Double.valueOf(totalUptime) / 60) / 2) {
				wut = wutByTempData.get(k).getWut() * 60;
				startWarmUpTime = DateUtils.getReducedTime(v.get(0).getLst(), "yyyyMMddHHmmss", 1L, ChronoUnit.HOURS);
				int i;
				for (i = 0; i <= (wutByTempData.get(k).getIndexWUT() + 1) / 6; i++) {
					if (i == (wutByTempData.get(k).getIndexWUT() + 1) / 6) {
						int rate = (wutByTempData.get(k).getIndexWUT() + 1) % 6;
						if (rate > 0) {
							firstNormalTimeShotCount = v.get(i).getShotCount() * (6 - rate) / 6;
							warmUpShotCount += v.get(i).getShotCount() - firstNormalTimeShotCount;
							firstNormalTimePeriod = wutService.getUptimeSecondsByCtt(v.get(i)) * (6 - rate) / 6;
							normalTimeSecond += firstNormalTimePeriod;

							indexStartNormal = i + 1;
						} else {
							indexStartNormal = i;
							break;
						}
					} else {
						warmUpShotCount += v.get(i).getShotCount();
					}
				}

				for (; i < v.size() - 1; i++) {
					normalTimeSecond += wutService.getUptimeSecondsByCtt(v.get(i));
				}

				isValid = true;
			} else {
				BasedWUTData wutCttBased = wutService.calculateWUTByCtt(v);
				wut = wutCttBased.getWut();
				if (wut != null && wut <= Double.valueOf(totalUptime) / 2) {
					startWarmUpTime = DateUtils.getReducedTime(v.get(wutCttBased.getIndex()).getLst(), "yyyyMMddHHmmss", wut.longValue(), ChronoUnit.SECONDS);
					int i;
					for (i = 0; i <= wutCttBased.getIndex(); i++) {
						warmUpShotCount += v.get(i).getShotCount();
					}
					indexStartNormal = i;
					for (; i < v.size() - 1; i++) {
						normalTimeSecond += wutService.getUptimeSecondsByCtt(v.get(i));
					}
					isValid = true;
				}
			}
			if (isValid) {
				if (v.size() > 0) {
					SectionData warmUpTime = SectionData.builder().hour(wut / 3600).shotCount(warmUpShotCount).startedAt(startWarmUpTime).build();

					SectionData coolDownTime = SectionData.builder().hour((double) wutService.getUptimeSecondsByCtt(v.get(v.size() - 1)) / 3600)
							.shotCount(v.get(v.size() - 1).getShotCount())
							.startedAt(v.size() > 1 && v.get(v.size() - 2) != null ? v.get(v.size() - 2).getLst() : v.get(v.size() - 1).getLst()).build();

					String startNormalTime = DateUtils.getAddedTime(startWarmUpTime, "yyyyMMddHHmmss", wut.longValue(), ChronoUnit.SECONDS);

					SectionData normalTime = SectionData.builder()
							//						.hour((totalUptime / 3600) - warmUpTime.getHour() - coolDownTime.getHour())
							.hour(normalTimeSecond / 3600).shotCount(totalShot - warmUpTime.getShotCount() - coolDownTime.getShotCount()).startedAt(startNormalTime).build();

					SectionData downTime = SectionData.builder().hour(downTimeMap.get(k) != null ? Double.valueOf(downTimeMap.get(k)) : 0.0).startedAt(v.get(v.size() - 1).getLst())
							.build();

					WUTData aData = WUTData.builder().index(k).indexStartNormal(indexStartNormal).firstNormalTimePeriod(firstNormalTimePeriod / 3600)
							.firstNormalTimeShotCount(firstNormalTimeShotCount).endSection(endSection).warmUpTime(warmUpTime).normalTime(normalTime).coolDownTime(coolDownTime)
							.downTime(downTime).build();
					wutData.add(aData);
				}
			} else {
				if (v.size() > 0) {
					SectionData abnormalTime = SectionData.builder().hour(Double.valueOf(totalUptime) / 3600).shotCount(totalShot)
							.startedAt(DateUtils.getReducedTime(v.get(0).getLst(), "yyyyMMddHHmmss", v.get(0).getUptimeSeconds(), ChronoUnit.SECONDS)).build();

					SectionData downTime = SectionData.builder().hour(downTimeMap.get(k) != null ? Double.valueOf(downTimeMap.get(k)) : 0.0).startedAt(v.get(v.size() - 1).getLst())
							.build();

					WUTData aData = WUTData.builder().index(k).abnormalData(abnormalTime).downTime(downTime).build();
					wutData.add(aData);
				}
			}
		});
		List<WUTFullData> result = wutService.convertData(wutData, separatedRawData);
		result.forEach(data -> {
			log.debug(data.getTitle() + "\t\t\t\t\t" + data.getHour() + "\t\t\t\t" + data.getShotCount() + "\t\t\t\t" + data.getStartedAt() + "\t\t\t\t" + data.getEndAt());
		});
		return result;
	}

	public void setMaxCapacity(Mold mold) {
		setMoldDailyMaxCapacity(mold);
		if (mold.getProductionDays() == null) {
			mold.setProductionDays("7");
		}
		// Calculate max capacity per week
		mold.setMaxCapacityPerWeek(mold.getDailyMaxCapacity() * Integer.valueOf(mold.getProductionDays()));
	}

	@Transactional
	public String resolveDataMatching(String ci) {
		Counter counter = counterRepository.findByEquipmentCode(ci).orElseThrow(
				() -> new BizException("DATA_NOT_FOUND", "Counter code:" + ci + " does not exist!!", new Property("mode", "Counter"), new Property("equipmentCode", ci)));

		Mold mold = counter.getMold();
		if (mold == null) {
			return "Mold is not matched";
		}

		// Adjust Statistics(firstData, fsc, shotCount, moldId, moldCode)
		{
			List<Statistics> statList = statisticsRepository.findByCiAndMoldIdIsNullAndMoldCodeIsNullOrderByIdDesc(ci);
			Integer len = statList.size();
			if (len == 0) {
				return null;
			}

			for (int i = 0; i < len - 1; i++) {
				Statistics stat = statList.get(i);
				Statistics statNext = statList.get(i + 1);
				stat.setFirstData(false);
				stat.setFsc(statNext.getSc());
				stat.setShotCount(stat.getSc() - statNext.getSc());
				stat.setMoldId(mold.getId());
				stat.setMoldCode(mold.getEquipmentCode());
			}
			statList.get(len - 1).setMoldId(mold.getId());
			statList.get(len - 1).setMoldCode(mold.getEquipmentCode());
			statisticsRepository.saveAll(statList);
		}

		// Adjust Statistics(uptimeSeconds)
		List<Statistics> statList = statisticsRepository.findByCi(ci);
		if (statList != null && statList.size() > 0) {
			statList.forEach(stat -> {
				stat.setUptimeSeconds((long) (stat.getCt() * 0.1) * stat.getShotCount());
			});
			statisticsRepository.saveAll(statList);
		}

		// Adjust StatisticsPart
		{
			List<StatisticsPart> list = new ArrayList<>();
			List<MoldPart> moldParts = mold.getMoldParts().stream().filter(part -> part.getCavity() > 0).collect(Collectors.toList());
			statList.forEach(stat -> {
				moldParts.forEach(moldPart -> {
					StatisticsPart statPart = new StatisticsPart();
					statPart.setStatistics(stat);
					if (moldPart.getPart() == null) {
						return;
					}
					Part part = moldPart.getPart();
					statPart.setPartId(part.getId());
					statPart.setPartCode(part.getPartCode());
					statPart.setCavity(moldPart.getCavity());

					if (part.getCategory() != null) {
						Category project = part.getCategory();
						statPart.setProjectId(project.getId());
						statPart.setProjectName(project.getName());

						if (project.getParent() != null) {
							Category category = project.getParent();
							statPart.setCategoryId(category.getId());
							statPart.setCategoryName(category.getName());
						}
					}

					list.add(statPart);
				});
			});
			if (!ObjectUtils.isEmpty(list)) {
				statisticsPartRepository.saveAll(list);
			}
		}

		// Adjust Cdata(moldId, moldCode)
		// Adjust Mold(lastShot, lastShotAt)
		{
			List<Cdata> cdataList = cdataRepository.findByCiAndMoldIdIsNullAndMoldCodeIsNullOrderByIdDesc(ci);
			if (cdataList != null && cdataList.size() > 0) {
				cdataList.forEach(cdata -> {
					cdata.setMoldId(mold.getId());
					cdata.setMoldCode(mold.getEquipmentCode());
				});
				cdataRepository.saveAll(cdataList);

				// Update Mold
				mold.setLastShot(cdataList.get(0).getSc());
				mold.setLastShotAt(cdataList.get(0).getCreatedAt());
			}
		}

		return "Success!";
	}

	private void setMoldDailyMaxCapacity(Mold mold) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson") ? true : false;
		Double dailyMaxCapacity = 0.0;

		OptimalCycleTime oct = MoldUtils.getOptimalCycleTime(mold.getId(), mold.getContractedCycleTimeToCalculation(), null);
		int cycleTime = ((Double) oct.getValue()).intValue();
		if (cycleTime != 0) {
			if (mold.getShiftsPerDay() == null || mold.getShiftsPerDay().equals(""))
				mold.setShiftsPerDay("24");
			if (mold.getProductionDays() == null || mold.getProductionDays().equals(""))
				mold.setProductionDays("7");
			Double uptimeTarget = isDyson ? (mold.getUptimeTarget() != null ? mold.getUptimeTarget() : 90) * 0.01 : 1.0;
			dailyMaxCapacity = (Double.valueOf(mold.getShiftsPerDay()) * 3600 * uptimeTarget) / (cycleTime * 0.1);
			Integer numberOfCavities = mold.getMoldParts().stream().map(x -> x.getCavity()).reduce(0, Integer::sum);
			mold.setDailyMaxCapacity(dailyMaxCapacity.intValue() * (numberOfCavities != null ? numberOfCavities : 0));
		} else {
			mold.setDailyMaxCapacity(0);
		}
	}

	public List<Mold> initDailyMaxCapacity() {

		List<Mold> molds = moldRepository.findAll();
		molds.forEach(mold -> {
			setMaxCapacity(mold);

			// Calculate passed days
			if (mold.getCreatedAt() == null) {
				mold.setPassedDays(0);
				mold.setCreatedAt(Instant.now());
			} else {
				long diffSeconds = Instant.now().getEpochSecond() - mold.getCreatedAt().getEpochSecond();
				long passedDays = diffSeconds / (3600 * 24);
				mold.setPassedDays((int) passedDays);
			}
		});
		return moldRepository.saveAll(molds);
	}

	@Transactional(propagation = Propagation.NEVER)
	public void updatePassedDays() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(MoldService.class, "PASSED_DAY"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			Page<Mold> page;
			int pageNo[] = { 0 };
			int pageSize = 100;
			while (pageNo[0] < 10000 && !(page = TranUtils.doNewTran(() -> moldRepository.findAll(PageRequest.of(pageNo[0]++, pageSize)))).isEmpty()) {
				page.forEach(m -> {
					TranUtils.doNewTran(() -> {
						Mold mold = moldRepository.findWithPessimisticLockById(m.getId()).orElse(null);
						if (mold == null) {
							return;
						}
						// Calculate passed days
						if (mold.getCreatedAt() == null) {
							mold.setPassedDays(0);
							mold.setCreatedAt(Instant.now());
						} else {
							long diffSeconds = Instant.now().getEpochSecond() - mold.getCreatedAt().getEpochSecond();
							long passedDays = diffSeconds / (3600 * 24);
							mold.setPassedDays((int) passedDays);
						}
						save(mold);
					});
				});
			}
		});
	}

	public List<ToolingCycleTimeData> getAvgCycleTimeMoldInRange(List<Long> moldIds, ProductivitySearchPayload payload, CycleTimeStatus status) {
		return moldRepository.getAvgCycleTimeInRange(moldIds, payload, status);
	}

	public List<CycleTimeOverviewDetailData> getReportCycleTimeInRange(List<Long> moldIds, ProductivitySearchPayload payload, DataRangeType dataRangeType) {
		return moldRepository.getReportCycleTimeInRange(moldIds, payload, dataRangeType);
	}

	public List<ToolingCycleTimeData> getComplianceShotCountInRange(List<Long> moldIds, ProductivitySearchPayload payload) {
		return moldRepository.getComplianceShotCountInRange(moldIds, payload);
	}

	public List<ToolingCycleTimeData> findToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable, boolean getAll) {
		return moldRepository.findToolingCycleTimeData(moldIds, payload, pageable, getAll);
	}

	public Long countToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable, boolean getAll) {
		return moldRepository.countToolingCycleTimeData(moldIds, payload, getAll);
	}

	@Async
	public void procWutAll() {
		JobUtils.runIfNotRunning("MoldService::procWutAll", new JobOptions().setClustered(true), () -> this._procWutAll());
	}

	private void _procWutAll() {
		Instant start = Instant.now();
		Long total = moldRepository.count();
		log.info("[calculateWUTAllData] total mold: " + total);
		log.info("[calculateWUTAllData] total:" + total);
		List<Mold> moldList = moldRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
		long success = 0;
		long error = 0;
		for (int i = 0; i < total; i++) {
			try {
				Mold mold = moldList.get(i);
				log.info(i + " process for mold: " + mold.getEquipmentCode());
				List<StatisticsWut> statisticsWutList = calculateWUTForMold(mold, null, null);
				if (statisticsWutList.size() > 0)
					success++;
				//for val
				List<StatisticsWut> statisticsWutBaseList = calculateWUTForMold(mold, null, null, true);
				log.info("StatisticsWut: {} ,StatisticsWutVal: {}", statisticsWutList.size(), statisticsWutBaseList.size());
			} catch (Exception e) {
				e.printStackTrace();
				error++;
				log.error("mold error: " + moldList.get(i).getId());
			}
		}
		log.info("[calculateWUTAllData] success:" + success);
		log.info("[calculateWUTAllData] fail:" + error);
		log.info("[calculateWUTAllData] run time:" + (Instant.now().getEpochSecond() - start.getEpochSecond()) + "s");
	}

	public List<StatisticsWut> calculateWUTForMold(Mold mold, String startTime, String endTime) throws JSONException {
		return calculateWUTForMold(mold, startTime, endTime, false);
	}

	public List<StatisticsWut> calculateWUTForMold(Mold mold, String startTime, String endTime, boolean valData) throws JSONException {
		List<MoldCttTempData> rawData = moldRepository.findMoldCttTempData(mold.getId(), startTime, endTime);
		if (!rawData.isEmpty()) {
			//filter
			int indexStart = 0;
			int indexEnd = -1;
			for (int j = 0; j < rawData.size(); j++) {
				if (rawData.get(j).getUptimeSeconds() == 0) {
					indexStart = j;
					break;
				}
			}
//                    if (indexStart >= 0) {
			for (int j = rawData.size() - 1; j >= indexStart; j--) {
				if (rawData.get(j).getUptimeSeconds() == 0) {
					indexEnd = j;
					break;
				}
			}
//                    }
			if (indexEnd >= 0) {
				List<MoldCttTempData> rawDataCal = new ArrayList<>();
				//ignore data error
				for (int r = indexStart; r <= indexEnd; r++) {
					MoldCttTempData mtd = rawData.get(r);
					if (StringUtils.isEmpty(mtd.getTemp())) {
						continue;
					}

					String[] temp = mtd.getTemp().split("/");
					if (Arrays.asList(temp).contains(""))
						continue;
					rawDataCal.add(mtd);
				}
				log.info("rawDataCal size " + rawDataCal.size());

				List<WUTFullData> wutFullDataList = calculateWUT(rawDataCal);
				log.info("wutFullDataList size " + wutFullDataList.size());
				List<StatisticsWut> statisticsWutList = statisticsWutService.saveList(mold, wutFullDataList, valData);
//					success++;
				return statisticsWutList;
			}
		}
		return new ArrayList();
	}

	public void checkWUTForStatisticsNew(Long moldId, String lst) {
		try {
//			if (insertStats == null) return;
//			Statistics statisticsUptime = insertStats.stream().sorted(Comparator.comparing(Statistics::getLst).reversed())
//					.filter(s -> s.getUptimeSeconds() != null && s.getUptimeSeconds().equals(0l)).findFirst().orElse(null);
			if (!StringUtils.isEmpty(lst)) {

				// check last cdata exits
				Cdata last = cdataRepository.findFirstByMoldIdAndLstLessThanOrderByLstDesc(moldId, lst).orElse(null);
				if (last != null) {
					Statistics statisticsLast = statisticsRepository.findFirstByCdataId(last.getId()).orElse(null);
					if (statisticsLast != null && statisticsLast.getUptimeSeconds() != null && statisticsLast.getUptimeSeconds().intValue() == 0) {
						log.info("Exist statisticsLast UptimeSeconds eq 0 " + statisticsLast.getId());
						return;
					}
				}

				String startTime = null;
				Mold mold = findById(moldId);

				log.info("checkWUTForStatisticsNew mold: {} ", mold);

				if (mold != null) {
					log.info("checkWUTForStatisticsNew mold.getEquipmentCode(): {} ", mold.getEquipmentCode());
					StatisticsWut statisticsWut = statisticsWutRepository.findFirstByMoldIdAndValDataOrderByStartedAtDesc(mold.getId(), false).orElse(null);
					if (statisticsWut != null) {
						Instant startTimeIns = DateUtils.getInstant(statisticsWut.getStartedAt(), DateUtils.DEFAULT_DATE_FORMAT);
						if (statisticsWut.getHourValue() > 0) {
							startTimeIns = startTimeIns.plusSeconds(Double.valueOf(statisticsWut.getHourValue() * 3600).longValue());
						}
						startTime = DateUtils.getDate(startTimeIns, DateUtils.DEFAULT_DATE_FORMAT);
					}
					calculateWUTForMold(mold, startTime, lst);
					calculateWUTForMold(mold, startTime, lst, true);
				}
			}
		} catch (Exception e) {
			log.error("checkWUTForStatisticsNew Catch Exception: {}", e.getMessage(), e);
		}
	}

	public void initValueOperatedStart() {
		Long total = moldRepository.count();
		int pageSize = 10;
		int numPage = Long.valueOf(total / pageSize + (total % pageSize == 0 ? 0 : 1)).intValue();
		log.info("Start [initValueOperatedStart] current size " + total);
		Instant fromT = Instant.now();
		int addNew = 0;
		for (int page = 0; page < numPage; page++) {
			Page<Mold> shotList = moldRepository.findAll(PageRequest.of(page, pageSize));
			List<Mold> listSave = new ArrayList<>();
			for (int i = 0; i < shotList.getContent().size(); i++) {
				Mold m = shotList.getContent().get(i);
//				Statistics statisticsLast = statisticsRepository.findFirstByMoldIdOrderByLstAsc(m.getId()).orElse(null);
				Statistics statisticsLast = statisticsService.firstStatistics(m.getId());
				if (statisticsLast != null && !StringUtils.isEmpty(statisticsLast.getLst())) {
//					if(m.getOperatedStartAt()==null){
					m.setOperatedStartAt(DateUtils.getInstant(statisticsLast.getLst(), DateUtils.DEFAULT_DATE_FORMAT));
					addNew++;
					listSave.add(m);
//					}
				}
			}
			moldRepository.saveAll(listSave);
		}
		log.info("End [initValueOperatedStart] day " + DateUtils.getDay(Instant.now().minus(1, ChronoUnit.DAYS)) + " dua time "
				+ (Instant.now().getEpochSecond() - fromT.getEpochSecond()) + "s new: " + addNew);

	}

	public Page<MoldDetachment> findMoldDetachmentAll(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldDetachment> list = moldDetachmentRepository.findAllOrderByOperatingStatus(predicate, pageable);
			return new PageImpl<>(list, pageable, moldDetachmentRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldDetachment> list = moldDetachmentRepository.findAllOrderByStatus(predicate, pageable);
			return new PageImpl<>(list, pageable, moldDetachmentRepository.count(predicate));
		}
		return moldDetachmentRepository.findAll(predicate, pageable);
	}

	public MoldDetachment findMoldDetachmentById(Long id) {
		return moldDetachmentRepository.findById(id).orElseGet(() -> new MoldDetachment());
	}

	public MoldDetachment createOrUpdateMoldDetachment(Mold mold, EquipmentStatus statusOld, EquipmentStatus counterStatusNew) {
		MoldDetachment moldDetachment = null;
		if (mold != null && (counterStatusNew == EquipmentStatus.DETACHED || statusOld == EquipmentStatus.DETACHED) && statusOld != counterStatusNew) {
			//notion for
			List<MoldDetachment> moldDetachmentList = moldDetachmentRepository.findByLatestAndMold(true, mold);
			moldDetachment = moldDetachmentList.stream().filter(m -> Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.ALERT).contains(m.getNotificationStatus()))
					.findFirst().orElse(null);
			if (counterStatusNew == EquipmentStatus.DETACHED) {
				if (!moldDetachmentList.isEmpty()) {
					moldDetachmentList.stream().filter(m -> Arrays.asList(NotificationStatus.ALERT).contains(m.getNotificationStatus())).forEach(o -> o.setLatest(false));
					moldDetachmentRepository.saveAll(moldDetachmentList);
				}
				moldDetachment = new MoldDetachment(mold, Instant.now(), null);
				moldDetachment.setCreatedAt(Instant.now());
				moldDetachmentRepository.save(moldDetachment);
				processAlert(moldDetachment, mold);
			} else if (moldDetachment != null && statusOld == EquipmentStatus.DETACHED
					&& Arrays.asList(EquipmentStatus.AVAILABLE, EquipmentStatus.FAILURE, EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED, EquipmentStatus.INSTALLED).contains(counterStatusNew)) {

				moldDetachmentList.stream().forEach(o -> o.setLatest(false));
				moldDetachmentRepository.saveAll(moldDetachmentList);

				moldDetachment.setEquipmentStatus(counterStatusNew);
				moldDetachment.setNotificationStatus(NotificationStatus.FIXED);
				moldDetachment.setRepairTime(Instant.now());
				moldDetachment.setLatest(true);
				//set message
				if (EquipmentStatus.AVAILABLE.equals(counterStatusNew)) {
					moldDetachment.setMessage("The counter status is changed to \"Available\".");
				} else if (EquipmentStatus.FAILURE.equals(counterStatusNew)) {
					moldDetachment.setMessage("The counter status is changed to \"Failure\".");
				} else if (EquipmentStatus.DISCARDED.equals(counterStatusNew)) {
					moldDetachment.setMessage("The counter is discarded.");
				}
				moldDetachment.setConfirmedAt(Instant.now());
				if (SecurityUtils.isLogin()) {
					moldDetachment.setConfirmedBy(SecurityUtils.getName());
				}

				moldDetachmentRepository.save(moldDetachment);
			}
		}
		return moldDetachment;

	}

	private void processAlert(MoldDetachment moldDetachment, Mold mold) {

		// 3. Generate log user alert
		/*
				List<User> supplierList = subTierService.getTierSuppliers(mold);

				if (serverName.equalsIgnoreCase("dyson")) {
					List<User> engineers = mold.getEngineers();
					engineers.forEach(engineer -> {
						if (!supplierList.contains(engineer)) supplierList.add(engineer);
					});
				} else {
					List<User> oemList = userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
					oemList.forEach(oem -> {
						if (!supplierList.contains(oem)) supplierList.add(oem);
					});
				}
		*/
		List<User> supplierList = getSupplierListOfMold(mold);

		List<UserAlert> userAlertList = userAlertRepository.findByUserInAndAlertType(supplierList, AlertType.DETACHMENT);
		Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
		List<LogUserAlert> logUserAlerts = generateLogUserAlertDetachment(userAlertMap, Arrays.asList(moldDetachment), true);
		logUserAlertRepository.saveAll(logUserAlerts);

		List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(UserAlert::getEmail).collect(Collectors.toList());
		Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
		mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME,  Arrays.asList(moldDetachment));
	}

	@Deprecated
	public void saveMoldDetachment(MoldPayload payload) {
		// MoldMaintenance 기준 처리..
		MoldDetachment moldDetachment = moldDetachmentRepository.findById(payload.getId()).orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));
		List<MoldDetachment> lastMoldDetachment = moldDetachmentRepository.findByMoldIdAndNotificationStatusInAndLatest(moldDetachment.getMoldId(),
				Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true);
		if (lastMoldDetachment != null && lastMoldDetachment.size() > 0) {
			lastMoldDetachment.forEach(me -> {
				me.setLatest(false);
			});
			moldDetachmentRepository.saveAll(lastMoldDetachment);
		}

		moldDetachment.setNotificationStatus(NotificationStatus.CONFIRMED);
		moldDetachment.setMessage(payload.getMessage());
		moldDetachment.setConfirmedAt(Instant.now());
		moldDetachment.setConfirmedBy(SecurityUtils.getName());
		moldDetachment.setLatest(true);

		moldDetachmentRepository.save(moldDetachment);

		/*
				Mold mold = moldDetachment.getMold();
				mold.setDetachmentStatus(DetachmentStatus.WITHIN_TOLERANCE);
				moldRepository.save(mold);
		*/
	}

	public void saveMoldDetachment(List<MoldPayload> moldAlertPayloadList) {
		List<Long> ids = moldAlertPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<MoldDetachment> moldDetachmentList = moldDetachmentRepository.findByIdIsIn(ids);

		if (moldDetachmentList == null || moldDetachmentList.size() == 0)
			return;

		List<Long> moldIdsExist = moldDetachmentList.stream().map(x -> x.getMoldId()).collect(Collectors.toList());
		List<MoldDetachment> moldDetachmentExistList = moldDetachmentRepository.findByMoldIdIsInAndNotificationStatusInAndLatest(moldIdsExist,
				Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true);
		moldDetachmentExistList.forEach(moldDetachment -> moldDetachment.setLatest(false));
		moldDetachmentRepository.saveAll(moldDetachmentExistList);

		moldDetachmentList.forEach(moldDetachment -> {
			MoldPayload payload = moldAlertPayloadList.stream().filter(x -> x.getId().equals(moldDetachment.getId())).findAny().orElse(null);
			if (payload != null) {
				moldDetachment.setNotificationStatus(NotificationStatus.CONFIRMED);
				moldDetachment.setMessage(payload.getMessage());
				moldDetachment.setConfirmedAt(Instant.now());
				moldDetachment.setConfirmedBy(SecurityUtils.getName());
				moldDetachment.setLatest(true);
			}
		});
		moldDetachmentRepository.saveAll(moldDetachmentList);
	}

	public List<MoldMaintenance> resolveWrongMoldMaintenance() {
		List<MoldMaintenance> allData = moldMaintenanceRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
		List<MoldMaintenance> result = new ArrayList<>();
		List<MoldMaintenance> toRemove = new ArrayList<>();
		allData.forEach(moldMaintenance -> {
			MoldMaintenance existing = result.stream()
					.filter(x -> x.getMoldId().equals(moldMaintenance.getMoldId()) && !x.getMaintenanceStatus().equals(MaintenanceStatus.DONE)
							&& x.getMaintenanceStatus().equals(moldMaintenance.getMaintenanceStatus()) && x.getPeriodStart().equals(moldMaintenance.getPeriodStart())
							&& x.getPeriodEnd().equals(moldMaintenance.getPeriodEnd()))
					.findFirst().orElse(null);
			if (existing == null) {
				result.add(moldMaintenance);
			} else {
				toRemove.add(moldMaintenance);
			}
		});
		moldMaintenanceRepository.deleteAll(toRemove);
		return result;
	}
	/*

		public void loadTreeCompanyForPayLoad(final MoldPayload moldPayload) {
			moldPayload.setAccessMoldIds(new ArrayList<>());
			moldPayload.setAccessCompanyIds(new ArrayList<>());
			loadTreeCompanyForPayLoad(moldPayload.getAccessCompanyIds(),moldPayload.getAccessMoldIds());
		}
		public void loadTreeCompanyForPayLoad(final DashboardFilterPayload moldPayload) {
			moldPayload.setAccessMoldIds(new ArrayList<>());
			moldPayload.setAccessCompanyIds(new ArrayList<>());
			loadTreeCompanyForPayLoad(moldPayload.getAccessCompanyIds(),moldPayload.getAccessMoldIds());
		}
	*/

	@Deprecated
	/**
	 * @param accessCompanyIds
	 * @param accessMoldIds    null if without get moldId
	 */
	public void loadTreeCompanyForPayLoad(final List<Long> accessCompanyIds, final List<Long> accessMoldIds) {
		if (!SecurityUtils.isAdmin() && SecurityUtils.getCompanyId() != null) {
			loadTreeCompanyForPayLoad(SecurityUtils.getCompanyId(), accessCompanyIds, accessMoldIds);
		}
	}

	@Deprecated
	public void loadTreeCompanyForPayLoad(Long currentCompanyId, final List<Long> accessCompanyIds, final List<Long> accessMoldIds) {
		try {

			//load mold with tree company
//			if (!SecurityUtils.isAdmin() && SecurityUtils.getCompanyId() != null) {
//			if (accessHierarchyService.isRoot(SecurityUtils.getCompanyId())) {
//				moldPayload.setRootCompany(true);
//			} else {

			List<Long> otherMoldIds = accessMoldIds != null ? new ArrayList<>() : null;
			Set<Long> companyTree = accessHierarchyService.getFullCompanyChildrenId(currentCompanyId, otherMoldIds);
			if (accessMoldIds != null)
				accessMoldIds.addAll(otherMoldIds);
			accessCompanyIds.addAll(companyTree.stream().collect(Collectors.toList()));
//			}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//
	public List<DashboardChartDataOte> findOte(DashboardFilterPayload payload, DateViewType dateViewType) {
		Long endTime = new Date().getTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if (dateViewType.equals(DateViewType.LAST30DAYS)) {
			calendar.add(Calendar.DATE, -30);
		} else {
			calendar.add(Calendar.DATE, -7);
		}
		Long startTime = calendar.getTimeInMillis();
		payload.setStartTime(startTime / 1000);
		payload.setEndTime(endTime / 1000);

		List<ChartDataOte> statisticsDataOteList = statisticsRepository.findChartDataOte(payload);
		List<ChartDataOte> chartDataOtePart = statisticsRepository.findChartDataOtePart(payload);
//		Map<String,ChartDataOte> mapStatisticsDataOte=new HashMap<>();
		Map<String, ChartDataOte> mapDataOtePart = new HashMap<>();
//		Map<String,ChartDataOte> mapChartDataOteList=new HashMap<>();
//		statisticsDataOteList.stream().forEach(s->mapStatisticsDataOte.put(s.getMoldCode(),s));
		chartDataOtePart.stream().forEach(s -> mapDataOtePart.put(s.getMoldCode(), s));
		statisticsDataOteList.stream().forEach(s -> {
			Integer targetUptime = s.getTargetTimeOfToolingInHours();
			if (targetUptime != null && (payload.getEndTime() - payload.getStartTime()) / (3600l * 1000 * 24) > 7) {
				targetUptime = targetUptime * 4;
			}
			Double availability = targetUptime == null || targetUptime == 0 ? 1d : (s.getUptime() == null ? 0d : s.getUptime() * 1d / targetUptime);
			if (availability > 1)
				availability = 1d;
			Integer numberOfRejectedParts = s.getNumberOfRejectedParts() == null ? 0 : s.getNumberOfRejectedParts();
			Double performance = 0d;
			Double quality = 1d;
			ChartDataOte otep = mapDataOtePart.containsKey(s.getMoldCode()) ? mapDataOtePart.get(s.getMoldCode()) : null;
			if (otep != null) {
				performance = otep.getTotalNumberOfPartsProduced() == null ? 0d
						: (otep.getTheoreticalNumberOfParts() == null || otep.getTheoreticalNumberOfParts() == 0 ? 1d
								: otep.getTotalNumberOfPartsProduced() * 1d / otep.getTheoreticalNumberOfParts());
				if (performance > 1)
					performance = 1d;

				if (otep.getTotalNumberOfPartsProduced() != null && otep.getTotalNumberOfPartsProduced() != 0) {
					quality = 1d - Double.valueOf(numberOfRejectedParts) / otep.getTotalNumberOfPartsProduced();
				}
			}
			long ote = Math.round(availability * performance * quality * 100);
			s.setDataPercent(Double.valueOf(ote));
		});

		Comparator<ChartDataOte> comparator = (h1, h2) -> h1.getDataPercent().compareTo(h2.getDataPercent());
		statisticsDataOteList.sort(comparator.reversed());
		Integer total = statisticsDataOteList.size();
		List<ChartDataOte> highOTE = statisticsDataOteList.stream().filter(s -> s.getDataPercent() > Const.OTE.HIGH).collect(Collectors.toList());
		List<ChartDataOte> mediumOTE = statisticsDataOteList.stream().filter(s -> s.getDataPercent() > Const.OTE.MEDIUM && s.getDataPercent() <= Const.OTE.HIGH)
				.collect(Collectors.toList());
		List<ChartDataOte> lowOTE = statisticsDataOteList.stream().filter(s -> s.getDataPercent() <= Const.OTE.MEDIUM).collect(Collectors.toList());
		DashboardChartDataOte high = new DashboardChartDataOte("High", highOTE.size(), total, highOTE);
		DashboardChartDataOte medium = new DashboardChartDataOte("Medium", mediumOTE.size(), total, mediumOTE);
		DashboardChartDataOte low = new DashboardChartDataOte("Low", lowOTE.size(), total, lowOTE);

		List<DashboardChartDataOte> result = new ArrayList<>();
		result.add(high);
		result.add(medium);
		result.add(low);
		return result;
	}

	public List<MoldMaintenance> recoverLastShotMadeMaintenance() {
		List<MoldMaintenance> finalLastMoldMaintenanceList = moldMaintenanceRepository
				.findAllByMaintenanceStatusIsInAndLatestOrderByIdDesc(Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);
		if (finalLastMoldMaintenanceList != null && finalLastMoldMaintenanceList.size() > 0) {
			finalLastMoldMaintenanceList.forEach(finalLastMoldMaintenance -> {
				Integer finalPeriod = 0;
				if (finalLastMoldMaintenance.getPeriodStart() != null && finalLastMoldMaintenance.getMold().getPreventUpcoming() != null)
					finalPeriod = finalLastMoldMaintenance.getPeriodStart() + finalLastMoldMaintenance.getMold().getPreventUpcoming();
				else if (finalLastMoldMaintenance.getPeriodEnd() != null && finalLastMoldMaintenance.getMold().getPreventOverdue() != null)
					finalPeriod = finalLastMoldMaintenance.getPeriodEnd() - finalLastMoldMaintenance.getMold().getPreventOverdue();

				if (finalLastMoldMaintenance.getMold().getLastShot() != null) {
					finalLastMoldMaintenance.setLastShotMade(finalLastMoldMaintenance.getMold().getLastShot() - finalPeriod + finalLastMoldMaintenance.getMold().getPreventCycle());
				} else {
					finalLastMoldMaintenance.setLastShotMade(0);
				}
			});
			return moldMaintenanceRepository.saveAll(finalLastMoldMaintenanceList);
		}
		return null;
	}

	public Mold deleteMold(Mold mold) {
		if (mold != null) {
			mold.setDeleted(true);
			moldRepository.save(mold);
		}

		//update data completion rate
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.TOOLING, mold.getLocation().getCompanyId());
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, mold.getLocation().getCompanyId());

		return mold;
	}

	public Mold restoreMold(Mold mold) {
		if (mold != null) {
			mold.setDeleted(false);
			moldRepository.save(mold);
		}
		//update data completion rate
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.TOOLING, mold.getLocation().getCompanyId());
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, mold.getLocation().getCompanyId());
		return mold;
	}

	public void deleteMoldInBatch(List<Long> ids) {
		ids.forEach(id -> {
			Mold mold = moldRepository.getOne(id);
			deleteMold(mold);
		});

		Mold mold = moldRepository.getOne(ids.get(0));
		//update data completion rate
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.TOOLING, mold.getLocation().getCompanyId());
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, mold.getLocation().getCompanyId());
	}

	public List<MiniComponentData> getListMoldIdsToMatch() {
		return moldRepository.findMoldsUnmatchedWithMachine();
	}

	public Page<MiniComponentData> getListMoldLiteData(Predicate predicate, Pageable pageable) {
		return moldRepository.findPageMoldsUnmatchedWithMachine(predicate, pageable);
	}

	public Page<MachineMoldData> getListMoldToMatch(Predicate predicate, Pageable pageable) {
		return moldRepository.findMoldToMatch(predicate, pageable);
	}

	public UptimeRatioData findUptimeRatioData(String type, Long id, String from, String to, Pageable pageable) {
		Page<UptimeRatioTooling> uptimeRatioToolingPage = statisticsRepository.findUptimeRatioTooling(null, type, id, from, to, pageable, true);

		List<UptimeRatioTooling> uptimeRatioTooling = uptimeRatioToolingPage.getContent();
		List<UptimeRatioTooling> frequentlyTooling = uptimeRatioTooling.stream().filter(x -> x.getUptimeRatio() > 50).collect(Collectors.toList());
		List<UptimeRatioTooling> occasionallyTooling = uptimeRatioTooling.stream().filter(x -> x.getUptimeRatio() > 20 && x.getUptimeRatio() <= 50).collect(Collectors.toList());
		List<UptimeRatioTooling> rarelyTooling = uptimeRatioTooling.stream().filter(x -> x.getUptimeRatio() > 0 && x.getUptimeRatio() <= 20).collect(Collectors.toList());
		List<UptimeRatioTooling> neverTooling = uptimeRatioTooling.stream().filter(x -> x.getUptimeRatio() == 0).collect(Collectors.toList());

		UptimeRatioData result = UptimeRatioData.builder().neverCount(neverTooling.size()).rarelyCount(rarelyTooling.size()).occasionallyCount(occasionallyTooling.size())
				.frequentlyCount(frequentlyTooling.size()).build();
		Page<UptimeRatioDetails> details = findUptimeRatioDetails(type, id, from, to, pageable);
		result.setDetails(details);
		return result;
	}

	public Page<UptimeRatioDetails> findUptimeRatioDetails(String type, Long id, String from, String to, Pageable pageable) {
		Page<UptimeRatioDetails> details = new PageImpl<>(new ArrayList<>(), pageable, 1);
		//todo test
		try {
			details = statisticsRepository.findUptimeRatioDetailsNew(type, id, from, to, pageable);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		Page<UptimeRatioDetails> details = statisticsRepository.findUptimeRatioDetails(type, id, from, to, pageable);
		return details;
	}

	public Page<UptimeRatioTooling> findUptimeRatioTooling(FrequentUsage frequent, String type, Long id, String from, String to, Pageable pageable) {
		Page<UptimeRatioTooling> result = statisticsRepository.findUptimeRatioTooling(frequent, type, id, from, to, pageable, false);
		List<UptimeRatioTooling> content = result.getContent();
		if (content != null && content.size() > 0) {
			List<Long> toolingIds = result.stream().map(x -> x.getMoldId()).distinct().collect(Collectors.toList());
			List<MiniGeneralData> info = type.equalsIgnoreCase("SUPPLIER") ? partRepository.findPartByMoldIdIn(toolingIds) : companyRepository.findCompanyByMoldIdIn(toolingIds);
			result.stream().forEach(x -> {
				List<MiniGeneralData> filteredCompanies = info.stream().filter(anInfo -> anInfo.getRefId().equals(x.getMoldId())).collect(Collectors.toList());
				x.setInfo(filteredCompanies);
			});
		}
		return result;
	}

	@Transactional
	public ApiResponse switchMoldPart(SwitchMoldPartPayload payload) {
		if (payload.getMoldId() == null || payload.getCurrentPartId() == null || payload.getToBePartId() == null)
			return ApiResponse.error();
		MoldPart currentMoldPart = moldPartRepository.findByMoldIdAndPartId(payload.getMoldId(), payload.getCurrentPartId());
		MoldPart toBeMoldPart = moldPartRepository.findByMoldIdAndPartId(payload.getMoldId(), payload.getToBePartId());
		Instant switchedTime = payload.isHappensNow() ? Instant.now() : Instant.ofEpochMilli(payload.getSwitchedTime());

		Integer currentActiveCavities = currentMoldPart.getCavity();
		Integer toBeActiveCavities = toBeMoldPart.getCavity();
		currentMoldPart.setCavity(toBeActiveCavities);
//		currentMoldPart.setSwitchedTime(switchedTime);
		toBeMoldPart.setCavity(currentActiveCavities);
		toBeMoldPart.setSwitchedTime(switchedTime);
		moldPartRepository.saveAll(Arrays.asList(currentMoldPart, toBeMoldPart));

		if (!payload.isHappensNow()) {
			updateStatisticPart(payload.getMoldId(), switchedTime, toBeMoldPart);
			updatePartProjectProduced(currentMoldPart);
			updatePartProjectProduced(toBeMoldPart);
		}

		versioningService.writeHistory(toBeMoldPart.getMold());

		ApiResponse response = ApiResponse.success();
		response.setData(toBeMoldPart);
		return response;
	}

	private void updateStatisticPart(Long moldId, Instant switchTime, MoldPart moldPart) {
		List<Statistics> statistics = statisticsRepository.findByMoldIdAndLstGreaterThanEqual(moldId, DateUtils.getDate(switchTime, DateUtils.DEFAULT_DATE_FORMAT))
				.orElse(new ArrayList<>());
		if (CollectionUtils.isNotEmpty(statistics)) {
			List<StatisticsPartReplaced> statisticsPartReplacedList = new ArrayList<>();
			List<Long> ids = statistics.stream().map(Statistics::getId).collect(Collectors.toList());
			List<StatisticsPart> statisticsParts = statisticsPartRepository.findByStatisticsIdIn(ids);
			Part part = moldPart.getPart();
			Category project = part.getCategory();
			statisticsParts.forEach(sp -> {
				StatisticsPartReplaced spr = DataUtils.deepCopy(sp, StatisticsPartReplaced.class);
				statisticsPartReplacedList.add(spr);
				sp.setCavity(moldPart.getCavity());
				sp.setPartId(part.getId());
				sp.setPartCode(part.getPartCode());

				if (project != null) {
					sp.setProjectId(project.getId());
					sp.setProjectName(project.getName());

					if (project.getParent() != null) {
						Category category = project.getParent();
						sp.setCategoryId(category.getId());
						sp.setCategoryName(category.getName());
					}
				}
			});

			statisticsPartReplacedRepository.saveAll(statisticsPartReplacedList);
			statisticsPartRepository.saveAll(statisticsParts);
		}

	}

	private void updatePartProjectProduced(MoldPart moldPart) {
		Long partId = moldPart.getPartId();
		Long projectId = moldPart.getPart().getCategoryId();
		Optional<PartProjectProduced> optional = partProjectProducedRepository.findByPartIdAndProjectId(partId, projectId);
		PartProjectProduced partProjectProduced;
		if (optional.isPresent()) {
			partProjectProduced = optional.get();
		} else {
			partProjectProduced = new PartProjectProduced();
			partProjectProduced.setPartId(partId);
			partProjectProduced.setProjectId(projectId);
		}

		partProjectProduced.setTotalProduced(statisticsRepository.getPartProjectProduced(partId, projectId).longValue());
		partProjectProduced.setTotalProducedVal(statisticsRepository.getPartProjectProduced(partId, projectId).longValue());
		partProjectProducedRepository.save(partProjectProduced);
	}

	public ApiResponse migrateDataForMoldTotalCavities() {
		List<Mold> molds = moldRepository.findAll();
		molds.forEach(mold -> {
			Integer maxTotalCavities = mold.getParts().stream().mapToInt(PartData::getTotalCavities).max().orElse(0);
			mold.setTotalCavities(maxTotalCavities);
		});

		System.out.println("Number of molds updated cavities: " + molds.size());
		moldRepository.saveAll(molds);
		return ApiResponse.success(CommonMessage.OK);
	}

	public List<AccelerationData> getAccelerationData(ChartPayload payload) {
		List<AccelerationData> accelerationDataList = new ArrayList<>();
		Mold mold = findById(payload.getMoldId());
		if (mold == null) {
			return accelerationDataList;
		}
		List<DataAcceleration> dataAccelerations = dataAccelerationRepository.findAllByCounterIdAndMeasurementDateStartsWithOrderByMeasurementDate(mold.getCounterCode(),
				payload.getDate());
		if (dataAccelerations.isEmpty()) {
			return accelerationDataList;
		}
		String firstTime0 = dataAccelerations.get(0).getMeasurementDate();
		Instant firstTimeInstant0 = DateUtils.getInstant(firstTime0, DateUtils.DEFAULT_DATE_FORMAT);
		for (int i = 0; i < dataAccelerations.size(); i++) {
			List<Acceleration> accelerations = dataAccelerations.get(i).getAccelerations();
			String firstTime = dataAccelerations.get(i).getMeasurementDate();
			Instant firstTimeInstant = DateUtils.getInstant(firstTime, DateUtils.DEFAULT_DATE_FORMAT);
			Double rangeValLast = firstTimeInstant.toEpochMilli() - firstTimeInstant0.toEpochMilli() / 1000d;
			for (int j = 0; j < accelerations.size(); j++) {
				Acceleration acceleration = accelerations.get(j);
				if (acceleration.getTime() != null) {
					Instant timeVal = firstTimeInstant.plusMillis(Math.round(Double.valueOf(acceleration.getTime()) * 1000));
					Double rangeVal = 0d;
					try {
						rangeVal = rangeValLast + Double.valueOf(acceleration.getTime());
					} catch (Exception e) {
						e.printStackTrace();
					}
//					AccelerationData accelerationData=
//							new AccelerationData(dataAccelerations.get(i).getDataId(),mold.getEquipmentCode(),DateUtils.getDate(timeVal,DateUtils.HH_mm_ss),acceleration.getValue(),rangeVal.toString());
					AccelerationData accelerationData = new AccelerationData(dataAccelerations.get(i).getDataId(), mold.getEquipmentCode(),
							DateUtils.getDate(timeVal, DateUtils.HH_mm_ss), acceleration.getValue(), acceleration.getTime());
					accelerationDataList.add(accelerationData);
				}
			}
		}
		return accelerationDataList;
	}

	public List<Mold> findListMoldDown() {
		return moldRepository.findByLastShotMadeAtBeforeAndDeletedIsFalse(Instant.now().minus(1, ChronoUnit.HOURS));
	}

	public List<MoldDowntimeEvent> findMoldDowntimeEventByMoldIdInAndDowntimeStatusAndLatest(List<Long> moldIds, DowntimeStatus downtimeStatus, Boolean latest) {
		if (moldIds == null || moldIds.size() == 0 || latest == null)
			return new ArrayList<>();
		return moldDowntimeEventRepository.findByMoldIdInAndDowntimeStatusAndLatest(moldIds, downtimeStatus, latest);
	}

	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
		try {
			List<Mold> molds = moldRepository.findAllById(dto.getIds());
			molds.forEach(mold -> {
				if ((!dto.isEnabled() || ToolingStatus.DISPOSED.equals(mold.getToolingStatus())) && mold.getCounter() != null) {
					Counter counter = mold.getCounter();
					counter.setEquipmentStatus(EquipmentStatus.AVAILABLE);
					counter.setMold(null);
					counterService.save(counter);
					mold.setCounterId(null);
					mold.setCounter(null);
				}
				mold.setDeleted(!dto.isEnabled());
				save(mold);
				versioningService.writeHistory(findById(mold.getId()));
			});

			//update data completion rate
			dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.TOOLING, molds.get(0).getLocation().getCompanyId());
			dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, molds.get(0).getLocation().getCompanyId());

			return ApiResponse.success(CommonMessage.OK, molds);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	public ApiResponse getPartsChangeHistory(Long moldId, String start, String end) {
		try {
			Integer partProduced = 0;
			String singlePartName = null;
			Optional<List<Statistics>> optionalStatistics = statisticsRepository.findByMoldIdAndLstBetween(moldId, start, end);
			if (optionalStatistics.isPresent()) {
				List<Long> statisticsIds = optionalStatistics.get().stream().filter(s -> s.getCt() > 0 || s.getFirstData()).map(Statistics::getId).collect(Collectors.toList());
				if (!ObjectUtils.isEmpty(statisticsIds)) {
					partProduced = statisticsPartRepository.countDistinctPartIdByStatisticsIdIn(statisticsIds);
				}
			}
			if (partProduced == 1) {
				StatisticsPart singleSP = statisticsPartRepository.findFirstByStatisticsId(optionalStatistics.get().get(0).getId());
				Part part = partRepository.getOne(singleSP.getPartId());
				singlePartName = part.getName();
			}
			List<RevisionHistory> histories = versioningService.getListHistoryInRange(moldId, RevisionObjectType.MOLD, start, end);
			if (CollectionUtils.isEmpty(histories)) {
				PartChangeData data = new PartChangeData();
				Instant startDate = start != null ? DateUtils.getInstant(start, DateUtils.DEFAULT_DATE_FORMAT) : Instant.now();
				Optional<RevisionHistory> optional = reversionHistoryRepository.findFirstByOriginIdAndRevisionObjectTypeAndCreatedAtBeforeOrderByCreatedAtDesc(moldId,
						RevisionObjectType.MOLD, startDate);
				//find the nearest revision history before start date
				if (optional.isPresent()) {
					RevisionHistory revisionHistory = optional.get();
					MoldVersion moldVersion = moldVersionRepository.getOne(revisionHistory.getRevisionId());
					if (moldVersion.getParts() != null) {
						Set<MoldPart> moldParts = versioningService.getListMoldPartFromMoldVersion(moldVersion.getParts());
						fillPartChangeInfo(data, moldParts, true);
						data.setDateTime(moldVersion.getCreatedAt());
						data.setReportedBy(userRepository.getOne(revisionHistory.getEditedBy()).getName());
					}
				} else {
					Instant endDate = end != null ? DateUtils.getInstant(end, DateUtils.DEFAULT_DATE_FORMAT) : Instant.now();
					Optional<RevisionHistory> optional1 = reversionHistoryRepository.findFirstByOriginIdAndRevisionObjectTypeAndCreatedAtAfterOrderByCreatedAtAsc(moldId,
							RevisionObjectType.MOLD, endDate);
					Mold mold = moldRepository.getOne(moldId);
					//find the nearest revision history after end date
					if (optional1.isPresent()) {
						RevisionHistory revisionHistory = optional1.get();
						MoldVersion moldVersion = moldVersionRepository.getOne(revisionHistory.getRevisionId());
						Set<MoldPart> moldParts = versioningService.getListMoldPartFromMoldVersion(moldVersion.getParts());
						fillPartChangeInfo(data, moldParts, true);
						data.setDateTime(mold.getCreatedAt());
						data.setReportedBy(userRepository.getOne(mold.getCreatedBy()).getName());
					} else {
						//find the current parts
						Set<MoldPart> moldParts = new HashSet<>(mold.getMoldParts());
						fillPartChangeInfo(data, moldParts, true);
						data.setDateTime(mold.getCreatedAt());
						data.setReportedBy(userRepository.getOne(mold.getCreatedBy()).getName());
					}
				}
				PartChangeDataFull dataFull = new PartChangeDataFull(partProduced, singlePartName, Collections.singletonList(data));
				return ApiResponse.success(CommonMessage.OK, dataFull);
			}

			List<Long> revisionIds = histories.stream().map(RevisionHistory::getRevisionId).collect(Collectors.toList());
			List<MoldVersion> moldVersions = moldVersionRepository.findAllById(revisionIds);
			List<PartChangeData> partChangeData = new ArrayList<>();
			List<String> partChanges = new ArrayList<>();
			for (MoldVersion moldVersion : moldVersions) {
				if (CollectionUtils.isEmpty(partChanges) || partChanges.get(partChanges.size() - 1) == null
						|| !partChanges.get(partChanges.size() - 1).equals(moldVersion.getParts().trim())) {
					partChanges.add(moldVersion.getParts() != null ? moldVersion.getParts().trim() : null);

					Set<MoldPart> moldParts = versioningService.getListMoldPartFromMoldVersion(moldVersion.getParts());
					PartChangeData data = new PartChangeData();
					if (!ObjectUtils.isEmpty(partChangeData)) {
						data.setOriginal(partChangeData.get(partChangeData.size() - 1).getChanged());
					} else {
						Instant startDate = start != null ? DateUtils.getInstant(start, DateUtils.DEFAULT_DATE_FORMAT) : Instant.now();
						Optional<RevisionHistory> optional = reversionHistoryRepository.findFirstByOriginIdAndRevisionObjectTypeAndCreatedAtBeforeOrderByCreatedAtDesc(moldId,
								RevisionObjectType.MOLD, startDate);
						if (optional.isPresent()) {
							RevisionHistory revisionHistory = optional.get();
							MoldVersion mv = moldVersionRepository.getOne(revisionHistory.getRevisionId());
							if (mv.getParts() != null) {
								Set<MoldPart> mp = versioningService.getListMoldPartFromMoldVersion(mv.getParts());
								fillPartChangeInfo(data, mp, true);
							}
						}
					}
					fillPartChangeInfo(data, moldParts, false);
					data.setDateTime(moldVersion.getCreatedAt());

					RevisionHistory revisionHistory = histories.stream().filter(h -> h.getRevisionId().equals(moldVersion.getId())).findFirst().orElse(null);
					data.setReportedBy(userRepository.getOne(revisionHistory == null ? moldRepository.getOne(moldId).getCreatedBy() : revisionHistory.getEditedBy()).getName());

					partChangeData.add(data);
				}
			}

			if (partChangeData.size() == 1 && CollectionUtils.isEmpty(partChangeData.get(0).getOriginal()) && partChangeData.get(0).getChanged().size() == 1) {
				partChangeData.get(0).setOriginal(partChangeData.get(0).getChanged());
				partChangeData.get(0).setChanged(null);
			}
			if (partChangeData.size() > 1) {
				if (CollectionUtils.isEmpty(partChangeData.get(0).getOriginal()) && CollectionUtils.isEmpty(partChangeData.get(0).getChanged())) {
					partChangeData.remove(0);
				}

				if (partChangeData.size() == 1 && CollectionUtils.isEmpty(partChangeData.get(0).getOriginal()) && partChangeData.get(0).getChanged().size() == 1) {
					partChangeData.get(0).setOriginal(partChangeData.get(0).getChanged());
					partChangeData.get(0).setChanged(null);
				}
			}
			PartChangeDataFull data = new PartChangeDataFull(partProduced, singlePartName, partChangeData);

			return ApiResponse.success(CommonMessage.OK, data);
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	private void fillPartChangeInfo(PartChangeData data, Set<MoldPart> moldParts, boolean isOriginalOnly) {
		List<PartLiteData> list = new ArrayList<>();
		moldParts.stream().filter(moldPart -> moldPart.getCavity() > 0).forEach(moldPart -> {
			Part part = partRepository.getOne(moldPart.getPartId());
			PartLiteData liteData = new PartLiteData(part.getId(), part.getPartCode(), part.getName());
			list.add(liteData);
		});
		if (isOriginalOnly)
			data.setOriginal(list);
		else
			data.setChanged(list);
	}

	public List<Long> getUsedMoldIds(Long partId, String fromDate, String toDate) {
		return moldRepository.findUsedMoldIds(partId, fromDate, toDate);
	}

	public ApiResponse getNumberInactiveTooling() {
		Pair<Integer, Pair<Integer, Integer>> inactiveConfig = getInactiveConfig();
		Integer firstLevelMonthNumber = inactiveConfig.getFirst();
		Integer secondLevelMonthNumber = inactiveConfig.getSecond().getFirst();
		Integer thirdLevelMonthNumber = inactiveConfig.getSecond().getSecond();

		ZonedDateTime now = ZonedDateTime.now();
		Instant firstLevelDate = now.minusMonths(firstLevelMonthNumber).toInstant();
		Instant secondLevelDate = now.minusMonths(secondLevelMonthNumber).toInstant();
		Instant thirdLevelDate = now.minusMonths(thirdLevelMonthNumber).toInstant();

		List<Long> filteredIds = findAllMiniDataByGeneralFilter().stream().map(MiniComponentData::getId).collect(Collectors.toList());

		Long firstLevelCount = moldRepository.countByLastShotAt(secondLevelDate, firstLevelDate, filteredIds);
		InactiveToolingCount firstLevelInactiveToolingCount = new InactiveToolingCount(DashboardSettingLevel.FIRST_LEVEL, firstLevelCount, firstLevelMonthNumber);

		Long secondLevelCount = moldRepository.countByLastShotAt(thirdLevelDate, secondLevelDate, filteredIds);
		InactiveToolingCount secondLevelInactiveToolingCount = new InactiveToolingCount(DashboardSettingLevel.SECOND_LEVEL, secondLevelCount, secondLevelMonthNumber);

		Long thirdLevelCount = moldRepository.countByLastShotAt(null, thirdLevelDate, filteredIds);
		InactiveToolingCount thirdLevelInactiveToolingCount = new InactiveToolingCount(DashboardSettingLevel.THIRD_LEVEL, thirdLevelCount, thirdLevelMonthNumber);

		List<InactiveToolingCount> inactiveToolingCountList = Arrays.asList(firstLevelInactiveToolingCount, secondLevelInactiveToolingCount, thirdLevelInactiveToolingCount);
		return ApiResponse.success(CommonMessage.OK, inactiveToolingCountList);

	}

	@Transactional
	public ApiResponse deleteDuplicatedToolingByEquipmentCode(String equipmentCode) {
		List<Mold> moldList = moldRepository.findAllByEquipmentCodeOrderByIdDesc(equipmentCode);
		List<Long> moldIdToDelete = Lists.newArrayList();

		for (int i = 1; i < moldList.size(); i++) {
			moldIdToDelete.add(moldList.get(i).getId());
			customFieldValueService.deleteCustomFieldValueByObjectId(moldList.get(i).getId());
		}

		List<MoldPart> moldPartList = moldPartRepository.findAllByMoldIdIn(moldIdToDelete);
		moldLocationRepository.deleteAllByMoldIdIn(moldIdToDelete);
		moldPartRepository.deleteAll(moldPartList);
		moldStandardValueRepository.deleteAllByMoldIdIn(moldIdToDelete);
		moldDataSubmissionRepository.deleteAllByMoldIdIn(moldIdToDelete);
		moldRepository.deleteAllByIdIn(moldIdToDelete);
		return ApiResponse.success(CommonMessage.OK);
	}

	public ApiResponse getFirstShotYear() {
		try {
			String firstDataYear;
			Statistics statistics = statisticsRepository.findFirstByDayIsNotNullOrderByDayAsc().orElse(null);
			if (statistics != null) {
				firstDataYear = statistics.getYear();
			} else {
				firstDataYear = String.valueOf(LocalDate.now().getYear());
			}

			return ApiResponse.success(CommonMessage.OK, firstDataYear);
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	public ApiResponse fixDuplicatedMaintenanceData() {
		try {
			List<MoldMaintenance> maintenances = moldMaintenanceRepository
					.findAllByMaintenanceStatusIsInAndLatestOrderByIdDesc(Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);

			List<MoldMaintenance> toKeep = new ArrayList<>();
			Map<Long, List<MoldMaintenance>> map = maintenances.stream().flatMap(moldMaintenance -> {
				Map<Long, MoldMaintenance> um = new HashMap<>();
				um.put(moldMaintenance.getMoldId(), moldMaintenance);
				return um.entrySet().stream();
			}).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			map.forEach((k, v) -> {
				if (v.size() == 1) {
					toKeep.add(v.get(0));
				} else {
					Comparator<MoldMaintenance> comparator = Comparator.comparing(MoldMaintenance::getCreatedAt);
					toKeep.add(v.stream().max(comparator).get());
				}
			});

			List<Long> toKeepIds = toKeep.stream().map(MoldMaintenance::getId).collect(Collectors.toList());
			List<MoldMaintenance> toRemove = maintenances.stream().filter(m -> !toKeepIds.contains(m.getId())).collect(Collectors.toList());
			toRemove.forEach(m -> m.setLatest(false));
			moldMaintenanceRepository.saveAll(toRemove);

			//mold disconnect
			List<MoldDisconnect> moldDisconnects = moldDisconnectRepository
					.findByNotificationStatusIsInAndLatest(Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true);
			List<MoldDisconnect> moldDisconnectsToKeep = new ArrayList<>();
			Map<Long, List<MoldDisconnect>> moldDisconnectMap = moldDisconnects.stream().flatMap(moldDisconnect -> {
				Map<Long, MoldDisconnect> um = new HashMap<>();
				um.put(moldDisconnect.getMoldId(), moldDisconnect);
				return um.entrySet().stream();
			}).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
			moldDisconnectMap.forEach((k, v) -> {
				if (v.size() == 1) {
					moldDisconnectsToKeep.add(v.get(0));
				} else {
					Comparator<MoldDisconnect> comparator = Comparator.comparing(MoldDisconnect::getCreatedAt);
					moldDisconnectsToKeep.add(v.stream().max(comparator).get());
				}
			});

			List<Long> moldDisconnectToKeepIds = moldDisconnectsToKeep.stream().map(MoldDisconnect::getId).collect(Collectors.toList());
			List<MoldDisconnect> moldDisconnectsToRemove = moldDisconnects.stream().filter(m -> !moldDisconnectToKeepIds.contains(m.getId())).collect(Collectors.toList());
			moldDisconnectsToRemove.forEach(m -> m.setLatest(false));
			moldDisconnectRepository.saveAll(moldDisconnectsToRemove);

			Map<AlertType, Object> result = new HashMap<>();
			result.put(AlertType.MAINTENANCE, toRemove);
			result.put(AlertType.DISCONNECTED, moldDisconnectsToRemove);
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	public ApiResponse restoreUpdateLastShot() {
		List<Preset> presets = presetRepository.findAllByPresetStatusAndPreset(PresetStatus.READY, "0");
		List<Counter> counters = counterRepository.findAllByEquipmentCodeIn(presets.stream().map(p -> p.getCi()).collect(Collectors.toList()));
		List<Mold> molds = counters.stream().map(c -> c.getMold()).filter(m -> m != null).collect(Collectors.toList());
		loadAccumulatedShot(true, molds, null);
		log.info("Total mold check restore {}", molds.size());
		int[] numChange = { 0 };
		molds.stream().forEach(m -> {
			log.info("{} lastShot {} accumulatedShot {}", m.getEquipmentCode(), m.getLastShot(), m.getAccumulatedShot());
			if (m.getAccumulatedShot() != null && (m.getLastShot() == null || m.getLastShot() < m.getAccumulatedShot())) {
				m.setLastShot(m.getAccumulatedShot());
				Counter counter = m.getCounter();
				counter.setPresetCount(m.getLastShot());
				counterService.save(counter);
				presetService.autoCreatePreset(counter);
				BeanUtils.get(RstStpService.class).postAlert(m, counter);
				numChange[0]++;
			}
		});
		log.info("Total mold restored {}", numChange[0]);
		return ApiResponse.success(String.format("Total mold check restore %s ", molds.size()) + String.format("Total mold restored %s", numChange[0]));
	}

	public ApiResponse getDataCount() {
		try {
			List<AlertCount> result = new ArrayList<>();
			ExecutorService executorService = Executors.newFixedThreadPool(8);

			executorService = new DelegatingSecurityContextExecutorService(executorService, SecurityContextHolder.getContext());
//			CompletableFuture<Long> moldFuture = CompletableFuture.supplyAsync(() -> {
//				MoldPayload payload = new MoldPayload();
//				payload.setStatus("all");
//				payload.setServerName(serverName);
//				long count = moldRepository.count(payload.getPredicate());
//				return count;
//			}, executorService);
			// 1. Tooling
			Future<Long> moldFuture = executorService.submit(() -> {
				MoldPayload payload = new MoldPayload();
				payload.setStatus("all");
				payload.setServerName(serverName);
				long count = moldRepository.count(payload.getPredicate());
				return count;
			});
			// 2. Part
			Future<Long> partFuture = executorService.submit(() -> {
				PartPayload payload = new PartPayload();
				payload.setStatus("active");
//				payload.setAccessType(AccessType.ADMIN_MENU);
				long count = partRepository.count(payload.getPredicate());
				return count;
			});
			// 3. Category
			Future<Long> categoryFuture = executorService.submit(() -> {
				CategoryParam payload = new CategoryParam();
				payload.setStatus("1");
				payload.setEnabled(true);
				long count = categoryRepository.count(payload.getPredicate());
				return count;
			});
			// 4. Company
			Future<Long> companyFuture = executorService.submit(() -> {
				CompanyPayload payload = new CompanyPayload();
				payload.setStatus("active");
				long count = companyRepository.count(payload.getPredicate());
				return count;
			});
			// 5. Location
			Future<Long> locationFuture = executorService.submit(() -> {
				LocationPayload payload = new LocationPayload();
				payload.setStatus("active");
				long count = locationRepository.count(payload.getPredicate());
				return count;
			});
			// 6. Machine
			Future<Long> machineFuture = executorService.submit(() -> {
				MachinePayload payload = new MachinePayload();
				payload.setStatus("enabled");
				long count = machineRepository.count(payload.getPredicate());
				return count;
			});
			// 7. Terminal
			Future<Long> terminalFuture = executorService.submit(() -> {
				TerminalPayload payload = new TerminalPayload();
				payload.setStatus("all");
				long count = terminalRepository.count(payload.getPredicate());
				return count;
			});
			// 8. Counter - Sensor
			Future<Long> counterFuture = executorService.submit(() -> {
				CounterPayload payload = new CounterPayload();
				payload.setStatus("all");
				long count = counterRepository.count(payload.getPredicate());
				return count;
			});

			long moldCount = moldFuture.get();
			long partCount = partFuture.get();
			long categoryCount = categoryFuture.get();
			long companyCount = companyFuture.get();
			long locationCount = locationFuture.get();
			long machineCount = machineFuture.get();
			long terminalCount = terminalFuture.get();
			long counterCount = counterFuture.get();
			executorService.shutdown();

			result.add(new AlertCount("", "Tooling", moldCount));
			result.add(new AlertCount("", "Part", partCount));
			result.add(new AlertCount("", "Category", categoryCount));
			result.add(new AlertCount("", "Company", companyCount));
			result.add(new AlertCount("", "Efficiency", locationCount));
			result.add(new AlertCount("", "Misconfigure", machineCount));
			result.add(new AlertCount("", "Terminal", terminalCount));
			result.add(new AlertCount("", "Counter", counterCount));

			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	public TotalToolingData getTotalToolingChartData(TabbedOverviewGeneralFilterPayload payload) {
		List<CountLocationMold> countLocationMoldList = moldRepository.countLocationMold(payload);
		List<MapChartData> mapChartDataList = moldRepository.getMapData(payload);
		List<GoogleMapData.MapData> mapData = getMapDataList(mapChartDataList);
		return new TotalToolingData(mapData, countLocationMoldList);
	}

	public DashboardTabData getTotalToolingTabData(TabbedOverviewGeneralFilterPayload payload) {
		Instant instantNowTo = Instant.now();
		Instant instantNowFrom = DateTimeUtils.getDayBefore(Instant.now(), payload.getDuration(), true);
		Instant instantBeforeFrom = DateTimeUtils.getDayBefore(Instant.now(), payload.getDuration() + 1, true);
		Instant instantBeforeTo = DateTimeUtils.getDayBefore(Instant.now(), payload.getDuration() * 2, false);

		Long total = moldRepository.countByPredicate(payload.getMoldFilter());

		Long countToolingCreateNow = moldRepository.countByPredicate(QMold.mold.createdAt.between(instantNowFrom, instantNowTo).and(payload.getMoldFilter()));
		Long countToolingCreateBefore = moldRepository.countByPredicate(QMold.mold.createdAt.between(instantBeforeFrom, instantBeforeTo).and(payload.getMoldFilter()));

		return new DashboardTabData(DashboardChartType.TOTAL_TOOLING, (double) total, (double) countToolingCreateNow - countToolingCreateBefore);
	}

	public List<InactiveToolingCount> getInactiveToolingChartData(TabbedOverviewGeneralFilterPayload payload) {
		List<DashboardSetting> dashboardSettingList = dashboardSettingRepository.findAllByUserId(SecurityUtils.getUserId());
		Optional<DashboardSetting> firstLevelSettingOptional = dashboardSettingList.stream()
				.filter(dashboardSetting -> dashboardSetting.getLevel() == DashboardSettingLevel.FIRST_LEVEL).findFirst();

		Optional<DashboardSetting> secondLevelSettingOptional = dashboardSettingList.stream()
				.filter(dashboardSetting -> dashboardSetting.getLevel() == DashboardSettingLevel.SECOND_LEVEL).findFirst();

		Optional<DashboardSetting> thirdLevelSettingOptional = dashboardSettingList.stream()
				.filter(dashboardSetting -> dashboardSetting.getLevel() == DashboardSettingLevel.THIRD_LEVEL).findFirst();

		ZonedDateTime now = ZonedDateTime.now();
		Integer firstLevelMonthNumber = firstLevelSettingOptional.isPresent() ? firstLevelSettingOptional.get().getMonthNumber() : Const.DASHBOARD_SETTING_DEFAULT.FIRST_LEVEL;
		Instant firstLevelDate = now.minusMonths(firstLevelMonthNumber).toInstant();

		Integer secondLevelMonthNumber = secondLevelSettingOptional.isPresent() ? secondLevelSettingOptional.get().getMonthNumber() : Const.DASHBOARD_SETTING_DEFAULT.SECOND_LEVEL;
		Instant secondLevelDate = now.minusMonths(secondLevelMonthNumber).toInstant();

		Integer thirdLevelMonthNumber = thirdLevelSettingOptional.isPresent() ? thirdLevelSettingOptional.get().getMonthNumber() : Const.DASHBOARD_SETTING_DEFAULT.THIRD_LEVEL;
		Instant thirdLevelDate = now.minusMonths(thirdLevelMonthNumber).toInstant();

		Long firstLevelCount = moldRepository.countByLastShotAt(secondLevelDate, firstLevelDate, payload);
		InactiveToolingCount firstLevelInactiveToolingCount = new InactiveToolingCount(DashboardSettingLevel.FIRST_LEVEL, firstLevelCount, firstLevelMonthNumber);

		Long secondLevelCount = moldRepository.countByLastShotAt(thirdLevelDate, secondLevelDate, payload);
		InactiveToolingCount secondLevelInactiveToolingCount = new InactiveToolingCount(DashboardSettingLevel.SECOND_LEVEL, secondLevelCount, secondLevelMonthNumber);

		Long thirdLevelCount = moldRepository.countByLastShotAt(null, thirdLevelDate, payload);
		InactiveToolingCount thirdLevelInactiveToolingCount = new InactiveToolingCount(DashboardSettingLevel.THIRD_LEVEL, thirdLevelCount, thirdLevelMonthNumber);

		return Arrays.asList(firstLevelInactiveToolingCount, secondLevelInactiveToolingCount, thirdLevelInactiveToolingCount);
	}

	public DashboardTabData getInactiveToolingTabData(TabbedOverviewGeneralFilterPayload payload) {
		List<InactiveToolingCount> inactiveToolingCountList = getInactiveToolingChartData(payload);
		Long total = inactiveToolingCountList.stream().map(InactiveToolingCount::getCount).reduce(Long::sum).orElse(0L);
		return new DashboardTabData(DashboardChartType.INACTIVE_TOOLING, (double) total);
	}

	public ApiResponse updateMold() {
		try {
			List<Mold> molds = moldRepository.findAll();
			molds.forEach(m -> {
				m.setDay(DateUtils.getDate(m.getCreatedAt(), DateUtils.YYYY_MM_DD_DATE_FORMAT));
				m.setWeek(DateUtils.getYearWeek(m.getDay(), DateUtils.YYYY_MM_DD_DATE_FORMAT));
				m.setMonth(m.getDay().substring(0, 6));
			});
			moldRepository.saveAll(molds);

			return ApiResponse.success();
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	public void saveDayWeekMonth(Mold mold) {
		try {
			mold.setDay(DateUtils.getDate(mold.getCreatedAt(), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			mold.setWeek(DateUtils.getYearWeek(mold.getDay(), DateUtils.YYYY_MM_DD_DATE_FORMAT));
			mold.setMonth(mold.getDay().substring(0, 6));
			moldRepository.save(mold);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DigitalizationRateData getDigitalizationRateChartData(TabbedOverviewGeneralFilterPayload payload) {
		Long totalMold = moldRepository.countByPredicate(payload.getMoldFilter());
		if (totalMold == 0) {
			return new DigitalizationRateData(0D, 0D);
		}
		Long totalInstallMold = moldRepository.countByPredicate(QMold.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED).and(payload.getMoldFilter()));
		double digitalPercentRaw = (double) totalInstallMold * 100 / (double) totalMold;
		double digitalPercent = new BigDecimal(digitalPercentRaw).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

		return new DigitalizationRateData(NumberUtils.roundOffNumber(digitalPercent), NumberUtils.roundOffNumber(100D - digitalPercent));
	}

	public DashboardTabData getDigitalizationRateTabData(TabbedOverviewGeneralFilterPayload payload) {
		DigitalizationRateData digitalizationRateData = getDigitalizationRateChartData(payload);
		double digitalPercent = digitalizationRateData.getDigitalPercent();
		return new DashboardTabData(DashboardChartType.DIGITALIZATION_RATE, digitalPercent);
	}

	public List<DashboardChartData> getCycleTimeChartData(TabbedOverviewGeneralFilterPayload payload) {
		QMoldCycleTime moldCycleTime = QMoldCycleTime.moldCycleTime;

		List<DashboardChartData> result = new ArrayList<>();
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(moldCycleTime.cycleTimeStatus.ne(CycleTimeStatus.WITHIN_TOLERANCE));
		predicate.and(moldCycleTime.notificationStatus.eq(NotificationStatus.ALERT));
		predicate.and(moldCycleTime.latest.isTrue());

		predicate.and(payload.getMoldFilter());

		if (AccessControlUtils.isAccessFilterRequired()) {
			predicate.and(moldCycleTime.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		QUserAlert userAlert = QUserAlert.userAlert;
		predicate.and(moldCycleTime.periodType.eq(JPAExpressions.select(userAlert.periodType).from(userAlert)
				.where(userAlert.user.id.eq(SecurityUtils.getUserId()).and(userAlert.alertType.eq(AlertType.CYCLE_TIME)))));

		Iterable<MoldCycleTime> list = moldCycleTimeRepository.findAll(predicate);

		long outsideL1 = 0;
		long outsideL2 = 0;

		for (MoldCycleTime m : list) {
			if (CycleTimeStatus.OUTSIDE_L1 == m.getCycleTimeStatus()) {
				outsideL1++;
			} else if (CycleTimeStatus.OUTSIDE_L2 == m.getCycleTimeStatus()) {
				outsideL2++;
			}
		}

		result.add(new DashboardChartData("Outside L1", outsideL1));
		result.add(new DashboardChartData("Outside L2", outsideL2));

		return result;
	}

	public DashboardTabData getCycleTimeTabData(TabbedOverviewGeneralFilterPayload payload) {
		List<DashboardChartData> cycleTimeList = getCycleTimeChartData(payload);
		long totalCycleTime = cycleTimeList.stream().map(DashboardChartData::getData).reduce(Long::sum).orElse(0L);
		return new DashboardTabData(DashboardChartType.CT_DEVIATION, (double) totalCycleTime);
	}

	public ApiResponse getMaintenanceMapData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			MaintenanceMapData result = new MaintenanceMapData();
			List<MaintenanceContinentData> data = new ArrayList<>();
			List<MaintenanceContinentData> upcomingData = moldMaintenanceRepository.findMoldMaintenanceGroupByContinent(payload, MaintenanceStatus.UPCOMING);
			List<MaintenanceContinentData> overdueData = moldMaintenanceRepository.findMoldMaintenanceGroupByContinent(payload, MaintenanceStatus.OVERDUE);
			Stream.of(ContinentName.values()).forEach(c -> {
				MaintenanceContinentData mcd = new MaintenanceContinentData(c);
				//upcoming count
				MaintenanceContinentData upcoming = upcomingData.stream().filter(u -> u.getContinent().equals(c)).findAny().orElse(null);
				if (upcoming == null)
					mcd.setUpcoming(0L);
				else
					mcd.setUpcoming(upcoming.getCount());
				//overdue count
				MaintenanceContinentData overdue = overdueData.stream().filter(u -> u.getContinent().equals(c)).findAny().orElse(null);
				if (overdue == null)
					mcd.setOverdue(0L);
				else
					mcd.setOverdue(overdue.getCount());

				mcd.setCount(mcd.getOverdue() + mcd.getUpcoming());
				data.add(mcd);
			});

			result.setUpcoming(upcomingData.stream().mapToLong(MaintenanceContinentData::getCount).sum());
			result.setOverdue(overdueData.stream().mapToLong(MaintenanceContinentData::getCount).sum());
			result.setTotal(result.getUpcoming() + result.getOverdue());
			result.setData(data);

			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	public DashboardTabData getMaintenanceTabData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			DashboardTabData data = new DashboardTabData(DashboardChartType.MAINTENANCE);
			data.setKpi(moldMaintenanceRepository.countMaintenance(payload).doubleValue());
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return new DashboardTabData(DashboardChartType.MAINTENANCE);
		}
	}

	public EndOfLifeCycleChartData getEndOfLifeCycleChartData(TabbedOverviewGeneralFilterPayload payload) {
		RefurbishmentConfig config = OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class);
		if (config == null) {
			return new EndOfLifeCycleChartData();
		}
		Long highPriorityToolingNumber = moldEndLifeCycleRepository.countByPriorityTypeAndPredicate(PriorityType.HIGH, payload.getMoldFilter());
		Long mediumPriorityToolingNumber = moldEndLifeCycleRepository.countByPriorityTypeAndPredicate(PriorityType.MEDIUM, payload.getMoldFilter());
		return new EndOfLifeCycleChartData(highPriorityToolingNumber, mediumPriorityToolingNumber, config);
	}

	public DashboardTabData getEndOfLifeCycleTabData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			EndOfLifeCycleChartData endOfLifeCycleChartData = getEndOfLifeCycleChartData(payload);
			Long total = endOfLifeCycleChartData.getHighPriority() + endOfLifeCycleChartData.getMediumPriority();
			return new DashboardTabData(DashboardChartType.END_OF_LIFE_CYCLE, (double) total);
		} catch (Exception e) {
			e.printStackTrace();
			return new DashboardTabData(DashboardChartType.END_OF_LIFE_CYCLE, 0D);
		}
	}

	public void procWactAll() {
		JobUtils.runIfNotRunningQuietly("MoldService.procWactAll", new JobOptions().setClustered(true), () -> {
			List<Mold> moldList = moldRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
			moldList.stream().forEach(m -> BeanUtils.get(MoldService.class).procWact(m));
		});
	}

	@Async
	public void procWactAllAsync() {
		procWactAll();
	}

	@Transactional
	public void procWact(Mold mold) {
		if (mold == null) {
			return;
		}
		try {
			double wact = MoldUtils.getWact(mold.getId(), mold.getContractedCycleTime(), null);
			if (mold.getWeightedAverageCycleTime() == null || mold.getWeightedAverageCycleTime() != wact) {
				mold.setWeightedAverageCycleTime(wact);
				moldRepository.save(mold);
			}
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "MOLD_OCT_FAIL", null, "Mold Optimal Cycle Time Fail at moldId:" + mold.getId(), e);
		}
	}

	public ApiResponse autoConfirmRelocationAlerts() {
		List<MoldLocation> results = moldLocationRepository.findByMoldLocationStatusAndLatest(MoldLocationStatus.CHANGED, true);
		List<MoldPayload> payloads = new ArrayList<>();
		results.forEach(moldLocation -> {
			if (!moldLocationRepository.existsByIdBeforeAndMoldId(moldLocation.getId(), moldLocation.getMoldId())) {
				MoldPayload payload = MoldPayload.builder().build();
				payload.setId(moldLocation.getId());
				payload.setMessage("Auto confirm");
				payloads.add(payload);
			}
		});
		saveMoldLocation(payloads);
		return ApiResponse.success(CommonMessage.OK);
	}

	public ApiResponse fixLastShotAtData() {
		try {
			List<Mold> molds = moldRepository.findAll();
			molds.forEach(mold -> {

				Optional<List<Statistics>> lastOptional = statisticsRepository.findFirstByMoldIdAndCtGreaterThanOrFirstDataIsTrueOrderByLstDesc(mold.getId(), PageRequest.of(0, 1));
				if (lastOptional.isPresent()) {
					Statistics last = lastOptional.get().get(0);
					String hour = last.getHour();
					mold.setLastShotAt(DateUtils.getInstant(hour, DateUtils.yyyyMMddHH));
				}

				Optional<List<Statistics>> lastValOptional = statisticsRepository.findFirstByMoldIdAndCtValGreaterThanOrFirstDataIsTrueOrderByLstDesc(mold.getId(),
						PageRequest.of(0, 1));
				if (lastValOptional.isPresent()) {
					Statistics last = lastValOptional.get().get(0);
					String hour = last.getHour();
					mold.setLastShotAtVal(DateUtils.getInstant(hour, DateUtils.yyyyMMddHH));
				}
			});

			return ApiResponse.success(CommonMessage.OK, molds.size());
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(e.getMessage());
		}
	}

	public void fillResinCodeChangeData(List<ChartData> list, ChartPayload payload) {
		String end;
		switch (payload.getDateViewType()) {
			case HOUR:
				String dateStr = payload.getDate();
				if (!StringUtils.isEmpty(dateStr)) {
					Instant date = DateUtils2.toInstant(dateStr, DatePattern.yyyyMMdd, Zone.SYS);
					end = DateUtils.getNextDay(date, DatePattern.yyyyMMddHHmmss, 1);
				} else {
					end = payload.getEndDate() + "000000";
				}
				list.forEach(item -> {
					List<ResinCodeChange> changes = resinCodeChangeRepository.findByHour(item.getTitle());
					if (CollectionUtils.isNotEmpty(changes)) {
						List<ResinCodeChangeData> data = new ArrayList<>();
						changes.forEach(c -> {
							ResinCodeChangeData dateItem = new ResinCodeChangeData(c);
							String start = DateUtils2.format(c.getTime(), DatePattern.yyyyMMddHHmmss, Zone.SYS);
							Double newWact = statisticsRepository.getPartWACTBetween(c.getPartId(), start, end);
							dateItem.setNewWACT(newWact);
							data.add(dateItem);
						});
					item.setResinCodeChangeData(data);
					}
				});
				break;
			case DAY:
				String monthStr = payload.getMonth();
				if (!StringUtils.isEmpty(monthStr)) {
					Instant month = DateUtils2.toInstant(monthStr, DatePattern.yyyyMM, Zone.SYS);
					Instant firstDayOfNextMonth = DateUtils.getFirstDayOfNextMonth(month).toInstant();
					end = DateUtils2.format(firstDayOfNextMonth, DatePattern.yyyyMMddHHmmss, Zone.SYS);
				} else {
					end = payload.getEndDate() + "000000";
				}
				list.forEach(item -> {
					List<ResinCodeChange> changes = resinCodeChangeRepository.findByDay(item.getTitle());
					if (CollectionUtils.isNotEmpty(changes)) {
						List<ResinCodeChangeData> data = new ArrayList<>();
						changes.forEach(c -> {
							ResinCodeChangeData dateItem = new ResinCodeChangeData(c);
							String start = DateUtils2.format(c.getTime(), DatePattern.yyyyMMddHHmmss, Zone.SYS);
							Double newWact = statisticsRepository.getPartWACTBetween(c.getPartId(), start, end);
							dateItem.setNewWACT(newWact);
							data.add(dateItem);
						});
						item.setResinCodeChangeData(data);
					}
				});
				break;
			case WEEK:
				String yearStr = payload.getYear();
				if (!StringUtils.isEmpty(yearStr)) {
					Instant year = DateUtils2.toInstant(yearStr, DatePattern.yyyy, Zone.SYS);
					Instant firstDayOfNextYear = DateUtils.getFirstDayOfNextYear(year).toInstant();
					end = DateUtils2.format(firstDayOfNextYear, DatePattern.yyyyMMddHHmmss, Zone.SYS);
				} else {
					end = payload.getEndDate() + "000000";
				}
				list.forEach(item -> {
					List<ResinCodeChange> changes = resinCodeChangeRepository.findByWeek(item.getTitle());
					if (CollectionUtils.isNotEmpty(changes)) {
						List<ResinCodeChangeData> data = new ArrayList<>();
						changes.forEach(c -> {
							ResinCodeChangeData dateItem = new ResinCodeChangeData(c);
							String start = DateUtils2.format(c.getTime(), DatePattern.yyyyMMddHHmmss, Zone.SYS);
							Double newWact = statisticsRepository.getPartWACTBetween(c.getPartId(), start, end);
							dateItem.setNewWACT(newWact);
							data.add(dateItem);
						});
						item.setResinCodeChangeData(data);
					}
				});
				break;
			case MONTH:
				String yearStr2 = payload.getYear();
				if (!StringUtils.isEmpty(yearStr2)) {
					Instant year2 = DateUtils2.toInstant(yearStr2, DatePattern.yyyy, Zone.SYS);
					Instant firstDayOfNextYear2 = DateUtils.getFirstDayOfNextYear(year2).toInstant();
					end = DateUtils2.format(firstDayOfNextYear2, DatePattern.yyyyMMddHHmmss, Zone.SYS);
				} else {
					end = payload.getEndDate() + "000000";
				}
				list.forEach(item -> {
					List<ResinCodeChange> changes = resinCodeChangeRepository.findByMonth(item.getTitle());
					if (CollectionUtils.isNotEmpty(changes)) {
						List<ResinCodeChangeData> data = new ArrayList<>();
						changes.forEach(c -> {
							ResinCodeChangeData dateItem = new ResinCodeChangeData(c);
							String start = DateUtils2.format(c.getTime(), DatePattern.yyyyMMddHHmmss, Zone.SYS);
							Double newWact = statisticsRepository.getPartWACTBetween(c.getPartId(), start, end);
							dateItem.setNewWACT(newWact);
							data.add(dateItem);
						});
						item.setResinCodeChangeData(data);
					}
				});
				break;
		}
	}
	public void valid(MoldPayload item, Long id) {

		//check required field because fields is required to use for report calc
		Mold moldExist = id !=null ? moldRepository.findById(id)//
						.orElse(null) : null;

		if (item.getAccumulatedShots() != null) {
			if (moldExist != null)
				loadAccumulatedShot(Arrays.asList(moldExist), null);
			int minAccumulatedShot = moldExist != null ? (moldExist.getAccumulatedShot() != null ? moldExist.getAccumulatedShot() : 0) : 0;
			assertGteValue(item.getAccumulatedShots(), "accumulated_shots", 0,
				moldExist != null ? "tooling's current accumulated shots(" + minAccumulatedShot + ")" : "0");

		}

		ValueUtils.assertNotEmpty(item.getDesignedShot(), "forecasted_max_shots");
		assertGtValue(item.getDesignedShot(),"forecasted_max_shots",0);

		if (item.getLifeYears() != null && (item.getLifeYears() < 1 || item.getLifeYears() > 99)) {
			assertGteValue(item.getLifeYears(),"forecasted_tool_life",1);
			assertLteValue(item.getLifeYears(),"forecasted_tool_life",99);
/*
			if (item.getLifeYears() < 1)
				throw new BizException("greater_than",  new Property("field1", MessageUtils.get("forecasted_tool_life", null)),
					new Property("field2", 0));
			if (item.getLifeYears() > 99)
				throw new BizException("less_than_or_equal_to",  new Property("field1", MessageUtils.get("forecasted_tool_life", null)),
					new Property("field2", 99));
*/
		}
		ValueUtils.assertNotEmpty(item.getLocationId(), "location_code");
		ValueUtils.assertNotEmpty(item.getToolMakerCompanyId(), "toolmaker_id");
		ValueUtils.assertNotEmpty(item.getTotalCavities(), "total_number_of_cavities");
		assertGteValue(item.getTotalCavities(),"total_number_of_cavities",0);

		assertNumberOrNull("tooling",item.getLabour(),"required_labor");

		ValueUtils.assertNotEmpty(item.getSupplierCompanyId(), "supplier_id");
		//default value
		if (org.apache.commons.lang3.StringUtils.isBlank(item.getShiftsPerDay())) {
			if (moldExist != null) {
				item.setShiftsPerDay(moldExist.getShiftsPerDay());
			} else {
				item.setShiftsPerDay("24");
			}
		}
		assertNumberOrNull("tooling", item.getShiftsPerDay(),"production_hour_per_day");

		if(!StringUtils.isEmpty(item.getShiftsPerDay()))
			assertGtValue(Double.valueOf(item.getShiftsPerDay()),"production_hour_per_day",0);

		if (org.apache.commons.lang3.StringUtils.isBlank(item.getProductionDays())) {
			if (moldExist != null) {
				item.setProductionDays(moldExist.getProductionDays());
			} else {
				item.setProductionDays("7");
			}
		}
		assertIntegerNumberOrNull("tooling", item.getProductionDays(),"production_day_per_week");

		if(!StringUtils.isEmpty(item.getProductionDays()))
			assertGtValue(Integer.valueOf(item.getProductionDays()),"production_day_per_week",0);


		// both contractedCycleTime and toolmakerContractedCycleTime must not be null
		if (item.getToolmakerContractedCycleTime() != null && item.getToolmakerContractedCycleTime().intValue() == 0)
			item.setToolmakerContractedCycleTime(null);

		if (item.getToolmakerContractedCycleTime() == null){
			ValueUtils.assertNotEmpty(item.getContractedCycleTime(), "supplier_approved_cycle_time");
			assertGtValue(item.getContractedCycleTime(),"supplier_approved_cycle_time",0);
/*
			if(item.getContractedCycleTime()<=0){
				throw new BizException("greater_than",  new Property("field1", MessageUtils.get("supplier_approved_cycle_time", null)),
					new Property("field2", 0));
			}
*/
		}
		if (item.getContractedCycleTime() != null && item.getContractedCycleTime().intValue() == 0)
			item.setContractedCycleTime(null);

		if (item.getContractedCycleTime() == null){
			ValueUtils.assertNotEmpty(item.getToolmakerContractedCycleTime(), "toolmaker_approved_cycle_time");
			assertGtValue(item.getToolmakerContractedCycleTime(),"toolmaker_approved_cycle_time",0);
/*
			if(item.getToolmakerContractedCycleTime()<=0){
				throw new BizException("greater_than",  new Property("field1", MessageUtils.get("toolmaker_approved_cycle_time", null)),
					new Property("field2", 0));
			}
*/
		}


		ValueUtils.assertNotEmpty(item.getCycleTimeLimit1(), "cycle_time_tolerance_l1");
		ValueUtils.assertNotEmpty(item.getCycleTimeLimit1Unit(), "cycle_time_tolerance_l1_unit");
		ValueUtils.assertNotEmpty(item.getCycleTimeLimit2(), "cycle_time_tolerance_l2");
		ValueUtils.assertNotEmpty(item.getCycleTimeLimit2Unit(), "cycle_time_tolerance_l2_unit");
		ValueUtils.assertNotEmpty(item.getUptimeTarget(), "uptime_target");
		ValueUtils.assertNotEmpty(item.getUptimeLimitL1(), "uptime_tolerance_l1");
		ValueUtils.assertNotEmpty(item.getUptimeLimitL2(), "uptime_tolerance_l2");

		if(item.getPmStrategy() == null) item.setPmStrategy(PM_STRATEGY.SHOT_BASED);
		if(item.getPmStrategy().equals(PM_STRATEGY.SHOT_BASED)){
		ValueUtils.assertNotEmpty(item.getPreventCycle(), "maintenance_interval");
		ValueUtils.assertNotEmpty(item.getPreventUpcoming(), "upcoming_maintenance_tolerance");
		}else {
			ValueUtils.assertNotEmpty(item.getPmFrequency(), "frequency");
			ValueUtils.assertNotEmpty(item.getSchedInterval(), "schedule_every");
			if (item.getPmFrequency().equals(PM_FREQUENCY.MONTHLY)) {
				ValueUtils.assertNotEmpty(item.getSchedOrdinalNum(), "On (1st ,2nd,etc.)");
			}
			ValueUtils.assertNotEmpty(item.getSchedDayOfWeek(), "On Date (Monday, Tuesday, etc)");
			ValueUtils.assertNotEmpty(item.getSchedStartDate(), "first_schedule_starts_on");

			//for
			ValueUtils.assertNotEmpty(item.getRecurrConstraintType(), "Continue Type");

			if(RECURR_CONSTRAINT_TYPE.FOR.equals(item.getRecurrConstraintType())){
				ValueUtils.assertNotEmpty(item.getRecurrNum(), "For (number of times)");
			}else
			if(RECURR_CONSTRAINT_TYPE.UNTIL.equals(item.getRecurrConstraintType())){
				ValueUtils.assertNotEmpty(item.getRecurrDueDate(), "Until");
			}
			//Upcoming Maintenance Tolerance
			ValueUtils.assertNotEmpty(item.getSchedUpcomingTolerance(), "upcoming_maintenance_tolerance");
		}
//		ValueUtils.assertNotEmpty(item.getPreventOverdue(), "overdue_maintenance_tolerance");

		assertGtValue(item.getCycleTimeLimit1(),"cycle_time_tolerance_l1",0);
		if (OutsideUnit.PERCENTAGE.equals(item.getCycleTimeLimit1Unit()))
			assertLtValue(item.getCycleTimeLimit1(), "cycle_time_tolerance_l1", 100);
		assertGtValue(item.getCycleTimeLimit2(),"cycle_time_tolerance_l2",0);
		if (OutsideUnit.PERCENTAGE.equals(item.getCycleTimeLimit2Unit()))
			assertLtValue(item.getCycleTimeLimit2(),"cycle_time_tolerance_l2",100);
		assertGtValue(item.getUptimeTarget(),"uptime_target",0);
		assertLtValue(item.getUptimeTarget(),"uptime_target",100);
		assertGtValue(item.getUptimeLimitL1(),"uptime_tolerance_l1",0);
		assertLtValue(item.getUptimeLimitL1(),"uptime_tolerance_l1",100);
		assertGtValue(item.getUptimeLimitL2(),"uptime_tolerance_l2",0);
		assertLtValue(item.getUptimeLimitL2(),"uptime_tolerance_l2",100);
		if(item.getPmStrategy().equals(PM_STRATEGY.SHOT_BASED)) {
		assertGtValue(item.getPreventCycle(),"maintenance_interval",0);

		assertGtValue(item.getPreventUpcoming(),"upcoming_maintenance_tolerance",0);


		assertLtValue(item.getPreventUpcoming(),"upcoming_maintenance_tolerance", item.getPreventCycle(),"maintenance_interval");
		}else {
			assertGtValue(item.getSchedInterval(),"schedule_every",0);

			Instant schedStartDate = null;
			try{
				if (item.getSchedStartDate() != null)
					schedStartDate = DateUtils2.toInstant(item.getSchedStartDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
			}catch (Exception e){
				LogUtils.saveErrorQuietly(e);
				throw com.emoldino.framework.util.DataUtils.newDataValueInvalidException("tooling", "First schedule starts on (yyyyMMdd)", item.getSchedStartDate());
			}

			if (RECURR_CONSTRAINT_TYPE.FOR.equals(item.getRecurrConstraintType())) {
				assertGteValue(item.getRecurrNum(), "For (number of times)", 0);
			} else if (RECURR_CONSTRAINT_TYPE.UNTIL.equals(item.getRecurrConstraintType())) {
				Instant dueDate = null;
				try {
					if (item.getRecurrDueDate() != null) {
						dueDate = DateUtils2.toInstant(item.getRecurrDueDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
					}
				} catch (Exception e) {
					LogUtils.saveErrorQuietly(e);
					throw com.emoldino.framework.util.DataUtils.newDataValueInvalidException("tooling", "Until (yyyyMMdd)", item.getRecurrDueDate());
				}

				if (dueDate!= null) {
					assertGteValue(Long.valueOf(item.getRecurrDueDate()),"Until (yyyyMMdd)"
						,Long.valueOf(DateUtils2.newString(DatePattern.yyyyMMdd, Zone.SYS)),"today's date");
				}

				if(schedStartDate != null && dueDate!= null){
					assertGteValue(dueDate.truncatedTo(ChronoUnit.DAYS).toEpochMilli(),"Until (yyyyMMdd)"
						,schedStartDate.truncatedTo(ChronoUnit.DAYS).toEpochMilli(),"First schedule starts on (yyyyMMdd)");
				}

			}

			if (item.getPmFrequency().equals(PM_FREQUENCY.WEEKLY)) {
				assertLtValue(item.getSchedUpcomingTolerance(),"Upcoming Maintenance Tolerance (for Time Based)",7);

			}else if (item.getPmFrequency().equals(PM_FREQUENCY.MONTHLY)){
				assertLtValue(item.getSchedUpcomingTolerance(),"Upcoming Maintenance Tolerance (for Time Based)",28);
			}
		}

/*
		if(item.getPreventUpcoming()>=item.getPreventCycle()){
			throw new BizException("less_than",  new Property("field1", MessageUtils.get("upcoming_maintenance_tolerance", null)),
							new Property("field2", MessageUtils.get("maintenance_interval", null)));
		}
*/


		// moldParts
		if (!ObjectUtils.isEmpty(item.getMoldParts())) {
			Arrays.stream(item.getMoldParts()).forEach(moldPart -> {
				moldPart.setTotalCavities(item.getTotalCavities());
				if (moldPart.getCavity() == null) {
					moldPart.setCavity(0);
				}

				{
					assertGteValue(moldPart.getCavity(),"working_cavities",0);
					assertLteValue(moldPart.getCavity(),"working_cavities",item.getTotalCavities(), "total_number_of_cavities");

/*
					if(moldPart.getCavity()<0){
						throw new BizException("greater_than_or_equal_to",  new Property("field1", MessageUtils.get("working_cavities", null)),
							new Property("field2", 0));
					}
					if (moldPart.getCavity() > item.getTotalCavities()) {
						throw new BizException("less_than_or_equal_to",  new Property("field1", MessageUtils.get("working_cavities", null)),
							new Property("field2", MessageUtils.get("total_number_of_cavities", null)));
					}
*/
				}
			});
		} else {
			item.setMoldParts(null);
		}
		// Require Part for import new Tooling
		if (moldExist == null) {
			ValueUtils.assertNotEmpty(item.getMoldParts(), "parts");
		}

		if(org.apache.commons.lang3.StringUtils.isNotBlank(item.getToolingComplexity())
				&& serverName.equals("dyson")
				&& !Arrays.asList("A+", "A", "B", "C", "D").contains(item.getToolingComplexity().trim())) {

			throw new BizException("invalid_data_include",
					new Property("field1", MessageUtils.get("tooling_complexity", null)),
					new Property("field2", "A+, A, B, C, D"));
		}

		// require for other field by configuration
		BeanUtils.get(GeneralConfigService.class).validRequiredField(item,ConfigCategory.TOOLING);
	}

	public void loadWorkOrderDetail(List<Mold> molds) {
		molds.forEach(this::loadWorkOrderDetail);
	}

	public void loadWorkOrderDetail(Mold mold) {
		Optional<WorkOrder> optionalWorkOrder = workOrderRepository.findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus.COMPLETED, mold.getId());
		optionalWorkOrder.ifPresent(workOrder -> mold.setLastWorkOrderId(workOrder.getId()));
		mold.setWorkOrderHistory(workOrderRepository.countByStatusInAndAssetId(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED), mold.getId()));
	}

	public void loadPM(List<Mold> molds) {
		try{
		molds.forEach(this::loadPM);
		}catch (Exception e){
			log.info(e.getMessage());
		}
	}

	public void loadPM(Mold mold) {
		MoldMaintenance moldMaintenance = moldMaintenanceRepository.findFirstByMoldIdAndMaintenanceStatusIsInAndLatestIsTrue(mold.getId(),Arrays.asList(MaintenanceStatus.UPCOMING,MaintenanceStatus.OVERDUE));
		mold.setMoldMaintenance(new MoldMaintenanceDTO(moldMaintenance));
	}

	public ApiResponse getMoldLocationIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldLocationRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldDisconnectIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldDisconnectRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldCycleTimeIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldCycleTimeRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldMaintenanceIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldMaintenanceRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldCorrectiveIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldCorrectiveRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldEfficiencyIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldEfficiencyRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldMisconfiguredIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldMisconfigureRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldDataSubmissionIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldDataSubmissionRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldRefurbishmentIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldRefurbishmentRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldDetachmentIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldDetachmentRepository.getAllIds(predicate));
	}

	public ApiResponse getMoldDowntimeIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, moldDowntimeEventRepository.getAllIds(predicate));
	}


	public ApiResponse getAccumulatedShotByIdAndTime(Long id, Long time) {
		String timeStr = DateUtils2.format(Instant.ofEpochSecond(time), DateUtils2.DatePattern.yyyyMMddHHmmss, DateUtils2.Zone.SYS);
		MoldAccumulatedData data = moldRepository.findMoldAccumulatedShotByLstLessThan(timeStr, id);
		return ApiResponse.success(CommonMessage.OK, data.getAccumulatedShots());
	}

	public void migrationPreviousLocation() {
		List<Long> moldIdHaveMoreThan2Location = moldLocationRepository.getListMoldIdHaveMoreThan2Location();
		List<MoldLocation> moldLocationList2Save = Lists.newArrayList();
		moldIdHaveMoreThan2Location.forEach(moldId -> {
			List<MoldLocation> moldLocationList = moldLocationRepository.findAllByMoldIdOrderByIdDesc(moldId);
			for (int i = 0; i < moldLocationList.size(); i++) {
				if(moldLocationList.get(i).getPreviousLocation() == null && i + 1 < moldLocationList.size()) {
					moldLocationList.get(i).setPreviousLocation(moldLocationList.get(i+1).getLocation());
				}
			}
			moldLocationList2Save.addAll(moldLocationList);
		});
		moldLocationRepository.saveAll(moldLocationList2Save);
	}

	public void saveMoldPmPlan(MoldPayload payload, Mold mold) {
		if (payload == null || payload.getPmStrategy() == null) {
			return;
		}
		MoldPmPlan pmPlan = BeanUtils.get(MoldPmPlanRepository.class).findByMoldId(mold.getId()).orElse(null);
		if(payload.isModifyMoldPmPlan(pmPlan)) {
			if (pmPlan == null) {
				pmPlan = new MoldPmPlan();
				pmPlan.setPmStrategy(PM_STRATEGY.SHOT_BASED);
				pmPlan.setMoldId(mold.getId());
			}

			if (payload.getPmStrategy() != pmPlan.getPmStrategy()) {
				cancelWorkOrderAndMoldMaintain(mold.getId());
			}
			if (payload.getPmStrategy() == PM_STRATEGY.TIME_BASED) {
				pmPlan.setMoldId(mold.getId());
				pmPlan.setPmStrategy(PM_STRATEGY.TIME_BASED);
				pmPlan.setPmFrequency(payload.getPmFrequency());
				pmPlan.setSchedStartDate(payload.getSchedStartDate());
				pmPlan.setSchedInterval(payload.getSchedInterval());
				pmPlan.setSchedOrdinalNum(payload.getSchedOrdinalNum());
				pmPlan.setSchedDayOfWeek(payload.getSchedDayOfWeek() == null ? null : payload.getSchedDayOfWeek().name());
				pmPlan.setSchedUpcomingTolerance(payload.getSchedUpcomingTolerance());
				pmPlan.setRecurrConstraintType(payload.getRecurrConstraintType());
				pmPlan.setRecurrNum(payload.getRecurrNum());
				pmPlan.setRecurrDueDate(payload.getRecurrDueDate());
				nextSchedDateMoldPmPlan(pmPlan, false);
			} else {
				pmPlan.setPmStrategy(PM_STRATEGY.SHOT_BASED);
			}
			BeanUtils.get(MoldPmPlanRepository.class).save(pmPlan);
		}

	}

	public void scheduleMaintainMoldByTime() {
		scheduleMaintainMoldByTime(null, null);
	}
	private void scheduleMaintainMoldByTime(Long moldId, MoldPayload payload) {
		List<MoldPmPlan> moldPmPlanList = moldPmPlanRepository.findAllByPmStrategyAndNextUpcomingToleranceDate(PM_STRATEGY.TIME_BASED,
				DateUtils2.format(Instant.now(), DatePattern.yyyyMMdd, Zone.SYS));

		if (CollectionUtils.isNotEmpty(moldPmPlanList)) {
			for (MoldPmPlan moldPmPlan : moldPmPlanList) {
				Mold mold = moldRepository.getOne(moldPmPlan.getMoldId());

//				get last mold maintenance
				List<MoldMaintenance> lastMoldMaintenanceList = moldMaintenanceRepository.findAllByMoldIdAndMaintenanceStatusIsInAndLatestOrderByIdDesc(moldPmPlan.getMoldId(),
						Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);
				if (CollectionUtils.isNotEmpty(lastMoldMaintenanceList)) {
					MoldMaintenance lastMoldMaintenance = lastMoldMaintenanceList.get(0);
					lastMoldMaintenance.setLatest(false);
					moldMaintenanceRepository.save(lastMoldMaintenance);
				}

				MoldMaintenance maintenance = new MoldMaintenance();
				maintenance.setMold(mold);
				maintenance.setAccumulatedShot(mold.getLastShot());
				maintenance.setMaintenanceStatus(MaintenanceStatus.UPCOMING);
				maintenance.setPreventCycle(mold.getPreventCycle());
				maintenance.setPreventUpcoming(mold.getPreventUpcoming());
				maintenance.setLatest(true);

				moldMaintenanceRepository.save(maintenance);

				workOrderService.cancelOldWorkOrder(moldPmPlan.getMoldId());
				WorkOrder workOrder = generateWorkOrder(moldPmPlan, maintenance, mold);
				// save maintenance file for work order

				saveFileWorkOrder(workOrder.getId(), mold.getId(), moldId, payload);

				maintenance.setWorkOrder(workOrder);
				maintenance.setWorkOrderId(workOrder.getId());
				moldMaintenanceRepository.save(maintenance);
				nextSchedDateMoldPmPlan(moldPmPlan, true);
			}

		}
	}

	private void saveFileWorkOrder(Long workOrderId, Long moldId, Long saveMoldId, MoldPayload payload) {
		if (moldId.equals(saveMoldId) && payload != null) {
			if (payload.getFiles() != null) {
				fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_FILE, workOrderId, payload.getFiles()));
			}
			if (payload.getSecondFiles() != null) {
				fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_FILE, workOrderId, payload.getSecondFiles()));
			}
		} else {
			List<FileStorage> listToSave = new ArrayList<>();

			List<FileStorage> moldMaintenanceDocument = fileStorageRepository.findByRefIdAndStorageType(moldId, StorageType.MOLD_MAINTENANCE_DOCUMENT);
			if (CollectionUtils.isNotEmpty(moldMaintenanceDocument)) {
				moldMaintenanceDocument.forEach(d -> {
					FileStorage fileStorage = new FileStorage();
					fileStorage.setRefId(workOrderId);
					fileStorage.setFileName(d.getFileName());
					fileStorage.setFileSize(d.getFileSize());
					fileStorage.setSaveLocation(d.getSaveLocation());
					fileStorage.setStorageType(StorageType.WORK_ORDER_FILE);

					listToSave.add(fileStorage);
				});
			}

			List<FileStorage> instructionVideo = fileStorageRepository.findByRefIdAndStorageType(moldId, StorageType.MOLD_INSTRUCTION_VIDEO);
			if (CollectionUtils.isNotEmpty(instructionVideo)) {
				instructionVideo.forEach(d -> {
					FileStorage fileStorage = new FileStorage();
					fileStorage.setRefId(workOrderId);
					fileStorage.setFileName(d.getFileName());
					fileStorage.setFileSize(d.getFileSize());
					fileStorage.setSaveLocation(d.getSaveLocation());
					fileStorage.setStorageType(StorageType.WORK_ORDER_FILE);

					listToSave.add(fileStorage);
				});
			}
			fileStorageRepository.saveAll(listToSave);
		}

	}

	private void cancelWorkOrderAndMoldMaintain(Long moldId) {
		workOrderService.cancelOldWorkOrder(moldId);
		List<MoldMaintenance> lastMoldMaintenanceList = moldMaintenanceRepository.findAllByMoldIdAndMaintenanceStatusIsInAndLatestOrderByIdDesc(moldId,
				Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);
		if (CollectionUtils.isNotEmpty(lastMoldMaintenanceList)) {
			lastMoldMaintenanceList.forEach(moldMaintenance -> moldMaintenance.setLatest(false));
			moldMaintenanceRepository.saveAll(lastMoldMaintenanceList);
		}
	}

	private WorkOrder generateWorkOrder(MoldPmPlan moldPmPlan, MoldMaintenance moldMaintenance, Mold mold) {
		WorkOrderPayload payload = new WorkOrderPayload();
		payload.setWorkOrderId(workOrderService.generateWorkOrderId());
		payload.setOrderType(WorkOrderType.PREVENTATIVE_MAINTENANCE);
		payload.setDetails("Automatically generated Preventative Maintenance work order.\nDue date is based on eMoldino’s PM Checkpoint prediction.");
		payload.setPriority(PriorityType.MEDIUM);
		payload.setMoldIds(Collections.singletonList(moldPmPlan.getMoldId()));
		payload.setCost(true);
		payload.setPickingList(true);
		payload.setStart(DateUtils2.toInstant(moldPmPlan.getNextUpcomingToleranceDate(), DatePattern.yyyyMMdd, Zone.SYS));
		payload.setEnd(DateUtils2.toInstant(moldPmPlan.getNextSchedDate(), DatePattern.yyyyMMdd, Zone.SYS));


		WorkOrder workOrder = payload.getModel();
		workOrder.setStatus(WorkOrderStatus.REQUESTED);

		if (moldMaintenance.getMold() != null) {
			if (CollectionUtils.isNotEmpty(moldMaintenance.getMold().getPlantEngineersInCharge()))
				payload.setUserIds(moldMaintenance.getMold().getPlantEngineersInCharge().stream().map(User::getId).collect(Collectors.toList()));
			else if (CollectionUtils.isNotEmpty(moldMaintenance.getMold().getEngineersInCharge())) {
				payload.setUserIds(moldMaintenance.getMold().getEngineersInCharge().stream().map(User::getId).collect(Collectors.toList()));
			}
		}

		if (moldMaintenance.getMold() != null && CollectionUtils.isNotEmpty(moldMaintenance.getMold().getEngineersInCharge())) {
			workOrder.setCreatedBy(moldMaintenance.getMold().getEngineersInCharge().get(0).getId());
		} else {
			User user = userRepository.findByEmailAndDeletedIsFalse("support@emoldino.com").orElse(null);
			workOrder.setCreatedBy(user != null ? user.getId() : null);
		}

		WorkOrder workOrderSave = BeanUtils.get(WorkOrderService.class).saveNormalWorkOrder(workOrder, payload);
		if (moldMaintenance.getMold() != null && CollectionUtils.isNotEmpty(moldMaintenance.getMold().getEngineersInCharge())) {
			WorkOrderUser workOrderUser = new WorkOrderUser();
			workOrderUser.setWorkOrderId(workOrderSave.getId());
			workOrderUser.setWorkOrder(workOrderSave);
			workOrderUser.setUser(moldMaintenance.getMold().getEngineersInCharge().get(0));
			workOrderUser.setUserId(moldMaintenance.getMold().getEngineersInCharge().get(0).getId());
			workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
			workOrderUserRepository.save(workOrderUser);
		}
		return workOrder;
	}

	public void nextSchedDateMoldPmPlan(MoldPmPlan moldPmPlan, boolean createdWorkOrder) {
		String currentDate = DateUtils2.format(Instant.now(), DateUtils2.DatePattern.yyyyMMdd, Zone.SYS);
		if (Integer.parseInt(currentDate) <= Integer.parseInt(moldPmPlan.getSchedStartDate()) && !createdWorkOrder) {
			Instant schedStartDate = DateUtils2.toInstant(moldPmPlan.getSchedStartDate(), DateUtils2.DatePattern.yyyyMMdd, Zone.SYS);
			Instant minusDate = DateUtils.plusDays(schedStartDate, -moldPmPlan.getSchedUpcomingTolerance());
			String dateMinusStr = DateUtils2.format(minusDate, DateUtils2.DatePattern.yyyyMMdd, Zone.SYS);
			if (Integer.parseInt(dateMinusStr) <= Integer.parseInt(currentDate)) {
				moldPmPlan.setNextUpcomingToleranceDate(currentDate);
			} else {
				moldPmPlan.setNextUpcomingToleranceDate(dateMinusStr);
			}
			moldPmPlan.setNextSchedDate(moldPmPlan.getSchedStartDate());
		} else {
			Instant currentNextSchedDate = DateUtils2.toInstant(moldPmPlan.getNextSchedDate() == null ? moldPmPlan.getSchedStartDate() : moldPmPlan.getNextSchedDate(), DateUtils2.DatePattern.yyyyMMdd, Zone.SYS);
			if (moldPmPlan.getPmFrequency() == PM_FREQUENCY.WEEKLY) {
				Instant datePlusWeek = DateUtils.pushWeek(currentNextSchedDate, moldPmPlan.getSchedInterval());
				Instant nextSchedDate = DateUtils.withDayOfWeek(datePlusWeek, DayOfWeek.valueOf(moldPmPlan.getSchedDayOfWeek()));
				moldPmPlan.setNextSchedDate(DateUtils2.format(nextSchedDate, DateUtils2.DatePattern.yyyyMMdd, Zone.SYS));
				Instant nextUpcomingToleranceDate = DateUtils.plusDays(nextSchedDate, -moldPmPlan.getSchedUpcomingTolerance());
				moldPmPlan.setNextUpcomingToleranceDate(DateUtils2.format(nextUpcomingToleranceDate, DateUtils2.DatePattern.yyyyMMdd, Zone.SYS));
			} else {
				Instant datePlusWeek = DateUtils.pushMonth(currentNextSchedDate, moldPmPlan.getSchedInterval());
				Instant nextSchedDate = DateUtils.withDayOfMonth(datePlusWeek, DayOfWeek.valueOf(moldPmPlan.getSchedDayOfWeek()), moldPmPlan.getSchedOrdinalNum());
				moldPmPlan.setNextSchedDate(DateUtils2.format(nextSchedDate, DateUtils2.DatePattern.yyyyMMdd, Zone.SYS));
				Instant nextUpcomingToleranceDate = DateUtils.plusDays(nextSchedDate, -moldPmPlan.getSchedUpcomingTolerance());
				moldPmPlan.setNextUpcomingToleranceDate(DateUtils2.format(nextUpcomingToleranceDate, DateUtils2.DatePattern.yyyyMMdd, Zone.SYS));
			}
		}
	}

	public void changeTabPayload(MoldPayload payload) {
		payload.setServerName(serverName);

		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TOOLING_SETTING));

		if (payload.getIsModalSelected() != null && payload.getIsModalSelected()) {
			payload.setSelectedFields(Collections.singletonList("company"));
		}
		payload.setIsDefaultTab(true);
		if (payload.getTabId() != null) {
			Optional<TabTable> tabTableOptional = tabTableRepository.findById(payload.getTabId());
			if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
				List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payload.getTabId());
				List<Long> moldIdList = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
				payload.setIds(moldIdList);
				payload.setIsDefaultTab(tabTableOptional.get().getIsDefaultTab());
			}
		}
		if (payload.getInactiveLevel() != null) {
			filterInactiveNew(payload);
		}
	}

	private void loadEngineersInfo(List<Mold> molds) {
		UserParam param = new UserParam();
		param.setStatus("active");
		List<Long> allowedEngineerIds = userService.getAllIds(param);

		molds.forEach(m -> {
			List<UserLiteDTO> engineers = new ArrayList<>();
			m.getEngineersInCharge().forEach(u -> {
				if (allowedEngineerIds.contains(u.getId())) {
					engineers.add(new UserLiteDTO(u));
				}
			});
			m.setEngineers(engineers);
		});
	}
	public MoldPmPlan getMoldPmPlanByMoldId(Long moldId) {
		return moldPmPlanRepository.findByMoldId(moldId).orElse(null);
	}

	private void loadPlantEngineersInfo(List<Mold> molds) {
		UserParam param = new UserParam();
		param.setStatus("active");
		List<Long> allowedEngineerIds = userService.getAllIds(param);

		molds.forEach(m -> {
			List<UserLiteDTO> plantEngineers = new ArrayList<>();
			m.getPlantEngineersInCharge().forEach(u -> {
				if (allowedEngineerIds.contains(u.getId())) {
					plantEngineers.add(new UserLiteDTO(u));
				}
			});
			m.setPlantEngineers(plantEngineers);
		});
	}
	private void loadUntilNextPm(List<Mold> molds) {
		molds.forEach(mold -> {
			Optional<WorkOrder> workOrder = BeanUtils.get(WorkOrderRepository.class).findFirstByStatusAndAssetIdAndOrderType(WorkOrderStatus.COMPLETED, mold.getId(), WorkOrderType.PREVENTATIVE_MAINTENANCE);
			Integer shotSinceCompletion;
			if (workOrder.isPresent() && workOrder.get().getMoldMaintenance() != null) {
				shotSinceCompletion = (mold.getLastShot() != null ? mold.getLastShot() : 0) - (workOrder.get().getMoldMaintenance().getAccumulatedShot() != null ? workOrder.get().getMoldMaintenance().getAccumulatedShot() : 0);
				mold.setPmCheckpoint(mold.getPreventCycle() + (workOrder.get().getMoldMaintenance().getAccumulatedShot() != null ? workOrder.get().getMoldMaintenance().getAccumulatedShot() : 0));
			} else {
				shotSinceCompletion= mold.getLastShot() != null ? mold.getLastShot() : 0;
				mold.setPmCheckpoint(shotSinceCompletion > mold.getPreventCycle()
						? (((int)(shotSinceCompletion / mold.getPreventCycle()) + 1) * mold.getPreventCycle())
						: mold.getPreventCycle());
			}
			Integer countUp = workOrder.isPresent() ? (shotSinceCompletion < mold.getPreventCycle() ? shotSinceCompletion : mold.getPreventCycle()) : (shotSinceCompletion % mold.getPreventCycle());
			if (countUp<0){
				mold.setUntilNextPm(0);
			}else if (countUp > mold.getPreventCycle()) {
				mold.setUntilNextPm(mold.getPreventCycle());
			}else {
				mold.setUntilNextPm(countUp);
			}
		});
	}

	public void enqueuePMWorkOrderDueDate(Long moldId) {
		JobCall call = new JobCall();
		call.setLogicName(ReflectionUtils.toName(MoldService.class, "updatePMWorkOrderDueDate"));
		call.getParams().add(new JobCallParam("moldId", Long.class, moldId));
		JobUtils.enqueue("woTaskExecutor", "PMWorkOrderDueDate." + moldId, false, call, null, null, null);
	}

	public void updatePMWorkOrderDueDate(Long moldId) {
		LogicUtils.assertNotNull(moldId, "moldId");

		Mold mold;
		Page<Mold> page = TranUtils.doNewTran(() -> moldRepository.findAllByMasterFilter(//
				Q.mold.id.eq(moldId).and(Q.mold.toolingStatus.in(ToolingStatus.values())), //
				ActiveStatus.ENABLED, //
				PageRequest.of(0, 1)//
		));
		mold = page.isEmpty() ? null : page.getContent().get(0);
		if (mold == null//
				|| (mold.getPmStrategy() != null && !PM_STRATEGY.SHOT_BASED.equals(mold.getPmStrategy()))) {
			return;
		}

		QWorkOrderAsset qWorkOrderAsset = QWorkOrderAsset.workOrderAsset;
		JPQLQuery<WorkOrder> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(qWorkOrderAsset.workOrder)//
				.distinct()//
				.from(qWorkOrderAsset)//
				.where(//
						qWorkOrderAsset.assetId.eq(moldId)//
								.and(qWorkOrderAsset.workOrder.status.notIn(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)))//
								.and(qWorkOrderAsset.workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE))//
				);

		Instant end = mold.getPmCheckpointPrediction() == null || mold.getPmCheckpointPrediction() <= 0 ? //
				DateUtils2.newInstant() //
				: DateUtils2.newInstant().plus(mold.getPmCheckpointPrediction(), ChronoUnit.DAYS);
		com.emoldino.framework.util.DataUtils.runContentBatch(query, 100, true, workOrders -> {
			workOrders.forEach(wo -> TranUtils.doNewTran(() -> {
				if (mold.getLastShot() != null && mold.getPmCheckpoint() != null && mold.getLastShot() < mold.getPmCheckpoint()) {
					wo.setEnd(end);
					workOrderRepository.save(wo);
				}
			}));
		});
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void migratePMWorkOrderDueDate() {
		JobUtils.runIfNotRunning("migratePMWorkOrderDueDate", new JobOptions().setClustered(true).setOnceOnly(true), () -> {
			BooleanBuilder builder = new BooleanBuilder(//
					Q.mold.toolingStatus.in(Arrays.asList(//
							ToolingStatus.IN_PRODUCTION, //
							ToolingStatus.IDLE, //
							ToolingStatus.INACTIVE, //
							ToolingStatus.SENSOR_DETACHED, //
							ToolingStatus.SENSOR_OFFLINE, //
							ToolingStatus.ON_STANDBY, //
							ToolingStatus.NO_SENSOR//
			)));
			com.emoldino.framework.util.DataUtils.runBatch(//
					Sort.by("id"), 100, true, //
					pageable -> moldRepository.findAllByMasterFilter(builder, ActiveStatus.ENABLED, pageable), //
					mold -> updatePMWorkOrderDueDate(mold.getId())//
			);
		});
	}

	public void loadRemainingPartsCount(List<Mold> molds) {
		try{
			molds.forEach(this::loadRemainingPartsCount);
		}catch (Exception e){
			log.info(e.getMessage());
		}
	}

	public void loadRemainingPartsCount(Mold mold) {
		int forecastedMaxShots = ValueUtils.toInteger(mold.getDesignedShot(), 0);
		int accumulatedShot = ValueUtils.toInteger(mold.getLastShot(), 0);
		int totalCavities = ValueUtils.toInteger(mold.getTotalCavities(), 0);

		long remainingPartsCount = Math.max(0, (forecastedMaxShots - accumulatedShot) * (long)totalCavities);
		mold.setRemainingPartsCount(remainingPartsCount);
	}

}
