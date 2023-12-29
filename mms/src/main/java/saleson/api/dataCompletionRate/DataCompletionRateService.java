package saleson.api.dataCompletionRate;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.framework.util.ThreadUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import saleson.api.category.CategoryRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.configuration.GeneralConfigRepository;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.dataCompletionRate.payload.CategoryClone;
import saleson.api.dataCompletionRate.payload.DataCompletionGroupByType;
import saleson.api.dataCompletionRate.payload.DataCompletionItem;
import saleson.api.dataCompletionRate.payload.DataCompletionOrderLite;
import saleson.api.dataCompletionRate.payload.DataCompletionOrderPayload;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.dataCompletionRate.payload.MoldClone;
import saleson.api.dataRequest.DataRequestRepository;
import saleson.api.location.LocationRepository;
import saleson.api.machine.MachineRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.notification.NotificationService;
import saleson.api.part.PartRepository;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;
import saleson.common.notification.MailService;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.config.GeneralConfig;
import saleson.model.data.completionRate.AllTypeCompletionRateData;
import saleson.model.data.completionRate.AvgCompletionRateData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.service.mail.DataCompletionOrderForCreatorEmailContent;
import saleson.service.mail.DataCompletionOrderForReceiverEmailContent;
import saleson.service.util.NumberUtils;

@Service
public class DataCompletionRateService {

    @Autowired
    @Lazy
    LocationRepository locationRepository;
    @Autowired
    @Lazy
    MachineRepository machineRepository;
    @Autowired
    @Lazy
    CompanyRepository companyRepository;
    @Autowired
    @Lazy
    CategoryRepository categoryRepository;
    @Autowired
    @Lazy
    MoldRepository moldRepository;
    @Autowired
    @Lazy
    PartRepository partRepository;
    @Autowired
    DataCompletionRateRepository dataCompletionRateRepository;
    @Autowired
    DataCompletionOrderRepository dataCompletionOrderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MailService mailService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    GeneralConfigService generalConfigService;
    @Autowired
    GeneralConfigRepository generalConfigRepository;
    @Autowired
    DataCompletionOrderForReceiverEmailContent dataCompletionOrderForReceiverEmailContent;
    @Autowired
    DataCompletionOrderForCreatorEmailContent dataCompletionOrderForCreatorEmailContent;
    @Autowired
    @Lazy
    UserService userService;

    @Autowired
    private DataRequestRepository dataRequestRepository;

    @Value("${host.url}")
    String host;
    @Value("${customer.server.name.initial}")
    String serverNameInitial;

