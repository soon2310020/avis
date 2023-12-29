package saleson.api.dataRequest;

import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;
import com.emoldino.api.common.resource.base.id.util.IdUtils;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.company.CompanyRepository;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.configuration.GeneralConfigRepository;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.dataRequest.payload.*;
import saleson.api.location.LocationRepository;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.MachineRepository;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.notification.NotificationService;
import saleson.api.part.PartRepository;
import saleson.api.part.payload.PartPayload;
import saleson.api.user.UserRepository;
import saleson.api.user.payload.UserParam;
import saleson.common.enumeration.*;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.*;
import saleson.model.config.GeneralConfig;
import saleson.model.data.completionRate.AvgCompletionRateData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.service.util.NumberUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DataRequestService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    private DataRequestRepository dataRequestRepository;

    @Autowired
    private DataRequestObjectRepository dataRequestObjectRepository;

    @Autowired
    private DataRequestUserRepository dataRequestUserRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private MoldRepository moldRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private GeneralConfigRepository generalConfigRepository;

    @Autowired
    private NotificationService notificationService;

    public AllDataRequest getDataRequest(DataRequestPayload payload, Pageable pageable) {

        Page<DataRequest> dataRequestPage = dataRequestRepository.findAll(payload.getPredicate(), pageable);

        DataRequestPayload payloadCountDataRequest = new DataRequestPayload();
        payloadCountDataRequest.setType("DATA_REGISTRATION");
        DataRequestPayload payloadCountDataCompletion = new DataRequestPayload();
        payloadCountDataCompletion.setType("DATA_COMPLETION");

        return AllDataRequest.builder()
                .data(dataRequestPage)
                .allCount(dataRequestRepository.count(new DataRequestPayload().getPredicate()))
                .dataRegistrationCount(dataRequestRepository.count(payloadCountDataRequest.getPredicate()))
                .dataCompletionCount(dataRequestRepository.count(payloadCountDataCompletion.getPredicate()))
                .build();
    }

    public DataRequestDetail getDetailDataRequest(Long id) throws ExecutionException, InterruptedException {
        DataRequest dataRequest = dataRequestRepository.getOne(id);
        DataRequestDetail response = new DataRequestDetail(dataRequest);

        if (dataRequest.getRequestDataType() == RequestDataType.DATA_COMPLETION) {
            response.setIsSelectedCompany(dataRequestObjectRepository.existsByDataRequestIdAndObjectType(id, ObjectType.COMPANY));
            response.setIsSelectedMachine(dataRequestObjectRepository.existsByDataRequestIdAndObjectType(id, ObjectType.MACHINE));
            response.setIsSelectedLocation(dataRequestObjectRepository.existsByDataRequestIdAndObjectType(id, ObjectType.LOCATION));
            response.setIsSelectedMold(dataRequestObjectRepository.existsByDataRequestIdAndObjectType(id, ObjectType.TOOLING));
            response.setIsSelectedPart(dataRequestObjectRepository.existsByDataRequestIdAndObjectType(id, ObjectType.PART));

            NumberIncompleteData incompleteData = getNumberIncompleteData(id);

            Long allCompanyNumberCompletion = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.COMPANY);
            response.setCompanyNumber(allCompanyNumberCompletion.intValue());
            response.setCompanyDoneNumber((int) (allCompanyNumberCompletion - incompleteData.getCompanyNumber()));

            Long allLocationNumberCompletion = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.LOCATION);
            response.setLocationNumber(allLocationNumberCompletion.intValue());
            response.setLocationDoneNumber((int) (allLocationNumberCompletion.intValue() - incompleteData.getLocationNumber()));

            Long allMachineNumberCompletion = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.MACHINE);
            response.setMachineNumber(allMachineNumberCompletion.intValue());
            response.setMachineDoneNumber((int) (allMachineNumberCompletion.intValue() - incompleteData.getMachineNumber()));

            Long allMoldNumberCompletion = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.TOOLING);
            response.setMoldNumber(allMoldNumberCompletion.intValue());
            response.setMoldDoneNumber((int) (allMoldNumberCompletion.intValue() - incompleteData.getMoldNumber()));

            Long allPartNumberCompletion = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.PART);
            response.setPartNumber(allPartNumberCompletion.intValue());
            response.setPartDoneNumber((int) (allPartNumberCompletion.intValue() - incompleteData.getPartNumber()));



        } else {

            response.setCompanyDoneNumber((int)dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.COMPANY));
            response.setLocationDoneNumber((int)dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.LOCATION));
            response.setMachineDoneNumber((int)dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.MACHINE));
            response.setMoldDoneNumber((int)dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.TOOLING));
            response.setPartDoneNumber((int)dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(id, ObjectType.PART));

        }



        List<DataRequestUser> dataRequestUserList = dataRequestUserRepository.getAllByDataRequestId(id);
        List<User> assignedToList = dataRequestUserList.stream().map(DataRequestUser::getUser).collect(Collectors.toList());
        List<Long> assignedToIdList = dataRequestUserList.stream().map(DataRequestUser::getUserId).collect(Collectors.toList());
        response.setAssignedToList(assignedToList);
        response.setAssignedToIdList(assignedToIdList);

        return response;
    }


    public CompletionRateWidgetData getCompletionRateWidgetData() {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIgnoreDashboardFilter(true);

        Pageable pageable = PageRequest.of(0, 999999);


        Page<CompletionRateData> avgCompany = companyRepository.getCompletionRateData(payload, pageable, true);
        CompletionRateWidgetItem company = new CompletionRateWidgetItem(companyRepository.count(new CompanyPayload().getPredicate()),  avgCompany.getContent().isEmpty() ? 0 : avgCompany.getContent().get(0).getRate());

        Page<CompletionRateData> avgLocation = locationRepository.getCompletionRateData(payload, pageable, true);
        CompletionRateWidgetItem location = new CompletionRateWidgetItem(locationRepository.count(new LocationPayload().getPredicate()), avgLocation.getContent().isEmpty() ? 0 : avgLocation.getContent().get(0).getRate());

        Page<CompletionRateData> avgPart = partRepository.getCompletionRateData(payload, pageable, true);
        PartPayload partPayload = new PartPayload();
        partPayload.setStatus("active");
        CompletionRateWidgetItem part = new CompletionRateWidgetItem(partRepository.count(partPayload.getPredicate()), avgPart.getContent().isEmpty() ? 0 : avgPart.getContent().get(0).getRate());

        Page<CompletionRateData> avgMachine = machineRepository.getCompletionRateData(payload, pageable, true);
        MachinePayload machinePayload = new MachinePayload();
        machinePayload.setStatus("enabled");
        CompletionRateWidgetItem machine = new CompletionRateWidgetItem(machineRepository.count(machinePayload.getPredicate()), avgMachine.getContent().isEmpty() ? 0 : avgMachine.getContent().get(0).getRate());

        List<String> deletedFieldMold = getDeletedFieldsByType(ConfigCategory.TOOLING);
        payload.setDeletedFields(deletedFieldMold);
        Page<CompletionRateData> avgMold = moldRepository.getCompletionRateData(payload, pageable, true);
        CompletionRateWidgetItem mold = new CompletionRateWidgetItem(moldRepository.count(new MoldPayload().getPredicate()), avgMold.getContent().isEmpty() ? 0 : avgMold.getContent().get(0).getRate());

        DoubleSummaryStatistics stats = Stream.of(company, location, part, mold, machine)
                .mapToDouble(CompletionRateWidgetItem::getCompletedPercent)
                .summaryStatistics();

        return CompletionRateWidgetData.builder()
                .company(company)
                .location(location)
                .part(part)
                .mold(mold)
                .machine(machine)
                .overallCompletionRate(NumberUtils.roundOffNumber(stats.getAverage()))
                .build();
    }


    public DataRequest saveDataRequest(DataRequestDetail dataRequestDetail) {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        DataRequest dataRequest;

        List<Long> userIdNotificationList;

        if(dataRequestDetail.getId() != null) {
            dataRequest = dataRequestRepository.getOne(dataRequestDetail.getId());


            List<DataRequestUser> dataRequestUserList = dataRequestUserRepository.getAllByDataRequestId(dataRequestDetail.getId());

            List<Long> currentUserIdList = dataRequestUserList.stream().map(DataRequestUser::getUserId).collect(Collectors.toList());

            if (dataRequestDetail.isReOpen()) {
                userIdNotificationList = dataRequestDetail.getAssignedToIdList();
            } else {
                userIdNotificationList = dataRequestDetail.getAssignedToIdList().stream()
                        .filter(userId -> !currentUserIdList.contains(userId))
                        .collect(Collectors.toList());
            }


            List<DataRequestUser> dataRequestUserList2Delete = dataRequestUserList.stream()
                    .filter(dataRequestUser -> !dataRequestDetail.getAssignedToIdList().contains(dataRequestUser.getUserId()))
                    .collect(Collectors.toList());

            dataRequestUserRepository.deleteAll(dataRequestUserList2Delete);

            List<DataRequestObject> dataRequestObjectList = dataRequestObjectRepository.findAllByDataRequestId(dataRequestDetail.getId());
            dataRequestObjectRepository.deleteAll(dataRequestObjectList);

        } else {
            dataRequest = new DataRequest();
            dataRequest.setCreatedByUser(user);
            dataRequest.setDataRequestStatus(DataCompletionRequestStatus.REQUESTED);
            userIdNotificationList = dataRequestDetail.getAssignedToIdList();
        }

        dataRequestDetail.bindingData(dataRequest);
        dataRequestRepository.save(dataRequest);

        if (dataRequestDetail.getRequestType() == RequestDataType.DATA_COMPLETION) {
            saveDataRequestObject(dataRequestDetail, dataRequest);
        }

        List<DataRequestUser> dataRequestUserList = userIdNotificationList
                .stream()
                .map(userId -> new DataRequestUser(dataRequest, userId))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(dataRequestUserList)) {
            dataRequestUserRepository.saveAll(dataRequestUserList);
        }

        if (dataRequestDetail.isReOpen()) {
            notificationService.createNotificationDataRequestForReopen(dataRequest, userIdNotificationList);
        } else if (CollectionUtils.isNotEmpty(userIdNotificationList)){
            notificationService.createDataRequestNotification(dataRequest, userIdNotificationList);
        }


        if (dataRequestDetail.getFiles() != null) {
            fileStorageService.save(new FileInfo(StorageType.DATA_REQUEST_FILE, dataRequest.getId(), dataRequestDetail.getFiles()));
        }
        return dataRequest;

    }

    private void saveDataRequestObject(DataRequestDetail dataRequestDetail, DataRequest dataRequest) {
        List<DataRequestObject> dataRequestObjectList = Lists.newArrayList();
        Pageable pageable = PageRequest.of(0, 9999);
        if (dataRequestDetail.getIsSelectedCompany()) {
            DataCompletionRatePayload dataCompletionRatePayloadCompany = new DataCompletionRatePayload();
            dataCompletionRatePayloadCompany.setObjectType(ObjectType.COMPANY);
            dataCompletionRatePayloadCompany.setUncompletedData(true);
            Page<CompletionRateData> companyPage = companyRepository.getCompletionRateData(dataCompletionRatePayloadCompany, pageable, false);

            List<Long> idList = companyPage.get().map(CompletionRateData::getId).collect(Collectors.toList());
            dataRequestObjectList.addAll(idList
                    .stream()
                    .map(objectId -> new DataRequestObject(dataRequest, objectId, ObjectType.COMPANY))
                    .collect(Collectors.toList()));
        }
        if (dataRequestDetail.getIsSelectedLocation()) {
            DataCompletionRatePayload dataCompletionRatePayloadLocation = new DataCompletionRatePayload();
            dataCompletionRatePayloadLocation.setObjectType(ObjectType.LOCATION);
            dataCompletionRatePayloadLocation.setUncompletedData(true);
            Page<CompletionRateData> locationPage = locationRepository.getCompletionRateData(dataCompletionRatePayloadLocation, pageable, false);

            List<Long> idList = locationPage.get().map(CompletionRateData::getId).collect(Collectors.toList());
            dataRequestObjectList.addAll(idList
                    .stream()
                    .map(objectId -> new DataRequestObject(dataRequest, objectId, ObjectType.LOCATION))
                    .collect(Collectors.toList()));
        }

        if (dataRequestDetail.getIsSelectedMachine()) {
            DataCompletionRatePayload dataCompletionRatePayloadMachine = new DataCompletionRatePayload();
            dataCompletionRatePayloadMachine.setObjectType(ObjectType.MACHINE);
            dataCompletionRatePayloadMachine.setUncompletedData(true);
            Page<CompletionRateData> machinePage = machineRepository.getCompletionRateData(dataCompletionRatePayloadMachine, pageable, false);


            List<Long> idList = machinePage.get().map(CompletionRateData::getId).collect(Collectors.toList());
            dataRequestObjectList.addAll(idList
                    .stream()
                    .map(objectId -> new DataRequestObject(dataRequest, objectId, ObjectType.MACHINE))
                    .collect(Collectors.toList()));
        }

        if (dataRequestDetail.getIsSelectedMold()) {
            Optional<List<GeneralConfig>> generalConfigs = generalConfigRepository.findByConfigCategoryAndDeletedFieldIsTrue(ConfigCategory.TOOLING);
            List<String> deletedFieldMold = generalConfigs.map(configs -> configs.stream().map(GeneralConfig::getFieldName).collect(Collectors.toList())).orElseGet(ArrayList::new);
            DataCompletionRatePayload dataCompletionRatePayloadMold = new DataCompletionRatePayload();
            dataCompletionRatePayloadMold.setObjectType(ObjectType.TOOLING);
            dataCompletionRatePayloadMold.setUncompletedData(true);
            dataCompletionRatePayloadMold.setDeletedFields(deletedFieldMold);
            Page<CompletionRateData> moldPage =  moldRepository.getCompletionRateData(dataCompletionRatePayloadMold, pageable, false);

            List<Long> idList = moldPage.get().map(CompletionRateData::getId).collect(Collectors.toList());
            dataRequestObjectList.addAll(idList
                    .stream()
                    .map(objectId -> new DataRequestObject(dataRequest, objectId, ObjectType.TOOLING))
                    .collect(Collectors.toList()));
        }

        if (dataRequestDetail.getIsSelectedPart()) {
            DataCompletionRatePayload dataCompletionRatePayloadPart = new DataCompletionRatePayload();
            dataCompletionRatePayloadPart.setObjectType(ObjectType.PART);
            dataCompletionRatePayloadPart.setUncompletedData(true);
            Page<CompletionRateData> partPage = partRepository.getCompletionRateData(dataCompletionRatePayloadPart, pageable, false);


            List<Long> idList = partPage.get().map(CompletionRateData::getId).collect(Collectors.toList());
            dataRequestObjectList.addAll(idList
                    .stream()
                    .map(objectId -> new DataRequestObject(dataRequest, objectId, ObjectType.PART))
                    .collect(Collectors.toList()));
        }

        if (CollectionUtils.isNotEmpty(dataRequestObjectList)) {
            dataRequestObjectRepository.saveAll(dataRequestObjectList);
        }
    }

    public void changeStatusDataRequest(UpdateStatusDTO updateStatusDTO) {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        DataRequest dataRequest = dataRequestRepository.getOne(updateStatusDTO.getId());
        dataRequest.setDataRequestStatus(updateStatusDTO.getDataRequestStatus());
        if (updateStatusDTO.getDataRequestStatus() == DataCompletionRequestStatus.CANCELLED) {
            dataRequest.setCancelReason(updateStatusDTO.getReason());
            dataRequest.setCancelledBy(user);
        }
        if (updateStatusDTO.getDataRequestStatus() == DataCompletionRequestStatus.DECLINED) {
            dataRequest.setDeclineReason(updateStatusDTO.getReason());
            dataRequest.setDeclinedBy(user);
        }

        if (dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.CANCELLED || dataRequest.getDataRequestStatus() == DataCompletionRequestStatus.DECLINED) {
            List<DataRequestUser> dataRequestUserList = dataRequestUserRepository.getAllByDataRequestId(updateStatusDTO.getId());
            notificationService.createDataRequestNotification(dataRequest, dataRequestUserList.stream().map(DataRequestUser::getUserId).collect(Collectors.toList()));
        }

        dataRequestRepository.save(dataRequest);
    }

    public String getDataRequestId() {
        return IdUtils.gen(IdRuleCode.DATA_REQUEST,null);
//
//        long maxIndex =dataRequestRepository.count();
//        return "DR-"+String.format("%07d", maxIndex+1);
    }

    public NumberIncompleteData getNumberIncompleteData(Long requestId) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService = new DelegatingSecurityContextExecutorService(executorService, SecurityContextHolder.getContext());
        NumberIncompleteData numberIncompleteData = new NumberIncompleteData();
        Pageable pageable = PageRequest.of(0, 1);
        Future<Long> locationCountFuture = executorService.submit(() -> {
            DataCompletionRatePayload dataCompletionRatePayloadLocation = new DataCompletionRatePayload();
            dataCompletionRatePayloadLocation.setObjectType(ObjectType.LOCATION);
            dataCompletionRatePayloadLocation.setUncompletedData(true);
            dataCompletionRatePayloadLocation.setDataRequestId(requestId);

            return locationRepository.getCompletionRateData(dataCompletionRatePayloadLocation, pageable, false).getTotalElements();
        });

        Future<Long> toolingCountFuture = executorService.submit(() -> {
            Optional<List<GeneralConfig>> generalConfigs = generalConfigRepository.findByConfigCategoryAndDeletedFieldIsTrue(ConfigCategory.TOOLING);
            List<String> deletedFieldMold = generalConfigs.map(configs -> configs.stream().map(GeneralConfig::getFieldName).collect(Collectors.toList())).orElseGet(ArrayList::new);
            DataCompletionRatePayload dataCompletionRatePayloadMold = new DataCompletionRatePayload();
            dataCompletionRatePayloadMold.setObjectType(ObjectType.TOOLING);
            dataCompletionRatePayloadMold.setUncompletedData(true);
            dataCompletionRatePayloadMold.setDeletedFields(deletedFieldMold);
            dataCompletionRatePayloadMold.setDataRequestId(requestId);
            return moldRepository.getCompletionRateData(dataCompletionRatePayloadMold, pageable, false).getTotalElements();
        });

        Future<Long> partCountFuture = executorService.submit(() -> {
            DataCompletionRatePayload dataCompletionRatePayloadPart = new DataCompletionRatePayload();
            dataCompletionRatePayloadPart.setObjectType(ObjectType.PART);
            dataCompletionRatePayloadPart.setUncompletedData(true);
            dataCompletionRatePayloadPart.setDataRequestId(requestId);
            return partRepository.getCompletionRateData(dataCompletionRatePayloadPart, pageable, false).getTotalElements();
        });

        Future<Long> machineCountFuture = executorService.submit(() -> {
            DataCompletionRatePayload dataCompletionRatePayloadMachine = new DataCompletionRatePayload();
            dataCompletionRatePayloadMachine.setObjectType(ObjectType.MACHINE);
            dataCompletionRatePayloadMachine.setUncompletedData(true);
            dataCompletionRatePayloadMachine.setIgnoreDashboardFilter(true);
            dataCompletionRatePayloadMachine.setDataRequestId(requestId);
            return machineRepository.getCompletionRateData(dataCompletionRatePayloadMachine, pageable, false).getTotalElements();
        });

        Future<Long> companyCountFuture = executorService.submit(() -> {
            DataCompletionRatePayload dataCompletionRatePayloadCompany = new DataCompletionRatePayload();
            dataCompletionRatePayloadCompany.setObjectType(ObjectType.COMPANY);
            dataCompletionRatePayloadCompany.setUncompletedData(true);
            dataCompletionRatePayloadCompany.setDataRequestId(requestId);
            return companyRepository.getCompletionRateData(dataCompletionRatePayloadCompany, pageable, false).getTotalElements();
        });

        numberIncompleteData.setMoldNumber(toolingCountFuture.get());
        numberIncompleteData.setPartNumber(partCountFuture.get());
        numberIncompleteData.setMachineNumber(machineCountFuture.get());
        numberIncompleteData.setLocationNumber(locationCountFuture.get());
        numberIncompleteData.setCompanyNumber(companyCountFuture.get());
        executorService.shutdown();
        return numberIncompleteData;
    }

    @Transactional
    public void saveDataRequestObject(Long dataRequestId, Long objectId, ObjectType objectType) {
        DataRequest dataRequest = dataRequestRepository.getOne(dataRequestId);

        DataRequestObject dataRequestObject = new DataRequestObject();
        dataRequestObject.setDataRequestId(dataRequestId);
        dataRequest.setId(dataRequestId);
        dataRequestObject.setDataRequest(dataRequest);
        dataRequestObject.setObjectId(objectId);
        dataRequestObject.setObjectType(objectType);
        dataRequestObjectRepository.save(dataRequestObject);

        long currentNumberMold = 0;
        if(dataRequest.getNumberMoldRequest() != null && dataRequest.getNumberMoldRequest() != null) {
            currentNumberMold = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(dataRequestId, ObjectType.TOOLING);
        }
        long currentNumberLocation = 0;
        if(dataRequest.getNumberLocationRequest() != null && dataRequest.getNumberLocationRequest() != null) {
            currentNumberLocation = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(dataRequestId, ObjectType.LOCATION);
        }
        long currentNumberMachine = 0;
        if(dataRequest.getNumberMachineRequest() != null && dataRequest.getNumberMachineRequest() != null) {
            currentNumberMachine = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(dataRequestId, ObjectType.MACHINE);
        }
        long currentNumberCompany = 0;
        if(dataRequest.getNumberCompanyRequest() != null && dataRequest.getNumberCompanyRequest() != null) {
            currentNumberCompany = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(dataRequestId, ObjectType.COMPANY);
        }
        long currentNumberPart = 0;
        if(dataRequest.getNumberPartRequest() != null && dataRequest.getNumberPartRequest() != null) {
            currentNumberPart = dataRequestObjectRepository.countAllByDataRequestIdAndObjectType(dataRequestId, ObjectType.PART);
        }

        if (compareLongAndInt(currentNumberMold, dataRequest.getNumberMoldRequest())
                && compareLongAndInt(currentNumberLocation, dataRequest.getNumberLocationRequest())
                && compareLongAndInt(currentNumberMachine, dataRequest.getNumberMachineRequest())
                && compareLongAndInt(currentNumberCompany, dataRequest.getNumberCompanyRequest())
                && compareLongAndInt(currentNumberPart, dataRequest.getNumberPartRequest())) {
            dataRequest.setDataRequestStatus(DataCompletionRequestStatus.COMPLETED);
            dataRequestRepository.save(dataRequest);
            List<DataRequestUser> dataRequestUserList = dataRequestUserRepository.getAllByDataRequestId(dataRequestId);
            notificationService.createDataRequestNotification(dataRequest, dataRequestUserList.stream().map(DataRequestUser::getUserId).collect(Collectors.toList()));
        }

    }

    public void pushNotificationOverdue() {
        List<DataRequest> dataRequestOverdueList = dataRequestRepository.findAllByDueDateAndStatusIn(Instant.now(), Arrays.asList(DataCompletionRequestStatus.REQUESTED, DataCompletionRequestStatus.IN_PROGRESS));
        if (CollectionUtils.isNotEmpty(dataRequestOverdueList)) {
            dataRequestOverdueList.forEach(dataRequest -> {
                List<DataRequestUser> dataRequestUserList = dataRequestUserRepository.getAllByDataRequestId(dataRequest.getId());
                notificationService.createNotificationForOverdueDataRequest(dataRequest, dataRequestUserList);
            });
        }
        List<DataRequest> dataRequest3DayBeforeDeadLineList =
                dataRequestRepository.findAllByDueDateAndStatusIn(DateUtils.plusDays(Instant.now(), 3), Arrays.asList(DataCompletionRequestStatus.REQUESTED, DataCompletionRequestStatus.IN_PROGRESS));

        if (CollectionUtils.isNotEmpty(dataRequest3DayBeforeDeadLineList)) {
            dataRequest3DayBeforeDeadLineList.forEach(dataRequest -> {
                List<DataRequestUser> dataRequestUserList = dataRequestUserRepository.getAllByDataRequestId(dataRequest.getId());
                notificationService.createNotificationForOverdueDataRequest(dataRequest, dataRequestUserList);
            });
        }

    }

    public void completeDataCompletion(Long objectId, ObjectType objectType) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<DataRequest> dataRequestList = dataRequestRepository
                    .findByRequestDataTypeAndObjectIdAndObjectTypeAndStatus(RequestDataType.DATA_COMPLETION, objectId, objectType);

            if (CollectionUtils.isNotEmpty(dataRequestList)) {
                dataRequestList.forEach(dataRequest -> {
                    NumberIncompleteData numberIncompleteData = null;
                    try {
                        numberIncompleteData = getNumberIncompleteData(dataRequest.getId());
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (numberIncompleteData.isCompleted()) {
                        dataRequest.setDataRequestStatus(DataCompletionRequestStatus.COMPLETED);
                        notificationService.createDataRequestNotification(dataRequest, dataRequestUserRepository.getAllByDataRequestId(dataRequest.getId()).stream()
                                .map(DataRequestUser::getUserId).collect(Collectors.toList()));
                    }
                });
            }
            dataRequestRepository.saveAll(dataRequestList);
        });
        executor.shutdown();
    }

    private boolean compareLongAndInt(Long longData, Integer intData) {
        longData = longData == null ? 0L : longData;
        intData = intData == null ? 0 : intData;
        return longData.equals(intData.longValue());
    }

    private List<String> getDeletedFieldsByType(ConfigCategory category) {
        Optional<List<GeneralConfig>> generalConfigs = generalConfigRepository.findByConfigCategoryAndDeletedFieldIsTrue(category);
        return generalConfigs.map(configs -> configs.stream().map(GeneralConfig::getFieldName).collect(Collectors.toList())).orElseGet(ArrayList::new);
    }
}
