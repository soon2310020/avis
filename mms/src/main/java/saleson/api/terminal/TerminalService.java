package saleson.api.terminal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import saleson.api.company.CompanyRepository;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.location.LocationRepository;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.terminal.payload.TerminalAlertPayload;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.versioning.service.VersioningService;
import saleson.api.workOrder.WorkOrderRepository;
import saleson.api.workOrder.WorkOrderService;
import saleson.common.constant.CommonMessage;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.*;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.SecurityUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.data.CounterToolingCode;
import saleson.model.data.TerminalData;
import saleson.model.logs.LogDisconnection;
import saleson.model.logs.QLogDisconnection;
import saleson.service.transfer.LogDisconnectionService;

@Service
public class TerminalService {

	@Autowired
	TerminalRepository terminalRepository;

	@Autowired
	TerminalDisconnectRepository terminalDisconnectRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	FileStorageService fileStorageService;

	@Autowired
	LogDisconnectionService logDisconnectionService;

	@Autowired
	VersioningService versioningService;

	@Autowired
	private WorkOrderRepository workOrderRepository;

	@Autowired
	private WorkOrderService workOrderService;
	@Autowired
	private TabTableDataRepository tabTableDataRepository;

	@Autowired
	private TabTableRepository tabTableRepository;
	@Autowired
	private ColumnTableConfigService columnTableConfigService;


	public Page<Terminal> findAll(Predicate predicate, Pageable pageable) {
		return terminalRepository.findAll(predicate, pageable);
	}
	public List<Long> findAllIds (TerminalPayload payload){
		changeTabPayload(payload);
		BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)
				.select(Projections.constructor(Long.class, Q.terminal.id))
				.from(Q.terminal);
		query.where(builder);
		QueryResults<Long> results = query.fetchResults();
		return results.getResults();

	}

	public Terminal getTerminal(Long id) {
		Terminal terminal=findById(id);
		if(terminal!=null) loadWorkOrderDetail(terminal);
		return terminal;
	}
	public Terminal findById(Long id) {
		Optional<Terminal> category =  terminalRepository.findById(id);
		if (category.isPresent()) {
			return category.get();
		} else {
			return null;
		}

	}

	public Page<TerminalData> findTerminalData(Predicate predicate, Pageable pageable){
        String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        if(SpecialSortProperty.terminalSortProperties.contains(properties[0])){
/*
        	List<CounterToolingCode> mappedAllByLocation = terminalRepository.findTiCiSameLocation(predicate, null);
        	List<String> allTerminalCodes = mappedAllByLocation.stream().map(x -> x.getTerminalCode()).distinct().collect(Collectors.toList());
        	List<CounterToolingCode> ticiAll = terminalRepository.findLastStatisticsTerminalCounter(allTerminalCodes);
        	Map<String, Long> mappedTerminalNumberCounter = new HashMap<>();
        	allTerminalCodes.forEach(terminalCode -> {
        		if(!mappedTerminalNumberCounter.containsKey(terminalCode)){
        			Long count = ticiAll.stream()
							.filter(x -> x.getTerminalCode().equalsIgnoreCase(terminalCode) && mappedAllByLocation.contains(x)).count();
        			mappedTerminalNumberCounter.put(terminalCode, count);
				}
			});
        	Map<String, Long> sortedMapTerminalNumberCounter = ComparatorUtils.sortByValue(mappedTerminalNumberCounter, pageable.getPageSize(), pageable.getPageNumber(), directions[0]);
			List<String> terminalCodes = new ArrayList<>(sortedMapTerminalNumberCounter.keySet());
			List<Terminal> terminalResults = terminalRepository.findByTerminalCodeInSorted(terminalCodes);
			List<TerminalData> result = new ArrayList<>();
			terminalResults.forEach(terminal -> {
				TerminalData terminalData = TerminalData.builder()
						.terminal(terminal)
						.build();
				terminalData.setNumberOfCounter(sortedMapTerminalNumberCounter.get(terminal.getEquipmentCode()));
				result.add(terminalData);
			});
*/
			Page<TerminalData> result = terminalRepository.findTerminalDataSortByNumberCounter(predicate,pageable);
			result.forEach(terminalData -> loadWorkOrderDetail(terminalData.getTerminal()));
			loadWorkOrderDetail(result.stream().map(TerminalData::getTerminal).collect(Collectors.toList()));
//			return new PageImpl<>(result, pageable, allTerminalCodes.size());
			return result;
        }
		Page<Terminal> terminals;
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<Terminal> list = terminalRepository.findAllOrderByOperatingStatus(predicate, pageable);
			terminals = new PageImpl<>(list, pageable, terminalRepository.count(predicate));
		} else if (SpecialSortProperty.terminalConnectionStatus.equals(properties[0])) {
			List<Terminal> list = terminalRepository.findAllOrderByConnectionStatus(predicate, pageable);
			terminals = new PageImpl<>(list, pageable, terminalRepository.count(predicate));
		} else {
			terminals = terminalRepository.findAll(predicate, pageable);
		}
		List<String> terminalCodes = terminals.getContent().stream().map(x -> x.getEquipmentCode()).collect(Collectors.toList());