    public AvgCompletionRateData getCompletionRateDataListDataLeak(DataCompletionRatePayload payload, Pageable pageable) {
        AvgCompletionRateData data = getCompletionRateDataList(payload, pageable);
        if(data!=null && data.getData()!=null && data.getData().getContent()!=null && !data.getData().getContent().isEmpty()){
            List<User> allUser=userService.getAllDataLeakUserInList(null);
            List<Long> allUserId=allUser.stream().map(u->u.getId()).collect(Collectors.toList());
            data.getData().getContent().stream().forEach(cr->{
                if(cr.getRequestedTo()!=null){
                    cr.setRequestedTo(cr.getRequestedTo().stream().filter(u->allUserId.contains(u.getId())).collect(Collectors.toList()));
                }
            });
        }
    return data;
    }
    public AvgCompletionRateData getCompletionRateDataList(DataCompletionRatePayload payload, Pageable pageable) {
        switch (payload.getObjectType()) {
            case COMPANY:
                Page<CompletionRateData> avgCompany = companyRepository.getCompletionRateData(payload, pageable, true);
                AvgCompletionRateData dataCompany = new AvgCompletionRateData(avgCompany.getContent().isEmpty() ? 0 : avgCompany.getContent().get(0).getRate(), companyRepository.getCompletionRateData(payload, pageable, false));
                dataCompany.getData().getContent().forEach(cr -> {
                    Optional<List<DataCompletionOrder>> orders = dataCompletionOrderRepository.findByCompaniesContainsAndCompletedCompanyIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(companyRepository.getOne(cr.getId()));
                    List<User> requestedUsers = new ArrayList<>();
                    orders.ifPresent(dataCompletionOrders -> dataCompletionOrders.forEach(order -> {
                        requestedUsers.addAll(order.getAssignedUsers().stream().filter(au -> !requestedUsers.contains(au)).collect(Collectors.toList()));
                    }));
                    cr.setRequestedTo(requestedUsers);
                });

                dataCompany.setDataRequestList(dataRequestRepository.findDataRequestIdByObjectType(payload.getObjectType()));

//                checkAndSortByRequestedTo(dataCompany, pageable);
                return dataCompany;
            case LOCATION:
                Page<CompletionRateData> avgLocation = locationRepository.getCompletionRateData(payload, pageable, true);
                AvgCompletionRateData dataLocation = new AvgCompletionRateData(avgLocation.getContent().isEmpty() ? 0 : avgLocation.getContent().get(0).getRate(), locationRepository.getCompletionRateData(payload, pageable, false));
                dataLocation.getData().getContent().forEach(cr -> {
                    Optional<List<DataCompletionOrder>> orders = dataCompletionOrderRepository.findByLocationsContainsAndCompletedLocationIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(locationRepository.getOne(cr.getId()));
                    List<User> requestedUsers = new ArrayList<>();
                    orders.ifPresent(dataCompletionOrders -> dataCompletionOrders.forEach(order -> {
                        requestedUsers.addAll(order.getAssignedUsers().stream().filter(au -> !requestedUsers.contains(au)).collect(Collectors.toList()));
                    }));
                    cr.setRequestedTo(requestedUsers);
                });

                dataLocation.setDataRequestList(dataRequestRepository.findDataRequestIdByObjectType(payload.getObjectType()));
//                checkAndSortByRequestedTo(dataLocation, pageable);
                return dataLocation;
//            case CATEGORY:
//                Page<CompletionRateData> avgCategory = categoryRepository.getCompletionRateData(payload, pageable, true);
//                AvgCompletionRateData dataCategory = new AvgCompletionRateData(avgCategory.getContent().isEmpty() ? 0 : avgCategory.getContent().get(0).getRate(), categoryRepository.getCompletionRateData(payload, pageable, false));
//                dataCategory.getData().getContent().forEach(cr -> {
//                    Optional<List<DataCompletionOrder>> orders = dataCompletionOrderRepository.findByCategoriesContainsAndCompletedCategoryIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(categoryRepository.getOne(cr.getId()));
//                    List<User> requestedUsers = new ArrayList<>();
//                    orders.ifPresent(dataCompletionOrders -> dataCompletionOrders.forEach(order -> {
//                        requestedUsers.addAll(order.getAssignedUsers().stream().filter(au -> !requestedUsers.contains(au)).collect(Collectors.toList()));
//                    }));
//                    cr.setRequestedTo(requestedUsers);
//                    CategoryClone categoryClone = new CategoryClone();
//                    try {
//                        BeanUtils.copyProperties(categoryClone, cr.getData());
//                        if (categoryClone.getParent() != null) {
//                            categoryClone.setParentName(categoryClone.getParent().getName());
//                        }
//                    } catch (IllegalAccessException | InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                    cr.setData(categoryClone);
//                });
////                checkAndSortByRequestedTo(dataCategory, pageable);
//                return dataCategory;
            case PART:
                List<String> deletedFieldPart = getDeletedFieldsByType(ConfigCategory.PART);
                payload.setDeletedFields(deletedFieldPart);
                Page<CompletionRateData> avgPart = partRepository.getCompletionRateData(payload, pageable, true);
                AvgCompletionRateData dataPart = new AvgCompletionRateData(avgPart.getContent().isEmpty() ? 0 : avgPart.getContent().get(0).getRate(), partRepository.getCompletionRateData(payload, pageable, false));
                dataPart.getData().getContent().forEach(cr -> {
                    Optional<List<DataCompletionOrder>> orders = dataCompletionOrderRepository.findByPartsContainsAndCompletedPartIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(partRepository.getOne(cr.getId()));
                    List<User> requestedUsers = new ArrayList<>();
                    orders.ifPresent(dataCompletionOrders -> dataCompletionOrders.forEach(order -> {
                        requestedUsers.addAll(order.getAssignedUsers().stream().filter(au -> !requestedUsers.contains(au)).collect(Collectors.toList()));
                    }));
                    cr.setRequestedTo(requestedUsers);
                });
                dataPart.setDataRequestList(dataRequestRepository.findDataRequestIdByObjectType(payload.getObjectType()));

//                checkAndSortByRequestedTo(dataPart, pageable);
                return dataPart;
            case TOOLING:
                List<String> deletedFieldMold = getDeletedFieldsByType(ConfigCategory.TOOLING);
                payload.setDeletedFields(deletedFieldMold);
                Page<CompletionRateData> avgMold = moldRepository.getCompletionRateData(payload, pageable, true);
                AvgCompletionRateData dataMold = new AvgCompletionRateData(avgMold.getContent().isEmpty() ? 0 : avgMold.getContent().get(0).getRate(), moldRepository.getCompletionRateData(payload, pageable, false));
                dataMold.getData().getContent().forEach(cr -> {
                    Optional<List<DataCompletionOrder>> orders = dataCompletionOrderRepository.findByMoldsContainsAndCompletedMoldIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(moldRepository.getOne(cr.getId()));
                    List<User> requestedUsers = new ArrayList<>();
                    orders.ifPresent(dataCompletionOrders -> dataCompletionOrders.forEach(order -> {
                        requestedUsers.addAll(order.getAssignedUsers().stream().filter(au -> !requestedUsers.contains(au)).collect(Collectors.toList()));
                    }));
                    cr.setRequestedTo(requestedUsers);
                });

                dataMold.setDataRequestList(dataRequestRepository.findDataRequestIdByObjectType(payload.getObjectType()));

//                checkAndSortByRequestedTo(dataMold, pageable);
                return dataMold;
            case MACHINE:
                Page<CompletionRateData> avgMachine = machineRepository.getCompletionRateData(payload, pageable, true);
                AvgCompletionRateData dataMachine = new AvgCompletionRateData(avgMachine.getContent().isEmpty() ? 0 : avgMachine.getContent().get(0).getRate(), machineRepository.getCompletionRateData(payload, pageable, false));
                dataMachine.getData().getContent().forEach(cr -> {
                    Optional<List<DataCompletionOrder>> orders = dataCompletionOrderRepository.findByMachinesContainsAndCompletedMachineIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(machineRepository.getOne(cr.getId()));
                    List<User> requestedUsers = new ArrayList<>();
                    orders.ifPresent(dataCompletionOrders -> dataCompletionOrders.forEach(order -> {
                        requestedUsers.addAll(order.getAssignedUsers().stream().filter(au -> !requestedUsers.contains(au)).collect(Collectors.toList()));
                    }));
                    cr.setRequestedTo(requestedUsers);
                });

                dataMachine.setDataRequestList(dataRequestRepository.findDataRequestIdByObjectType(payload.getObjectType()));

//                checkAndSortByRequestedTo(dataMachine, pageable);
                return dataMachine;
        }
        return null;
    }

    private void checkAndSortByRequestedTo(AvgCompletionRateData data, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        List<CompletionRateData> dataList = new ArrayList<>(data.getData().getContent());
        if ("requestedTo".equalsIgnoreCase(properties[0])) {
            dataList.sort(Comparator.comparing(CompletionRateData::getFirstRequestedUserName));
            if (directions[0].equals(Sort.Direction.ASC)) {
                dataList.sort(Comparator.nullsFirst(Comparator.comparing(CompletionRateData::getFirstRequestedUserName)).reversed());
            }
        }
        data.setData(new PageImpl<>(dataList, pageable, data.getData().getTotalElements()));
    }

    public AllTypeCompletionRateData getCompletionRateForAllObjectTypes(DataCompletionRatePayload payload){
        List<AvgCompletionRateData> list = new ArrayList<>();
        Pageable pageable = getPageableForListAll();

//        DataCompletionRatePayload companyPayload = DataUtils.deepCopy(payload, DataCompletionRatePayload.class);
//        if (SecurityUtils.getCompanyType().equals(CompanyType.IN_HOUSE)) {
//            companyPayload.setCompanyType(CompanyType.IN_HOUSE);
//        }
        Page<CompletionRateData> avgCompany = companyRepository.getCompletionRateData(payload, pageable, true);
        list.add( new AvgCompletionRateData(ObjectType.COMPANY, avgCompany.getContent().isEmpty() ? 0 : avgCompany.getContent().get(0).getRate()));
        Page<CompletionRateData> avgLocation = locationRepository.getCompletionRateData(payload, pageable, true);
        list.add( new AvgCompletionRateData(ObjectType.LOCATION, avgLocation.getContent().isEmpty() ? 0 : avgLocation.getContent().get(0).getRate()));
        Page<CompletionRateData> avgCategory = categoryRepository.getCompletionRateData(payload, pageable, true);
        list.add( new AvgCompletionRateData(ObjectType.CATEGORY, avgCategory.getContent().isEmpty() ? 0 : avgCategory.getContent().get(0).getRate()));
        Page<CompletionRateData> avgMachine = machineRepository.getCompletionRateData(payload, pageable, true);
        list.add( new AvgCompletionRateData(ObjectType.MACHINE, avgMachine.getContent().isEmpty() ? 0 : avgMachine.getContent().get(0).getRate()));

        List<String> deletedFieldPart = getDeletedFieldsByType(ConfigCategory.PART);
        payload.setDeletedFields(deletedFieldPart);
        Page<CompletionRateData> avgPart = partRepository.getCompletionRateData(payload, pageable, true);
        list.add( new AvgCompletionRateData(ObjectType.PART, avgPart.getContent().isEmpty() ? 0 : avgPart.getContent().get(0).getRate()));

        List<String> deletedFieldMold = getDeletedFieldsByType(ConfigCategory.TOOLING);
        payload.setDeletedFields(deletedFieldMold);
        Page<CompletionRateData> avgMold = moldRepository.getCompletionRateData(payload, pageable, true);
        list.add( new AvgCompletionRateData(ObjectType.TOOLING, avgMold.getContent().isEmpty() ? 0 : avgMold.getContent().get(0).getRate()));

        AllTypeCompletionRateData data = new AllTypeCompletionRateData();
        data.setData(list);
        DoubleSummaryStatistics stats = list.stream()
                .filter(l -> !l.getObjectType().equals(ObjectType.CATEGORY))
                .mapToDouble(AvgCompletionRateData::getAvgRate)
                .summaryStatistics();
        data.setOverall(NumberUtils.roundOffNumber(stats.getAverage()));

        return data;
    }

