package saleson.api.workOrder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.framework.repository.Q;
import com.emoldino.api.common.resource.base.workorder.enumeration.WorkOrderParticipantType;
import com.emoldino.framework.util.HttpUtils;
import com.google.common.base.Strings;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlan;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlanRepository;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;
import com.emoldino.api.common.resource.base.id.util.IdUtils;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.framework.repository.Q;
import com.emoldino.api.common.resource.base.workorder.enumeration.WorkOrderParticipantType;
import com.emoldino.framework.enumeration.ActiveStatus;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import saleson.api.checklist.ChecklistRepository;
import saleson.api.counter.CounterRepository;
import saleson.api.machine.MachineRepository;
import saleson.api.mold.MoldMaintenanceRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.notification.NotificationService;
import saleson.api.terminal.TerminalRepository;
import saleson.api.user.UserRepository;
import saleson.api.workOrder.payload.ApproveRequestApprovalPayload;
import saleson.api.workOrder.payload.ExportWorkOrderPayload;
import saleson.api.workOrder.payload.SearchPayload;
import saleson.api.workOrder.payload.WorkOrderPayload;
import saleson.api.workOrder.payload.WorkOrderSummary;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.*;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageRepository;
import saleson.common.service.FileStorageService;
import saleson.common.util.*;
import saleson.dto.*;
import saleson.model.*;
import saleson.model.checklist.Checklist;
import saleson.model.data.MaintenanceData;
import saleson.model.data.MoldAccumulatedData;