/*

		List<CounterToolingCode> mappedByLocation = terminalRepository.findTiCiSameLocation(predicate, terminalCodes);

		List<CounterToolingCode> tici = terminalRepository.findLastStatisticsTerminalCounter(terminalCodes);
*/

		Page<TerminalData> resultTerminalData = terminalRepository.findTerminalDataSortByNumberCounter(
			new BooleanBuilder().and(QTerminal.terminal.equipmentCode.in(terminalCodes)), PageRequest.of(0,10000));

		List<TerminalData> terminalDataList = new ArrayList<>();
		terminals.getContent().forEach(terminal -> {
/*
			TerminalData terminalData = TerminalData.builder()
					.terminal(terminal)
					.build();
			Long counter = tici.stream()
					.filter(x ->
							x.getTerminalCode().equalsIgnoreCase(terminal.getEquipmentCode())
							&& mappedByLocation.contains(x))
					.count();
			terminalData.setNumberOfCounter(counter);
*/
			TerminalData terminalData = resultTerminalData.stream()
				.filter(t -> t.getTerminal() != null && t.getTerminal().getId().equals(terminal.getId())).findFirst().orElse(null);
			if (terminalData != null)
				terminalDataList.add(terminalData);
		});

		loadWorkOrderDetail(terminalDataList.stream().map(TerminalData::getTerminal).collect(Collectors.toList()));
		return new PageImpl<>(terminalDataList, pageable, terminalRepository.count(predicate));
	}

	public List<CounterToolingCode> getCounterToolingCodesById(Long id) {
		return terminalRepository.getCounterToolingCodesById(id);
	}

	@Transactional
	public void save(Terminal terminal) {
		if (terminal.getLocationId() != null) {

			Optional<Location> locationOptional = locationRepository.findById(terminal.getLocationId());
			if (locationOptional.isPresent()) {
				terminal.setLocation(locationOptional.get());
				terminal.setCompanyId(locationOptional.get().getCompany().getId());
			}
		}
		boolean newObj = terminal.getId() ==null;
		Terminal terminalNew = terminalRepository.save(terminal);
		if(newObj) versioningService.writeHistory(terminalNew);
	}

	@Transactional
	public void deleteById(Long id) {
		terminalRepository.deleteById(id);
	}

	public boolean existsByEquipmentCode(String equipmentCode) {
		return terminalRepository.existsByEquipmentCode(equipmentCode);
	}

	public void save(Terminal terminal, MultipartFile[] files) {

		save(terminal);

		if (files != null) {
			fileStorageService.save(new FileInfo(StorageType.TERMINAL, terminal.getId(), files));
		}
	}

	public Optional<Terminal> findByEquipmentCode(String equipmentCode) {
		return terminalRepository.findByEquipmentCode(equipmentCode);
	}

	public Page<TerminalDisconnect> findDisconnectAll(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<TerminalDisconnect> page;
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<TerminalDisconnect> list = terminalDisconnectRepository.findAllOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, terminalDisconnectRepository.count(predicate));
		}else if (properties[0].contains(SpecialSortProperty.lastAlertAt)) {
			List<TerminalDisconnect> list = terminalDisconnectRepository.findAllOrderByAlertDate(predicate, pageable);
			page = new PageImpl<>(list, pageable, terminalDisconnectRepository.count(predicate));
		} else
			page = terminalDisconnectRepository.findAll(predicate, pageable);

		loadWorkOrderDetail(page.getContent().stream().map(TerminalDisconnect::getTerminal).collect(Collectors.toList()));
		return page;
	}

	public void loadLastAlertAt(List<TerminalDisconnect> list, String status) {
		list.forEach(t -> {
			Page<LogDisconnection> page;
			if ("alert".equals(status)) {
				page = getDisconnectHistoryByTerminalId(t.getTerminalId(), PageRequest.of(0,1,Sort.by(Sort.Direction.DESC, "id")));
			} else {
				Page<LogDisconnection> latestLog = getDisconnectHistoryByTerminalId(t.getTerminalId(), null, PageRequest.of(0,1,Sort.by(Sort.Direction.DESC, "id")));
				if (latestLog.hasContent() && latestLog.getContent().get(0).getEvent().equals(Event.DISCONNECT)) {
					page = getDisconnectHistoryByTerminalId(t.getTerminalId(), PageRequest.of(1,1,Sort.by(Sort.Direction.DESC, "id")));
				} else {
					page = getDisconnectHistoryByTerminalId(t.getTerminalId(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));
				}
			}

			if (!page.isEmpty()) {
				t.setLastAlertAt(page.getContent().get(0).getEventAt());
			}
		});
	}

	public void saveTerminalDisconnect(TerminalAlertPayload payload) {

		// MoldMaintenance 기준 처리..
		TerminalDisconnect terminalDisconnect = terminalDisconnectRepository.findById(payload.getId())
				.orElseThrow(() -> new RuntimeException("ERROR: DATA (id)"));

//		List<TerminalDisconnect> terminalDisconnectExistList = terminalDisconnectRepository.findByTerminalIdAndNotificationStatus(terminalDisconnect.getTerminalId(), NotificationStatus.CONFIRMED);
//		if(terminalDisconnectExistList != null && terminalDisconnectExistList.size() > 0) terminalDisconnectRepository.deleteAll(terminalDisconnectExistList);
		TerminalDisconnect lastTerminalDisconnect = terminalDisconnectRepository
				.findByTerminalIdAndNotificationStatusAndLatest(terminalDisconnect.getTerminalId(), NotificationStatus.CONFIRMED, true).orElse(null);
		if(lastTerminalDisconnect != null){
			lastTerminalDisconnect.setLatest(false);
			terminalDisconnectRepository.save(lastTerminalDisconnect);
		}

		terminalDisconnect.setNotificationStatus(NotificationStatus.CONFIRMED);
		terminalDisconnect.setMessage(payload.getMessage());
		terminalDisconnect.setConfirmedAt(Instant.now());
		terminalDisconnect.setConfirmedBy(SecurityUtils.getName());

		terminalDisconnectRepository.save(terminalDisconnect);
	}

	public void saveTerminalDisconnect(List<TerminalAlertPayload> terminalPayloadList){
		List<Long> ids = terminalPayloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<TerminalDisconnect> terminalDisconnectList = terminalDisconnectRepository.findByIdIsIn(ids);

		List<Long> moldIdsExist = terminalDisconnectList.stream().map(x -> x.getTerminalId()).collect(Collectors.toList());
		List<TerminalDisconnect> terminalDisconnectExistList = terminalDisconnectRepository.findByTerminalIdIsInAndNotificationStatus(moldIdsExist, NotificationStatus.CONFIRMED);
		if(terminalDisconnectExistList != null && terminalDisconnectExistList.size() > 0) terminalDisconnectRepository.deleteAll(terminalDisconnectExistList);

		if(terminalDisconnectList == null || terminalDisconnectList.size() == 0) return;
		terminalDisconnectList.forEach(terminalDisconnect -> {
			TerminalAlertPayload payload = terminalPayloadList.stream().filter(x -> x.getId().equals(terminalDisconnect.getId())).findAny().orElse(null);
			if(payload != null) {
				terminalDisconnect.setNotificationStatus(NotificationStatus.CONFIRMED);
				terminalDisconnect.setMessage(payload.getMessage());
				terminalDisconnect.setConfirmedAt(Instant.now());
				terminalDisconnect.setConfirmedBy(SecurityUtils.getName());
			}
		});
		terminalDisconnectRepository.saveAll(terminalDisconnectList);
	}

	public Page<LogDisconnection> getDisconnectHistoryByTerminalId(Long terminalId, NotificationStatus notificationStatus, Pageable pageable){
		QLogDisconnection logDisconnection = QLogDisconnection.logDisconnection;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(logDisconnection.equipmentType.eq(EquipmentType.TERMINAL));
		if(terminalId != null) builder.and(logDisconnection.equipmentId.eq(terminalId));
		if(notificationStatus != null) builder.and(logDisconnection.notificationStatus.eq(notificationStatus));
		return logDisconnectionService.findLogDisconnection(builder, pageable);
	}

	public Page<LogDisconnection> getDisconnectHistoryByTerminalId(Long terminalId, Pageable pageable){
		QLogDisconnection logDisconnection = QLogDisconnection.logDisconnection;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(logDisconnection.equipmentType.eq(EquipmentType.TERMINAL));
		builder.and(logDisconnection.event.eq(Event.DISCONNECT));
		if(terminalId != null) builder.and(logDisconnection.equipmentId.eq(terminalId));
		return logDisconnectionService.findLogDisconnection(builder, pageable);
	}

	public List<Long> standardizeData(){
		List<TerminalDisconnect> terminalDisconnectList = terminalDisconnectRepository.findByNotificationStatusIsInAndLatest(Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true);
		Map<Long, List<TerminalDisconnect>> terminalDisconnectMap = new HashMap<>();
		terminalDisconnectList.forEach(terminalDisconnect -> {
			if(!terminalDisconnectMap.containsKey(terminalDisconnect.getTerminalId())) terminalDisconnectMap.put(terminalDisconnect.getTerminalId(), new ArrayList<>());
			terminalDisconnectMap.get(terminalDisconnect.getTerminalId()).add(terminalDisconnect);
		});
		terminalDisconnectMap.forEach((k, v) -> {
			if(terminalDisconnectMap.get(k) != null && terminalDisconnectMap.get(k).size() > 1){
				for(TerminalDisconnect td : terminalDisconnectMap.get(k)){
					if(terminalDisconnectMap.get(k).indexOf(td) == terminalDisconnectMap.get(k).size() - 1) break;
					td.setLatest(false);
				}
				terminalDisconnectRepository.saveAll(terminalDisconnectMap.get(k));
			}
		});
		return terminalDisconnectRepository.findByNotificationStatusIsInAndLatest(Arrays.asList(NotificationStatus.CONFIRMED, NotificationStatus.FIXED), true)
				.stream().map(x -> x.getTerminalId()).collect(Collectors.toList());
	}
	@Transactional
	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto){
		try
		{
			List<Terminal> terminals = terminalRepository.findAllById(dto.getIds());
			terminals.forEach(terminal -> {;
				terminal.setEnabled(dto.isEnabled());
				save(terminal);
				Terminal finalObj = findById(terminal.getId());
				versioningService.writeHistory(finalObj);
			});
			return ApiResponse.success(CommonMessage.OK, terminals);
		}
		catch (Exception e){
			return ApiResponse.error(e.getMessage());
		}
	}

	public void loadWorkOrderDetail(List<Terminal> terminals) {
		terminals.forEach(this::loadWorkOrderDetail);
	}

	public void loadWorkOrderDetail(Terminal terminal) {
		Optional<WorkOrder> optionalWorkOrder = workOrderRepository.findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus.COMPLETED, terminal.getId());
		optionalWorkOrder.ifPresent(workOrder -> terminal.setLastWorkOrderId(workOrder.getId()));
		terminal.setWorkOrderHistory(workOrderRepository.countByStatusInAndAssetId(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED), terminal.getId()));
	}

	public ApiResponse getTerminalDisconnectIds(Predicate predicate) {
		return ApiResponse.success(CommonMessage.OK, terminalDisconnectRepository.getAllIds(predicate));
	}

	public void changeTabPayload(TerminalPayload payload) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TERMINAL_SETTING));
		payload.setDefaultTab(true);
		if (payload.getTabId() != null) {
			Optional<TabTable> tabTableOptional = tabTableRepository.findById(payload.getTabId());
			if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
				List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payload.getTabId());
				List<Long> moldIdList = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
				payload.setIds(moldIdList);
				payload.setDefaultTab(tabTableOptional.get().getIsDefaultTab());
			}
		}
	}
}