    public CompletionRateData getCompanyCompletionRateForLocation(Long companyId) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setCompanyId(Collections.singletonList(companyId));
        Pageable pageable = getPageableForListAll();
        Page<CompletionRateData> avg = locationRepository.getCompletionRateData(payload, pageable, true);
        return avg.getContent().isEmpty() ? new CompletionRateData(null, 0D) : avg.getContent().get(0);
    }

    public CompletionRateData getCompanyCompletionRateForMachine(Long companyId) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setCompanyId(Collections.singletonList(companyId));
        Pageable pageable = getPageableForListAll();
        Page<CompletionRateData> avg = machineRepository.getCompletionRateData(payload, pageable, true);
        return avg.getContent().isEmpty() ? new CompletionRateData(null, 0D) : avg.getContent().get(0);
    }

    public CompletionRateData getCompanyCompletionRateForCompany(Long companyId) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setCompanyId(Collections.singletonList(companyId));
        Pageable pageable = getPageableForListAll();
        Page<CompletionRateData> avg = companyRepository.getCompletionRateData(payload, pageable, true);
        return avg.getContent().isEmpty() ? new CompletionRateData(null, 0D) : avg.getContent().get(0);
    }

    public CompletionRateData getCompanyCompletionRateForCategory() {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        Pageable pageable = getPageableForListAll();
        Page<CompletionRateData> avg = categoryRepository.getCompletionRateData(payload, pageable, true);
        return avg.getContent().isEmpty() ? new CompletionRateData(null, 0D) : avg.getContent().get(0);
    }

    public CompletionRateData getCompanyCompletionRateForPart(Long companyId) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setCompanyId(Collections.singletonList(companyId));
        List<String> deletedFieldPart = getDeletedFieldsByType(ConfigCategory.PART);
        payload.setDeletedFields(deletedFieldPart);
        Pageable pageable = getPageableForListAll();
        Page<CompletionRateData> avg = partRepository.getCompletionRateData(payload, pageable, true);
        return avg.getContent().isEmpty() ? new CompletionRateData(null, 0D) : avg.getContent().get(0);
    }

    public CompletionRateData getCompanyCompletionRateForTooling(Long companyId) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setCompanyId(Collections.singletonList(companyId));
        List<String> deletedFieldMold = getDeletedFieldsByType(ConfigCategory.TOOLING);
        payload.setDeletedFields(deletedFieldMold);
        Pageable pageable = getPageableForListAll();
        Page<CompletionRateData> avg = moldRepository.getCompletionRateData(payload, pageable, true);
        return avg.getContent().isEmpty() ? new CompletionRateData(null, 0D) : avg.getContent().get(0);
    }

    public Page<CompletionRateData> getCompanyCompletionRateForTooling(DataCompletionRatePayload payload, Pageable pageable) {
        List<String> deletedFieldMold = getDeletedFieldsByType(ConfigCategory.TOOLING);
        payload.setDeletedFields(deletedFieldMold);
        return moldRepository.getCompletionRateData(payload, pageable, false);
    }

    public AvgCompletionRateData getCompanyCompletionRate(DataCompletionRatePayload payload, Pageable pageable) {
        Page<CompletionRateData> avg = dataCompletionRateRepository.getCompanyCompletionRate(payload, pageable, true);
        return new AvgCompletionRateData(avg.getContent().isEmpty() ? 0D : avg.getContent().get(0).getRate(), dataCompletionRateRepository.getCompanyCompletionRate(payload, pageable, false));
    }

    public ApiResponse updateAllCompletionRate() {
//        CompletionRateData categoryRate = getCompanyCompletionRateForCategory();
        List<Company> companies = companyRepository.findByEnabled(true);

        List<DataCompletionRate> listReturn = new ArrayList<>();
        companies.forEach(company -> {
            Long companyId = company.getId();
            CompletionRateData companyRate = getCompanyCompletionRateForCompany(companyId);
            CompletionRateData locationRate = getCompanyCompletionRateForLocation(companyId);
            CompletionRateData partRate = getCompanyCompletionRateForPart(companyId);
            CompletionRateData moldRate = getCompanyCompletionRateForTooling(companyId);
            CompletionRateData machineRate = getCompanyCompletionRateForMachine(companyId);

            Optional<DataCompletionRate> optional = dataCompletionRateRepository.findByCompanyId(companyId);
            DataCompletionRate dataCompletionRate;
            if (optional.isPresent()) {
                dataCompletionRate = optional.get();
            } else {
                dataCompletionRate = new DataCompletionRate();
                dataCompletionRate.setCompanyId(company.getId());
                dataCompletionRate.setCompanyCode(company.getCompanyCode());
                dataCompletionRate.setCompanyName(company.getName());
            }
            dataCompletionRate.setCompanyRate(companyRate.getRate());
            dataCompletionRate.setLocationRate(locationRate.getRate());
//            dataCompletionRate.setCategoryRate(categoryRate.getRate());
            dataCompletionRate.setPartRate(partRate.getRate());
            dataCompletionRate.setMoldRate(moldRate.getRate());
            dataCompletionRate.setMachineRate(machineRate.getRate());

            listReturn.add(dataCompletionRate);
            dataCompletionRateRepository.save(dataCompletionRate);
        });

        return ApiResponse.success(CommonMessage.OK, listReturn);
    }

	public void updateCompanyCompletionRateByObjectType(ObjectType objectType, Long companyId) {
		// TODO Temporarily skip at Category Case. Because Category case causes error now.
		if (objectType == ObjectType.CATEGORY) {
			return;
		}

		final Authentication auth = SecurityUtils.getAuthentication();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			ThreadUtils.doScopeSupports("DataCompletionRateService.updateCompanyCompletionRateByObjectType", () -> {
				System.out.println("== start update completion rate ==");
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				SecurityUtils.setAuthentication(auth);

				Optional<DataCompletionRate> optional = dataCompletionRateRepository.findByCompanyId(companyId);
				DataCompletionRate dataCompletionRate;
				if (optional.isPresent()) {
					dataCompletionRate = optional.get();
				} else {
					dataCompletionRate = new DataCompletionRate();
					Company company = companyRepository.getOne(companyId);
					dataCompletionRate.setCompanyId(company.getId());
					dataCompletionRate.setCompanyCode(company.getCompanyCode());
					dataCompletionRate.setCompanyName(company.getName());
				}

				switch (objectType) {
				case COMPANY:
					CompletionRateData companyRate = getCompanyCompletionRateForCompany(companyId);
					dataCompletionRate.setCompanyRate(companyRate.getRate());
					break;
				case LOCATION:
					CompletionRateData locationRate = getCompanyCompletionRateForLocation(companyId);
					dataCompletionRate.setLocationRate(locationRate.getRate());
					break;
//	                case CATEGORY:
//	                    CompletionRateData categoryRate = getCompanyCompletionRateForCategory();
//	                    dataCompletionRate.setCategoryRate(categoryRate.getRate());
//	                    break;
				case PART:
					CompletionRateData partRate = getCompanyCompletionRateForPart(companyId);
					dataCompletionRate.setPartRate(partRate.getRate());
					break;
				case TOOLING:
					CompletionRateData moldRate = getCompanyCompletionRateForTooling(companyId);
					dataCompletionRate.setMoldRate(moldRate.getRate());
					break;
				case MACHINE:
					CompletionRateData machineRate = getCompanyCompletionRateForMachine(companyId);
					dataCompletionRate.setMachineRate(machineRate.getRate());
					break;
				}
				dataCompletionRateRepository.save(dataCompletionRate);
				try {
					System.out.println(objectType + " : " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataCompletionRate));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				System.out.println("== end update completion rate ==");
			});
		});
		executor.shutdown();
	}

    public String generateRequestID() {
        BigDecimal index = dataCompletionOrderRepository.findMaxIndex().orElseGet(() -> new BigDecimal(0));
        String serverInitial = serverNameInitial.toUpperCase(Locale.ROOT);
        return serverInitial + "-DC-" + String.format("%06d", index.intValue() + 1);
    }

    public List<DataCompletionGroupByType> getReceivedCompletionOrders(){
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        List<DataCompletionOrder> orders = dataCompletionOrderRepository.findByAssignedUsersContains(user);
        List<DataCompletionGroupByType> list = new ArrayList<>();
        Pageable pageable = getPageableForListLimit20();

        orders.forEach(order -> {
            if (CollectionUtils.isNotEmpty(order.getCompanies()))
                list.add(getDataCompletionItemsForCompany(order, pageable));
            if (CollectionUtils.isNotEmpty(order.getLocations()))
                list.add(getDataCompletionItemsForLocation(order, pageable));
            if (CollectionUtils.isNotEmpty(order.getCategories()))
                list.add(getDataCompletionItemsForCategory(order, pageable));
            if (CollectionUtils.isNotEmpty(order.getParts()))
                list.add(getDataCompletionItemsForPart(order, pageable));
            if (CollectionUtils.isNotEmpty(order.getMolds()))
                list.add(getDataCompletionItemsForMold(order, pageable));
            if (CollectionUtils.isNotEmpty(order.getMachines()))
                list.add(getDataCompletionItemsForMachine(order, pageable));
        });

        return list;
    }

    public Page<DataCompletionItem> getListReceivedCompletionOrderItem(Long id, ObjectType objectType, Pageable pageable) {
        DataCompletionOrder order = dataCompletionOrderRepository.getOne(id);
        switch (objectType) {
            case COMPANY:
                return getCompanyItems(order, pageable);
            case LOCATION:
                return getLocationItems(order, pageable);
            case CATEGORY:
                return getCategoryItems(order, pageable);
            case PART:
                return getPartItems(order, pageable);
            case TOOLING:
                return getMoldItems(order, pageable);
            case MACHINE:
                return getMachineItems(order, pageable);
        }
        return null;
    }

    public Page<DataCompletionOrderLite> getCreatedCompletionOrders(Pageable pageable) {
        Page<DataCompletionOrder> page = dataCompletionOrderRepository.findByCreatedBy(SecurityUtils.getUserId(), pageable);
        List<DataCompletionOrderLite> lites = page.getContent().stream().map(this::bindData).collect(Collectors.toList());
        return new PageImpl<>(lites, pageable, page.getTotalElements());
    }

    @Transactional
    public ApiResponse save(DataCompletionOrderPayload payload){
        DataCompletionOrder order = bindData(payload);

        //send mail to assigned users
        User sender = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        String supportCenter = host + "/support/customer-support/";
        order.getAssignedUsers().forEach(receiver -> {
            List<String> receivers = Collections.singletonList(receiver.getEmail());
            String title = sender.getName() + " from " + sender.getCompany().getName() + " has assigned a Data Completion Order to you";
            String message = " has assigned a Data Completion Order to you";
            sendMailToReceiver(receivers, title, sender, message,
                    order.getMolds().size(), order.getParts().size(), order.getMachines().size(),
                    order.getCompanies().size(), order.getLocations().size(), order.getMachines().size(),
                    supportCenter);
        });

        order.setSentMailDay(DateUtils.getDay(Instant.now()));
        order.setCompleted(false);
        order.setCreatedBy(SecurityUtils.getUserId());
        notificationService.createDataCompletionNotification(order);
        dataCompletionOrderRepository.save(order);

        payload.setAssignedUsers(order.getAssignedUsers());
        return ApiResponse.success(CommonMessage.OK, payload);
    }

    public ApiResponse update(DataCompletionOrderPayload payload) {
        try {
            List<User> listToSendMail = new ArrayList<>();

            DataCompletionOrder order = dataCompletionOrderRepository.getOne(payload.getId());
            if (!StringUtils.isEmpty(payload.getDueDay())) {
                order.setDueDate(DateUtils.getInstant(payload.getDueDay(), DateUtils.DEFAULT_DATE_FORMAT));
                order.setDueDay(payload.getDueDay().substring(0, 8));

                listToSendMail.addAll(order.getAssignedUsers());
            }

            if (CollectionUtils.isNotEmpty(payload.getAssignedUsers())) {
                List<Long> userIDs = payload.getAssignedUsers().stream().map(User::getId).collect(Collectors.toList());
                List<User> users = userRepository.findByIdInAndDeletedIsFalse(userIDs);
                //get list new assigned users
                listToSendMail.addAll(users.stream().filter(user -> !order.getAssignedUsers().contains(user)).collect(Collectors.toList()));

                order.setAssignedUsers(users);
            }

            User sender = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
            String supportCenter = host + "/support/customer-support/";
            listToSendMail.forEach(receiver -> {
                List<String> receivers = Collections.singletonList(receiver.getEmail());
                String title = sender.getName() + " from " + sender.getCompany().getName() + " has assigned a Data Completion Order to you";
                String message = " has assigned a Data Completion Order to you";
                sendMailToReceiver(receivers, title, sender, message,
                        order.getMolds().size(), order.getParts().size(), order.getMachines().size(),
                        order.getCompanies().size(), order.getLocations().size(), order.getCategories().size(),
                        supportCenter);
            });

            notificationService.createDataCompletionNotification(order);
            dataCompletionOrderRepository.save(order);
            return ApiResponse.success(CommonMessage.OK, order);
        } catch (Exception e) {
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    public void sendRemindMail(String day, boolean lastReminded) {
        Optional<List<DataCompletionOrder>> optional = dataCompletionOrderRepository.findByDueDayAndCompletedIsFalseAndLastRemindedIsFalse(day);
        if (optional.isPresent()) {
            List<DataCompletionOrder> orders = optional.get();
            orders.forEach(order -> {
                // send remind mail to assigned users
                User creator = userRepository.getOne(order.getCreatedBy());
                String supportCenter = host + "/support/customer-support/";
                order.getAssignedUsers().forEach(receiver -> {
                    List<String> receivers = Collections.singletonList(receiver.getEmail());
                    String title = creator.getName() + " from " + creator.getCompany().getName() + " is waiting for you to complete the Data Completion Order";
                    String message = " is waiting for you to complete the Data Completion Order";
                    sendMailToReceiver(receivers, title, creator, message,
                            order.getMolds().size(), order.getParts().size(), order.getMachines().size(),
                            order.getCompanies().size(), order.getLocations().size(), order.getCategories().size(),
                            supportCenter);
                });

                //send mail to Project Manager
                String titleForCreator = "Your Data Completion Order has not yet been completed";
                List<String> receivers = Collections.singletonList(creator.getEmail());
                sendMailToCreator(receivers, titleForCreator,
                        order.getMolds().size(), order.getParts().size(), order.getMachines().size(),
                        order.getCompanies().size(), order.getLocations().size(), order.getCategories().size(),
                        supportCenter);
                notificationService.createDataCompletionNotification(order);
                if (lastReminded) {
                    order.setLastReminded(true);
                    dataCompletionOrderRepository.save(order);
                }
            });
        }
    }

    private void sendMailToReceiver(List<String> receivers, String title, User sender, String message,
                                          int moldNumber, int partNumber, int machineNumber,
                                          int companyNumber, int locationNumber, int categoryNumber, String supportCenter) {
        String loginUrl = host + "/admin/data-completion?role=receiver";
        String content = dataCompletionOrderForReceiverEmailContent.generateMailContent(new Object[]{
                sender.getName(),
                sender.getCompany().getName(),
                message,
                moldNumber,
                partNumber,
                machineNumber,
                companyNumber,
                locationNumber,
                categoryNumber,
                loginUrl,
                supportCenter
        });
        mailService.sendMailByContent(receivers, title, content);
    }

    private void sendMailToCreator(List<String> receivers, String title,
                                          int moldNumber, int partNumber, int machineNumber,
                                          int companyNumber, int locationNumber, int categoryNumber, String supportCenter) {
        String loginUrl = host + "/admin/data-completion?role=creator";
        String content = dataCompletionOrderForCreatorEmailContent.generateMailContent(new Object[]{
                moldNumber,
                partNumber,
                machineNumber,
                companyNumber,
                locationNumber,
                categoryNumber,
                loginUrl,
                supportCenter
        });
        mailService.sendMailByContent(receivers, title, content);
    }

    @Transactional
    public void updateCompletionOrder(Long userId, ObjectType objectType, Long objectId) {
        final Authentication auth = SecurityUtils.getAuthentication();
        ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			ThreadUtils.doScopeSupports("DataCompletionRateService.updateCompletionOrder", () -> {
				SecurityUtils.setAuthentication(auth);

	            User user = userRepository.getOne(userId);
	            DataCompletionRatePayload payload = new DataCompletionRatePayload();
	            Pageable pageable = getPageableForListAll();
	            switch (objectType) {
	                case COMPANY:
	                    updateCompletionOrderByCompany(payload, pageable, objectId, user);
	                    break;
	                case LOCATION:
	                    updateCompletionOrderByLocation(payload, pageable, objectId, user);
	                    break;
	                case CATEGORY:
	                    updateCompletionOrderByCategory(payload, pageable, objectId, user);
	                    break;
	                case PART:
	                    updateCompletionOrderByPart(payload, pageable, objectId, user);
	                    break;
	                case TOOLING:
	                    updateCompletionOrderByMold(payload, pageable, objectId, user);
	                    break;
	                case MACHINE:
	                    updateCompletionOrderByMachine(payload, pageable, objectId, user);
	                    break;
	            }
			});
        });
        executor.shutdown();
    }

    private void updateCompletionOrderByCompany(DataCompletionRatePayload payload, Pageable pageable, Long objectId, User user) {
        Company company = companyRepository.getOne(objectId);
        Optional<DataCompletionOrder> optional = dataCompletionOrderRepository.findByCompaniesContainsAndAssignedUsersContainsAndCompletedCompanyIsFalseAndCompletedIsFalse(company, user);
        if (optional.isPresent()) {
            DataCompletionOrder order = optional.get();
            List<Long> ids = order.getCompanies().stream().map(Company::getId).collect(Collectors.toList());
            payload.setIds(ids);
            Page<CompletionRateData> data = companyRepository.getCompletionRateData(payload, pageable, false);
            order.setCompletedCompany(data.getContent().stream().anyMatch(d -> d.getRate() == 100));
            checkCompletedOrder(order);
            dataCompletionOrderRepository.save(order);
        }
    }

    private void updateCompletionOrderByLocation(DataCompletionRatePayload payload, Pageable pageable, Long objectId, User user) {
        Location location = locationRepository.getOne(objectId);
        Optional<DataCompletionOrder> optional = dataCompletionOrderRepository.findByLocationsContainsAndAssignedUsersContainsAndCompletedLocationIsFalseAndCompletedIsFalse(location, user);
        if (optional.isPresent()) {
            DataCompletionOrder order = optional.get();
            List<Long> ids = order.getLocations().stream().map(Location::getId).collect(Collectors.toList());
            payload.setIds(ids);
            Page<CompletionRateData> data = locationRepository.getCompletionRateData(payload, pageable, false);
            order.setCompletedLocation(data.getContent().stream().anyMatch(d -> d.getRate() == 100));
            checkCompletedOrder(order);
            dataCompletionOrderRepository.save(order);
        }
    }

    private void updateCompletionOrderByCategory(DataCompletionRatePayload payload, Pageable pageable, Long objectId, User user) {
        Category category = categoryRepository.getOne(objectId);
        Optional<DataCompletionOrder> optional = dataCompletionOrderRepository.findByCategoriesContainsAndAssignedUsersContainsAndCompletedCategoryIsFalseAndCompletedIsFalse(category, user);
        if (optional.isPresent()) {
            DataCompletionOrder order = optional.get();
            List<Long> ids = order.getCategories().stream().map(Category::getId).collect(Collectors.toList());
            payload.setIds(ids);
            Page<CompletionRateData> data = categoryRepository.getCompletionRateData(payload, pageable, false);
            order.setCompletedCategory(data.getContent().stream().anyMatch(d -> d.getRate() == 100));
            checkCompletedOrder(order);
            dataCompletionOrderRepository.save(order);
        }
    }

    private void updateCompletionOrderByPart(DataCompletionRatePayload payload, Pageable pageable, Long objectId, User user) {
        Part part = partRepository.getOne(objectId);
        Optional<DataCompletionOrder> optional = dataCompletionOrderRepository.findByPartsContainsAndAssignedUsersContainsAndCompletedPartIsFalseAndCompletedIsFalse(part, user);
        if (optional.isPresent()) {
            DataCompletionOrder order = optional.get();
            List<Long> ids = order.getParts().stream().map(Part::getId).collect(Collectors.toList());
            payload.setIds(ids);
            List<String> deletedFieldPart = getDeletedFieldsByType(ConfigCategory.PART);
            payload.setDeletedFields(deletedFieldPart);
            Page<CompletionRateData> data = partRepository.getCompletionRateData(payload, pageable, false);
            order.setCompletedPart(data.getContent().stream().anyMatch(d -> d.getRate() == 100));
            checkCompletedOrder(order);
            dataCompletionOrderRepository.save(order);
        }
    }

    private void updateCompletionOrderByMold(DataCompletionRatePayload payload, Pageable pageable, Long objectId, User user) {
        Mold mold = moldRepository.getOne(objectId);
        Optional<DataCompletionOrder> optional = dataCompletionOrderRepository.findByMoldsContainsAndAssignedUsersContainsAndCompletedMoldIsFalseAndCompletedIsFalse(mold, user);
        if (optional.isPresent()) {
            DataCompletionOrder order = optional.get();
            List<Long> ids = order.getMolds().stream().map(Mold::getId).collect(Collectors.toList());
            payload.setIds(ids);
            List<String> deletedFields = getDeletedFieldsByType(ConfigCategory.TOOLING);
            payload.setDeletedFields(deletedFields);
            Page<CompletionRateData> data = moldRepository.getCompletionRateData(payload, pageable, false);
            order.setCompletedMold(data.getContent().stream().anyMatch(d -> d.getRate() == 100));
            checkCompletedOrder(order);
            dataCompletionOrderRepository.save(order);
        }
    }

    private void updateCompletionOrderByMachine(DataCompletionRatePayload payload, Pageable pageable, Long objectId, User user) {
        Machine machine = machineRepository.getOne(objectId);
        Optional<DataCompletionOrder> optional = dataCompletionOrderRepository.findByMachinesContainsAndAssignedUsersContainsAndCompletedMachineIsFalseAndCompletedIsFalse(machine, user);
        if (optional.isPresent()) {
            DataCompletionOrder order = optional.get();
            List<Long> ids = order.getMachines().stream().map(Machine::getId).collect(Collectors.toList());
            payload.setIds(ids);
            Page<CompletionRateData> data = machineRepository.getCompletionRateData(payload, pageable, false);
            order.setCompletedMachine(data.getContent().stream().anyMatch(d -> d.getRate() == 100));
            checkCompletedOrder(order);
            dataCompletionOrderRepository.save(order);
        }
    }

    private void checkCompletedOrder(DataCompletionOrder order) {
        if (!order.isCompletedCompany()
                || !order.isCompletedLocation()
                || !order.isCompletedCategory()
                || !order.isCompletedPart()
                || !order.isCompletedMold()
                || !order.isCompletedMachine()) {
            order.setCompleted(false);
        } else {
            order.setCompleted(true);
        }
    }

    private DataCompletionOrderLite bindData(DataCompletionOrder order) {
        DataCompletionOrderLite orderLite = new DataCompletionOrderLite();
        orderLite.setId(order.getId());
        orderLite.setOrderId(order.getOrderId());
        orderLite.setDueDay(DateUtils.getDate(order.getDueDate(), DateUtils.YYYY_MM_dd));
        orderLite.setNumberOfCompanies(order.getCompanies().size());
        orderLite.setNumberOfLocations(order.getLocations().size());
        orderLite.setNumberOfCategories(order.getCategories().size());
        orderLite.setNumberOfParts(order.getParts().size());
        orderLite.setNumberOfMolds(order.getMolds().size());
        orderLite.setNumberOfMachines(order.getMachines().size());
        orderLite.setUsers(order.getAssignedUsers());
        orderLite.setCompleted(order.isCompleted());

        return orderLite;
    }

    private DataCompletionGroupByType getDataCompletionItemsForCompany(DataCompletionOrder order, Pageable pageable){
        DataCompletionGroupByType dataCompletionGroupByType = bindCommonData(order);
        dataCompletionGroupByType.setObjectType(ObjectType.COMPANY);
        dataCompletionGroupByType.setItems(getCompanyItems(order, pageable));
        return dataCompletionGroupByType;
    }

    private Page<DataCompletionItem> getCompanyItems(DataCompletionOrder order, Pageable pageable) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIds(order.getCompanies().stream().map(Company::getId).collect(Collectors.toList()));
        Page<CompletionRateData> completionRateData = companyRepository.getCompletionRateData(payload, pageable, false);
        return getDataCompletionItems(completionRateData, order.getDueDate());
    }

    private DataCompletionGroupByType getDataCompletionItemsForLocation(DataCompletionOrder order, Pageable pageable){
        DataCompletionGroupByType dataCompletionGroupByType = bindCommonData(order);
        dataCompletionGroupByType.setObjectType(ObjectType.LOCATION);
        dataCompletionGroupByType.setItems(getLocationItems(order, pageable));
        return dataCompletionGroupByType;
    }

    private Page<DataCompletionItem> getLocationItems(DataCompletionOrder order, Pageable pageable) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIds(order.getLocations().stream().map(Location::getId).collect(Collectors.toList()));
        Page<CompletionRateData> completionRateData = locationRepository.getCompletionRateData(payload, pageable, false);
        return getDataCompletionItems(completionRateData, order.getDueDate());
    }

    private DataCompletionGroupByType getDataCompletionItemsForCategory(DataCompletionOrder order, Pageable pageable){
        DataCompletionGroupByType dataCompletionGroupByType = bindCommonData(order);
        dataCompletionGroupByType.setObjectType(ObjectType.CATEGORY);
        dataCompletionGroupByType.setItems(getCategoryItems(order, pageable));
        return dataCompletionGroupByType;
    }

    private Page<DataCompletionItem> getCategoryItems(DataCompletionOrder order, Pageable pageable) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIds(order.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
        Page<CompletionRateData> completionRateData = categoryRepository.getCompletionRateData(payload, pageable, false);
        return getDataCompletionItemsCategory(completionRateData, order.getDueDate());
    }

    private DataCompletionGroupByType getDataCompletionItemsForPart(DataCompletionOrder order, Pageable pageable){
        DataCompletionGroupByType dataCompletionGroupByType = bindCommonData(order);
        dataCompletionGroupByType.setObjectType(ObjectType.PART);
        dataCompletionGroupByType.setItems(getPartItems(order, pageable));
        return dataCompletionGroupByType;
    }

    private Page<DataCompletionItem> getPartItems(DataCompletionOrder order, Pageable pageable) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIds(order.getParts().stream().map(Part::getId).collect(Collectors.toList()));
        List<String> deletedFieldPart = getDeletedFieldsByType(ConfigCategory.PART);
        payload.setDeletedFields(deletedFieldPart);
        Page<CompletionRateData> completionRateData = partRepository.getCompletionRateData(payload, pageable, false);
        return getDataCompletionItems(completionRateData, order.getDueDate());
    }

    private DataCompletionGroupByType getDataCompletionItemsForMold(DataCompletionOrder order, Pageable pageable){
        DataCompletionGroupByType dataCompletionGroupByType = bindCommonData(order);
        dataCompletionGroupByType.setObjectType(ObjectType.TOOLING);
        dataCompletionGroupByType.setItems(getMoldItems(order, pageable));
        return dataCompletionGroupByType;
    }

    private Page<DataCompletionItem> getMoldItems(DataCompletionOrder order, Pageable pageable) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIds(order.getMolds().stream().map(Mold::getId).collect(Collectors.toList()));
        List<String> deletedFieldTooling = getDeletedFieldsByType(ConfigCategory.TOOLING);
        payload.setDeletedFields(deletedFieldTooling);
        Page<CompletionRateData> completionRateData = moldRepository.getCompletionRateData(payload, pageable, false);
        return getDataCompletionItemsMold(completionRateData, order.getDueDate());
    }

    private DataCompletionGroupByType getDataCompletionItemsForMachine(DataCompletionOrder order, Pageable pageable){
        DataCompletionGroupByType dataCompletionGroupByType = bindCommonData(order);
        dataCompletionGroupByType.setObjectType(ObjectType.MACHINE);
        dataCompletionGroupByType.setItems(getMachineItems(order, pageable));
        return dataCompletionGroupByType;
    }

    private Page<DataCompletionItem> getMachineItems(DataCompletionOrder order, Pageable pageable) {
        DataCompletionRatePayload payload = new DataCompletionRatePayload();
        payload.setIds(order.getMachines().stream().map(Machine::getId).collect(Collectors.toList()));
        Page<CompletionRateData> completionRateData = machineRepository.getCompletionRateData(payload, pageable, false);
        return getDataCompletionItems(completionRateData, order.getDueDate());
    }

    private Page<DataCompletionItem> getDataCompletionItems(Page<CompletionRateData> completionRateData, Instant dueDate) {
        List<DataCompletionItem> completionItems = completionRateData.getContent().stream().map(data -> this.bindData(data, dueDate)).collect(Collectors.toList());
        return new PageImpl<>(completionItems, completionRateData.getPageable(), completionRateData.getTotalElements());
    }

    private DataCompletionItem bindData(CompletionRateData completionRateData, Instant dueDate) {
        DataCompletionItem item = new DataCompletionItem();
        item.setName(completionRateData.getName());
        item.setDueDate(DateUtils.getDate(dueDate, "yyyy-MM-dd"));
        item.setCompleted(completionRateData.getRate() == 100);
        item.setData(completionRateData.getData());
        return item;
    }

    private Page<DataCompletionItem> getDataCompletionItemsMold(Page<CompletionRateData> completionRateData, Instant dueDate) {
        List<DataCompletionItem> completionItems = completionRateData.getContent().stream().map(data -> this.bindDataMold(data, dueDate)).collect(Collectors.toList());
        return new PageImpl<>(completionItems, completionRateData.getPageable(), completionRateData.getTotalElements());
    }

    private DataCompletionItem bindDataMold(CompletionRateData completionRateData, Instant dueDate) {
        DataCompletionItem item = new DataCompletionItem();
        item.setName(completionRateData.getName());
        item.setDueDate(DateUtils.getDate(dueDate, "yyyy-MM-dd"));
        item.setCompleted(completionRateData.getRate() == 100);
        MoldClone moldClone = new MoldClone();
        try {
            BeanUtils.copyProperties(moldClone, completionRateData.getData());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        item.setData(moldClone);
        return item;
    }

    private Page<DataCompletionItem> getDataCompletionItemsCategory(Page<CompletionRateData> completionRateData, Instant dueDate) {
        List<DataCompletionItem> completionItems = completionRateData.getContent().stream().map(data -> this.bindDataCategory(data, dueDate)).collect(Collectors.toList());
        return new PageImpl<>(completionItems, completionRateData.getPageable(), completionRateData.getTotalElements());
    }

    private DataCompletionItem bindDataCategory(CompletionRateData completionRateData, Instant dueDate) {
        DataCompletionItem item = new DataCompletionItem();
        item.setName(completionRateData.getName());
        item.setDueDate(DateUtils.getDate(dueDate, "yyyy-MM-dd"));
        item.setCompleted(completionRateData.getRate() == 100);
        CategoryClone categoryClone = new CategoryClone();
        try {
            BeanUtils.copyProperties(categoryClone, completionRateData.getData());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        item.setData(categoryClone);
        return item;
    }

    private DataCompletionGroupByType bindCommonData(DataCompletionOrder order){
        DataCompletionGroupByType dataCompletionGroupByType = new DataCompletionGroupByType();
        dataCompletionGroupByType.setId(order.getId());
        dataCompletionGroupByType.setOrderId(order.getOrderId());
        dataCompletionGroupByType.setManagerName(userRepository.getOne(order.getCreatedBy()).getName());
        dataCompletionGroupByType.setCompanyName(userRepository.getOne(order.getCreatedBy()).getCompany().getName());

        return dataCompletionGroupByType;
    }

    private DataCompletionOrder bindData(DataCompletionOrderPayload payload){
        DataCompletionOrder order = new DataCompletionOrder();

        order.setOrderId(payload.getOrderId());
        BigDecimal index = dataCompletionOrderRepository.findMaxIndex().orElseGet(() -> new BigDecimal(0));
        order.setOrderIndex(index.intValue() + 1);
        order.setDueDate(DateUtils.getInstant(payload.getDueDay(), DateUtils.DEFAULT_DATE_FORMAT));
        order.setDueDay(payload.getDueDay().substring(0, 8));

        List<Long> userIDs = payload.getAssignedUsers().stream().map(User::getId).collect(Collectors.toList());
        List<User> users = userRepository.findByIdInAndDeletedIsFalse(userIDs);
        order.setAssignedUsers(users);

        Pageable pageable = getPageableForListAll();
        DataCompletionRatePayload completionRatePayload = new DataCompletionRatePayload();
        completionRatePayload.setUncompletedData(true);

        order.setCompanies(getListCompanyForCompletionOrder(payload.isAllCompany(), payload.getCompanies(), pageable, completionRatePayload));
        if (CollectionUtils.isEmpty(order.getCompanies())) order.setCompletedCompany(true);
        order.setLocations(getListLocationForCompletionOrder(payload.isAllLocation(), payload.getLocations(), pageable, completionRatePayload));
        if (CollectionUtils.isEmpty(order.getLocations())) order.setCompletedLocation(true);
        order.setCategories(getListCategoryForCompletionOrder(payload.isAllCategory(), payload.getCategories(), pageable, completionRatePayload));
        if (CollectionUtils.isEmpty(order.getCategories())) order.setCompletedCategory(true);
        order.setParts(getListPartForCompletionOrder(payload.isAllPart(), payload.getParts(), pageable, completionRatePayload));
        if (CollectionUtils.isEmpty(order.getParts())) order.setCompletedPart(true);
        order.setMolds(getListMoldForCompletionOrder(payload.isAllTooling(), payload.getMolds(), pageable, completionRatePayload));
        if (CollectionUtils.isEmpty(order.getMolds())) order.setCompletedMold(true);
        order.setMachines(getListMachineForCompletionOrder(payload.isAllMachine(), payload.getMachines(), pageable, completionRatePayload));
        if (CollectionUtils.isEmpty(order.getMachines())) order.setCompletedMachine(true);

        return order;
    }

    private List<Company> getListCompanyForCompletionOrder(boolean all, List<Company> list, Pageable pageable, DataCompletionRatePayload payload){
        List<Long> ids;
        if (all){
            Page<CompletionRateData> data = companyRepository.getCompletionRateData(payload, pageable, false);
            ids = data.getContent().stream().map(CompletionRateData::getId).collect(Collectors.toList());
        } else {
            ids = list.stream().map(Company::getId).collect(Collectors.toList());
        }

        return companyRepository.findAllById(ids);
    }

    private List<Location> getListLocationForCompletionOrder(boolean all, List<Location> list, Pageable pageable, DataCompletionRatePayload payload){
        List<Long> ids;
        if (all){
            Page<CompletionRateData> data = locationRepository.getCompletionRateData(payload, pageable, false);
            ids = data.getContent().stream().map(CompletionRateData::getId).collect(Collectors.toList());
        } else {
            ids = list.stream().map(Location::getId).collect(Collectors.toList());
        }

        return locationRepository.findAllById(ids);
    }

    private List<Category> getListCategoryForCompletionOrder(boolean all, List<Category> list, Pageable pageable, DataCompletionRatePayload payload){
        List<Long> ids;
        if (all){
            Page<CompletionRateData> data = categoryRepository.getCompletionRateData(payload, pageable, false);
            ids = data.getContent().stream().map(CompletionRateData::getId).collect(Collectors.toList());
        } else {
            ids = list.stream().map(Category::getId).collect(Collectors.toList());
        }

        return categoryRepository.findAllById(ids);
    }

    private List<Part> getListPartForCompletionOrder(boolean all, List<Part> list, Pageable pageable, DataCompletionRatePayload payload){
        List<Long> ids;
        if (all){
            List<String> deletedFieldPart = getDeletedFieldsByType(ConfigCategory.PART);
            payload.setDeletedFields(deletedFieldPart);
            Page<CompletionRateData> data = partRepository.getCompletionRateData(payload, pageable, false);
            ids = data.getContent().stream().map(CompletionRateData::getId).collect(Collectors.toList());
        } else {
            ids = list.stream().map(Part::getId).collect(Collectors.toList());
        }

        return partRepository.findAllById(ids);
    }

    private List<Mold> getListMoldForCompletionOrder(boolean all, List<Mold> list, Pageable pageable, DataCompletionRatePayload payload){
        List<Long> ids;
        if (all){
            List<String> deletedFieldMold = getDeletedFieldsByType(ConfigCategory.TOOLING);
            payload.setDeletedFields(deletedFieldMold);
            Page<CompletionRateData> data = moldRepository.getCompletionRateData(payload, pageable, false);
            ids = data.getContent().stream().map(CompletionRateData::getId).collect(Collectors.toList());
        } else {
            ids = list.stream().map(Mold::getId).collect(Collectors.toList());
        }

        return moldRepository.findAllById(ids);
    }

    private List<Machine> getListMachineForCompletionOrder(boolean all, List<Machine> list, Pageable pageable, DataCompletionRatePayload payload){
        List<Long> ids;
        if (all){
            Page<CompletionRateData> data = machineRepository.getCompletionRateData(payload, pageable, false);
            ids = data.getContent().stream().map(CompletionRateData::getId).collect(Collectors.toList());
        } else {
            ids = list.stream().map(Machine::getId).collect(Collectors.toList());
        }

        return machineRepository.findAllById(ids);
    }

    private List<String> getDeletedFieldsByType(ConfigCategory category) {
        Optional<List<GeneralConfig>> generalConfigs = generalConfigRepository.findByConfigCategoryAndDeletedFieldIsTrue(category);
        return generalConfigs.map(configs -> configs.stream().map(GeneralConfig::getFieldName).collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    private Pageable getPageableForListAll(){
        return getPageableForListLimit(100000,"name");
    }

    private Pageable getPageableForListLimit20(){
        return getPageableForListLimit(20,"name");
    }
    private Pageable getPageableForListLimit(int size,String... properties){
        return new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }

            @Override
            public int getPageSize() {
                return size;
            }

            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Sort getSort() {
                return new Sort(Sort.Direction.ASC,properties);
            }

            @Override
            public Pageable next() {
                return null;
            }

            @Override
            public Pageable previousOrFirst() {
                return null;
            }

            @Override
            public Pageable first() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
    }
}