import javax.servlet.http.HttpServletResponse;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderRepository workOrderRepository;
    @Autowired
    private WorkOrderAssetRepository workOrderAssetRepository;
    @Autowired
    private WorkOrderUserRepository workOrderUserRepository;
    @Autowired
    private MoldRepository moldRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MachineRepository machineRepository;
    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private CounterRepository counterRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Autowired
    private WorkOrderCostRepository workOrderCostRepository;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private MoldMaintenanceRepository moldMaintenanceRepository;

    @Autowired
    private MoldService moldService;
    @Autowired
    private WorkOrderService workOrderService;


    @Value("${file.storage.location}")
    private String fileStorageLocation;

    @Value("${system.storage.type}")
    private String systemStorageType;

    @Autowired
    private ObjectMapper objectMapper;

    public WorkOrder findById(Long id) {
        return workOrderRepository.getOne(id);
    }

    @Transactional
    public WorkOrder saveNormalWorkOrder(WorkOrder workOrder, WorkOrderPayload payload) {
        if (payload.getOrderType().equals(WorkOrderType.PREVENTATIVE_MAINTENANCE)) {
            workOrderService.cancelOldWorkOrder(payload.getMoldIds().get(0));
        }
        payload.setCreated(true);
        WorkOrder result = save(workOrder, payload, true, false);
        if (payload.getOrderType().equals(WorkOrderType.PREVENTATIVE_MAINTENANCE)) {
            generateMoldMaintenance(workOrder, payload.getMoldIds().get(0));
        }
        return result;
    }

    @Transactional
    public WorkOrder saveRequestApprovalWorkOrder(WorkOrder workOrder, WorkOrderPayload payload) {
        return save(workOrder, payload, true, true);
    }

    @Transactional
    public WorkOrder updateNormalWorkOrder(WorkOrder workOrder, WorkOrderPayload payload) {
        return save(workOrder, payload, false, false);
    }

    @Transactional
    public WorkOrder updateCMWorkOrder(WorkOrder workOrder, WorkOrderPayload payload) {
        return save(workOrder, payload, false, true);
    }

    public WorkOrder approveRequestApproval(Long id, boolean approved, ApproveRequestApprovalPayload payload) {
        WorkOrder workOrder = workOrderRepository.getOne(id);
        User currentUser = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        workOrder.setApproved(approved);
        if (approved) {
            //approve request approval
            workOrder.setStatus(WorkOrderStatus.REQUESTED_NOT_FINISHED);
        } else {
            //decline request approval
            workOrder.setStatus(WorkOrderStatus.CANCELLED);
            workOrder.setCompletedOn(DateUtils2.newInstant());
            workOrder.setCancelledReason(payload.getCancelledReason());
            workOrder.setCancelledBy(currentUser);
        }
        WorkOrder workOrderSaved = workOrderRepository.save(workOrder);

        //send notification
        notificationService.createWorkOrderCreationApprovalRequestApprovedNotification(workOrder);
        return workOrderSaved;
    }

    public WorkOrder updateMyOrder(Long id, UpdateWorkOrderDTO updateWorkOrderDTO) {
        WorkOrder workOrder = workOrderRepository.getOne(id);
        workOrder.setStatus(updateWorkOrderDTO.getStatus());
        User currentUser = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        switch (updateWorkOrderDTO.getStatus()) {
            case DECLINED:
                workOrder.setDeclinedReason(updateWorkOrderDTO.getReason());
                workOrder.setDeclinedBy(currentUser);
                workOrder.setCompletedOn(DateUtils2.newInstant());
                notificationService.updateStatusWorkOrderNotification(workOrder, workOrder.getCreatedBy(), NotificationType.WORK_ORDER_DECLINED);
                break;
            case CANCELLED:
                workOrder.setCancelledReason(updateWorkOrderDTO.getReason());
                workOrder.setCancelledBy(currentUser);
                workOrder.setCompletedOn(DateUtils2.newInstant());
                if (workOrder.getOrderType().equals(WorkOrderType.PREVENTATIVE_MAINTENANCE) && workOrder.getMoldMaintenanceId() != null) {
                    //complete mold maintenance
                    completeMoldMaintenance(workOrder, null);
                }
                break;
            case ACCEPTED:
                workOrder.setStartedOn(Instant.now());
                break;
        }

        WorkOrder workOrderSaved = workOrderRepository.save(workOrder);

        // notification
        if (WorkOrderStatus.ACCEPTED.equals(updateWorkOrderDTO.getStatus()))
            notificationService.createWorkOrderAcceptNotification(workOrder);
        else if (WorkOrderStatus.DECLINED.equals(updateWorkOrderDTO.getStatus()))
            notificationService.createWorkOrderDeclinedNotification(workOrder);
        else
            notificationService.createWorkOrderCancelledNotification(workOrder);
        return workOrderSaved;
    }

    public ApiResponse requestChange(MultipartFormData data) throws IOException {
        WorkOrderDTO dto = objectMapper.readValue(data.getPayload(), WorkOrderDTO.class);
        WorkOrder workOrder = workOrderRepository.getOne(dto.getId());

        if(workOrder.getStatus() != WorkOrderStatus.COMPLETED) throw new RuntimeException("Work order status must be COMPLETED");
        WorkOrder requestedChange = new WorkOrder();
        BeanUtils.copyProperties(workOrder, requestedChange);
        requestedChange.setId(null);
        dto.bidingData(requestedChange);
        requestedChange.setRequestChange(true);
        requestedChange.setOriginalId(workOrder.getId());
        requestedChange.setWorkOrderUsers(null);
        requestedChange.setChecklistId(workOrder.getChecklistId());
        requestedChange.setPicklistId(workOrder.getPicklistId());
        requestedChange.setCreatedBy(SecurityUtils.getUserId());
        WorkOrder savedRequestedChange = workOrderRepository.save(requestedChange);

        List<Long> userIds = workOrder.getWorkOrderUsers().stream().map(WorkOrderUser::getUserId).collect(Collectors.toList());
        List<Long> moldIds = workOrder.getWorkOrderAssets().stream().filter(a -> ObjectType.TOOLING.equals(a.getType())).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
        List<Long> machineIds = workOrder.getWorkOrderAssets().stream().filter(a -> ObjectType.MACHINE.equals(a.getType())).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
        List<Long> terminalIds = workOrder.getWorkOrderAssets().stream().filter(a -> ObjectType.TERMINAL.equals(a.getType())).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
        List<Long> counterIds = workOrder.getWorkOrderAssets().stream().filter(a -> ObjectType.COUNTER.equals(a.getType())).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
        saveWorkOrderUsers(savedRequestedChange, userIds, WorkOrderParticipantType.ASSIGNEE);
        saveWorkOrderAssets(savedRequestedChange, moldIds, machineIds, terminalIds, counterIds);

        List<WorkOrderCost> workOrderCostList = dto.getWorkOrderCostList();
        workOrderCostList.forEach(workOrderCost -> workOrderCost.setWorkOrder(savedRequestedChange));
        workOrderCostRepository.deleteAllByWorkOrderId(savedRequestedChange.getId());
        workOrderCostRepository.saveAll(workOrderCostList);

        //save file
        MultipartFile[] costFiles = data.getSecondFiles();
        MultipartFile[] powFiles = data.getForthFiles();
        if(costFiles != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_COST_FILE, savedRequestedChange.getId(), costFiles));
            fileStorageService.cloneFile(StorageType.WORK_ORDER_COST_FILE,workOrder.getId(),StorageType.WORK_ORDER_COST_FILE,savedRequestedChange.getId());
        }else {
            fileStorageService.cloneFile(StorageType.WORK_ORDER_COST_FILE,workOrder.getId(),StorageType.WORK_ORDER_COST_FILE,savedRequestedChange.getId());
        }
        if(powFiles != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_POW_FILE, savedRequestedChange.getId(), powFiles));
            fileStorageService.removeAndCloneFile(StorageType.WORK_ORDER_POW_FILE,workOrder.getId(),StorageType.WORK_ORDER_POW_FILE,savedRequestedChange.getId(),dto.getRemoveFileId());
        } else  {
            fileStorageService.removeAndCloneFile(StorageType.WORK_ORDER_POW_FILE,workOrder.getId(),StorageType.WORK_ORDER_POW_FILE,savedRequestedChange.getId(),dto.getRemoveFileId());
        }
        workOrder.setStatus(WorkOrderStatus.CHANGE_REQUESTED);
        workOrderRepository.save(workOrder);

        //send notification
        notificationService.createWorkOrderModificationRequestNotification(savedRequestedChange, workOrder.getCreatorIds());
        return ApiResponse.success(CommonMessage.OK);
    }

    @Transactional
    public ApiResponse approveRequestChange(Long id, UpdateWorkOrderDTO updateWorkOrderDTO){
        WorkOrder requestedChange = workOrderRepository.getOne(id);
        WorkOrder original = workOrderRepository.getOne(requestedChange.getOriginalId());
        if (updateWorkOrderDTO.getApproved() != null && updateWorkOrderDTO.getApproved()) {
            cloneWorkOrderData(original, requestedChange);
            List<WorkOrderCost> costs = workOrderCostRepository.findAllByWorkOrderId(id);
            List<WorkOrderCost> newCosts = new ArrayList<>();
            costs.forEach(cost -> {
                WorkOrderCost newCost = new WorkOrderCost();
                newCost.setWorkOrder(original);
                newCost.setWorkOrderId(original.getId());
                newCost.setCost(cost.getCost());
                newCost.setDetails(cost.getDetails());
                newCosts.add(newCost);
            });
            fileStorageService.updateFile(requestedChange.getId(),original.getId(),StorageType.WORK_ORDER_COST_FILE);
            fileStorageService.updateFile(requestedChange.getId(),original.getId(),StorageType.WORK_ORDER_POW_FILE);
            workOrderCostRepository.deleteAllByWorkOrderId(original.getId());
            workOrderCostRepository.saveAll(newCosts);


            requestedChange.setRequestChangeApproved(true);

        } else {
            requestedChange.setRejectedChangeReason(updateWorkOrderDTO.getReason());
            User currentUser = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
            requestedChange.setRejectedChangeBy(currentUser);
        }
        original.setStatus(WorkOrderStatus.COMPLETED);
        workOrderRepository.save(original);
        workOrderRepository.save(requestedChange);
//        notificationService.createWorkOrderRejectApproveRequestChangeNotification(requestedChange, requestedChange.getCreatedBy(), updateWorkOrderDTO.getApproved());
        notificationService.createWorkOrderModificationRequestApproveNotification(requestedChange, requestedChange.getCreatedBy(), updateWorkOrderDTO.getApproved());
        return ApiResponse.success();
    }

    private void cloneWorkOrderData(WorkOrder orig, WorkOrder dest) {
        orig.setStartedOn(dest.getStartedOn());
        orig.setCompletedOn(dest.getCompletedOn());
        orig.setChecklistItems(dest.getChecklistItems());
        orig.setPicklistItems(dest.getPicklistItems());
        orig.setRefCode(dest.getRefCode());

    }

    @Transactional
    public WorkOrder save(WorkOrder workOrder, WorkOrderPayload payload, boolean notify, boolean isApproval) {

        if (payload.getChecklistId() != null && !payload.isCreated()) {
            Checklist checklist = checklistRepository.getOne(payload.getChecklistId());
            workOrder.setChecklistItems(checklist.getChecklistItems());
        }

        //save CM shot info
        if (WorkOrderType.CORRECTIVE_MAINTENANCE.equals(payload.getOrderType())) {
            Long moldId = payload.getMoldIds().get(0);

            String reportFailureTime = DateUtils2.format(payload.getFailureTime(), DateUtils2.DatePattern.yyyyMMddHHmmss, DateUtils2.Zone.SYS);
            MoldAccumulatedData reportFailureData = moldRepository.findMoldAccumulatedShotByLstLessThan(reportFailureTime, moldId);
            workOrder.setReportFailureShot(reportFailureData.getAccumulatedShots());


            String startWorkOrderTime = DateUtils2.format(payload.getStart(), DateUtils2.DatePattern.yyyyMMddHHmmss, DateUtils2.Zone.SYS);
            MoldAccumulatedData startWorkOrderData = moldRepository.findMoldAccumulatedShotByLstLessThan(startWorkOrderTime, moldId);
            workOrder.setStartWorkOrderShot(startWorkOrderData.getAccumulatedShots());
        }
        if ((WorkOrderType.DISPOSAL == payload.getOrderType() || WorkOrderType.REFURBISHMENT == payload.getOrderType())
                && CollectionUtils.isNotEmpty(payload.getMoldIds())) {
            Mold mold = moldRepository.getOne(payload.getMoldIds().get(0));
            workOrder.setReportFailureShot(mold.getLastShot());
        }

        //update status when finish creating CM work order after approval
        boolean specialNotify = false;
        if (workOrder.getStatus().equals(WorkOrderStatus.REQUESTED_NOT_FINISHED)) {
            specialNotify = true;
            workOrder.setStatus(WorkOrderStatus.REQUESTED);
        }

        if (workOrder.getCreatedBy() == null) workOrder.setCreatedBy(SecurityUtils.getUserId());


        WorkOrder workOrderSaved = workOrderRepository.save(workOrder);

        //save assets and assigned users
        saveWorkOrderAssets(workOrderSaved, payload.getMoldIds(), payload.getMachineIds(), payload.getTerminalIds(), payload.getCounterIds());
        saveWorkOrderUsers(workOrderSaved, payload.getUserIds(), WorkOrderParticipantType.ASSIGNEE);
        if (WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(workOrderSaved.getOrderType())){
            List<Mold> molds = moldRepository.findAllById(payload.getMoldIds());
            List<WorkOrderUser> workOrderUsers = molds.stream().flatMap(m -> m.getEngineersInCharge().stream()).map(e -> {
                WorkOrderUser workOrderUser = new WorkOrderUser();
                workOrderUser.setWorkOrderId(workOrderSaved.getId());
                workOrderUser.setWorkOrder(workOrderSaved);
                workOrderUser.setUser(e);
                workOrderUser.setUserId(e.getId());
                workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
                return workOrderUser;
            }).collect(Collectors.toList());
            workOrderUserRepository.saveAll(workOrderUsers);
        }

        //save work order cost
        if (payload.getCostEstimate() != null || payload.getCostDetails() != null) {
            WorkOrderCost workOrderCost = WorkOrderCost.builder()
                    .workOrderId(workOrderSaved.getId())
                    .workOrder(workOrderSaved)
                    .cost(payload.getCostEstimate())
                    .details(payload.getCostDetails())
                    .build();
            workOrderCostRepository.deleteAllByWorkOrderId(workOrderSaved.getId());
            workOrderCostRepository.save(workOrderCost);
        }

        //upload file or picture
        if(payload.getFiles() != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_FILE, workOrderSaved.getId(), payload.getFiles()));
        }
        if(payload.getPowFiles() != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_POW_FILE, workOrderSaved.getId(), payload.getPowFiles()));
        }
        if(payload.getCostFile() != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_COST_FILE, workOrderSaved.getId(), payload.getCostFile()));
        }

        if (notify) {
            if (isApproval) {
                //send notification to all OEM users
                notificationService.createWorkOrderCreationApprovalRequestNotification(workOrderSaved);
            } else {
                //send notification to assigned users
                notificationService.createWorkOrderNotification(workOrderSaved, payload.getUserIds());
            }
        }

        //special case for finishing CM work order after approval
        if (specialNotify) {
            notificationService.createWorkOrderNotification(workOrderSaved, payload.getUserIds());
        }

        return workOrderSaved;
    }

    @Transactional
    public void saveWorkOrderAssets(WorkOrder order, List<Long> moldIds, List<Long> machineIds, List<Long> terminalIds, List<Long> counterIds) {
        List<WorkOrderAsset> assets = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(moldIds)) {
            moldIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                        .workOrderId(order.getId())
                        .workOrder(order)
                        .assetId(id)
                        .assetCode(moldRepository.getOne(id).getEquipmentCode())
                        .type(ObjectType.TOOLING)
                        .build();
                assets.add(asset);
            });
        }
        if (CollectionUtils.isNotEmpty(machineIds)) {
            machineIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                        .workOrderId(order.getId())
                        .workOrder(order)
                        .assetId(id)
                        .assetCode(machineRepository.getOne(id).getMachineCode())
                        .type(ObjectType.MACHINE)
                        .build();
                assets.add(asset);
            });
        }
        if (CollectionUtils.isNotEmpty(terminalIds)) {
            terminalIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                        .workOrderId(order.getId())
                        .workOrder(order)
                        .assetId(id)
                        .assetCode(terminalRepository.getOne(id).getEquipmentCode())
                        .type(ObjectType.TERMINAL)
                        .build();
                assets.add(asset);
            });
        }
        if (CollectionUtils.isNotEmpty(counterIds)) {
            counterIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                        .workOrderId(order.getId())
                        .workOrder(order)
                        .assetId(id)
                        .assetCode(counterRepository.getOne(id).getEquipmentCode())
                        .type(ObjectType.COUNTER)
                        .build();
                assets.add(asset);
            });
        }
        workOrderAssetRepository.deleteAllByWorkOrderId(order.getId());
        workOrderAssetRepository.saveAll(assets);
    }

    @Transactional
    public void saveWorkOrderUsers(WorkOrder order, List<Long> userIds, WorkOrderParticipantType workOrderParticipantType) {
        List<WorkOrderUser> workOrderUsers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIds)) {
            userIds.forEach(id -> {
                WorkOrderUser workOrderUser = WorkOrderUser.builder()
                        .workOrderId(order.getId())
                        .workOrder(order).userId(id)
                        .user(userRepository.getOne(id))
                        .participantType(workOrderParticipantType)
                        .build();
                if (workOrderParticipantType.equals(WorkOrderParticipantType.REPORTER)){
                    User user = userRepository.getOne(id);
                    if(!workOrderUserRepository.existsByWorkOrderAndUserAndParticipantType(order,user,WorkOrderParticipantType.REPORTER)) workOrderUsers.add(workOrderUser);
                } else if (workOrderParticipantType.equals(WorkOrderParticipantType.APPROVAL_REQUESTER)){
                    User user = userRepository.getOne(id);
                    if(!workOrderUserRepository.existsByWorkOrderAndUserAndParticipantType(order,user,WorkOrderParticipantType.APPROVAL_REQUESTER)) workOrderUsers.add(workOrderUser);
                }
                else workOrderUsers.add(workOrderUser);
            });
        }
        if (!Arrays.asList(WorkOrderParticipantType.REPORTER, WorkOrderParticipantType.APPROVAL_REQUESTER).contains(workOrderParticipantType)) workOrderUserRepository.deleteAllByWorkOrderId(order.getId());
        workOrderUserRepository.saveAll(workOrderUsers);
    }

    public String generateWorkOrderId() {
        return IdUtils.gen(IdRuleCode.WORK_ORDER,null);
//        Long total = workOrderRepository.count();
//        return ValueUtils.generateNameWithAutoIncrementIndex("WO-", 7, total.intValue());
    }
    public List<Long> findAllIds(SearchPayload payload){
        BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QUser createdBy = new QUser("createdBy");
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        QUser user = new QUser("user");
        JPQLQuery<Long> query = com.emoldino.framework.util.BeanUtils.get(JPAQueryFactory.class).select(Projections.constructor(Long.class, workOrder.id));
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(payload.getAssetIds())) {
            query . from(workOrder)
                    .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                    .leftJoin(workOrderUser).on(workOrder.id.eq(workOrderUser.workOrderId)
                            .and(workOrderUser.id.eq(JPAExpressions.select(workOrderUser.id.max()).from(workOrderUser).where(workOrderUser.workOrderId.eq(workOrder.id)))))
                    .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId).and(workOrderAsset.assetId.in(payload.getAssetIds())))
                    .leftJoin(user).on(user.id.eq(workOrderUser.userId))
                    .where(builder);
        } else {
            query. from(workOrder)
                    .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                    .leftJoin(workOrderUser).on(workOrder.id.eq(workOrderUser.workOrderId)
                            .and(workOrderUser.id.eq(JPAExpressions.select(workOrderUser.id.max()).from(workOrderUser).where(workOrderUser.workOrderId.eq(workOrder.id)))))
                    .leftJoin(user).on(user.id.eq(workOrderUser.userId))
                    .where(builder);
        }
        QueryResults<Long> results = query.fetchResults();
        return results.getResults();
    }

    public WorkOrderResponse searchWorkOrder(SearchPayload searchPayload, Pageable pageable) {
        WorkOrderResponse response = new WorkOrderResponse();
        if (!SecurityUtils.isAdmin()) {
            searchPayload.setChildCompanyIdList(AccessControlUtils.getAllAccessibleCompanyIds() );
        }
        List<WorkOrderDTO> workOrderDTOList = workOrderRepository.searchWorkOrderWithAssetIds(searchPayload.getPredicate(), pageable, searchPayload.getAssetIds());
        workOrderDTOList.forEach(w -> {
            Set<Location> plants = new HashSet<>();
            procLocationInfo(w.getWorkOrderAssets(), plants);
            w.setPlantList(new ArrayList<>(plants));
        });
        long total = workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicate(), searchPayload.getAssetIds());
        Page<WorkOrderDTO> workOrderPage = new PageImpl<>(workOrderDTOList, pageable, total);
        loadMaintenanceData(workOrderDTOList);
        response.setWorkOrderList(workOrderPage);
        fillCountData(response, searchPayload);
        return response;
    }

    public ApiResponse getWorkOrderDetail(Long id) {
        WorkOrderDTO response = getWorkOrderDetailDTO(id);
        loadMaintenanceData(Collections.singletonList(response));
        return ApiResponse.success(CommonMessage.OK ,response);
    }

    public WorkOrderDTO getWorkOrderDetailDTO(Long id) {
        WorkOrder workOrder = workOrderRepository.getOne(id);
        User createdBy = workOrder.getCreatedBy() != null ? userRepository.getOne(workOrder.getCreatedBy()) : null;
        WorkOrderDTO response =null;
        if ( workOrderRepository.findOptionalById(id).isPresent()) response = workOrderRepository.findOptionalById(id).get();
        else response = new WorkOrderDTO(workOrder, createdBy);
        if (workOrder.getRejectedBy() != null) {
            response.setReviewedBy(new UserLiteDTO(workOrder.getRejectedBy()));
        }

        List<FileStorage> fileStorageList = fileStorageRepository.findByRefIdAndStorageType(id, StorageType.WORK_ORDER_FILE);
        if (systemStorageType.equalsIgnoreCase("cloud")) {
            response.setAttachments(fileStorageList.stream().map(FileStorage::getSaveLocation).collect(Collectors.toList()));

        } else {
            response.setAttachments(fileStorageList.stream().map(fileStorage ->
                    fileStorageLocation + fileStorage.getSaveLocation()).collect(Collectors.toList()));
        }

        Set<Location> plants = new HashSet<>();

//        List<Long> moldIdList = workOrder.getWorkOrderAssets().stream()
//                .filter(workOrderAsset -> workOrderAsset.getType() == ObjectType.TOOLING)
//                .map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
//
//        List<Long> machineIdList = workOrder.getWorkOrderAssets().stream()
//                .filter(workOrderAsset -> workOrderAsset.getType() == ObjectType.MACHINE)
//                .map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
//
//        List<Long> terminalIdList = workOrder.getWorkOrderAssets().stream()
//                .filter(workOrderAsset -> workOrderAsset.getType() == ObjectType.TERMINAL)
//                .map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
//
//        List<Long> counterIdList = workOrder.getWorkOrderAssets().stream()
//                .filter(workOrderAsset -> workOrderAsset.getType() == ObjectType.COUNTER)
//                .map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
//
//        List<Location> locationMoldList = CollectionUtils.isEmpty(moldIdList)
//                ? Lists.newArrayList()
//                : moldRepository.findLocationByMoldIdIn(moldIdList);
//
//        List<Location> locationMachineList = CollectionUtils.isEmpty(machineIdList)
//                ? Lists.newArrayList()
//                : machineRepository.findAllLocationByMachineId(machineIdList);
//
//        List<Location> locationTerminalList = CollectionUtils.isEmpty(terminalIdList)
//                ? Lists.newArrayList()
//                : terminalRepository.findAllLocationByTerminalId(terminalIdList);
//
//        List<Location> locationCounterList = CollectionUtils.isEmpty(counterIdList)
//                ? Lists.newArrayList()
//                : counterRepository.findAllLocationByCounterId(counterIdList);
//
//        locationMoldList.addAll(locationMachineList);
//        locationMoldList.addAll(locationTerminalList);
//        locationMoldList.addAll(locationCounterList);

        procLocationInfo(response.getWorkOrderAssets(), plants);
        response.setPlantList(new ArrayList<>(plants));
        response.setWorkOrderCostList(workOrderCostRepository.findAllByWorkOrderId(id));
        response.setMoldMaintenance(workOrder.getMoldMaintenance());

        if (workOrder.getOrderType() == WorkOrderType.REFURBISHMENT) {
            if (workOrder.getEstimatedExtendedLifeShot() == null) {
                List<Long> moldIds = workOrder.getWorkOrderAssets().stream().filter(w -> w.getType().equals(ObjectType.TOOLING)).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
                List<Mold> molds = moldRepository.findAllById(moldIds);
                if (CollectionUtils.isNotEmpty(molds)) response.setEstimatedExtendedLifeShot((long) (molds.get(0).getDesignedShot() != null ? molds.get(0).getDesignedShot() : 0));
            } else {
                response.setEstimatedExtendedLifeShot(workOrder.getEstimatedExtendedLifeShot());
            }
        }
        return response;
    }

    private void procLocationInfo(List<WorkOrderAsset> assets, Set<Location> plants) {
        assets.forEach(a -> {
            Location location;
            if (ObjectType.TERMINAL.equals(a.getType())) {
                location = terminalRepository.findLocationByTerminalId(a.getAssetId()).orElse(null);
            } else if (ObjectType.MACHINE.equals(a.getType())) {
                location = machineRepository.findLocationByMachineId(a.getAssetId()).orElse(null);
            } else if (ObjectType.COUNTER.equals(a.getType())) {
                location = counterRepository.findLocationByCounterId(a.getAssetId()).orElse(null);
            } else {
                location = moldRepository.findLocationByMoldId(a.getAssetId()).orElse(null);
            }

            if (location != null) {
                a.setLocationId(location.getId());
                a.setLocationCode(location.getLocationCode());
                a.setLocationName(location.getName());

                plants.add(location);
            }
        });
    }

    @Transactional
    public ApiResponse completeWorkOrder(WorkOrderDTO workOrderDTO, MultipartFile[] costFiles, MultipartFile[] checklistFiles, MultipartFile[] powFiles) {
        if(costFiles != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_COST_FILE, workOrderDTO.getId(), costFiles));
        }
        if(checklistFiles != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_CHECKLIST_FILE, workOrderDTO.getId(), checklistFiles));
        }
        if(powFiles != null){
            fileStorageService.save(new FileInfo(StorageType.WORK_ORDER_POW_FILE, workOrderDTO.getId(), powFiles));
        }
        WorkOrder workOrder = workOrderRepository.getOne(workOrderDTO.getId());
        workOrderDTO.getWorkOrderAssets().forEach(workOrderAsset -> {
            workOrderAsset.setWorkOrder(workOrder);
            if (workOrderAsset.getType() == ObjectType.MACHINE) {
                Machine machine = machineRepository.getOne(workOrderAsset.getAssetId());
                workOrderAsset.setAssetCode(machine.getMachineCode());
            } else if (workOrderAsset.getType() == ObjectType.TOOLING) {
                Mold mold = moldRepository.getOne(workOrderAsset.getAssetId());
                workOrderAsset.setAssetCode(mold.getEquipmentCode());
            } else if (workOrderAsset.getType() == ObjectType.TERMINAL) {
                Terminal terminal = terminalRepository.getOne(workOrderAsset.getAssetId());
                workOrderAsset.setAssetCode(terminal.getEquipmentCode());
            } else if (workOrderAsset.getType() == ObjectType.COUNTER) {
                Counter counter = counterRepository.getOne(workOrderAsset.getAssetId());
                workOrderAsset.setAssetCode(counter.getEquipmentCode());
            }
        });

        if (workOrderDTO.getChecklistId() != null) {
            workOrderDTO.setChecklist(checklistRepository.getOne(workOrderDTO.getChecklistId()));
        }
        if (workOrderDTO.getPicklistId() != null) {
            workOrderDTO.setPicklist(checklistRepository.getOne(workOrderDTO.getPicklistId()));
        }
        if (workOrderDTO.getRefCode()!=null){
            workOrder.setRefCode(workOrderDTO.getRefCode());
        }

        List<WorkOrderCost> workOrderCostList = workOrderDTO.getWorkOrderCostList();
        workOrderCostList.forEach(workOrderCost -> workOrderCost.setWorkOrder(workOrder));
        workOrderCostRepository.deleteAllByWorkOrderId(workOrder.getId());
        workOrderCostRepository.saveAll(workOrderCostList);

        workOrderDTO.bidingData(workOrder);

        if(!workOrderDTO.isInProcessing()){
        if (workOrderDTO.getRequestApproval() != null && workOrderDTO.getRequestApproval()) {
            workOrder.setStatus(WorkOrderStatus.PENDING_APPROVAL);
            workOrder.setRequestApproval(true);
            saveWorkOrderUsers(workOrder, List.of(Objects.requireNonNull(SecurityUtils.getUserId())), WorkOrderParticipantType.APPROVAL_REQUESTER);
            //send notification to engineer in charge
//            notificationService.createWorkOrderCompleteRequestApprovalNotification(workOrder);
            notificationService.createWorkOrderCompletionApprovalRequestNotification(workOrder);
        } else {
            workOrder.setStatus(WorkOrderStatus.COMPLETED);
            if ( workOrder.getOrderType() == WorkOrderType.REFURBISHMENT && workOrderDTO.getEstimatedExtendedLifeShot() != null){
                List<Long> moldIds = workOrder.getWorkOrderAssets().stream().filter(w -> w.getType() == ObjectType.TOOLING).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
                List<Mold> molds = moldRepository.findAllById(moldIds);
                molds.forEach(m ->  m.setDesignedShot(workOrderDTO.getEstimatedExtendedLifeShot().intValue()));
                moldRepository.saveAll(molds);
            }
            if (workOrder.getOrderType()!=null && workOrder.getOrderType().equals(WorkOrderType.DISPOSAL)){
                List<Long> moldIds = workOrder.getWorkOrderAssets().stream().filter(w -> w.getType().equals(ObjectType.TOOLING)).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
                BatchUpdateDTO batchUpdateDTO = new BatchUpdateDTO();
                batchUpdateDTO.setIds(moldIds);
                batchUpdateDTO.setEnabled(true);//disposed must be enabled true
                List<Mold> molds = moldRepository.findAllById(moldIds);
                molds.forEach(m ->  m.setEquipmentStatus(EquipmentStatus.DISPOSED));
                moldRepository.saveAll(molds);
                moldService.changeStatusInBatch(batchUpdateDTO);

            }
            // notification
            notificationService.createWorkOrderCompletedNotification(workOrder);
        }
        }
        saveWorkOrderUsers(workOrder, List.of(Objects.requireNonNull(SecurityUtils.getUserId())), WorkOrderParticipantType.REPORTER);
        workOrderRepository.save(workOrder);


        if(!workOrderDTO.isInProcessing()){
//        if (workOrder.getOrderType().equals(WorkOrderType.PREVENTATIVE_MAINTENANCE)) {
            if (Arrays.asList(WorkOrderType.PREVENTATIVE_MAINTENANCE, WorkOrderType.INSPECTION, WorkOrderType.CORRECTIVE_MAINTENANCE).contains(workOrder.getOrderType())) {
                //complete mold maintenance
                completeMoldMaintenance(workOrder, costFiles);
                Mold mold = moldRepository.getOne(workOrder.getWorkOrderAssets().get(0).getAssetId());
                updateMaintenanceCost(mold, workOrderCostList);
                //copy the old costFiles pushed to the previous process
                fileStorageService.cloneFile(StorageType.WORK_ORDER_COST_FILE, workOrderDTO.getId(), StorageType.MOLD_MAINTENANCE, mold.getId());
            }
        }
        return ApiResponse.success();
    }

    public ApiResponse reviewCompletionRequestApproval(WorkOrderPayload payload) {
        WorkOrder workOrder = workOrderRepository.getOne(payload.getId());
        workOrder.setRejectedById(SecurityUtils.getUserId());
        workOrder.setRejectedBy(userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId())));
        workOrder.setRejectedReason(payload.getRejectedReason());

        WorkOrderUser requester = workOrder.getWorkOrderUsers().stream().filter(wou -> WorkOrderParticipantType.APPROVAL_REQUESTER.equals(wou.getParticipantType())).findFirst().orElse(null);
        WorkOrderUser reporter = workOrder.getWorkOrderUsers().stream().filter(wou -> WorkOrderParticipantType.REPORTER.equals(wou.getParticipantType())).findFirst().orElse(null);

        Long requesterId = requester != null ? requester.getUserId() : ( reporter != null ? reporter.getUserId() : null);

        if (ReviewWorkOrderAction.CANCELLED.equals(payload.getAction())) {
            workOrder.setStatus(WorkOrderStatus.CANCELLED);
            workOrder.setCompletedOn(DateUtils2.newInstant());
            //send notification
//            notificationService.createWorkOrderCancelApprovalNotification(workOrder, payload.getReportedBy());
            notificationService.createWorkOrderCompletionApprovalRequestReviewNotification(workOrder, requesterId, NotiCode.WO_CPT_CANCELLED);
        } else if (ReviewWorkOrderAction.REJECTED.equals(payload.getAction())){
            workOrder.setRequestApprovalEnabled(false);
            workOrder.setStatus(WorkOrderStatus.ACCEPTED);
            workOrder.setCompletedOn(null);
            //send notification
//            notificationService.createWorkOrderRejectApprovalNotification(workOrder, payload.getReportedBy());
            notificationService.createWorkOrderCompletionApprovalRequestReviewNotification(workOrder, requesterId, NotiCode.WO_CPT_REJECTED);
        } else if (ReviewWorkOrderAction.APPROVED.equals(payload.getAction())) {
            workOrder.setStatus(WorkOrderStatus.COMPLETED);
            if (workOrder.getOrderType()!=null && workOrder.getOrderType().equals(WorkOrderType.DISPOSAL)){
                List<Long> moldIds = workOrder.getWorkOrderAssets().stream().filter(w -> w.getType().equals(ObjectType.TOOLING)).map(WorkOrderAsset::getAssetId).collect(Collectors.toList());
                BatchUpdateDTO batchUpdateDTO = new BatchUpdateDTO();
                batchUpdateDTO.setIds(moldIds);
                batchUpdateDTO.setEnabled(true);//disposed must be enabled true
                List<Mold> molds = moldRepository.findAllById(moldIds);
                molds.forEach(m ->  m.setEquipmentStatus(EquipmentStatus.DISPOSED));
                moldRepository.saveAll(molds);
                moldService.changeStatusInBatch(batchUpdateDTO);
            }

            if (workOrder.getOrderType() == WorkOrderType.REFURBISHMENT && workOrder.getEstimatedExtendedLifeShot() != null) {

                List<Long> moldIds = workOrder.getWorkOrderAssets().stream()
                        .filter(w -> w.getType() == ObjectType.TOOLING)
                        .map(WorkOrderAsset::getAssetId).collect(Collectors.toList());

                List<Mold> molds = moldRepository.findAllById(moldIds);
                molds.forEach(m ->  m.setDesignedShot(workOrder.getEstimatedExtendedLifeShot().intValue()));
                moldRepository.saveAll(molds);
            }

            //send notification
//            notificationService.createWorkOrderApproveApprovalNotification(workOrder, payload.getReportedBy());
            notificationService.createWorkOrderCompletionApprovalRequestReviewNotification(workOrder, requesterId, NotiCode.WO_CPT_APPROVED);
        }

        workOrderRepository.save(workOrder);
        return ApiResponse.success();
    }

    //private function
    private void fillCountData(WorkOrderResponse response, SearchPayload searchPayload) {
        response.setTotal(workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicateTotalOngoing(), searchPayload.getAssetIds()));
        response.setOverdue(workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicateOverdue(), searchPayload.getAssetIds()));
        response.setTotalHistory(workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicateTotalHistory(), searchPayload.getAssetIds()));
        boolean isHistory = searchPayload.getIsHistory()!=null?searchPayload.getIsHistory():false;
        for (WorkOrderType workOrderType: WorkOrderType.values()) {
            searchPayload.setIsHistory(false);
            searchPayload.setOrderType(Collections.singletonList(workOrderType));
            long count = workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicate(), searchPayload.getAssetIds());
            switch (workOrderType) {
                case GENERAL:
                    response.setGeneral(count);
                    break;
                case EMERGENCY:
                    response.setEmergency(count);
                    break;
                case INSPECTION:
                    response.setInspection(count);
                    break;
                case PREVENTATIVE_MAINTENANCE:
                    response.setPm(count);
                    break;
                case CORRECTIVE_MAINTENANCE:
                    response.setCm(count);
                    break;
                case REFURBISHMENT:
                    response.setRefurbishment(count);
                    break;
                case DISPOSAL:
                    response.setDisposal(count);
                    break;
            }
        }
        if (searchPayload.getIsPmWorkOrder()!=null&& searchPayload.getIsPmWorkOrder()){
            response.setCm(0L);
            response.setInspection(0L);
            if (!isHistory)
                response.setTotalHistory(0L);
        }
        WorkOrderSummary pmSummary = getPmSummary(searchPayload);
        response.setPmSummary(pmSummary);
    }

    private WorkOrderSummary getPmSummary(SearchPayload searchPayload) {
        WorkOrderSummary result = new WorkOrderSummary();
        result.setType(WorkOrderType.PREVENTATIVE_MAINTENANCE);
        searchPayload.setOrderType(Collections.singletonList(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        //total
        searchPayload.setIsHistory(null);
        searchPayload.setStatus(null);
        long total = workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicateTotal(), searchPayload.getAssetIds());
        result.setTotal(total);

        //open and overdue
        searchPayload.setIsHistory(false);
        searchPayload.setStatus("open");
        long open = workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicate(), searchPayload.getAssetIds());
        result.setOpen(open);

        searchPayload.setStatus("overdue");
        long overdue = workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicate(), searchPayload.getAssetIds());
        result.setOverdue(overdue);

        //completed
        searchPayload.setIsHistory(true);
        searchPayload.setStatus("completed");
        long completed = workOrderRepository.countByPredicateAndAssetIdsIn(searchPayload.getPredicate(), searchPayload.getAssetIds());
        result.setCompleted(completed);

        return result;
    }

    public void loadWorkOrderData(List<MaintenanceData> data) {
        data.forEach(d -> {
            MoldMaintenance moldMaintenance = moldMaintenanceRepository.getOne(d.getId());
            if (moldMaintenance.getWorkOrderId() != null) {
                d.setWorkOrder(getWorkOrderDetailDTO(moldMaintenance.getWorkOrderId()));
            }
            //fake data test
//            else {
//                if (data.indexOf(d) % 2 == 0)
//                    d.setWorkOrder(getWorkOrderDetailDTO(workOrderRepository.findFirstByStatusAndWorkOrderIdContainsAndEndAfter(WorkOrderStatus.REQUESTED, "W", Instant.now()).getId()));
//                else
//                    d.setWorkOrder(getWorkOrderDetailDTO(workOrderRepository.findFirstByStatusAndWorkOrderIdContainsAndEndBefore(WorkOrderStatus.REQUESTED, "W", Instant.now()).getId()));
//            }
        });
    }

    public void generateWorkOrder(MoldMaintenance moldMaintenance) {
        Long moldId = moldMaintenance.getMold() != null ? moldMaintenance.getMold().getId() : moldMaintenance.getMoldId();
        WorkOrderPayload payload = new WorkOrderPayload();
        payload.setWorkOrderId(generateWorkOrderId());
        payload.setOrderType(WorkOrderType.PREVENTATIVE_MAINTENANCE);
        payload.setDetails("Automatically generated Preventative Maintenance work order.\nDue date is based on eMoldino’s PM Checkpoint prediction.");
        payload.setPriority(PriorityType.MEDIUM);
        payload.setMoldIds(Collections.singletonList(moldId));
        payload.setCost(true);
        payload.setPickingList(true);
        payload.setStart(Instant.now());
        payload.setEnd(Instant.now().plusSeconds(moldMaintenance.getDueDate() != null ? moldMaintenance.getDueDate() * 24 * 60 * 60 : 0));


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

        WorkOrder result = saveNormalWorkOrder(workOrder, payload);

        if (moldId != null) {
            List<FileStorage> moldMaintenanceDocument = fileStorageRepository.findByRefIdAndStorageType(moldId, StorageType.MOLD_MAINTENANCE_DOCUMENT);
            List<FileStorage> listToSave = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(moldMaintenanceDocument)) {
                moldMaintenanceDocument.forEach(d -> {
                    FileStorage fileStorage = new FileStorage();
                    fileStorage.setRefId(d.getRefId());
                    fileStorage.setRefId2(d.getRefId2());
                    fileStorage.setFileName(d.getFileName());
                    fileStorage.setFileSize(d.getFileSize());
                    fileStorage.setSaveLocation(d.getSaveLocation());
                    fileStorage.setStorageType(StorageType.WORK_ORDER_FILE);

                    listToSave.add(fileStorage);
                });
            }
            fileStorageRepository.saveAll(listToSave);
        }
        if (moldMaintenance.getMold() != null && CollectionUtils.isNotEmpty(moldMaintenance.getMold().getEngineersInCharge())) {
            List<WorkOrderUser> workOrderUsers = moldMaintenance.getMold().getEngineersInCharge().stream().map(e -> {
            WorkOrderUser workOrderUser = new WorkOrderUser();
            workOrderUser.setWorkOrderId(result.getId());
            workOrderUser.setWorkOrder(result);
            workOrderUser.setUser(e);
            workOrderUser.setUserId(e.getId());
            workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
            return workOrderUser;
            }).collect(Collectors.toList());
            workOrderUserRepository.saveAll(workOrderUsers);
        }
        else{
            User user = userRepository.findByEmailAndDeletedIsFalse("support@emoldino.com").orElse(null);
            if (user !=null) {
                WorkOrderUser workOrderUser = new WorkOrderUser();
                workOrderUser.setWorkOrderId(result.getId());
                workOrderUser.setWorkOrder(result);
                workOrderUser.setUser(user);
                workOrderUser.setUserId(user.getId());
                workOrderUser.setParticipantType(WorkOrderParticipantType.CREATOR);
                workOrderUserRepository.save(workOrderUser);
            }
        }

        moldMaintenance.setWorkOrderId(result.getId());
        moldMaintenance.setWorkOrder(result);
        moldMaintenanceRepository.save(moldMaintenance);
    }

    public void generateMoldMaintenance(WorkOrder workOrder, Long moldId) {
        Mold mold = moldRepository.getOne(moldId);
        List<MoldMaintenance> finalLastMoldMaintenanceList =
                moldMaintenanceRepository.findAllByMoldIdAndMaintenanceStatusIsInAndLatestOrderByIdDesc(moldId,
                Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);
        if (finalLastMoldMaintenanceList != null && finalLastMoldMaintenanceList.size() > 0) {
            MoldMaintenance finalLastMoldMaintenance = finalLastMoldMaintenanceList.get(0);
            finalLastMoldMaintenance.setLatest(false);
            moldMaintenanceRepository.save(finalLastMoldMaintenance);
        }

        Integer scLastMaintenance = moldService.getScLastMaintenance(mold.getId());

        int moldLastShot = mold.getLastShot();
        int numShotFromMaintenance = (moldLastShot - scLastMaintenance);
        int maintenanceCount = numShotFromMaintenance / mold.getPreventCycle();

        //for over period
        int periodOverdue = 0;
        if (maintenanceCount > 0 && numShotFromMaintenance % mold.getPreventCycle() == 0 ) {
            maintenanceCount--;
        }
        int period = mold.getPreventCycle() * (maintenanceCount + 1) + scLastMaintenance;
        int periodStart = period - mold.getPreventUpcoming();
        int periodEnd = period + periodOverdue;


        mold.setMaintenanced(true);
        MoldMaintenance maintenance = new MoldMaintenance();
        maintenance.setMold(mold);
        maintenance.setAccumulatedShot(moldLastShot);
        maintenance.setMaintenanceStatus(MaintenanceStatus.UPCOMING);
        maintenance.setPreventCycle(mold.getPreventCycle());
        maintenance.setPreventUpcoming(mold.getPreventUpcoming());
        maintenance.setPeriodStart(periodStart);
        maintenance.setPeriodEnd(periodEnd);
        maintenance.setLatest(true);
        maintenance.setWorkOrderId(workOrder.getId());
        maintenance.setWorkOrder(workOrder);

        moldMaintenanceRepository.save(maintenance);
        moldService.procMaintenanceDueDate(maintenance, scLastMaintenance);

        workOrder.setMoldMaintenanceId(maintenance.getId());
        workOrder.setMoldMaintenance(maintenance);
        workOrderRepository.save(workOrder);
    }
