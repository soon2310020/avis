package saleson.api.mold;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.emoldino.framework.enumeration.ActiveStatus;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.QueryUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.counter.CounterRepository;
import saleson.api.machine.payload.MachineMoldData;
import saleson.api.mold.payload.DataSubmissionConfirmListPayload;
import saleson.api.mold.payload.ExportPayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.mold.payload.NoteAndChangeStatusPayLoad;
import saleson.api.mold.payload.SwitchMoldPartPayload;
import saleson.api.versioning.service.VersioningService;
import saleson.api.workOrder.WorkOrderService;
import saleson.common.config.Const;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.ListIdPayload;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.*;
import saleson.common.exception.AppException;
import saleson.common.payload.ApiResponse;
import saleson.common.scheduling.BatchJob;
import saleson.common.scheduling.ScheduledTask;
import saleson.common.util.DataUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.dto.CustomField.CustomFieldListDTO;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.MoldCorrective;
import saleson.model.MoldCycleTime;
import saleson.model.MoldDataSubmission;
import saleson.model.MoldDetachment;
import saleson.model.MoldDisconnect;
import saleson.model.MoldDowntimeEvent;
import saleson.model.MoldEfficiency;
import saleson.model.MoldLocation;
import saleson.model.MoldMisconfigure;
import saleson.model.MoldPart;
import saleson.model.MoldRefurbishment;
import saleson.model.data.AlertCount;
import saleson.model.data.MaintenanceData;
import saleson.model.data.MaintenanceLog;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldLiteData;
import saleson.model.data.MoldReportData;
import saleson.model.logs.LogDisconnection;
import saleson.service.transfer.LogDisconnectionService;

@Slf4j
@RestController
@RequestMapping("/api/molds")
public class MoldController {

	@Lazy
	@Autowired
	MoldService moldService;

	@Autowired
	LogDisconnectionService logDisconnectionService;

	@Autowired
	MoldRepository moldRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Value("${customer.server.name}")
	private String serverName;

	@Autowired
	VersioningService versioningService;

	@Autowired
	CounterRepository counterRepository;

	@Autowired
	CustomFieldValueService customFieldValueService;

	@Autowired
	DynamicExportService dynamicExportService;

	@Autowired
	BatchJob batchJob;

	@Autowired
	ColumnTableConfigService columnTableConfigService;

	@Autowired
	GeneralConfigService generalConfigService;


	@GetMapping("/test-operating-status")
	public String testOperatingStatus() {
		new ScheduledTask().updateOperatingStatus();
		return "OK";
	}

	@PostMapping("/filtered-by-ids")
	public ResponseEntity<Page<Mold>> getAllMolds_POST(MoldPayload payload, Pageable pageable, Model model,
													   @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMolds(payload, pageable, model);
	}

	/**
	 * 금형 전체 목록
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */

