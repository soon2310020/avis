package saleson.api.counter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.asset.resource.composite.rststp.service.RstStpService;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.company.CompanyRepository;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.counter.payload.BatchUpdateTermPayload;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.mold.MoldDisconnectRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.preset.PresetRepository;
import saleson.api.preset.PresetService;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.api.workOrder.WorkOrderRepository;
import saleson.api.workOrder.WorkOrderService;
import saleson.common.constant.CommonMessage;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.PresetStatus;
import saleson.common.enumeration.StorageType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.MoldDisconnect;
import saleson.model.Preset;
import saleson.model.TabTable;
import saleson.model.TabTableData;
import saleson.model.WorkOrder;
import saleson.model.data.CounterToolingData;
import saleson.service.transfer.LogDisconnectionService;

@Service
public class CounterService {

	@Autowired
	CounterRepository counterRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	MoldRepository moldRepository;

	@Autowired
	PresetRepository presetRepository;

	@Autowired
	VersioningService versioningService;

	@Autowired
	FileStorageService fileStorageService;

	@Lazy
	@Autowired
	MoldService moldService;

	@Autowired
	private WorkOrderRepository workOrderRepository;

	@Autowired
	private WorkOrderService workOrderService;

	@Autowired
	LogDisconnectionService logDisconnectionService;

	@Autowired
	MoldDisconnectRepository moldDisconnectRepository;
	@Autowired
	private TabTableRepository tabTableRepository;

	@Autowired
	private TabTableDataRepository tabTableDataRepository;
	@Autowired
	private ColumnTableConfigService columnTableConfigService;