@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelOldWorkOrder(Long moldId) {
        Optional<WorkOrder> optionalOldWorkOrder = workOrderRepository.findFirstByStatusNotInAndAssetIdAndOrderType(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED), moldId, WorkOrderType.PREVENTATIVE_MAINTENANCE);
        if (optionalOldWorkOrder.isPresent()) {
            //cancel old work order
            WorkOrder old = optionalOldWorkOrder.get();
            old.setStatus(WorkOrderStatus.CANCELLED);
            old.setCompletedOn(DateUtils2.newInstant());
            workOrderRepository.save(old);
        }
    }

    public long existMaintenanceWorkOrder(Long moldId) {
        return workOrderRepository.countByOrderTypeAndAssetIdAndAssetType(WorkOrderType.PREVENTATIVE_MAINTENANCE, moldId, ObjectType.TOOLING);
    }

    private void completeMoldMaintenance(WorkOrder workOrder, MultipartFile[] files) {
        if (workOrder.getMoldMaintenanceId() == null) return;

        MoldPayload payload = new MoldPayload();
        payload.setMessage(workOrder.getChecklistItemStr());
        payload.setMaintenanceStartTime(workOrder.getStartedOn() !=null ?workOrder.getStartedOn().getEpochSecond():workOrder.getStart().getEpochSecond());
        payload.setMaintenanceEndTime(workOrder.getCompletedOn() !=null ?workOrder.getCompletedOn().getEpochSecond():DateUtils2.newInstant().getEpochSecond());
        payload.setId(workOrder.getMoldMaintenanceId());
        payload.setFiles(files);

        moldService.completeMoldMaintenance(payload);
    }

    private void updateMaintenanceCost(Mold mold, List<WorkOrderCost> costs) {
        Integer totalCost = costs.stream().filter(c -> c != null && c.getCost() != null).mapToInt(c -> c.getCost().intValue()).sum();
        mold.setAccumulatedMaintenanceCost(mold.getAccumulatedMaintenanceCost() == null ? totalCost : (mold.getAccumulatedMaintenanceCost() + totalCost));
    }

    public void exportWorkOrder(HttpServletResponse response, ExportWorkOrderPayload payload) throws IOException {
        generateRageTimeFromTime(payload);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
             OutputStream outputStream = response.getOutputStream();) {

            String firstAssesCode = "";
            int numberAsset = 0;
            if(CollectionUtils.isNotEmpty(payload.getMoldIds())) {
                numberAsset+=payload.getMoldIds().size();
                Mold mold = moldRepository.getOne(payload.getMoldIds().get(0));
                firstAssesCode = mold.getEquipmentCode();
                for (Long assetId : payload.getMoldIds()) {
                    exportAssetWorkOrder(assetId, ObjectType.TOOLING, zipOutputStream, payload, response);
                }
            }

            if(CollectionUtils.isNotEmpty(payload.getMachineIds())) {
                numberAsset+=payload.getMachineIds().size();
                if(StringUtils.isEmpty(firstAssesCode)) {
                    Machine machine = machineRepository.getOne(payload.getMachineIds().get(0));
                    firstAssesCode = machine.getMachineCode();
                }
                for (Long assetId : payload.getMachineIds()) {
                    exportAssetWorkOrder(assetId, ObjectType.MACHINE, zipOutputStream, payload, response);
                }
            }

            if(CollectionUtils.isNotEmpty(payload.getTerminalIds())) {
                numberAsset+=payload.getTerminalIds().size();
                if(StringUtils.isEmpty(firstAssesCode)) {
                    Terminal terminal = terminalRepository.getOne(payload.getTerminalIds().get(0));
                    firstAssesCode = terminal.getEquipmentCode();
                }
                for (Long assetId : payload.getTerminalIds()) {
                    exportAssetWorkOrder(assetId, ObjectType.TERMINAL, zipOutputStream, payload, response);
                }
            }

            if(CollectionUtils.isNotEmpty(payload.getCounterIds())) {
                numberAsset+=payload.getCounterIds().size();
                if(StringUtils.isEmpty(firstAssesCode)) {
                    Counter counter = counterRepository.getOne(payload.getCounterIds().get(0));
                    firstAssesCode = counter.getEquipmentCode();
                }
                for (Long assetId : payload.getCounterIds()) {
                    exportAssetWorkOrder(assetId, ObjectType.COUNTER, zipOutputStream, payload, response);
                }
            }


            zipOutputStream.close();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename="+firstAssesCode+"+"+(numberAsset-1)+"_WO_"+payload.getDisplayTime()+".zip");
            outputStream.write(byteArrayOutputStream.toByteArray());
        }
    }

    private void exportAssetWorkOrder(Long assetId, ObjectType assessType, ZipOutputStream zipOutputStream, ExportWorkOrderPayload payload, HttpServletResponse response) {
        try {

            InputStream fileTemplate = ExcelUtils.getFileTemplateExcel("dynamic/TemplateWorkOrderExport.xlsx");
            XSSFWorkbook templateWorkbook = new XSSFWorkbook(fileTemplate);
            Sheet templateSheet = templateWorkbook.getSheetAt(0);
            Workbook wb = new SXSSFWorkbook(1000);
            String assetCode = "";
            switch (assessType) {
                case TOOLING: {
                    Mold mold = moldRepository.getOne(assetId);
                    assetCode = mold.getEquipmentCode();
                    break;
                }
                case MACHINE: {
                    Machine machine = machineRepository.getOne(assetId);
                    assetCode = machine.getMachineCode();
                    break;
                }
                case TERMINAL: {
                    Terminal terminal = terminalRepository.getOne(assetId);
                    assetCode = terminal.getEquipmentCode();
                    break;
                }
                case COUNTER: {
                    Counter counter = counterRepository.getOne(assetId);
                    assetCode = counter.getEquipmentCode();
                    break;
                }
            }

            if (payload.isAllTypes()) {
                ExportWorkOrderPayload clonePayload = getPayloadToSearch(payload, assetId, assessType);
                List<WorkOrderDTO> workOrderDTOList = getWorkOrderList2Export(clonePayload);
                addDataToSheet(workOrderDTOList, wb, templateSheet, assessType, assetCode, payload.getDisplayFullTime(), null);
            } else {
                for (WorkOrderType workOrderType : payload.getTypeList()) {
                    ExportWorkOrderPayload clonePayload = getPayloadToSearch(payload, assetId, assessType);
                    clonePayload.setTypeList(Collections.singletonList(workOrderType));
                    List<WorkOrderDTO> workOrderDTOList = getWorkOrderList2Export(clonePayload);
                    addDataToSheet(workOrderDTOList, wb, templateSheet, assessType, assetCode, payload.getDisplayFullTime(), workOrderType);
                }
            }

            if (!payload.isSingleFile()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                wb.write(bos);
                byte[] byteArray = bos.toByteArray();
                ZipEntry zipEntry = new ZipEntry(assetCode+"_WO_"+payload.getDisplayTime()+".xlsx");
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(byteArray);
                zipOutputStream.flush();
            } else {
                HttpUtils.respondWorkbook(wb, assetCode+"_WO_"+payload.getDisplayTime()+".xlsx", response);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<WorkOrderDTO> getWorkOrderList2Export(ExportWorkOrderPayload payload) {
        List<WorkOrderDTO> workOrderDTOList = workOrderRepository.getWorkOrderListForExport(payload.getPredicateExport());
        List<Long> workOrderIdList = workOrderDTOList.stream().map(WorkOrderDTO::getId).collect(Collectors.toList());
        List<WorkOrderCost> workOrderCostList = workOrderCostRepository.findAllByWorkOrderIdIn(workOrderIdList);
        Map<Long, List<WorkOrderCost>> workOrderCostMap = workOrderCostList.stream().collect(Collectors.groupingBy(WorkOrderCost::getWorkOrderId));
        workOrderDTOList.forEach(workOrderDTO -> {
            List<WorkOrderCost> workOrderCosts = workOrderCostMap.get(workOrderDTO.getId());
            if (CollectionUtils.isNotEmpty(workOrderCosts)) {
                workOrderDTO.setWorkOrderCostList(workOrderCosts);
            }
        });
        return workOrderDTOList;
    }

    private void mapLocationToWorkOrder(List<WorkOrderDTO> workOrderList) {
        List<Long> workOrderIdList = workOrderList.stream().map(WorkOrderDTO::getId).collect(Collectors.toList());
        List<WorkOrderAsset> workOrderAssetList = workOrderAssetRepository.findAllByWorkOrderIdIn(workOrderIdList);
        List<Long> moldIdList = Lists.newArrayList();
        List<Long> counterdIdList = Lists.newArrayList();
        List<Long> terminalIdList = Lists.newArrayList();
        List<Long> machineIdList = Lists.newArrayList();
        workOrderAssetList.forEach(workOrderAsset -> {
            if(workOrderAsset.getType() == ObjectType.TOOLING) {
                moldIdList.add(workOrderAsset.getAssetId());
            }
            if(workOrderAsset.getType() == ObjectType.COUNTER) {
                counterdIdList.add(workOrderAsset.getAssetId());
            }
            if(workOrderAsset.getType() == ObjectType.TERMINAL) {
                terminalIdList.add(workOrderAsset.getAssetId());
            }
            if(workOrderAsset.getType() == ObjectType.MACHINE) {
                machineIdList.add(workOrderAsset.getAssetId());
            }
        });

        List<Mold> moldList = moldRepository.findAllById(moldIdList);
        List<Counter> counterList = counterRepository.findAllById(counterdIdList);
        List<Terminal> terminalList = terminalRepository.findAllById(terminalIdList);
        List<Machine> machineList = machineRepository.findAllById(machineIdList);

        workOrderList.forEach(workOrderDTO -> {
            List<WorkOrderAsset> workOrderAssetListCheck = workOrderAssetList.stream()
                    .filter(workOrderAsset -> workOrderAsset.getWorkOrderId().equals(workOrderDTO.getId()))
                    .collect(Collectors.toList());
            List<Location> locationList = Lists.newArrayList();
            workOrderAssetListCheck.forEach(workOrderAsset -> {
                if(workOrderAsset.getType() == ObjectType.TOOLING) {
                    moldList.stream()
                            .filter(mold -> mold.getId().equals(workOrderAsset.getAssetId()))
                            .findFirst()
                            .ifPresent(mold -> locationList.add(mold.getLocation()));

                }
                if(workOrderAsset.getType() == ObjectType.COUNTER) {
                    counterList.stream()
                            .filter(counter -> counter.getId().equals(workOrderAsset.getAssetId()))
                            .findFirst()
                            .ifPresent(counter -> locationList.add(counter.getLocation()));
                }
                if(workOrderAsset.getType() == ObjectType.TERMINAL) {
                    terminalList.stream()
                            .filter(terminal -> terminal.getId().equals(workOrderAsset.getAssetId()))
                            .findFirst()
                            .ifPresent(terminal -> locationList.add(terminal.getLocation()));
                }
                if(workOrderAsset.getType() == ObjectType.MACHINE) {
                    machineList.stream()
                            .filter(machine -> machine.getId().equals(workOrderAsset.getAssetId()))
                            .findFirst()
                            .ifPresent(machine -> locationList.add(machine.getLocation()));
                }
            });

            workOrderDTO.setPlantList(locationList);
        });
    }

    private ExportWorkOrderPayload getPayloadToSearch(ExportWorkOrderPayload payload,Long assetId, ObjectType assessType) {
        ExportWorkOrderPayload clonePayload = payload.cloneSearchObject();
        switch (assessType) {
            case TOOLING: {
                clonePayload.setMoldIds(Collections.singletonList(assetId));
                break;
            }
            case MACHINE: {
                clonePayload.setMachineIds(Collections.singletonList(assetId));
                break;
            }
            case TERMINAL: {
                clonePayload.setTerminalIds(Collections.singletonList(assetId));
                break;
            }
            case COUNTER: {
                clonePayload.setCounterIds(Collections.singletonList(assetId));
                break;
            }
        }
        return clonePayload;
    }

    private void addDataToSheet(List<WorkOrderDTO> workOrderList, Workbook wb, Sheet templateSheet, ObjectType assessType, String assetName,String timeRange, WorkOrderType workOrderType) {
        mapLocationToWorkOrder(workOrderList);
        Map<String, List<WorkOrderDTO>> workOrderMap = workOrderList.stream().collect(Collectors.groupingBy(WorkOrderDTO::getWorkOrderId));

        final int COLUMN_WIDTH_LARGE = 4000;
        Sheet sheet = wb.createSheet(workOrderType == null ? "All types" : workOrderType.getTitle());

        CellStyle headerLabelStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 0);
        CellStyle headerValueStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 1);

        CellStyle headerTableStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 4, 0);
        CellStyle dataTableStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 5, 0);

        ExcelCommonUtils.setFullBorderStyle(headerTableStyle);
        ExcelCommonUtils.setFullBorderStyle(dataTableStyle);

        sheet.setColumnWidth(0, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(1, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(2, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(3, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(4, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(5, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(6, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(7, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(8, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(9, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(10, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(11, COLUMN_WIDTH_LARGE);
        sheet.setColumnWidth(12, COLUMN_WIDTH_LARGE);

        ExcelCommonUtils.writeCellValue(sheet, 0, 0, headerLabelStyle, assessType.getTitle()+ " ID" );
        ExcelCommonUtils.writeCellValue(sheet, 0, 1, headerValueStyle, assetName);
        ExcelCommonUtils.writeCellValue(sheet, 1, 0, headerLabelStyle, "Order Type");
        ExcelCommonUtils.writeCellValue(sheet, 1, 1, headerValueStyle, workOrderType == null ? "All types" : workOrderType.getTitle());
        ExcelCommonUtils.writeCellValue(sheet, 2, 0, headerLabelStyle, "Time Range");
        ExcelCommonUtils.writeCellValue(sheet, 2, 1, headerValueStyle, timeRange);


        ExcelCommonUtils.writeCellValue(sheet, 4, 0, headerTableStyle, "Work Order ID");
        ExcelCommonUtils.writeCellValue(sheet, 4, 1, headerTableStyle, "Assigned To");
        ExcelCommonUtils.writeCellValue(sheet, 4, 2, headerTableStyle, "Started On");
        ExcelCommonUtils.writeCellValue(sheet, 4, 3, headerTableStyle, "Completed On");
        ExcelCommonUtils.writeCellValue(sheet, 4, 4, headerTableStyle, "Total Time");
        ExcelCommonUtils.writeCellValue(sheet, 4, 5, headerTableStyle, "Asset(s)");
        ExcelCommonUtils.writeCellValue(sheet, 4, 6, headerTableStyle, "Plant(s)");
        ExcelCommonUtils.writeCellValue(sheet, 4, 7, headerTableStyle, "Order Type");
        ExcelCommonUtils.writeCellValue(sheet, 4, 8, headerTableStyle, "Priority");
        ExcelCommonUtils.writeCellValue(sheet, 4, 9, headerTableStyle, "Details");
        ExcelCommonUtils.writeCellValue(sheet, 4, 10, headerTableStyle, "Checklist");
        ExcelCommonUtils.writeCellValue(sheet, 4, 11, headerTableStyle, "Cost");
        ExcelCommonUtils.writeCellValue(sheet, 4, 12, headerTableStyle, "Pickling List");

        int startDataIndex = 5;
        for (Map.Entry<String, List<WorkOrderDTO>> entry : workOrderMap.entrySet()) {
            WorkOrderDTO workOrder = entry.getValue().get(0);
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 0, dataTableStyle, Strings.nullToEmpty(workOrder.getWorkOrderId()));

            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 1, dataTableStyle, Strings.nullToEmpty(workOrder.getWorkOrderUsers()
                    .stream().map(workOrderUser -> workOrderUser.getUser().getName())
                    .collect(Collectors.joining(","))));

            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 2, dataTableStyle, workOrder.getStartedOn() == null ? "" : DateUtils2.format(workOrder.getStartedOn(), DateUtils.YYYY_MM_DD_HH_MM, DateUtils2.Zone.SYS));

            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 3, dataTableStyle, workOrder.getCompletedOn() == null ? "" : DateUtils2.format(workOrder.getCompletedOn(), DateUtils.YYYY_MM_DD_HH_MM, DateUtils2.Zone.SYS));

            String duration = "";
            if(workOrder.getStartedOn() != null && workOrder.getCompletedOn() != null) {
                duration = workOrder.getStartedOn().equals(workOrder.getCompletedOn()) ? "0 minute" : DateUtils.getDurationDate(workOrder.getStartedOn(), workOrder.getCompletedOn());
            }
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 4, dataTableStyle, duration);
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 5, dataTableStyle, String.join(", ", workOrder.getWorkOrderAssets().stream().map(WorkOrderAsset::getAssetCode).filter(Objects::nonNull).collect(Collectors.toSet())));

            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 6, dataTableStyle, String.join(", ", workOrder.getPlantList().stream().filter(Objects::nonNull).map(Location::getName).collect(Collectors.toSet())));
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 7, dataTableStyle, Strings.nullToEmpty(workOrder.getOrderType().getTitle()));
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 8, dataTableStyle, Strings.nullToEmpty(workOrder.getPriority().getTitle()));
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 9, dataTableStyle, Strings.nullToEmpty(workOrder.getDetails()));
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 10, dataTableStyle,workOrder.getChecklistItemStr() == null ? "" : "-"+workOrder.getChecklistItemStr().replace("\n", "\n-"));
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 11, dataTableStyle, CollectionUtils.isEmpty(workOrder.getWorkOrderCostList()) ? "" : workOrder.getWorkOrderCostList().stream()
                    .filter(workOrderCost -> workOrderCost.getCost() != null || workOrderCost.getDetails() != null)
                    .map(workOrderCost -> workOrderCost.getCost() == null ? "" : workOrderCost.getDetails()).collect(Collectors.joining("\n")));
            ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 12, dataTableStyle, workOrder.getPicklistItemStr() == null ? "" : "-"+workOrder.getPicklistItemStr().replace("\n", "\n-"));
            startDataIndex++;
        }


    }





    private static void generateRageTimeFromTime(ExportWorkOrderPayload exportPayload) {
        if (exportPayload.getRangeType() == RangeType.CUSTOM_RANGE) {
            exportPayload.setStartTime(DateUtils2.toInstant(exportPayload.getFromDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils2.Zone.SYS));
            exportPayload.setEndTime(DateUtils.getEndOffDay(DateUtils2.toInstant(exportPayload.getToDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils2.Zone.SYS)));
            return;
        }
        String time = exportPayload.getTime();
        Calendar calendar = DateUtils.getCalendar();
        int year = Integer.parseInt(time.substring(0, 4));
        Instant fromDate = Instant.now();
        Instant toDate = Instant.now();
        if (exportPayload.getRangeType().equals(RangeType.WEEKLY)) {

            int weekValue = Integer.parseInt(time.substring(4, 6));
            calendar.setWeekDate(year, weekValue, Calendar.SUNDAY);
            fromDate = calendar.toInstant();
            calendar.add(Calendar.DATE, 6);
            toDate = calendar.toInstant();
        } else if (exportPayload.getRangeType().equals(RangeType.MONTHLY)) {

            fromDate = DateUtils.getInstant(time + "01000000", DateUtils.DEFAULT_DATE_FORMAT);
            calendar.setTimeInMillis(fromDate.toEpochMilli());
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            toDate = calendar.toInstant();
        }
        if (exportPayload.getRangeType().equals(RangeType.YEARLY)) {
//            timeRange = "Year " + time;
            fromDate = DateUtils.getInstant(time + "0101000000", DateUtils.DEFAULT_DATE_FORMAT);
            calendar.setTimeInMillis(fromDate.toEpochMilli());
            calendar.add(Calendar.YEAR, 1);
            calendar.add(Calendar.DATE, -1);
            toDate = calendar.toInstant();
        }
        exportPayload.setStartTime(fromDate);
        exportPayload.setEndTime(toDate);
    }


    private List<WorkOrderAsset>  genWorkOrderAssets(WorkOrder order, List<Long> moldIds, List<Long> machineIds
        , List<Long> terminalIds, List<Long> counterIds) {
        List<WorkOrderAsset> assets = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(moldIds)) {
            moldIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                    .workOrderId(order.getId())
                    .workOrder(order)
                    .assetId(id)
                    .assetCode(moldRepository.getOne(id).getEquipmentCode())
                    .type(ObjectType.TOOLING)
                    .build();
                assets.add(asset);
            });
        }
        if (CollectionUtils.isNotEmpty(machineIds)) {
            machineIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                    .workOrderId(order.getId())
                    .workOrder(order)
                    .assetId(id)
                    .assetCode(machineRepository.getOne(id).getMachineCode())
                    .type(ObjectType.MACHINE)
                    .build();
                assets.add(asset);
            });
        }
        if (CollectionUtils.isNotEmpty(terminalIds)) {
            terminalIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                    .workOrderId(order.getId())
                    .workOrder(order)
                    .assetId(id)
                    .assetCode(terminalRepository.getOne(id).getEquipmentCode())
                    .type(ObjectType.TERMINAL)
                    .build();
                assets.add(asset);
            });
        }
        if (CollectionUtils.isNotEmpty(counterIds)) {
            counterIds.forEach(id -> {
                WorkOrderAsset asset = WorkOrderAsset.builder()
                    .workOrderId(order.getId())
                    .workOrder(order)
                    .assetId(id)
                    .assetCode(counterRepository.getOne(id).getEquipmentCode())
                    .type(ObjectType.COUNTER)
                    .build();
                assets.add(asset);
            });
        }
        return assets;
    }

    private void loadMaintenanceData(List<WorkOrderDTO> workOrderDTOList) {
        workOrderDTOList.forEach(wo -> {
            if (WorkOrderType.PREVENTATIVE_MAINTENANCE.equals(wo.getOrderType())) {
                WorkOrderAsset assetMold = wo.getWorkOrderAssets().stream().filter(wa -> ObjectType.TOOLING.equals(wa.getType())).findFirst().orElse(null);
                if (assetMold != null) {
                    BooleanBuilder builder = new BooleanBuilder(Q.mold.id.eq(assetMold.getAssetId()).and(Q.mold.toolingStatus.in(ToolingStatus.values())));
                    Page<Mold> page = moldRepository.findAllByMasterFilter(builder, ActiveStatus.ENABLED, PageRequest.of(0,1));
                    Mold mold = CollectionUtils.isEmpty(page.getContent()) ? null : page.getContent().get(0);
                    if (mold != null) {
                        wo.setPreventCycle(mold.getPreventCycle());
                        wo.setPmStrategy(mold.getPmStrategy());
                        wo.setPmCheckpoint(mold.getPmCheckpoint());
                        wo.setPmCheckpointPrediction(mold.getPmCheckpointPrediction());
                        wo.setUntilNextPm(mold.getUntilNextPm());
                    }
                }
            }
        });
    }

    public ApiResponse getRequestChangeWorkOrder(Long originId) {
        WorkOrder workOrder = workOrderRepository.findFirstByOriginalIdOrderByIdDesc(originId);
        return getWorkOrderDetail(workOrder.getId());
    }
}