	@GetMapping
	public ResponseEntity<Page<Mold>> getAllMolds(MoldPayload payload, Pageable pageable, Model model) {
		Boolean deleted = payload.getDeleted();
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

		moldService.changeTabPayload(payload);
		String[] properties = { "" };
		Sort.Direction[] directions = { Sort.Direction.DESC };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);
		if ("lastShotAt".equals(properties[0]) && dataFilterEnabled) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), directions[0], "lastShotAtVal");
		}
		//		moldService.loadTreeCompanyForPayLoad(payload);
		Page<Mold> page;
		if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected()) {
			page = moldService.findAll(payload.getPredicate(), pageable, payload.getAccumulatedShotFilter(), payload.getFilterCode(), activeStatus);
		} else if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected()) {
			page = moldService.findAllWithTabbedDashboardFilter(payload.getPredicate(), pageable, payload.getAccumulatedShotFilter(), payload.getFilterCode(), activeStatus);
		} else {
			page = moldService.findAll(payload.getPredicate(), pageable, true, payload.getAccumulatedShotFilter(), payload.getFilterCode(), activeStatus);
		}

		page.forEach(item -> {
			moldService.setMaxCapacity(item);
			item.setDataFilterEnabled(dataFilterEnabled);
		});
		model.addAttribute("pageContent", page);
		return new ResponseEntity<>(page, HttpStatus.OK);
	}

	@GetMapping("/lite")
	public ApiResponse getMoldLiteData(@RequestParam(value = "code", required = false) String code, Pageable pageable) {
		return moldService.findMoldLiteData(code, pageable);
	}

	@GetMapping("/lite-data-work-order")
	public Page<MoldLiteData> getAllMoldLiteData(MoldPayload payload, Pageable pageable) {
		payload.setServerName(serverName);
		payload.setSelectedFields(new ArrayList<>());
		return moldService.getAllMoldLite(payload.getPredicate(),pageable);
	}
	@GetMapping("/ids")
	public ResponseEntity<List<Long>> getAllMoldIds(MoldPayload payload, Pageable pageable, Model model) {
		payload.setServerName(serverName);
		QueryUtils.includeDisabled(Q.mold);
		Page<Mold> pageContent = moldService.findAll(payload.getPredicate(), pageable, true, payload.getAccumulatedShotFilter());
		List<Long> ids = pageContent.getContent().stream().map(Mold::getId).collect(Collectors.toList());
		return new ResponseEntity<>(ids, HttpStatus.OK);
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/locations-filtered-by-ids")
	public ResponseEntity<?> getAllMoldLocations_POST(MoldPayload payload, Pageable pageable, Model model,
													   @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldLocations(payload, pageable, model);
	}

	/**
	 * 금형 위치정보
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("/locations")
	public ResponseEntity<?> getAllMoldLocations(MoldPayload payload, Pageable pageable, Model model) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.RELOCATION_ALERT));
		Page<MoldLocation> pageContent = moldService.findLocations(payload.getLocationPredicate(), pageable, payload.getAccumulatedShotFilter());
//		if (payload.getInList() != null && payload.getInList() == true)
//			moldService.addPreviousLocations(pageContent);
		model.addAttribute("pageContent", pageContent);
 		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/cycle-time-filtered-by-ids")
	public ResponseEntity<?> getAllMoldCycleTime_POST(MoldPayload payload, Pageable pageable, Model model,
													  @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldCycleTime(payload, pageable, model);
	}

	/**
	 * 금형 사이클타임 경고
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("/cycle-time")
	public ResponseEntity<Page<MoldCycleTime>> getAllMoldCycleTime(MoldPayload payload, Pageable pageable, Model model) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.CYCLE_TIME_ALERT));

		Page<MoldCycleTime> pageContent;
		if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
			pageContent = moldService.findCycleTimeAllFiltered(payload.getCycleTimePredicate(), pageable, payload.getAccumulatedShotFilter());
		else if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected())
			pageContent = moldService.findCycleTimeAllFiltered_New(payload.getCycleTimePredicate(), pageable, payload.getAccumulatedShotFilter());
		else
			pageContent = moldService.findCycleTimeAll(payload.getCycleTimePredicate(), pageable, payload.getAccumulatedShotFilter());

		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/cycle-time/{moldId}")
	public ResponseEntity<MoldCycleTime> getMoldCycleTimeById(@PathVariable(value = "moldId") Long id) {
		List<MoldCycleTime> moldCycleTimeList = moldService.findCycleTimeByMoldId(id);
		return new ResponseEntity(moldCycleTimeList, HttpStatus.OK);
	}

	@PutMapping("/cycle-time/{id}/confirm")
	public ApiResponse confirmCycleTime(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			moldService.saveMoldCycleTime(payload);
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PutMapping("/cycle-time/confirm")
	public ApiResponse confirmCycleTime(@RequestBody List<MoldPayload> payloadList) {
		if (payloadList == null || payloadList.size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		moldService.saveMoldCycleTime(payloadList);
		return ApiResponse.success();
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/efficiency-filtered-by-ids")
	public ResponseEntity<?> getAllMoldEfficiency_POST(MoldPayload payload, Pageable pageable, Model model,
													  @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldEfficiency(payload, pageable, model);
	}

	@DataLeakDetector(disabled = true)
	@GetMapping("/efficiency")
	public ResponseEntity<Page<MoldEfficiency>> getAllMoldEfficiency(MoldPayload payload, Pageable pageable, Model model) {
		Page<MoldEfficiency> pageContent;
		if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
			pageContent = moldService.findEfficiencyAllFiltered(payload.getEfficiencyPredicate(), pageable);
		else
			pageContent = moldService.findEfficiencyAll(payload.getEfficiencyPredicate(), pageable);

		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@PutMapping("/efficiency/{id}/confirm")
	public ApiResponse confirmEfficiency(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			moldService.saveMoldEfficiency(payload);
//			logEfficiencyService.confirm(id, payload.getMessage());
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PutMapping("/efficiency/confirm")
	public ApiResponse confirmEfficiency(@RequestBody List<MoldPayload> payloadList) {
		if (payloadList == null || payloadList.size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		moldService.saveMoldEfficiency(payloadList);
//		List<Long> ids = payloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
//		String message = payloadList.get(0).getMessage();
//		logEfficiencyService.confirm(ids, message);
		return ApiResponse.success();
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/disconnect-filtered-by-ids")
	public ResponseEntity<?> getAllDisconnectTime_POST(MoldPayload payload, Pageable pageable, Model model,
													   @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllDisconnectTime(payload, pageable, model);
	}

	/**
	 * 금형 Disconnect
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("/disconnect")
	public ResponseEntity<Page<MoldDisconnect>> getAllDisconnectTime(MoldPayload payload, Pageable pageable, Model model) {
		Page<MoldDisconnect> pageContent = moldService.findDisconnectAll(payload.getDisconnectPredicate(), pageable, payload.getAccumulatedShotFilter());
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@PutMapping("/disconnect/{id}/confirm")
	public ApiResponse disconnectConfirm(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			moldService.saveMoldDisconnect(payload);
			logDisconnectionService.confirm(id, payload.getMessage());
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PutMapping("/disconnect/confirm")
	public ApiResponse disconnectConfirm(@RequestBody List<MoldPayload> payloadList) {
		if (payloadList == null || payloadList.size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		moldService.saveMoldDisconnect(payloadList);
		List<Long> ids = payloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		String message = payloadList.get(0).getMessage();
		logDisconnectionService.confirm(ids, message);
		return ApiResponse.success();
	}

	@GetMapping("/disconnect/alert/{moldId}")
	public ResponseEntity<LogDisconnection> getMoldDisconnectHistoryById(@PathVariable(value = "moldId") Long id, NotificationStatus notificationStatus, Pageable pageable,
			Model model) {
		Page<LogDisconnection> pageContent = moldService.getDisconnectHistoryByMoldId(id, notificationStatus, pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity(pageContent, HttpStatus.OK);
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/maintenance-filtered-by-ids")
	public ResponseEntity<?> getAllMoldMaintenance_POST(MoldPayload payload, Pageable pageable, Model model,
													   @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldMaintenance(payload, pageable, model);
	}

	/**
	 * 금형 정비 목록
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("/maintenance")
	public ResponseEntity<Page<MaintenanceData>> getAllMoldMaintenance(MoldPayload payload, Pageable pageable, Model model) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MAINTENANCE_ALERT));

		Page<MaintenanceData> pageContent;
		if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
			pageContent = moldService.findMaintenanceDataAllFilter(payload.getMaintenancePredicate(), pageable, payload.getAccumulatedShotFilter());
		else if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected())
			pageContent = moldService.findMaintenanceDataAllFilter_New(payload.getMaintenancePredicate(), pageable, payload.getAccumulatedShotFilter());
		else
			pageContent = moldService.findMaintenanceDataAll(payload.getMaintenancePredicate(), pageable, payload.getAccumulatedShotFilter());

		BeanUtils.get(WorkOrderService.class).loadWorkOrderData(pageContent.getContent());
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/maintenance/history/{moldId}")
	public ResponseEntity<MaintenanceLog> getMaintenanceLogByMoldId(@PathVariable(value = "moldId") Long moldId) {
		return new ResponseEntity<>(moldService.findMaintenanceLogByMoldId(moldId), HttpStatus.OK);
	}

	@PutMapping("/maintenance/{id}/done")
	public ApiResponse maintenanceComplete(@PathVariable(value = "id", required = true) Long id, MultipartFormData formData) {

		try {
			MoldPayload payload = objectMapper.readValue(formData.getPayload(), MoldPayload.class);
			payload.setFiles(formData.getFiles());

			moldService.completeMoldMaintenance(payload);
			return new ApiResponse(true, "OK.");
		} catch (Exception e) {
			e.getStackTrace();
			return ApiResponse.error("Error: " + e.getMessage());
		}
	}

	@PostMapping("/maintenance/register-pm/{id}")
	public ApiResponse maintenanceRegister(@PathVariable(value = "id") Long id, MultipartFormData formData) {
		try {
			MoldPayload payload = objectMapper.readValue(formData.getPayload(), MoldPayload.class);
			payload.setFiles(formData.getFiles());

			moldService.registerMoldMaintenance(payload);
			return new ApiResponse(true, "OK.");
		} catch (Exception e) {
			e.getStackTrace();
			return ApiResponse.error("Error: " + e.getMessage());
		}
	}

	@GetMapping("/maintenance/molds-list")
	public ResponseEntity<List<MiniComponentData>> getMoldsForPMRegistering() {
		return new ResponseEntity<>(moldService.findMoldsForPMRegistering(), HttpStatus.OK);
	}

	@GetMapping("/maintenance/fix-duplicated-data")
	public ApiResponse fixDuplicatedData() {
		return moldService.fixDuplicatedMaintenanceData();
	}

	@GetMapping("/maintenance/recover-last-shot-made-maintenance")
	public ResponseEntity<?> recoverLastShotMadeMaintenance() {
		return new ResponseEntity<>(moldService.recoverLastShotMadeMaintenance(), HttpStatus.OK);
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/data-submission-filtered-by-ids")
	public ResponseEntity<?> getAllMoldDataSubmission_POST(MoldPayload payload, Pageable pageable, Model model,
														@RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldDataSubmission(payload, pageable, model);
	}

	@GetMapping("/data-submission")
	public ResponseEntity<Page<MoldDataSubmission>> getAllMoldDataSubmission(MoldPayload payload, Pageable pageable, Model model) {
		Page<MoldDataSubmission> pageContent = moldService.findDataSubmissionAll(payload.getDataSubmissionPredicate(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/data-submission/{moldId}")
	public ResponseEntity getDetailsDataSubmission(@PathVariable(value = "moldId") Long moldId, Pageable pageable, Model model) {
		Page<MoldDataSubmission> pageContent = moldService.findDataSubmissionByMoldId(moldId, pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/data-submission/{moldId}/disapproval-reason")
	public ResponseEntity getDisapprovalReason(@PathVariable(value = "moldId") Long moldId) {
		return ResponseEntity.ok(moldService.findLastDisapprovalByMoldId(moldId));
	}

	@GetMapping("/data-submission/{moldId}/approval-reason")
	public ResponseEntity getApprovalReason(@PathVariable(value = "moldId") Long moldId) {
		return ResponseEntity.ok(moldService.findLastDataSubmissionByMoldId(moldId, NotificationStatus.APPROVED));
	}

//	@PostMapping("/data-submission/confirm")
//	public ResponseEntity confirmSubmission(@RequestBody DataSubmissionConfirmPayload payload){
//		return ResponseEntity.ok(moldService.confirmDataSubmission(payload));
//	}

	@PostMapping("/data-submission/confirm")
	public ResponseEntity confirmSubmissionList(@RequestBody DataSubmissionConfirmListPayload payload) {
		return ResponseEntity.ok(moldService.confirmDataSubmission(payload));
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/downtime-filtered-by-ids")
	public ResponseEntity<?> getAllDowntime_POST(MoldPayload payload, Pageable pageable, Model model,
														   @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllDowntime(payload, pageable, model);
	}

	@DataLeakDetector(disabled = true)
	@GetMapping("/downtime")
	public ResponseEntity<Page<MoldDowntimeEvent>> getAllDowntime(MoldPayload payload, Pageable pageable, Model model) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.DOWNTIME_ALERT));
		Page<MoldDowntimeEvent> pageContent = moldService.findDowntimeEventAll(payload.getDowntimePredicate(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	/**
	 * Alert Count
	 * @return
	 */
	@GetMapping("/alert-count")
	public List<AlertCount> getAlertCount(@RequestParam(value = "adminPage", defaultValue = "false") String adminPage) throws ExecutionException, InterruptedException {
		return moldService.getAlertCount(adminPage);
	}

	/**
	 * Alert Count POST
	 * @return
	 */
	@PostMapping("/alert-count-filtered-by-ids")
	public List<AlertCount> getAlertCountByIds(@RequestBody ListIdPayload ids) throws ExecutionException, InterruptedException {
		return moldService.getAlertCountByIds(ids);
	}

	/**
	 * Alert Count (관리자용 terminal, counter, tooling)
	 *
	 * Teminal - 연결 끊긴 터미널 목록
	 * Counter - 금형과 연결이 안된 채 동작하거나 등록이 안된채 동작하는 카운터 목록
	 * Tooling - 카운터가 인스톨 되었는데 정상적으로 인지가 안되는 금형 리스트
	 * @return
	 */
	@GetMapping("/alert-count-for-admin")
	public List<AlertCount> getAlertCountForAdmin() {
		return moldService.getAlertCountForAdmin();
	}

	@PostMapping
	public ResponseEntity<?> newMold(MultipartFormData formData) {
		try {
			MoldPayload payload = objectMapper.readValue(formData.getPayload(), MoldPayload.class);
			payload.setFiles(formData.getFiles());
			payload.setSecondFiles(formData.getSecondFiles());
			payload.setThirdFiles(formData.getThirdFiles());
			payload.setForthFiles(formData.getForthFiles());
			payload.setEquipmentCode(StringUtils.trimWhitespace(payload.getEquipmentCode()));
			return newMold(payload);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
//			return ApiResponse.error("저장 중 오류 ");
			e.printStackTrace();
			return ResponseEntity.badRequest().body(e.getMessage());
		}
//		return ApiResponse.success();
	}

	public ResponseEntity<?> newMold(MoldPayload payload) {
		if (moldService.existsByEquipmentCode(payload.getEquipmentCode(), null)) {
//			return ApiResponse.error("ToolingID already registered.");
			return ResponseEntity.badRequest().body("Tooling ID is already registered in the system.");
		}
		moldService.valid(payload,null);
		Mold m = moldService.save(payload.getModel(), payload);
		moldService.saveDayWeekMonth(m);
		return ResponseEntity.ok(m);
	}

	@PostMapping("/import-molds")
	public ResponseEntity<?> newMolds(@RequestBody List<MoldPayload> payloadList) {
		for (MoldPayload payload : payloadList) {
			try {
				CustomFieldListDTO customFieldListDTO = new CustomFieldListDTO();
				customFieldListDTO.setCustomFieldDTOList(payload.getCustomFieldDTOList());
				Mold mold;
				if (payload.getId() == null) {
					mold = moldService.save(payload.getModel(), payload);
					moldService.saveDayWeekMonth(mold);
				} else {
					mold = moldService.findById(payload.getId());
					if (mold != null) {
						payload.setEquipmentStatus(mold.getEquipmentStatus());
						if (payload.getLastShot() == null || payload.getLastShot().equals(0)) {
							payload.setLastShot(mold.getLastShot());
						}
						moldService.save(payload.getModel(mold), payload);
						moldService.saveDayWeekMonth(mold);
					}
				}
				if (mold != null)
					customFieldValueService.editListCustomField(mold.getId(), customFieldListDTO);
			} catch (BizException e) {
				LogUtils.saveErrorQuietly(e);
				AbstractException ae = ValueUtils.toAe(e, null);
				return ResponseEntity.ok(new ApiResponse(false, ae.getCodeMessage(), payload.getEquipmentCode()));
			} catch (Exception exception) {
				return ResponseEntity.ok(new ApiResponse(false, "FAIL", payload.getEquipmentCode()));
			}
		}

		return ResponseEntity.ok(ApiResponse.success());
	}

	@GetMapping("/check-exists")
	public List<MiniComponentData> checkExistsToolingCode(@RequestParam List<String> moldCodes) {
		List<MiniComponentData> existsToolingCodes = moldService.getExistsMoldCodes(moldCodes);
		return existsToolingCodes;
	}

	@PostMapping("/check-exists-v2")
	public List<MiniComponentData> checkExistsToolingCodev2(@RequestBody List<String> moldCodes) {
//		List<String> moldCodesElements = Arrays.asList(moldCodes.split(","));
		List<MiniComponentData> existsToolingCodes = moldService.getExistsMoldCodes(moldCodes);
		return existsToolingCodes;
	}

	@DataLeakDetector(disabled = true)
	@GetMapping("{id}")
	public ResponseEntity<Mold> getMold(@PathVariable("id") Long id) {

		try {
			Mold mold = moldService.findById(id);
			moldService.setMaxCapacity(mold);
			return new ResponseEntity<>(mold, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	/**
	 * 해당 금형의 부품 리스트
	 * @param moldId
	 * @return
	 */
	@GetMapping("{id}/parts")
	public ResponseEntity<Map<String, Iterable<MoldPart>>> getMoldParts(@PathVariable("id") Long moldId) {

		try {

			Map<String, Iterable<MoldPart>> content = new HashMap<>();
			content.put("content", moldService.findMoldPartsById(moldId));

			return new ResponseEntity<>(content, HttpStatus.OK);

			//Page<MoldPart> moldParts = moldService.findMoldPartsById(id);
			//return new ResponseEntity<>(moldParts, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@PutMapping("{id}")
	/*public ApiResponse editMold(@PathVariable("id") Long id,
								   @RequestBody MoldPayload payload) {*/
	public ResponseEntity<?> editMold(@PathVariable("id") Long id, MultipartFormData formData) {
		try {
			MoldPayload payload = objectMapper.readValue(formData.getPayload(), MoldPayload.class);
			payload.setFiles(formData.getFiles());
			payload.setSecondFiles(formData.getSecondFiles());
			payload.setThirdFiles(formData.getThirdFiles());
			payload.setForthFiles(formData.getForthFiles());
			return editMold(id, payload);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		} catch (Exception e) {
			e.getStackTrace();
			return ResponseEntity.badRequest().body("ERROR " + e.getMessage());

		}
	}

	public ResponseEntity<?> editMold(@PathVariable("id") Long id, MoldPayload payload) {
		try {
			Mold mold = moldService.findById(id);
			Mold oldToCheck =  DataUtils.deepCopyJackSon(mold,Mold.class);

/*

			Mold updated = payload.getModel();
			updated.setId(mold.getId());
			DataUtils.mapCreateAndUpdateInfo(updated, mold);
			boolean isIdentical = DataUtils.deepCompare(mold, updated);
*/

			if (mold == null) {
//				return ApiResponse.error("ERROR");
				return ResponseEntity.badRequest().body("Fail!");

			}
//			Write log
			payload.setEquipmentCode(StringUtils.trimWhitespace(payload.getEquipmentCode()));
			//valid
			moldService.valid(payload,id);
			if (moldService.existsByEquipmentCode(payload.getEquipmentCode(), id)) {
//				return ApiResponse.error("ToolingID already registered.");
				return ResponseEntity.badRequest().body("Tooling ID is already registered in the system.");
			}
			moldService.save(payload.getModel(mold), payload);
			Mold moldFinal = moldService.findById(id);

			Mold newToCheck =  DataUtils.deepCopyJackSon(moldFinal,Mold.class);
			DataUtils.mapCreateAndUpdateInfo(oldToCheck, newToCheck);
			boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);

			if (!isIdentical) {
				versioningService.writeHistory(moldFinal);
			}
			moldService.saveDayWeekMonth(moldFinal);
			return ResponseEntity.ok(moldFinal);

		} catch (BizException e) {
			throw e;
		} catch (Exception e) {
			e.getStackTrace();
			return ResponseEntity.badRequest().body("ERROR " + e.getMessage());

		}
	}

	/**
	 * 장비 상태 변경
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/equipment-status")
	public ApiResponse equipmentStatus(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			Mold mold = moldService.findById(id);
			mold.setEquipmentStatus(payload.getEquipmentStatus());
			moldRepository.save(mold);
			//for detacted
			Counter counter = mold.getCounter();
			if (counter != null && mold.getEquipmentStatus() == EquipmentStatus.DETACHED) {

				EquipmentStatus oldStatus = counter.getEquipmentStatus();
				counter.setEquipmentStatus(mold.getEquipmentStatus());
				counterRepository.save(counter);
				moldService.createOrUpdateMoldDetachment(mold, oldStatus, mold.getEquipmentStatus());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	/**
	 * 변경된 위치 정보를 확인하였음.
	 * @param id
	 * @param payload
	 * @return
	 */
	@Deprecated
	@PutMapping("/{id}/location-status")
	public ApiResponse locationStatus(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			moldService.saveMoldLocation(payload);
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	/**
	 * 변경된 위치 정보를 확인하였음.
	 * @param id
	 * @param payload
	 * @return
	 */
	@Deprecated
	@PutMapping("/locations/{id}/confirm")
	public ApiResponse changeLocationStatus(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			moldService.saveMoldLocation(payload);
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PutMapping("/locations/confirm")
	@Deprecated
	public ApiResponse changeLocationStatus(@RequestBody List<MoldPayload> payloadList) {
		if (payloadList == null || payloadList.size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		moldService.saveMoldLocation(payloadList);
		return ApiResponse.success();
	}
	@PutMapping("/locations/changeStatus")
	public ApiResponse changeLocationsStatus(@RequestBody NoteAndChangeStatusPayLoad payLoad) {
		if (payLoad.getMoldPayloadList() == null || payLoad.getMoldPayloadList().size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		moldService.changeMoldLocationStatus(payLoad);
		return ApiResponse.success();
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteMold(@PathVariable("id") Long id) {
		moldService.deleteById(id);
		return new ApiResponse(true, "성공.");
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/corrective-filtered-by-ids")
	public ResponseEntity<?> getAllMoldCorrective_POST(MoldPayload payload, Pageable pageable, Model model,
												 @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldCorrective(payload, pageable, model);
	}

	/**
	 * 고장 목록 조회
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@GetMapping("/corrective")
	public ResponseEntity<Page<MoldCorrective>> getAllMoldCorrective(MoldPayload payload, Pageable pageable, Model model) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MAINTENANCE_ALERT));
		Page<MoldCorrective> pageContent = moldService.findCorrectiveAll(payload.getCorrectivePredicate(), pageable, payload.getAccumulatedShotFilter());
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/corrective/{id}")
	public ResponseEntity<MoldCorrective> getMoldCorrective(@PathVariable("id") Long id) {

		try {
			MoldCorrective mold = moldService.findMoldCorrectiveById(id);
			return new ResponseEntity<>(mold, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	/**
	 * 고장 등록
	 * @param corrective
	 * @return
	 */
	@PostMapping("/corrective")
	public ApiResponse insertCorrective(@RequestBody MoldCorrective corrective) {
		try {
			//moldService.saveCorrective(payload.getModel(), payload);

			if (corrective.getMoldId() == null) {
				return ApiResponse.error("Tooling ID not found");
			}

			moldService.saveCorrective(corrective);
		} catch (Exception e) {
			return ApiResponse.error("저장 중 오류 ");
		}
		return ApiResponse.success();
	}

	@PostMapping("/corrective/{id}")
	public ApiResponse updateCorrective(@PathVariable(value = "id", required = true) Long id, MultipartFormData formData, CorrectiveAction correctiveAction) {

		MoldCorrective corrective = null;
		try {
			corrective = objectMapper.readValue(formData.getPayload(), MoldCorrective.class);
			corrective.setFiles(formData.getFiles());
		} catch (Exception e) {
			return ApiResponse.error("Bad Request.");
		}

		if (!corrective.getId().equals(id)) {
			return ApiResponse.error("Bad Request.");
		}

		try {
			moldService.resisterRepair(corrective, correctiveAction);
		} catch (AppException e) {
			return ApiResponse.error(e.getMessage());

		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	/**
	 * 고장 처리 내용 수정
	 * @param id
	 * @param corrective
	 * @return
	 */
	@PutMapping("/corrective/{id}/cancel-repair")
	public ApiResponse cancelRepair(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldCorrective corrective) {

		if (!corrective.getId().equals(id)) {
			return ApiResponse.error("Bad Request.");
		}

		try {
			moldService.cancelRepair(corrective);
		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/misconfigure-filtered-by-ids")
	public ResponseEntity<?> getAllMoldMisconfigure_POST(MoldPayload payload, Pageable pageable, Model model,
													   @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllMoldMisconfigure(payload, pageable, model);
	}

	@DataLeakDetector(disabled = true)
	@GetMapping("/misconfigure")
	public ResponseEntity<Page<MoldMisconfigure>> getAllMoldMisconfigure(MoldPayload payload, Pageable pageable, Model model) {
		Page<MoldMisconfigure> pageContent = moldService.findMisconfigureAll(payload.getMisconfigurePredicate(), pageable, payload.getAccumulatedShotFilter());
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@PutMapping("/misconfigure/{id}/confirm")
	public ApiResponse confirmMisconfigure(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			moldService.saveMoldMisconfigure(payload);
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@GetMapping("/test-alerts")
	public ApiResponse getTestAlerts() {
		new ScheduledTask().procAlerts();
		return ApiResponse.success();
	}

	@GetMapping("/standardize-data")
	public ResponseEntity standardizeData() {
		return ResponseEntity.ok(moldService.standardizeData());
	}

	@GetMapping("/reports/capacity-utilization")
	public ResponseEntity<Page<MoldReportData>> reportCapacityUtilization(DashboardFilterPayload payload, Pageable pageable, Model model) {
		Page pageContent = moldService.findReportOeeOrCapacityUtilization(payload, pageable, ReportType.CAPACITY_UTILIZATION);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/reports/oee")
	public ResponseEntity<Page<MoldReportData>> reportOee(DashboardFilterPayload payload, Pageable pageable, Model model) {
		Page pageContent = moldService.findReportOeeOrCapacityUtilization(payload, pageable, ReportType.OEE);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping(value = "/exportPdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> printPdf(MoldPayload payload) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=tooling-report");
		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(new ByteArrayInputStream(moldService.exportPDFDataMolds(payload))));
	}

	@GetMapping("/exportExcel")
	public void printExcel(HttpServletResponse response, @RequestParam(required = false) List<Long> ids, @RequestParam(required = false) Integer timezoneOffsetClient,
			@RequestParam(name = "fileName", defaultValue = "tooling-detail", required = false) String fileName, Pageable pageable) {
		try {
			OutputStream outputStream = response.getOutputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=tooling-report-" + new Date().getTime() + ".xlsx");
			outputStream.write(moldService.exportExcelDataMolds(ids, timezoneOffsetClient, pageable).toByteArray());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/exportExcelDynamic")
	public ResponseEntity<?> exportExcelDynamic(HttpServletResponse response, ExportPayload payload

			, Pageable pageable) {
//		ByteArrayOutputStream byteArrayOutputStream=null;
		try {
			//valid
			ResponseEntity resValid = validExportExcelDynamic(payload);
			if (resValid != null)
				return resValid;

			/*
						if(Arrays.asList(RangeType.CUSTOM_RANGE).contains(payload.getRangeType())){
							if(StringUtils.isEmpty(payload.getFromDate())){
								return ResponseEntity.badRequest().body("From Date is required.");
							}
							if(StringUtils.isEmpty(payload.getToDate())){
								return ResponseEntity.badRequest().body("To Date is required.");
							}
						}else {
							if(StringUtils.isEmpty(payload.getTime())){
								return ResponseEntity.badRequest().body("Time is required.");
							}
						}
						if(payload.getDataTypes()==null || payload.getDataTypes().isEmpty()){
							return ResponseEntity.badRequest().body("Data Type is required.");

						}
						if(DateViewType.HOUR.equals(payload.getFrequency()) && payload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME) && payload.getDataTypes().size()==1){
							return ResponseEntity.badRequest().body("Uptime type not exist Hourly frequency data.");
						}
						if(!DateViewType.HOUR.equals(payload.getFrequency()) && payload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE) && payload.getDataTypes().size()==1){
							return ResponseEntity.badRequest().body("Temperature type exists only Hourly frequency data.");
						}
			*/
			log.info("Start exportExcelDynamic");
			/*
						OutputStream outputStream = response.getOutputStream();
						response.setContentType("application/octet-stream");
						response.setHeader("Content-Disposition", "attachment; filename=" + dynamicExportService.getExportDynamicFileName(payload));
						byteArrayOutputStream = dynamicExportService.exportExcelDynamic(payload, pageable);
						outputStream.write(byteArrayOutputStream.toByteArray());
						outputStream.close();
			*/
			dynamicExportService.exportExcelDynamic(response, payload, pageable);
			log.info("End exportExcelDynamic");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		/*
				finally {
					try {
						if (byteArrayOutputStream != null) byteArrayOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		*/
		return ResponseEntity.ok("Success.");
	}

	private ResponseEntity<?> validExportExcelDynamic(ExportPayload payload) {
		if (Arrays.asList(RangeType.CUSTOM_RANGE).contains(payload.getRangeType())) {
			if (StringUtils.isEmpty(payload.getFromDate())) {
				return ResponseEntity.badRequest().body("From Date is required.");
			}
			if (StringUtils.isEmpty(payload.getToDate())) {
				return ResponseEntity.badRequest().body("To Date is required.");
			}
		} else {
			if (StringUtils.isEmpty(payload.getTime())) {
				return ResponseEntity.badRequest().body("Time is required.");
			}
		}
		if (payload.getDataTypes() == null || payload.getDataTypes().isEmpty()) {
			return ResponseEntity.badRequest().body("Data Type is required.");

		}
		if (DateViewType.HOUR.equals(payload.getFrequency()) && payload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME) && payload.getDataTypes().size() == 1) {
			return ResponseEntity.badRequest().body("Uptime type not exist Hourly frequency data.");
		}
		if (!DateViewType.HOUR.equals(payload.getFrequency()) && payload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE) && payload.getDataTypes().size() == 1) {
			return ResponseEntity.badRequest().body("Temperature type exists only Hourly frequency data.");
		}

		return null;
	}

	@GetMapping("/exportExcelDynamicOneToolingPerFile")
	public ResponseEntity<?> exportExcelDynamicOneToolingPerFile(HttpServletResponse response, ExportPayload payload

			, Pageable pageable) {
		try {
			//valid
			ResponseEntity resValid = validExportExcelDynamic(payload);
			if (resValid != null)
				return resValid;
			log.info("Start exportExcelDynamicOneToolingPerFile");

			payload.setToolingForm(true);
			dynamicExportService.exportExcelDynamicOneToolingPerFile(response, payload, pageable);
			log.info("End exportExcelDynamicOneToolingPerFile");
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok("Success.");
	}

	@GetMapping("/exportExcelDynamicNew")
	public ResponseEntity<?> exportExcelDynamicNew(HttpServletResponse response, ExportPayload payload

			, Pageable pageable) {
		try {
			if (DateViewType.EVERY_SHOT == payload.getFrequency()) {
				dynamicExportService.exportDynamicEveryShot(response, payload, pageable);
				return ResponseEntity.ok("Success.");
			} else {
				if (dynamicExportService.isExportOneToolingPerFile())
					return exportExcelDynamicOneToolingPerFile(response, payload, pageable);
				return exportExcelDynamic(response, payload, pageable);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/exportExcelImportTemplate")
	public void exportExcelImportTemplate(HttpServletResponse response) {
		try {
			OutputStream outputStream = response.getOutputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=tooling-import-template.xlsx");
			outputStream.write(moldService.exportExcelImportTemplate().toByteArray());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GetMapping(value = "/exportPdfDetailMolds")
	public void printDetailMoldPdf(HttpServletResponse response, @RequestParam(required = false) List<Long> ids,
			@RequestParam(name = "fileName", defaultValue = "tooling-detail", required = false) String fileName) {
		try {
			OutputStream outputStream = response.getOutputStream();
			response.setHeader("Content-Disposition", "inline; filename=tooling-detail-report");
			response.setContentType("application/pdf");
			outputStream.write(moldService.exportPdfDetailTooling(ids, fileName));
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/refurbishment-filtered-by-ids")
	public ResponseEntity<?> getAllRefurbishment_POST(MoldPayload payload, Pageable pageable, Model model,
														 @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllRefurbishment(payload, pageable, model);
	}

	/**
	 *
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("/refurbishment")
	public ResponseEntity<Page<MoldRefurbishment>> getAllRefurbishment(MoldPayload payload, Pageable pageable, Model model) {
		Page<MoldRefurbishment> pageContent = moldService.findMoldRefurbishmentAll(payload.getRefurbishmentPredicate(), pageable, payload.getAccumulatedShotFilter());
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/refurbishment/{id}")
	public ResponseEntity<MoldRefurbishment> getMoldRefurbishment(@PathVariable("id") Long id) {

		try {
			MoldRefurbishment mold = moldService.findMoldRefurbishmentById(id);
			return new ResponseEntity<>(mold, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	/*
		@PostMapping("/refurbishment")
		public ApiResponse insertCorrective(@RequestBody MoldRefurbishment refurbishment) {
			try {
				//moldService.saveCorrective(payload.getModel(), payload);

				if (refurbishment.getMoldId() == null) {
					return ApiResponse.error("Tooling ID not found");
				}


				moldService.saveRefurbishment(refurbishment);
			} catch (Exception e) {
				return ApiResponse.error("저장 중 오류 ");
			}
			return ApiResponse.success();
		}
	*/

	@PostMapping("/refurbishment/{id}")
	public ApiResponse updateRefurbishment(@PathVariable(value = "id", required = true) Long id, MultipartFormData formData, CorrectiveAction refurbishmentAction) {

		MoldRefurbishment refurbishment = null;
		try {
			refurbishment = objectMapper.readValue(formData.getPayload(), MoldRefurbishment.class);
			refurbishment.setFiles(formData.getFiles());
		} catch (Exception e) {
			return ApiResponse.error("Bad Request.");
		}

		if (!refurbishment.getId().equals(id)) {
			return ApiResponse.error("Bad Request.");
		}

		try {
			moldService.resisterRepairRefurbishment(refurbishment, refurbishmentAction);
		} catch (AppException e) {
			return ApiResponse.error(e.getMessage());

		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PostMapping("/refurbishment/{id}/discard")
	public ApiResponse discardRefurbishment(@PathVariable(value = "id", required = true) Long id) {

		MoldRefurbishment refurbishment = moldService.findMoldRefurbishmentById(id);
		if (refurbishment == null) {
			return ApiResponse.error("Refurbishment not exist.");
		}
		try {
			moldService.discardRefurbishment(refurbishment);
		} catch (AppException e) {
			e.printStackTrace();
			return ApiResponse.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			return ApiResponse.error("Bad Request.");
		}
		return ApiResponse.success();
	}

	@GetMapping("/wut")
	public ResponseEntity<?> calculateWUT(@RequestParam(required = false) String moldCode, @RequestParam(required = false) String startTime,
			@RequestParam(required = false) String endTime) throws JSONException {
		Mold mold = moldRepository.findByEquipmentCode(moldCode);
		if (mold == null) {
			return ResponseEntity.badRequest().body(ApiResponse.error("moldCode not exist!"));
		}
		return ResponseEntity.ok(moldService.calculateWUT(mold.getId(), startTime, endTime));
//		return ResponseEntity.ok(moldService.testConnection());
	}

	/*

	    @PutMapping("/refurbishment/{id}/cancel-repair")
	    public ApiResponse cancelRepair(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldRefurbishment refurbishment) {

	        if (!refurbishment.getId().equals(id)) {
	            return ApiResponse.error("Bad Request.");
	        }

	        try {
	            moldService.cancelRepair(refurbishment);
	        } catch (Exception e) {
	            log.error(e.getMessage());
	            if (log.isDebugEnabled()) {
	                e.printStackTrace();
	            }
	            return ApiResponse.error("상태 변경 중 오류 발생.");
	        }
	        return ApiResponse.success();
	    }
	*/
//	@GetMapping("/job-calculation-due-date-of-maintenance")
//	public ApiResponse jobCalculationDueDateOfMaintenance() {
//
//		try {
//			moldService.procMaintenanceDueDateAll();
//			return ApiResponse.success();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ApiResponse.error();
//		}
//	}

	@PostMapping("/init-daily-max-capacity")
	public ResponseEntity<?> initDailyMaxCapacity() {
		return ResponseEntity.ok(moldService.initDailyMaxCapacity());
	}

	@GetMapping("/proc-wut-all")
	public ResponseEntity<?> procWutAll() throws JSONException {
		moldService.procWutAll();
		return ResponseEntity.ok(Const.SUCCESS);

	}

	@GetMapping("/wut-statistics")
	public ResponseEntity<?> calculateWUTStatistics(@RequestParam(required = false) String moldCode, @RequestParam(required = false) String lst) throws JSONException {
		Mold mold = moldRepository.findByEquipmentCode(moldCode);
		if (mold == null) {
			return ResponseEntity.badRequest().body(ApiResponse.error("moldCode not exist!"));
		}
		moldService.checkWUTForStatisticsNew(mold.getId(), lst);
		return ResponseEntity.ok("OK");
//		return ResponseEntity.ok(moldService.testConnection());
	}

	@DataLeakDetector(disabled = true)
	@PostMapping("/detachment-filtered-by-ids")
	public ResponseEntity<?> getAllDetachment_POST(MoldPayload payload, Pageable pageable, Model model,
													  @RequestBody ListIdPayload ids) {
		payload.setFilteredIds(ids.getIds());
		return getAllDetachment(payload, pageable, model);
	}

// Detachment
	/**
	 *
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("/detachment")
	public ResponseEntity<Page<MoldDetachment>> getAllDetachment(MoldPayload payload, Pageable pageable, Model model) {
		Page<MoldDetachment> pageContent = moldService.findMoldDetachmentAll(payload.getDetachmentPredicate(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/detachment/{id}")
	public ResponseEntity<MoldDetachment> getMoldDetachment(@PathVariable("id") Long id) {

		try {
			MoldDetachment mold = moldService.findMoldDetachmentById(id);
			return new ResponseEntity<>(mold, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null);
		}
	}

	@PostMapping("/detachment/{id}")
	public ApiResponse updateDetachment(@PathVariable(value = "id", required = true) Long id, MultipartFormData formData, CorrectiveAction detachmentAction) {

		MoldDetachment detachment = null;
		try {
			detachment = objectMapper.readValue(formData.getPayload(), MoldDetachment.class);
//			detachment.setFiles(formData.getFiles());
		} catch (Exception e) {
			return ApiResponse.error("Bad Request.");
		}

		if (!detachment.getId().equals(id)) {
			return ApiResponse.error("Bad Request.");
		}

		try {
//			moldService.resisterRepairDetachment(detachment, detachmentAction);
		} catch (AppException e) {
			return ApiResponse.error(e.getMessage());

		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PutMapping("/detachment/{id}/confirm")
	public ApiResponse confirmDetachment(@PathVariable(value = "id", required = true) Long id, @RequestBody MoldPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("Id empty!");
		}

		try {
			moldService.saveMoldDetachment(payload);
//			logDetachmentService.confirm(id, payload.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error("Fail!");
		}
		return ApiResponse.success();
	}

	@PutMapping("/detachment/confirm")
	public ApiResponse confirmDetachment(@RequestBody List<MoldPayload> payloadList) {
		if (payloadList == null || payloadList.size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		moldService.saveMoldDetachment(payloadList);
//		List<Long> ids = payloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
//		String message = payloadList.get(0).getMessage();
//		logDetachmentService.confirm(ids, message);
		return ApiResponse.success();
	}

	@GetMapping("/init-operated-start")
	public ResponseEntity<?> initValueOperatedStart() throws JSONException {
		moldService.initValueOperatedStart();
		return ResponseEntity.ok("OK");
	}

	@PostMapping("/resolve-wrong-mold-maintenance")
	public ResponseEntity<?> resolveWrongMoldMaintenance() {
		return ResponseEntity.ok(moldService.resolveWrongMoldMaintenance());
	}

	@GetMapping("update-mold-engineer")
	public ResponseEntity updateMoldEngineer() {
		try {
			List<Mold> all = moldRepository.findAll();
			for (Mold m : all) {
				m.setEngineersInCharge(m.getEngineersInCharge());
			}
			moldRepository.saveAll(all);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Fail!");
		}
		return ResponseEntity.ok("Success!");
	}

	//softly detete
	@DeleteMapping("/delete/{id}")
	public ApiResponse deleteSystemNote(@PathVariable("id") Long id) {
		Mold mold = moldRepository.findById(id).orElse(null);
		if (mold == null) {
			return new ApiResponse(false, "Fail!");
		}
//		if(mold.getCreator()!=null && SecurityUtils.getUserId()!=null && !SecurityUtils.getUserId().equals(mold.getCreator().getId())){
//			return new ApiResponse(false, "User can only remove owner message!");
//		}
		moldService.deleteMold(mold);
		return new ApiResponse(true, "OK!");
	}

	@PostMapping("/restore/{id}")
	public ApiResponse restoreSystemNote(@PathVariable("id") Long id) {
		Mold mold = moldRepository.findById(id).orElse(null);
		if (mold == null) {
			return new ApiResponse(false, "Fail!");
		}
		moldService.restoreMold(mold);

		return new ApiResponse(true, "OK!");
	}

	@PostMapping("/delete-in-batch")
	public ApiResponse deleteMoldInBatch(@RequestBody List<Long> ids) {
		moldService.deleteMoldInBatch(ids);
		return new ApiResponse(true, "OK!");
	}

	@GetMapping("/list-to-match-old")
	public ResponseEntity<List<MiniComponentData>> getListMoldIdsToMatchWithMachine() {
		List<MiniComponentData> list = moldService.getListMoldIdsToMatch();
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping("/list-to-match")
	public ResponseEntity<Page<MachineMoldData>> searchMoldsLiteData(MoldPayload payload, Pageable pageable, Model model) {
		payload.setServerName(serverName);
		Page<MachineMoldData> pageContent = moldService.getListMoldToMatch(payload.getPredicateForMatching(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/check-existed")
	public ResponseEntity<MachineMoldData> checkExistedMold(@RequestParam("code") String code) {
		Mold mold = moldRepository.findByEquipmentCode(code);
		if (mold != null) {
			MachineMoldData data = new MachineMoldData();
			data.setId(mold.getId());
			data.setName(mold.getEquipmentCode());
			if (mold.getMachine() != null) {
				data.setMatchedId(mold.getMachineId());
				data.setMatchedName(mold.getMachine().getMachineCode());
			}
			return new ResponseEntity<>(data, HttpStatus.OK);
		} else {
			throw new RuntimeException(CommonMessage.OBJECT_NOT_FOUND);
		}
	}

	@PostMapping("/switch-part")
	public ApiResponse switchPart(@RequestBody SwitchMoldPartPayload payload) {
		return moldService.switchMoldPart(payload);
	}

	@GetMapping("/migrate-total-cavities")
	public ApiResponse migrateTotalCavities() {
		return moldService.migrateDataForMoldTotalCavities();
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return moldService.changeStatusInBatch(dto);
	}

	@GetMapping("/get-number-inactive-tooling")
	public ApiResponse getNumberInactiveTooling() {
		return moldService.getNumberInactiveTooling();
	}

	@GetMapping("/delete-duplicated-tooling-by-equipment-code")
	public ApiResponse deleteDuplicatedToolingByEquipmentCode(@RequestParam(value = "equipmentCode") String equipmentCode) {
		return moldService.deleteDuplicatedToolingByEquipmentCode(equipmentCode);
	}

	@GetMapping("/first-shot-year")
	public ApiResponse getFirstShotYear() {
		return moldService.getFirstShotYear();
	}

	@GetMapping("resolve-data-matching")
	public ResponseEntity<String> resolveDataMatching(@RequestParam String ci) {
		return ResponseEntity.ok(moldService.resolveDataMatching(ci));
	}

	@PostMapping("/restore-update-last-shot")
	public ApiResponse restoreUpdateLastShot() {
		try {
			return moldService.restoreUpdateLastShot();
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, e.getMessage());
		}
	}

	@GetMapping("/data-count")
	public ApiResponse getDataCount() {
		return moldService.getDataCount();
	}

	@GetMapping("/proc-wact-all")
	public ResponseEntity<?> procWactAll() throws JSONException {
		moldService.procWactAllAsync();
		return ResponseEntity.ok(Const.SUCCESS);

	}

	@Deprecated
	@PostMapping("/auto-confirm-relocation-alerts")
	public ApiResponse autoConfirmRelocationAlerts() {
		return moldService.autoConfirmRelocationAlerts();
	}

	@GetMapping("/update-mold")
	public ApiResponse updateMoldAndCorrective() {
		return moldService.updateMold();
	}

	@GetMapping("/fix-last-shot-at")
	public ApiResponse fixLastShotAt() {
		return moldService.fixLastShotAtData();
	}

	@GetMapping("/accumulated-shot")
	public ApiResponse getAccumulatedShot(@RequestParam Long id,
										  @RequestParam Long failureTime) {
		return moldService.getAccumulatedShotByIdAndTime(id, failureTime);
	}

	@GetMapping(value = "/migration-previous-location")
	public void migrationPreviousLocation() {
		moldService.migrationPreviousLocation();
	}

	@GetMapping(value = "/{moldId}/mold-pm-plan")
	public ApiResponse getMoldPmPlan(@PathVariable(value = "moldId") Long moldId) {
		return ApiResponse.success(CommonMessage.OK, moldService.getMoldPmPlanByMoldId(moldId));
	}

}