	public Page<Counter> findAll(Predicate predicate, Pageable pageable) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});
		Page<Counter> page;
		if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<Counter> list = counterRepository.findAllOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, counterRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<Counter> list = counterRepository.findAllOrderByStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, counterRepository.count(predicate));
		} else if (SpecialSortProperty.moldCounterSubscription.contains(properties[0])) {
			List<Counter> list = counterRepository.findAllOrderBySpecialField(predicate, pageable);
			page = new PageImpl<>(list, pageable, counterRepository.count(predicate));
		} else {
			page = counterRepository.findAll(predicate, pageable);
		}

		loadWorkOrderDetail(page.getContent());
		return page;

	}

	public List<Long> findAllIds(CounterPayload payload) {
		changeTabPayload(payload);
		BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class).select(Projections.constructor(Long.class, Q.counter.id)).from(Q.counter);
		query.where(builder);
		QueryResults<Long> results = query.fetchResults();
		return results.getResults();
	}

	public void changeTabPayload(CounterPayload payload) {
		payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.COUNTER_SETTING));
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

	public Counter findById(Long id) {
		Optional<Counter> category = counterRepository.findById(id);
		if (category.isPresent()) {
			return category.get();
		} else {
			return null;
		}
	}

	public boolean checkChangeShot(Counter counter, Mold mold, Integer presetCount) {
		boolean isChangeShot = false;
		if (mold != null) {
			int shotNum = counter.getShotCount() != null ? counter.getShotCount().intValue() : 0;
			int accumulatedShots = mold.getLastShot() != null ? mold.getLastShot().intValue() : 0;
			if (shotNum != accumulatedShots)
				isChangeShot = true;
			else if (presetCount != null && presetCount != shotNum)
				isChangeShot = true;
		}

		return isChangeShot;
	}

	@Transactional
	public void save(Counter counter, MultipartFile[] files) {
		EquipmentStatus status = counter.getEquipmentStatus();

		// 2023.08.31 Mickey.Park
		// Reset New Logic 적용 시 Counter와 Mold의 Shot이 다르게 적용되므로 주석처리
//		if (EquipmentStatus.INSTALLED.equals(counter.getEquipmentStatus())) {
//			boolean isChangeShot = checkChangeShot(counter, counter.getMold(), counter.getPresetCount());
//			if (!isChangeShot) {
//				counter.setPresetStatus(PresetStatus.APPLIED);
//			} else if (isChangeShot) {
//				counter.setPresetStatus(PresetStatus.READY);
//			}
//		}		

		if (counter.getPresetCount() == null && counter.getPresetStatus() == null) {
			// 신규 창착 등록 케이스 (preset 설정 없이 카운트 정보를 수신할 수 있도록)  ==> 바로 샷정보 넘어오면 저장함..
			// preset APPLIED로 등록 함...
			counter.setPresetStatus(PresetStatus.APPLIED);
		} else if (counter.getPresetCount() != null && counter.getPresetStatus() != PresetStatus.APPLIED) {
			counter.setPresetStatus(PresetStatus.READY);
		}

		boolean newCounter = counter.getId() == null;
		boolean isNeedAutoConfirmDisconnectAlert = false;

		Long moldId = null;
		if (counter.getMold() != null) {

			Mold mold = counter.getMold();
			EquipmentStatus oldStatus = mold.getEquipmentStatus();
			if (status == EquipmentStatus.INSTALLED) {
				if (mold.getEquipmentStatus() == EquipmentStatus.AVAILABLE || mold.getEquipmentStatus() == EquipmentStatus.DETACHED) {
					mold.setEquipmentStatus(EquipmentStatus.INSTALLED);
				}
				counter.setOperatingStatus(null);
				mold.setOperatingStatus(null);
				mold.setCounter(counter);
				isNeedAutoConfirmDisconnectAlert = true;
			} else if (status == EquipmentStatus.DETACHED) {
				mold.setEquipmentStatus(status);
			} else {
				mold.setCounter(null);
				mold.setEquipmentStatus(EquipmentStatus.AVAILABLE);
			}

			moldRepository.save(mold);
			moldId = mold.getId();
			moldService.createOrUpdateMoldDetachment(mold, oldStatus, status);
		}

		Counter counterSave = counterRepository.saveAndFlush(counter);

//		write history
		if (newCounter) {
			versioningService.writeHistory(counterSave);
		}

		if (isNeedAutoConfirmDisconnectAlert) {
			autoConfirmDisconnectAlert(moldId);
		}

		// 2023.09.01 Mickey.Park
		// Create Preset when registering a new counter - Apply only when entering mold information and setting Preset

		if (ObjectUtils.isEmpty(counter.getPresetStatus())) {
			if (!ObjectUtils.isEmpty(counter.getPresetCount()) && EquipmentStatus.INSTALLED.equals(counter.getEquipmentStatus())) {
				Preset preset = new Preset(counter.getEquipmentCode());
				preset.setPreset(counter.getPresetCount().toString());
				Preset newPreset = BeanUtils.get(PresetService.class).save(preset);
				BeanUtils.get(RstStpService.class).apply(newPreset);
			} else {
				counter.setPresetStatus(PresetStatus.APPLIED);
				BeanUtils.get(CounterService.class).save(counter);
			}
		}

		// Previous Logic - Delete
		// Preset 정보 등록equipment-status
//		if (counter.getPresetCount() != null && counter.getPresetStatus() != PresetStatus.APPLIED) {
//			// Pre
//			Preset preset = new Preset(counter.getEquipmentCode());
//			preset.setPreset(String.valueOf(counter.getPresetCount()));
//			preset.setPresetStatus(PresetStatus.READY);
//			preset.setApplyDesc("[" + DateUtils.getToday() + "] Preset setting is ready.");
//			preset.setShotCount(counter.getShotCount() == null ? 0 : counter.getShotCount()); // #25 PRESET 신청 시점의 counter의 shotCount 정보를 저장.
//			//add info user put
//			if (counter.getMold() != null) {
//				preset.setShotMissing(counter.getPresetCount() - (counter.getMold().getLastShot() != null ? counter.getMold().getLastShot() : 0));
//			}
//
//			// 1. 기존 카운터 CI로 READY 인 상태가 있는 경우 CANCEL로 업데이트 ..
//			presetRepository.updateReadyToCancelByCi(counter.getEquipmentCode());
//
//			// 2. Preset 정보 등록
//			presetRepository.save(preset);
//
//		}

		if (files != null) {
			fileStorageService.save(new FileInfo(StorageType.MOLD_COUNTER, moldId, counter.getId(), files));
		}
	}

	@Transactional
	public void migrationOPMoldAndCounterMissMatch() {
		List<Mold> moldList = moldRepository.findAllMoldHaveOPMissMatch();

		List<Counter> counterList = Lists.newArrayList();
		for (Mold mold : moldList) {
			mold.setOperatingStatus(null);
			Counter counter = mold.getCounter();
			counter.setOperatingStatus(null);
			counterList.add(counter);
			autoConfirmDisconnectAlert(mold.getId());
		}
		moldRepository.saveAll(moldList);
		counterRepository.saveAll(counterList);
	}

	@Transactional
	public void autoConfirmDisconnectAlert(Long moldId) {
		MoldPayload alertPayload = new MoldPayload();
		alertPayload.setLastAlert(true);
		alertPayload.setStatus("alert");
		alertPayload.setIds(Collections.singletonList(moldId));
		Iterable<MoldDisconnect> moldDisconnectPage = moldDisconnectRepository.findAll(alertPayload.getDisconnectPredicate());

		for (MoldDisconnect moldDisconnect : moldDisconnectPage) {
			MoldPayload payload = new MoldPayload();
			payload.setNotificationStatus(NotificationStatus.CONFIRMED);
			payload.setMessage("Auto confirm after a re-installation");
			payload.setId(moldDisconnect.getId());
			moldService.saveMoldDisconnect(payload);
			logDisconnectionService.confirm(payload.getId(), payload.getMessage());
		}

	}

	@Transactional
	public void save(Counter counter) {
		save(counter, null);
	}

	@Transactional
	public void save(CounterPayload payload) {
		Counter counter = payload.getModel();
		save(counter, payload.getFiles());
	}

	@Transactional
	public void deleteById(Long id) {
		counterRepository.deleteById(id);
	}

	public Optional<Counter> findByEquipmentCode(String equipmentCode) {
		return counterRepository.findByEquipmentCode(equipmentCode);
	}

	@Transactional
	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
		try {
			List<Counter> counters = counterRepository.findAllById(dto.getIds());
			counters.forEach(counter -> {
				;
				counter.setEnabled(dto.isEnabled());
				save(counter);
				Counter finalObj = findById(counter.getId());
				versioningService.writeHistory(finalObj);
			});
			return ApiResponse.success(CommonMessage.OK, counters);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	public ApiResponse getListCounter(List<String> counterCodeList, Pageable pageable, String searchText, Boolean isUnmatched) {
		List<CounterToolingData> counterToolingCodeList = counterRepository.getListCounter(counterCodeList, pageable, searchText, isUnmatched);

		return ApiResponse.success(CommonMessage.OK, counterToolingCodeList);
	}

	public void loadWorkOrderDetail(List<Counter> counters) {
		counters.forEach(this::loadWorkOrderDetail);
	}

	public void loadWorkOrderDetail(Counter counter) {
		Optional<WorkOrder> optionalWorkOrder = workOrderRepository.findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus.COMPLETED, counter.getId());
		optionalWorkOrder.ifPresent(workOrder -> counter.setLastWorkOrderId(workOrder.getId()));
		counter.setWorkOrderHistory(
				workOrderRepository.countByStatusInAndAssetId(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED), counter.getId()));
	}

	@Transactional
	public ApiResponse subscriptionTerm(BatchUpdateTermPayload dto) {
		try {
			ValueUtils.assertNotEmpty(dto.getSubscriptionTerm(), "subscription_term");

			List<Counter> counters = counterRepository.findAllById(dto.getIds());
			counters.forEach(counter -> {
				;
				counter.setSubscriptionTerm(dto.getSubscriptionTerm());
				save(counter);
				Counter finalObj = findById(counter.getId());
				versioningService.writeHistory(finalObj);
			});
			return ApiResponse.success(CommonMessage.OK, counters);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

}
