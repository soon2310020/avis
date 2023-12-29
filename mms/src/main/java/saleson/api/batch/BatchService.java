package saleson.api.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import saleson.api.batch.payload.BatchPayload;
import saleson.api.category.CategoryService;
import saleson.api.category.payload.CategoryParam;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.counter.CounterService;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.location.LocationService;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.MachineService;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertService;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartService;
import saleson.api.part.payload.PartPayload;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.terminal.TerminalService;
import saleson.api.terminal.payload.TerminalAlertPayload;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.api.workOrder.WorkOrderService;
import saleson.api.workOrder.payload.SearchPayload;
import saleson.api.workOrder.payload.WorkOrderPayload;
import saleson.common.FrameType;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.*;
import saleson.common.payload.ApiResponse;
import saleson.model.QPart;
import saleson.model.TabTable;
import saleson.model.TabTableData;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BatchService {
	@Autowired
	private ColumnTableConfigService columnTableConfigService;
	@Autowired
	private MoldService moldService;
	@Autowired
	private TerminalService terminalService;
	@Autowired
	private MachineDowntimeAlertService machineDowntimeAlertService;

	@Autowired
	private PartService partService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private LocationService locationService;
	@Autowired
	private CounterService counterService;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private WorkOrderService workOrderService;
	@Autowired
	private UserService userService;
	@Autowired
	private MachineService machineService;
	@Autowired
	private TabTableRepository tabTableRepository;
	@Autowired
	private TabTableDataRepository tabTableDataRepository;

	private final String PAGE_TYPE = "frameType";

	public ApiResponse getIds(BatchPayload payload) {
		MoldPayload moldPayload = payload.toMoldPayload();
		TerminalAlertPayload terminalAlertPayload = payload.toTerminalAlertPayload();
		SearchMachineDowntimePayload searchMachineDowntimePayload = payload.toMachineDowntimePayload();
		switch (payload.getPageType()) {
			case RELOCATION_ALERT:
				moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.RELOCATION_ALERT));
				return moldService.getMoldLocationIds(moldPayload.getLocationPredicate());
			case TOOLING_DISCONNECTION_ALERT:
				return moldService.getMoldDisconnectIds(moldPayload.getDisconnectPredicate());
			case TERMINAL_DISCONNECTION_ALERT:
				return terminalService.getTerminalDisconnectIds(terminalAlertPayload.getDisconnectPredicate());
			case CYCLE_TIME_ALERT:
				moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.CYCLE_TIME_ALERT));
				return moldService.getMoldCycleTimeIds(moldPayload.getCycleTimePredicate());
			case MAINTENANCE_ALERT:
				moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MAINTENANCE_ALERT));
				return moldService.getMoldMaintenanceIds(moldPayload.getMaintenancePredicate());
			case CORRECTIVE_MAINTENANCE_ALERT:
				moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MAINTENANCE_ALERT));
				return moldService.getMoldCorrectiveIds(moldPayload.getCorrectivePredicate());
			case UPTIME_ALERT:
				return moldService.getMoldEfficiencyIds(moldPayload.getEfficiencyPredicate());
			case RESET_ALERT:
				return moldService.getMoldMisconfiguredIds(moldPayload.getMisconfigurePredicate());
			case DATA_APPROVAL_ALERT:
				return moldService.getMoldDataSubmissionIds(moldPayload.getDataSubmissionPredicate());
			case REFURBISHMENT_ALERT:
				return moldService.getMoldRefurbishmentIds(moldPayload.getRefurbishmentPredicate());
			case DETACHMENT_ALERT:
				moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.DETACHMENT_ALERT));
				return moldService.getMoldDetachmentIds(moldPayload.getDetachmentPredicate());
			case DOWNTIME_ALERT:
				moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.DOWNTIME_ALERT));
				return moldService.getMoldDowntimeIds(moldPayload.getDowntimePredicate());
			case MACHINE_DOWNTIME_ALERT:
				return machineDowntimeAlertService.getMachineDowntimeAlertIds(searchMachineDowntimePayload, PageRequest.of(0, 999999));
		}
		return ApiResponse.success();
	}

	public <T> ApiResponse getAllIdsForEachFrame(Map<String, Object> request) throws InvocationTargetException, IllegalAccessException {
		String pageType = (String) request.get(PAGE_TYPE);
		if (Objects.nonNull(pageType) && !pageType.isEmpty()) {
			switch (PageType.valueOf(pageType.toUpperCase())) {
				case PART_SETTING:
					PartPayload partPayload = loadPayLoadForRequest(request, PartPayload.class);
					return ApiResponse.success(CommonMessage.OK, partService.findAllIdWithStatisticsPart(partPayload));
				case TOOLING_SETTING:
					MoldPayload moldPayload = loadPayLoadForRequest(request, MoldPayload.class);
					return ApiResponse.success(CommonMessage.OK, moldService.findAllIds(moldPayload));
				case LOCATION:
					LocationPayload locationPayload = loadPayLoadForRequest(request, LocationPayload.class);
					return ApiResponse.success(CommonMessage.OK, locationService.findAllIds(locationPayload));
				case COUNTER_SETTING:
					CounterPayload counterPayload = loadPayLoadForRequest(request, CounterPayload.class);
					return ApiResponse.success(CommonMessage.OK, counterService.findAllIds(counterPayload));
				case CATEGORY_SETTING:
					CategoryParam categoryPayload = loadPayLoadForRequest(request, CategoryParam.class);
					return ApiResponse.success(CommonMessage.OK, categoryService.findAllIds(categoryPayload));
				case COMPANY_SETTING:
					CompanyPayload companyPayload = loadPayLoadForRequest(request, CompanyPayload.class);
					return ApiResponse.success(CommonMessage.OK, companyService.findAllIds(companyPayload));
				case TERMINAL_SETTING:
					TerminalPayload terminalPayload = loadPayLoadForRequest(request, TerminalPayload.class);
					return ApiResponse.success(CommonMessage.OK, terminalService.findAllIds(terminalPayload));
				case WORK_ORDER:
					SearchPayload workOrderPayload = loadPayLoadForRequest(request, SearchPayload.class);
					return ApiResponse.success(CommonMessage.OK, workOrderService.findAllIds(workOrderPayload));
				case USER:
					UserParam userPayload = loadPayLoadForRequest(request, UserParam.class);
					return ApiResponse.success(CommonMessage.OK, userService.getAllIds(userPayload));
				case MACHINE_SETTING:
					MachinePayload machinePayload = loadPayLoadForRequest(request, MachinePayload.class);
					return ApiResponse.success(CommonMessage.OK, machineService.findAllIds(machinePayload));


			}

		}
		return ApiResponse.error("Dont have frameType or frameType is not correct.");

	}

	<T> T loadPayLoadForRequest(Map<String, Object> request, Class<T> payloadClazz) {
		ObjectMapper objectMapper = new ObjectMapper();
		request.entrySet().forEach(m -> {
			List<String> values = null;
			if (m.getValue() instanceof String) {
				String value = (String) m.getValue();
				if (value.contains(",") && !m.getKey().equals("where")) {
					values = Arrays.asList(value.trim().split(","));

					if (m.getKey().equals("equipmentStatus") || m.getKey().equals("equipmentStatuses")) {
						m.setValue(values.stream().map(EquipmentStatus::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("operatingStatus")||m.getKey().equals("operatingStatuses")){
						m.setValue(values.stream().map(OperatingStatus::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("tabType")){
						m.setValue(values.stream().map(TabType::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("inactiveLevel")) {
						m.setValue(values.stream().map(DashboardSettingLevel::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("counterStatusList")){
						m.setValue(values.stream().map(CounterStatus::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("moldStatusList")){
						m.setValue(values.stream().map(MoldStatus::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("typeList")||m.getKey().equals("orderType")){
						m.setValue(values.stream().map(WorkOrderType::valueOf).collect(Collectors.toList()));
					}
					else if (m.getKey().equals("priorityTypeList")){
						m.setValue(values.stream().map(PriorityType::valueOf).collect(Collectors.toList()));
					}
					else{
						m.setValue(values);
					}
				}else {
					if (payloadClazz.getName().equalsIgnoreCase( SearchPayload.class.getName()) && m.getKey().equals("orderType")&&Objects.nonNull(values)){
						m.setValue(values.stream().map(WorkOrderType::valueOf).collect(Collectors.toList()));
					}
						m.setValue(convertStringToEnum(m.getValue(),m.getKey()));
				}
			}
		});

		JSONObject jsonObjectRequest = new JSONObject(request);

			return objectMapper.convertValue(jsonObjectRequest, payloadClazz);

	}
	public Object convertStringToEnum(Object needed,String enumName){
		try{
		switch (enumName){
			case "equipmentStatuses":
			case "equipmentStatus":
				return Enum.valueOf(EquipmentStatus.class,(String) needed);
			case "operatingStatuses":
			case "operatingStatus":
				return Enum.valueOf(OperatingStatus.class,(String) needed);
			case "tabType":
				return Enum.valueOf(TabType.class,(String) needed);
			case "inactiveLevel":
				return Enum.valueOf(DashboardSettingLevel.class,(String) needed);
			case "counterStatusList":
				return Enum.valueOf(CounterStatus.class,(String) needed);
			case "moldStatusList":
				return Enum.valueOf(MoldStatus.class,(String) needed);
			case "periodType":
				return Enum.valueOf(PeriodType.class,(String) needed);
			case "orderType" :
				return Enum.valueOf(WorkOrderType.class,(String) needed);
			case "pageType":
				return Enum.valueOf(PageType.class,(String) needed);

		}}
		catch (IllegalArgumentException ex){
			return null;
		}
		if (needed.equals(""))
			return null;
		return needed;

	}
}
