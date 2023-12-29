package saleson.api.machine;

import com.amazonaws.services.kms.model.NotFoundException;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataRequest.DataRequestObjectRepository;
import saleson.api.dataRequest.DataRequestService;
import saleson.api.machine.payload.MachineMoldData;
import saleson.api.machine.payload.MachineMoldPayload;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.api.workOrder.WorkOrderRepository;
import saleson.api.workOrder.WorkOrderService;
import saleson.common.constant.CommonMessage;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.StorageType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.customProperty.ObjectCustomFieldValue;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldMachinePairData;
import saleson.model.customField.CustomFieldValue;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MachineService {

    @Autowired
    @Lazy
    MachineRepository machineRepository;
    @Autowired
    MoldRepository moldRepository;
    @Autowired
    MachineMoldMatchingHistoryRepository machineMoldMatchingHistoryRepository;

    @Autowired
    private DataRequestService dataRequestService;
    @Autowired
    VersioningService versioningService;
    @Autowired
    CustomFieldValueService customFieldValueService;
    @Autowired
    DataCompletionRateService dataCompletionRateService;
    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    private ColumnTableConfigService columnTableConfigService;

    @Autowired
    private TabTableRepository tabTableRepository;

    @Autowired
    private TabTableDataRepository tabTableDataRepository;

    public Page<Machine> findAll(Predicate predicate, Pageable pageable) {
        String property[] = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            property[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        Page<Machine> page;
        if (property[0].startsWith(SpecialSortProperty.customFieldSort)) {
            List<ObjectCustomFieldValue> objectCustomFieldValues = machineRepository.findAndSortWithCustomFieldValue(predicate, pageable);
            List<Machine> parts = objectCustomFieldValues.stream().map(o -> (Machine) o.getT()).collect(Collectors.toList());
            page = new PageImpl<>(parts, pageable, machineRepository.count(predicate));
        } else
            page = machineRepository.findAll(predicate, pageable);
        loadValueCustomField(page.getContent());
        loadWorkOrderDetail(page.getContent());
        return page;
    }
    public List<Long> findAllIds(MachinePayload payload){
        changeTabPayload(payload);
        BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
        JPQLQuery<Long> query = com.emoldino.framework.util.BeanUtils.get(JPAQueryFactory.class)
                .select(Projections.constructor(Long.class, Q.machine.id))
                .from(Q.machine);
        query.where(builder);
        QueryResults<Long> results = query.fetchResults();
        return results.getResults();

    }

    public Machine findById(Long id) {
        return machineRepository.findById(id).orElseThrow(()
                -> new NotFoundException(String.format("Cannot found machine with id: %s", id)));
    }

    @Transactional
    public void save(Machine machine) {
    	MachinePayload payload = null;
    	save(machine, payload);
    }

    @Transactional
    public Machine save(Machine machine, MachinePayload payload) {

        boolean objNew = machine.getId() == null;

        Machine machineSave = machineRepository.save(machine);

//		write history
        if (objNew) {
            versioningService.writeHistory(machineSave);
        } else {
            dataRequestService.completeDataCompletion(machine.getId(), ObjectType.MACHINE);
        }

        if (payload != null && payload.getDataRequestId() != null) {
            dataRequestService.saveDataRequestObject(payload.getDataRequestId(), machineSave.getId(), ObjectType.MACHINE);
        }



        //upload picture
        if(payload!=null && payload.getPictureFiles() != null){
            fileStorageService.save(new FileInfo(StorageType.MACHINE_PICTURE, machine.getId(), payload.getPictureFiles()));
        }
        //update data completion rate
        dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.MACHINE, machine.getCompanyId());
        //update data completion order
        dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.MACHINE, machine.getId());
        return machineSave;
    }

    @Transactional
    public void save(MachinePayload payload) {
        Machine machine = payload.getModel();
        save(machine, payload);
    }

    @Transactional
    public void deleteById(Long id) {
        machineRepository.deleteById(id);
    }


    public Optional<Machine> findByMaChineCode(String machineCode) {
        return machineRepository.findByMachineCode(machineCode);
    }

    public List<MiniComponentData> getListMachineIdsToMatch() {
        return machineRepository.findMachinesForMatchWithMold();
    }

    public Page<MiniComponentData> getListMachineLiteData(Predicate predicate, Pageable pageable) {
        return machineRepository.findMachineNotMatched(predicate, pageable);
    }

    public Page<MachineMoldData> getListMachineToMatch(Predicate predicate, Pageable pageable){
        return  machineRepository.findMachineToMatch(predicate, pageable);
    }

    @Transactional
    public String matchMachineWithTooling(Long machineId, Long moldId) {
        Mold mold = moldRepository.getOne(moldId);
        Machine machine = machineRepository.getOne(machineId);
        if (machine.getMold() != null) {
            return CommonMessage.MACHINE_WAS_MATCHED;
        } else if (mold.getMachine() != null) {
            return CommonMessage.TOOLING_WAS_MATCHED;
        } else {
            mold.setMachineId(machineId);
            mold.setMachine(machine);
            moldRepository.save(mold);
            machine.setMold(mold);
            machineRepository.save(machine);

            MachineMoldMatchingHistory history = new MachineMoldMatchingHistory();
            history.setMachine(machine);
            history.setMold(mold);
            Instant matchingTime = Instant.now();
            history.setMatchTime(matchingTime);
            history.setMatchDay(DateUtils2.format(matchingTime, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(machine.getLocationId()), null));
            history.setMatchHour(DateUtils2.format(matchingTime, "HHmm", LocationUtils.getZoneIdByLocationId(machine.getLocationId()), null));
            machineMoldMatchingHistoryRepository.save(history);

            return CommonMessage.OK;
        }
    }

    @Transactional
    public String unMatchMachineWithTooling(Long moldId, Long machineId) {
        Mold mold = moldRepository.getOne(moldId);
        Machine machine = machineRepository.getOne(machineId);
        if (machine.getMold() == null) {
            return CommonMessage.MACHINE_WAS_NOT_MATCHED;
        } else if (mold.getMachine() == null) {
            return CommonMessage.TOOLING_WAS_NOT_MATCHED;
        } else if (!mold.getMachine().getId().equals(machineId) || !machine.getMold().getId().equals(moldId)) {
            return CommonMessage.THIS_MACHINE_WAS_NOT_MATCHED_WITH_THIS_TOOLING;
        } else {
            mold.setMachineId(null);
            mold.setMachine(null);
            moldRepository.save(mold);
            machine.setMold(null);
            machineRepository.save(machine);

            Optional<MachineMoldMatchingHistory> optional = machineMoldMatchingHistoryRepository.findFirstByMachineAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(machine);
            MachineMoldMatchingHistory history;
            if (optional.isPresent()) {
                history = optional.get();
            } else {
                history = new MachineMoldMatchingHistory();
                history.setMachine(machine);
                history.setMold(mold);
            }
            Instant unmatchTime = Instant.now();
            history.setUnmatchTime(unmatchTime);
            history.setUnmatchDay(DateUtils2.format(unmatchTime, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(machine.getLocationId()), null));
            history.setUnmatchHour(DateUtils2.format(unmatchTime, "HHmm", LocationUtils.getZoneIdByLocationId(machine.getLocationId()), null));
            history.setCompleted(true);
            machineMoldMatchingHistoryRepository.save(history);
            return CommonMessage.OK;
        }
    }
    public String unMatchMachineWithToolingInBatch(BatchUpdateDTO machineMoldPayloads) {
        if(machineMoldPayloads.getIds()!=null)
        machineMoldPayloads.getIds().stream().forEach(machineId-> {
            Machine machine = machineRepository.findById(machineId).orElse(null);
            if(machine!= null && machine.getMold()!=null)
            unMatchMachineWithTooling(machine.getMold().getId(),machineId);
        });
        return CommonMessage.OK;
    }

    public List<MoldMachinePairData> getListMatchedMachines() {
        return moldRepository.findMoldsMatchedWithMachine();
    }

    public Page<MoldMachinePairData> getPageMatchedMachines(Predicate predicate, Pageable pageable) {
        return moldRepository.findPageMoldsMatchedWithMachine(predicate, pageable);
    }

    private void loadValueCustomField(List<Machine> list) {
        //get data for custom field
        Map<Long, Map<Long, List<CustomFieldValue>>> mapValueCustomField = customFieldValueService.mapValueCustomField(ObjectType.MACHINE, list.stream().map(m -> m.getId()).collect(Collectors.toList()));
        list.stream().forEach(item -> {
            if (mapValueCustomField.containsKey(item.getId())) {
                item.setCustomFieldValueMap(mapValueCustomField.get(item.getId()));
            }
        });
    }

    public boolean existsCode(String name, Long id) {
        if (id != null)
            return machineRepository.existsByMachineCodeAndIdNot(name, id);
        return machineRepository.existsByMachineCode(name);
    }

    public ResponseEntity<?> valid(MachinePayload payload, Long id) {
        ValueUtils.assertNotEmpty(payload.getMachineCode(), "machine_id");

        payload.setMachineCode(StringUtils.trimWhitespace(payload.getMachineCode()));
        boolean existsCode = existsCode(payload.getMachineCode(), id);
        if (existsCode) {
            return ResponseEntity.badRequest().body("Machine ID is already registered in the system.");
        }
//        if (StringUtils.isEmpty(payload.getLine())) {
//            return ResponseEntity.badRequest().body("Line cannot be empty.");
//        }
//        ValueUtils.assertNotEmpty(payload.getCompanyId(), "company");
        ValueUtils.assertNotEmpty(payload.getLocationId(), "location");

        return null;
    }

    public List<String> getDistinctLines() {
        List<Long> companyIds = dashboardGeneralFilterUtils.getCompanyIds();
        if (CollectionUtils.isNotEmpty(companyIds)) {
            return machineRepository.findDistinctLines(companyIds);
        } else {
            return new ArrayList<>();
        }
    }


    public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
        try {
            List<Machine> machines = machineRepository.findAllById(dto.getIds());
            machines.forEach(machine -> {
                machine.setEnabled(dto.isEnabled());
                save(machine);
                versioningService.writeHistory(findById(machine.getId()));
            });
            return ApiResponse.success(CommonMessage.OK, machines);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public void delete(Machine machine) {
        machine.setDeleted(true);
        machineRepository.save(machine);
        //update data completion rate
        dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.MACHINE, machine.getCompanyId());
    }

    public ApiResponse delete(Long id) {
        Machine machine = machineRepository.getOne(id);
        if (machine != null) {
            if (machine.getMold() == null) {
                delete(machine);
                return ApiResponse.success(CommonMessage.OK, machine);
            } else {
                return ApiResponse.error(CommonMessage.MACHINE_WAS_MATCHED);
            }
        } else {
            return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
        }
    }

    public void restore(Machine machine) {
        machine.setDeleted(false);
        machineRepository.save(machine);
    }

    public ApiResponse restore(Long id) {
        Machine machine = machineRepository.getOne(id);
        if (machine != null) {
            restore(machine);
            //update data completion rate
            dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.MACHINE, machine.getCompanyId());
            return ApiResponse.success(CommonMessage.OK, machine);
        } else {
            return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
        }
    }

    private static final String DELETED = "DELETED";
    private static final String NOT_DELETED = "NOT_DELETED";

    public ApiResponse deleteInBatch(BatchUpdateDTO dto) {
        Map<String, List<Long>> map = new HashMap<>();
        map.put(DELETED, new ArrayList<>());
        map.put(NOT_DELETED, new ArrayList<>());
        List<Machine> machines = machineRepository.findAllById(dto.getIds());
        machines.forEach(machine -> {
            if (machine.getMold() == null) {
                delete(machine);
                map.get(DELETED).add(machine.getId());
            } else {
                map.get(NOT_DELETED).add(machine.getId());
            }
        });

        if (CollectionUtils.isNotEmpty(map.get(DELETED))) {
            if (CollectionUtils.isNotEmpty(map.get(NOT_DELETED))) {
                return ApiResponse.success(CommonMessage.SOME_MACHINES_WERE_MATCHED, map);
            } else {
                return ApiResponse.success(CommonMessage.OK, map);
            }
        } else {
            return ApiResponse.error(CommonMessage.ALL_MACHINES_WERE_MATCHED);
        }
    }

    public void loadWorkOrderDetail(List<Machine> machines) {
        machines.forEach(this::loadWorkOrderDetail);
    }

    public void loadWorkOrderDetail(Machine machine) {
        Optional<WorkOrder> optionalWorkOrder = BeanUtils.get(WorkOrderRepository.class).findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus.COMPLETED, machine.getId());
        optionalWorkOrder.ifPresent(workOrder -> machine.setLastWorkOrderId(workOrder.getId()));
        machine.setWorkOrderHistory(BeanUtils.get(WorkOrderRepository.class).countByStatusInAndAssetId(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED), machine.getId()));
    }

    public void changeTabPayload(MachinePayload payload) {
        if (StringUtils.isEmpty(payload.getSearchType())) {
            payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MACHINE_SETTING));
        } else if ("LINE".equalsIgnoreCase(payload.getSearchType())) {
            payload.setSelectedFields(Arrays.asList("line"));
        } else {
            payload.setSelectedFields(Arrays.asList("machineCode"));
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
    }
}
