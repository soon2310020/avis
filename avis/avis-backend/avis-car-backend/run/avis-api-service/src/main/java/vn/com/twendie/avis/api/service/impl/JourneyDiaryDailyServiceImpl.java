package vn.com.twendie.avis.api.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.core.util.ObjUtils;
import vn.com.twendie.avis.api.core.util.ValidObjectUtil;
import vn.com.twendie.avis.api.model.payload.CreateOrUpdateBlankDiaryPayload;
import vn.com.twendie.avis.api.model.payload.EditAppDiaryDailyPayload;
import vn.com.twendie.avis.api.model.payload.UpdateWithoutDriverDiaryPayload;
import vn.com.twendie.avis.api.model.projection.JDDVehicleInfoProjection;
import vn.com.twendie.avis.api.model.projection.OvertimeInfo;
import vn.com.twendie.avis.api.model.response.*;
import vn.com.twendie.avis.api.repository.JourneyDiaryDailyRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.enumtype.WorkingDayEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;
import vn.com.twendie.avis.tracking.model.tracking.VehicleWorkingTime;
import vn.com.twendie.avis.tracking.service.TrackingGpsService;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.concurrent.TimeUnit.DAYS;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;
import static vn.com.twendie.avis.data.enumtype.NormListEnum.*;
import static vn.com.twendie.avis.data.enumtype.WorkingDayEnum.FLEXIBLE;
import static vn.com.twendie.avis.data.enumtype.WorkingDayEnum.MON_TO_SAT_PLUS_2_SUN;

@Service
@Slf4j
public class JourneyDiaryDailyServiceImpl implements JourneyDiaryDailyService {

    private final JourneyDiaryDailyRepo journeyDiaryDailyRepo;

    private final ContractChangeHistoryService contractChangeHistoryService;
    private final CostTypeService costTypeService;
    private final JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService;
    private final LogContractNormListService logContractNormListService;
    private final WorkingCalendarService workingCalendarService;
    private final WorkingDayService workingDayService;
    private final ContractService contractService;
    private final NormListService normListService;
    private final BranchService branchService;
    private final ContractTypeService contractTypeService;
    private final JourneyDiaryDailyLockService journeyDiaryDailyLockService;

    private final DateUtils dateUtils;
    private final ObjUtils objUtils;
    private final ListUtils listUtils;
    private final ModelMapper modelMapper;
    private final JourneyDiarySignatureService journeyDiarySignatureService;
    private final JourneyDiaryService journeyDiaryService;
    private final TrackingGpsService trackingGpsService;

    public JourneyDiaryDailyServiceImpl(JourneyDiaryDailyRepo journeyDiaryDailyRepo,
                                        ContractChangeHistoryService contractChangeHistoryService,
                                        JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService,
                                        CostTypeService costTypeService,
                                        LogContractNormListService logContractNormListService,
                                        WorkingCalendarService workingCalendarService,
                                        WorkingDayService workingDayService,
                                        ContractService contractService,
                                        NormListService normListService,
                                        BranchService branchService,
                                        ContractTypeService contractTypeService,
                                        JourneyDiaryDailyLockService journeyDiaryDailyLockService,
                                        DateUtils dateUtils,
                                        ObjUtils objUtils,
                                        ListUtils listUtils,
                                        ModelMapper modelMapper,
                                        JourneyDiarySignatureService journeyDiarySignatureService,
                                        JourneyDiaryService journeyDiaryService, TrackingGpsService trackingGpsService) {
        this.journeyDiaryDailyRepo = journeyDiaryDailyRepo;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.costTypeService = costTypeService;
        this.journeyDiaryDailyCostTypeService = journeyDiaryDailyCostTypeService;
        this.logContractNormListService = logContractNormListService;
        this.workingCalendarService = workingCalendarService;
        this.workingDayService = workingDayService;
        this.contractService = contractService;
        this.normListService = normListService;
        this.branchService = branchService;
        this.contractTypeService = contractTypeService;
        this.journeyDiaryDailyLockService = journeyDiaryDailyLockService;
        this.dateUtils = dateUtils;
        this.objUtils = objUtils;
        this.listUtils = listUtils;
        this.modelMapper = modelMapper;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
        this.journeyDiaryService = journeyDiaryService;
        this.trackingGpsService = trackingGpsService;
    }

    @Override
    public List<JourneyDiaryDaily> findAll() {
        List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyRepo.findAllByDeletedFalse();
        fetchChildren(journeyDiaryDailies);
        journeyDiaryDailyCostTypeService.fetchJourneyDiaryDailyCostTypes(journeyDiaryDailies);
        return journeyDiaryDailies;
    }

    @Override
    public List<JourneyDiaryDaily> find(Long contractId, Timestamp fromDate, Timestamp toDate) {
        List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyRepo
                .findAllByContractIdAndDateBetweenAndParentIsNullAndDeletedFalseOrderByDateDescCreatedAtDesc(
                        contractId, fromDate, toDate);
        fetchChildren(journeyDiaryDailies);
        journeyDiaryDailyCostTypeService.fetchJourneyDiaryDailyCostTypes(journeyDiaryDailies);
        return journeyDiaryDailies;
    }

    @Override
    public List<JourneyDiaryDaily> findAndName(String name, Long contractId, Timestamp fromDate,
                                               Timestamp toDate, List<Long> memberCustomerId) {

        List<JourneyDiaryDaily> journeyDiaryDailies;
        if (memberCustomerId == null || memberCustomerId.size() == 0) {
            journeyDiaryDailies = journeyDiaryDailyRepo
                    .search(name, fromDate, toDate, contractId);
        } else {
            journeyDiaryDailies = journeyDiaryDailyRepo.searchByMemberCustomerId(memberCustomerId, fromDate, toDate, contractId);
        }
        fetchChildren(journeyDiaryDailies);
        journeyDiaryDailyCostTypeService.fetchJourneyDiaryDailyCostTypes(journeyDiaryDailies);
        return journeyDiaryDailies;
    }

    @Override
    public List<JourneyDiaryDaily> findNameInMonth(String name, Long contractId, Timestamp month, List<Long> memberCustomerIds) {
        List<JourneyDiaryDaily> journeyDiaryDailies;
        if (memberCustomerIds == null || memberCustomerIds.size() == 0) {
            journeyDiaryDailies = journeyDiaryDailyRepo
                    .searchMonth(name, month, contractId);
        } else {
            journeyDiaryDailies = journeyDiaryDailyRepo.searchMonth(memberCustomerIds, month, contractId);
        }
        fetchChildren(journeyDiaryDailies);
        journeyDiaryDailyCostTypeService.fetchJourneyDiaryDailyCostTypes(journeyDiaryDailies);
        return journeyDiaryDailies;
    }

    @Override
    public List<JourneyDiaryDaily> findInMonth(Long contractId, Timestamp month) {
        List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyRepo
                .findByContractIdAndMonthAndParentIsNullAndDeletedFalseOrderByDateDescCreatedAtDesc(contractId,
                        dateUtils.getFirstDayOfMonth(month));
        fetchChildren(journeyDiaryDailies);
        return journeyDiaryDailies;
    }

    @Override
    public List<JDDVehicleInfoProjection> findJDDTicketFeeInfo(Timestamp fromDate, Timestamp toDate, String branchCode,
                                                               Collection<String> vehicleNumberPlates) {
        if (CollectionUtils.isEmpty(vehicleNumberPlates)) {
            return journeyDiaryDailyRepo.findJDDTicketFeeInfo(fromDate, toDate, branchCode,
                    AvisApiConstant.TICKET_FEE_REPORT_COST_CODES,
                    ContractTypeEnum.WITH_DRIVER.value());
        } else {
            return journeyDiaryDailyRepo.findJDDTicketFeeInfo(fromDate, toDate, branchCode,
                    AvisApiConstant.TICKET_FEE_REPORT_COST_CODES,
                    ContractTypeEnum.WITH_DRIVER.value(),
                    vehicleNumberPlates);
        }
    }

    @Override
    public JourneyDiaryDaily findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }

        return journeyDiaryDailyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot find journey diary daily with id: " + id)
                        .displayMessage(Translator.toLocale("jdd.error.not_found")));
    }

    @Override
    public void fetchChildren(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        if (!CollectionUtils.isEmpty(journeyDiaryDailies)) {
            Set<Long> parentIds = journeyDiaryDailies.stream()
                    .map(JourneyDiaryDaily::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<JourneyDiaryDaily> children = journeyDiaryDailyRepo.findByParentIdInAndDeletedFalse(parentIds);
            children.forEach(child -> child.setChildren(Sets.newHashSet()));
            Map<Long, Set<JourneyDiaryDaily>> childrenMap = children.stream()
                    .collect(Collectors.groupingBy(JourneyDiaryDaily::getParentId, Collectors.toSet()));
            journeyDiaryDailies.forEach(journeyDiaryDaily -> journeyDiaryDaily.setChildren(
                    childrenMap.getOrDefault(journeyDiaryDaily.getId(), Sets.newHashSet())
            ));
        }
    }

    @Override
    public Boolean editAppDiary(EditAppDiaryDailyPayload payload) {
        validWithLockTime(payload.getDate());

        ValidObjectUtil.trimReflectAndNormalizeString(payload);
        JourneyDiaryDaily currentJDD = findById(payload.getId());
        Contract contract = currentJDD.getContract();

        contractService.validContractType(ContractTypeEnum.WITH_DRIVER,
                currentJDD.getContract().getContractType());

        boolean needUpdateKM = false;
        boolean needUpdateOT = false;

        Long lastWorkingDayId = getLastWorkingDayId(contract, currentJDD);

        avoidChangeAfterContractTermination(currentJDD.getContract(), payload.getDate());
        avoidUpdateDiaryDate(currentJDD, payload.getDate());
        checkUpdateIsWeekend(currentJDD, payload.getIsWeekend(), lastWorkingDayId);
        checkAndThrowExceptionIfIsParent(currentJDD);
        checkUpdateWorkingTime(payload.getWorkingTimeGpsFrom(),
                payload.getWorkingTimeGpsTo(), "jdd.error.working_time_gps_is_invalid");
        checkUpdateKM(payload.getKmStart(),
                payload.getKmCustomerGetIn(),
                payload.getKmCustomerGetOut(),
                payload.getKmEnd());
        avoidUpdateNullValueCostType(payload.getJourneyDiaryDailyCostTypes());
        validUpdateDriverNameAndVehicleNumberPlate(currentJDD, payload);

        if (!payload.getKmStart().equals(currentJDD.getKmStart())
                || !payload.getKmEnd().equals(currentJDD.getKmEnd())
                || !payload.getKmCustomerGetIn().equals(currentJDD.getKmCustomerGetIn())
                || !payload.getKmCustomerGetOut().equals(currentJDD.getKmCustomerGetOut())) {
            needUpdateKM = true;
        }

        if (!payload.getWorkingTimeGpsFrom().equals(currentJDD.getWorkingTimeGpsFrom())
                || !payload.getWorkingTimeGpsTo().equals(currentJDD.getWorkingTimeGpsTo())) {
            needUpdateOT = true;
        }

        if (!payload.getIsWeekend().equals(currentJDD.getIsWeekend())) {
            needUpdateKM = true;
            needUpdateOT = true;
        }

        modelMapper.map(payload, currentJDD);
        updateJourneyDiaryDailyCosts(currentJDD, payload.getJourneyDiaryDailyCostTypes());

        JourneyDiaryDaily updatedJDD = journeyDiaryDailyRepo.save(currentJDD);

        if (needUpdateKM) {
            updateKmNumber(updatedJDD, true);
        }

        if (needUpdateOT) {
            updateOverTime(updatedJDD, true);
        }

        updateOvernight(updatedJDD);
        updateCosts(updatedJDD);

        return true;
    }

    private Long getLastWorkingDayId(Contract contract, JourneyDiaryDaily currentJDD) {
        Timestamp toDate = getEndDate(contract, currentJDD.getDate());

        String workingDayHistoryValue = contractChangeHistoryService.findLastChangeOfField(
                contract.getId(), WORKING_DAY_ID.getId(), dateUtils.getTomorrow(toDate));

        return Objects.isNull(workingDayHistoryValue) ? contract.getWorkingDay().getId()
                : Long.parseLong(workingDayHistoryValue);
    }

    private void validUpdateDriverNameAndVehicleNumberPlate(JourneyDiaryDaily currentJDD, EditAppDiaryDailyPayload payload) {
        if (Objects.isNull(currentJDD.getVehicle())) {
            if (StringUtils.isBlank(payload.getVehicleNumberPlate())) {
                throw new BadRequestException("Vehicle number plate is blank!!!")
                        .displayMessage(Translator.toLocale("vehicle.valid_error.number_plate_wrong_format"));
            } else {
                currentJDD.setVehicleNumberPlate(payload.getVehicleNumberPlate());
            }
        }

        if (Objects.isNull(currentJDD.getDriver())) {
            if (StringUtils.isBlank(payload.getDriverName())) {
                throw new BadRequestException("Driver name plate is blank!!!")
                        .displayMessage(Translator.toLocale("driver.error.driver_name_is_invalid"));
            } else {
                currentJDD.setDriverName(payload.getDriverName());
            }
        }
    }

    private void avoidChangeAfterContractTermination(Contract contract, Timestamp date) {
        Timestamp endDate = Objects.isNull(contract.getDateEarlyTermination()) ?
                contract.getToDatetime()
                : dateUtils.min(contract.getDateEarlyTermination(), contract.getToDatetime());
        if (date.after(endDate)) {
            throw new BadRequestException("Cannot change diary after contract end date!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.cannot_make_change_after_contract_end"));
        }
    }

    private void avoidUpdateNullValueCostType(List<CodeValueModel> journeyDiaryDailyCostTypes) {
        if (!CollectionUtils.isEmpty(journeyDiaryDailyCostTypes)
                && journeyDiaryDailyCostTypes.stream()
                .anyMatch(x -> x.getId() != null && x.getValue() == null)) {
            throw new BadRequestException("Cost type is null!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.cost_type_value_is_invalid"));
        }
    }

    private void checkUpdateKM(BigDecimal kmStart,
                               BigDecimal kmCustomerGetIn,
                               BigDecimal kmCustomerGetOut,
                               BigDecimal kmEnd) {
        if (kmStart.compareTo(kmCustomerGetIn) > 0) {
            throw new BadRequestException("kmStart > kmCustomerGetIn")
                    .displayMessage(Translator.toLocale("jdd.error.km_start_bigger_than_km_customer_get_in"));
        }

        if (kmCustomerGetIn.compareTo(kmCustomerGetOut) > 0) {
            throw new BadRequestException("kmCustomerGetIn > kmCustomerGetOut")
                    .displayMessage(Translator.toLocale("jdd.error.km_customer_get_in_bigger_than_km_customer_get_out"));
        }

        if (kmCustomerGetOut.compareTo(kmEnd) > 0) {
            throw new BadRequestException("kmCustomerGetOut > kmEnd")
                    .displayMessage(Translator.toLocale("jdd.error.km_customer_get_out_bigger_than_km_end"));
        }
    }

    private void checkUpdateWorkingTime(Time workingTimeFrom, Time workingTimeTo, String s) {
        if (workingTimeFrom.getTime() >= workingTimeTo.getTime()
                || workingTimeTo.getTime() - workingTimeFrom.getTime() < 60000) {
            throw new BadRequestException("Working time From > To or To - From less than a minute!!!")
                    .displayMessage(Translator.toLocale(s));
        }
    }

    private void updateJourneyDiaryDailyCosts(JourneyDiaryDaily currentJDD,
                                              List<CodeValueModel> costs) {
        List<JourneyDiaryDailyCostType> journeyDiaryDailyCostTypes = currentJDD.getJourneyDiaryDailyCostTypes();
        journeyDiaryDailyCostTypes.forEach(journeyDiaryDailyCostType -> {
            BigDecimal newValue = costs.stream()
                    .filter(cost -> journeyDiaryDailyCostType.getCostType().getCode().equals(cost.getCode()))
                    .map(CodeValueModel::getValue)
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(newValue)) {
                journeyDiaryDailyCostType.setValue(newValue);
            }
        });

        journeyDiaryDailyCostTypeService.saveAll(journeyDiaryDailyCostTypes);
    }

    private void avoidUpdateDiaryDate(JourneyDiaryDaily currentJDD, Timestamp date) {
        if (!currentJDD.getDate().equals(date)) {
            throw new BadRequestException("JDD date is immutable!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.cannot_change_date"));
        }
    }

    @Override
    public Boolean editManuallyCreatedDiary(CreateOrUpdateBlankDiaryPayload payload) {
        validWithLockTime(payload.getDate());

        ValidObjectUtil.trimReflectAndNormalizeString(payload);
        JourneyDiaryDaily currentJDD = findById(payload.getId());

        contractService.validContractType(ContractTypeEnum.WITH_DRIVER,
                currentJDD.getContract().getContractType());

        boolean needUpdateKM = false;
        boolean needUpdateOT = false;

        Long lastWorkingDayId = getLastWorkingDayId(currentJDD.getContract(), currentJDD);

        avoidChangeAfterContractTermination(currentJDD.getContract(), payload.getDate());
        avoidUpdateDiaryDate(currentJDD, payload.getDate());
        checkUpdateIsWeekend(currentJDD, payload.getIsWeekend(), lastWorkingDayId);
        checkAndThrowExceptionIfIsParent(currentJDD);
        checkUpdateWorkingTime(payload.getWorkingTimeGpsFrom(), payload.getWorkingTimeGpsTo(), "jdd.error.working_time_gps_is_invalid");
        checkUpdateKM(payload.getKmStart(),
                payload.getKmCustomerGetIn(),
                payload.getKmCustomerGetOut(),
                payload.getKmEnd());
        avoidUpdateNullValueCostType(payload.getJourneyDiaryDailyCostTypes());

        if (payload.getIsSelfDrive()) {
            validUpdateToSelfDrive(payload);
        } else {
            validUpdateToWithDriver(payload);
        }

        if (!payload.getKmStart().equals(currentJDD.getKmStart())
                || !payload.getKmEnd().equals(currentJDD.getKmEnd())
                || !payload.getKmCustomerGetIn().equals(currentJDD.getKmCustomerGetIn())
                || !payload.getKmCustomerGetOut().equals(currentJDD.getKmCustomerGetOut())) {
            needUpdateKM = true;
        }

        if (!payload.getWorkingTimeGpsFrom().equals(currentJDD.getWorkingTimeGpsFrom())
                || !payload.getWorkingTimeGpsTo().equals(currentJDD.getWorkingTimeGpsTo())) {
            needUpdateOT = true;
        }

        if (!payload.getIsSelfDrive().equals(currentJDD.getIsSelfDrive())
                || !payload.getIsWeekend().equals(currentJDD.getIsWeekend())) {
            needUpdateKM = true;
            needUpdateOT = true;
        }

        modelMapper.map(payload, currentJDD);
        updateJourneyDiaryDailyCosts(currentJDD, payload.getJourneyDiaryDailyCostTypes());

        JourneyDiaryDaily updatedJDD = journeyDiaryDailyRepo.save(currentJDD);
        updatedJDD.setFlagCreatedManually(true);

        updateSelfDrive(updatedJDD);

        if (checkAndUpdateOverDay(lastWorkingDayId, updatedJDD)) {
            needUpdateKM = true;
            needUpdateOT = true;
        }

        if (needUpdateKM) {
            updateKmNumber(updatedJDD, true);
        }

        if (needUpdateOT) {
            updateOverTime(updatedJDD, true);
        }

        updateOvernight(updatedJDD);
        updateCosts(updatedJDD);

        return true;
    }

    private Boolean checkAndUpdateOverDay(Long lastWorkingDayId, JourneyDiaryDaily currentJDD) {
        if (lastWorkingDayId.equals(FLEXIBLE.getId())) {
            Timestamp from = getStartDate(currentJDD.getContract(),
                    dateUtils.getFirstDayOfMonth(currentJDD.getDate()));

            Timestamp to = getEndDate(currentJDD.getContract(),
                    dateUtils.getLastDayOfMonth(currentJDD.getDate()));

            Integer workingDayValue = contractService.getContractValueAtTime(currentJDD.getContract(),
                    WORKING_DAY.getName(), dateUtils.getTomorrow(to), Integer.class);

            double diff = (dateUtils.getDaysBetween(from, to) + 1) * 1.0 / dateUtils.getMonthDays(from);

            if (currentJDD.getIsSelfDrive()) {
                currentJDD.setIsOverDay(false);
            }

            List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyRepo.findWrongOverDayJourneyDairyDaily(currentJDD.getContract().getId(),
                    (int) Math.floor(workingDayValue * diff), from, to);

            if (CollectionUtils.isEmpty(journeyDiaryDailies)) {
                return false;
            } else {
                updateKmAndOtOfOverDayDays(journeyDiaryDailies, currentJDD);
                return true;
            }
        }

        return false;
    }

    private void updateKmAndOtOfOverDayDays(List<JourneyDiaryDaily> journeyDiaryDailies,
                                            JourneyDiaryDaily currentJDD) {
        List<JourneyDiaryDaily> needUpdate = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getChildren)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(j -> !j.getIsSelfDrive())
                .collect(Collectors.toList());
        needUpdate.addAll(journeyDiaryDailies);

        if (needUpdate.contains(currentJDD)) {
            currentJDD.setIsOverDay(!currentJDD.getIsOverDay());
            needUpdate.remove(currentJDD);
        } else if ((Objects.nonNull(currentJDD.getParentId())
                && needUpdate.contains(currentJDD.getParent()))) {
            currentJDD.getParent().setIsOverDay(!currentJDD.getIsOverDay());
            currentJDD.setIsOverDay(!currentJDD.getIsOverDay());
            needUpdate.remove(currentJDD);
            needUpdate.remove(currentJDD.getParent());
        }

        needUpdate.forEach(j -> {
            j.setIsOverDay(!j.getIsOverDay());
            updateOverKm(j, false);
            updateOverTime(j, false);
        });
    }

    private void validUpdateToWithDriver(CreateOrUpdateBlankDiaryPayload payload) {
        if (StringUtils.isBlank(payload.getDriverName())) {
            throw new BadRequestException("Driver name must not be blank!!!")
                    .displayMessage(Translator.toLocale("jdd.error.driver_name_is_invalid"));
        }

        if (StringUtils.isBlank(payload.getCustomerNameUsed())) {
            throw new BadRequestException("Customer name must not be blank!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.customer_name_used_is_invalid"));
        }

        if (StringUtils.isBlank(payload.getTripItinerary())) {
            throw new BadRequestException("Trip itinerary must not be blank!!!")
                    .displayMessage(Translator.toLocale("jdd.error.trip_itinerary_is_invalid"));
        }

        if (StringUtils.isBlank(payload.getDriverName())) {
            throw new BadRequestException("Driver name must not be blank!!!")
                    .displayMessage(Translator.toLocale("driver.error.driver_name_is_invalid"));
        }
    }

    private void validUpdateToSelfDrive(CreateOrUpdateBlankDiaryPayload payload) {
        if (payload.getOvernight() != null && payload.getOvernight() > 0) {
            throw new BadRequestException("Self Drive cannot overnight!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.cannot_update_over_night_self_drive"));
        }

        payload.setDriverName(null);
    }

    private void checkAndThrowExceptionIfIsParent(JourneyDiaryDaily currentJDD) {
        if (!CollectionUtils.isEmpty(currentJDD.getChildren())) {
            throw new BadRequestException("Cannot edit parent Journey!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.cannot_edit_parent_journey"));
        }
    }

    @Override
    public Boolean delete(Long jddId) {
        JourneyDiaryDaily currentJDD = findById(jddId);

        contractService.validContractType(ContractTypeEnum.WITH_DRIVER,
                currentJDD.getContract().getContractType());
        validWithLockTime(currentJDD.getDate());

        if (!currentJDD.getFlagCreatedManually()
                || currentJDD.getKmStart() == null
                || currentJDD.getJourneyDiaryId() != null) {
            throw new BadRequestException("Cannot delete none manually created jdd!!!")
                    .displayMessage(Translator.toLocale("jdd.delete_error.cannot_delete_jdd_created_by_system"));
        }

        currentJDD.setDeleted(true);

        Long lastWorkingDayId = getLastWorkingDayId(currentJDD.getContract(), currentJDD);

        if (currentJDD.getParentId() != null) {

            int count = journeyDiaryDailyRepo.countByParentIdAndDeletedFalse(currentJDD.getParentId());

            JourneyDiaryDaily lastChild = journeyDiaryDailyRepo.findFirstByParentIdAndDeletedFalse(currentJDD.getParentId());

            if (count == 1) {
                currentJDD.getParent().setDeleted(true);
                lastChild.setParent(null);
            }

            currentJDD.setParent(null);

            JourneyDiaryDaily parent = lastChild.getParent();

            if (!Objects.isNull(parent)) {

                List<JourneyDiaryDaily> currentChildren = parent.getChildren();
                currentChildren.remove(currentJDD);
                parent.setChildren(currentChildren);

                updateSelfDrive(lastChild);
                checkAndUpdateOverDay(lastWorkingDayId, lastChild);

                updateOvernight(lastChild);
                updateKmNumber(lastChild, true);
                updateOverTime(lastChild, true);
                updateCosts(lastChild);
            } else {
                checkAndUpdateOverDay(lastWorkingDayId, lastChild);
            }

        } else {
            List<JourneyDiaryDaily> childes = currentJDD.getChildren();
            JourneyDiaryDaily newRow;

            if (!CollectionUtils.isEmpty(childes)) {
                if (childes.stream().anyMatch(x -> x.getJourneyDiaryId() != null)) {
                    throw new BadRequestException("Cannot delete when having system created children")
                            .displayMessage(Translator.toLocale("jdd.delete_error.cannot_delete_jdd_having_children_created_by_system"));
                } else {
                    childes.forEach(x -> {
                        x.setDeleted(true);
                        x.setParent(null);
                    });

                    newRow = createEmptyRow(currentJDD);
                }
            } else {
                newRow = createEmptyRow(currentJDD);
            }

            checkAndUpdateOverDay(lastWorkingDayId, newRow);
        }

        return true;
    }

    private JourneyDiaryDaily createEmptyRow(JourneyDiaryDaily currentJDD) {
        JourneyDiaryDaily emptyRow = journeyDiaryDailyRepo.save(JourneyDiaryDaily.builder()
                .contract(currentJDD.getContract())
                .date(currentJDD.getDate())
                .isHoliday(currentJDD.getIsHoliday())
                .isSelfDrive(false)
                .build());

        Timestamp toDate = getEndDate(currentJDD.getContract(), currentJDD.getDate());

        String workingDayHistoryValue = contractChangeHistoryService.findLastChangeOfField(
                currentJDD.getContractId(), WORKING_DAY_ID.getId(), dateUtils.getTomorrow(toDate));

        Long lastWorkingDayId = Objects.isNull(workingDayHistoryValue) ? currentJDD.getContract().getWorkingDay().getId()
                : Long.parseLong(workingDayHistoryValue);

        if (!lastWorkingDayId.equals(WorkingDayEnum.MON_TO_SAT_PLUS_2_SUN.getId())) {
            emptyRow.setIsWeekend(currentJDD.getIsWeekend());
        }

        createEmptyCosts(emptyRow);
        return emptyRow;

    }

    @Override
    public Boolean create(CreateOrUpdateBlankDiaryPayload payload) {
        validWithLockTime(payload.getDate());

        ValidObjectUtil.trimReflectAndNormalizeString(payload);
        payload.setId(null);
        Contract contract = contractService.findById(payload.getContractId());

        contractService.validContractType(ContractTypeEnum.WITH_DRIVER,
                contract.getContractType());
        avoidChangeAfterContractTermination(contract, payload.getDate());
        checkUpdateWorkingTime(payload.getWorkingTimeGpsFrom(), payload.getWorkingTimeGpsTo(), "jdd.error.working_time_gps_is_invalid");
        checkUpdateKM(payload.getKmStart(),
                payload.getKmCustomerGetIn(),
                payload.getKmCustomerGetOut(),
                payload.getKmEnd());
        avoidUpdateNullValueCostType(payload.getJourneyDiaryDailyCostTypes());

        if (payload.getIsSelfDrive()) {
            validUpdateToSelfDrive(payload);
        } else {
            validUpdateToWithDriver(payload);
        }

        JourneyDiaryDaily newJDD = journeyDiaryDailyRepo.save(modelMapper.map(payload, JourneyDiaryDaily.class));

        List<JourneyDiaryDaily> otherJDDs = journeyDiaryDailyRepo.findByContractIdAndDateAndDeletedFalse(payload.getContractId(), payload.getDate());

        if (CollectionUtils.isEmpty(otherJDDs)) {
            throw new NotFoundException("Cannot find empty row in date: " + payload.getDate().getTime());
        }

        if (otherJDDs.stream()
                .map(JourneyDiaryDaily::getParent)
                .filter(Objects::isNull)
                .count() > 1) {
            throw new BadRequestException("Concurrent insert!!!")
                    .displayMessage(Translator.toLocale("jdd.error.parent_already_exist"));
        }

        createEmptyCosts(newJDD);
        updateJourneyDiaryDailyCosts(newJDD, payload.getJourneyDiaryDailyCostTypes());
        newJDD.setFlagCreatedManually(true);
        JourneyDiaryDaily existJDD = otherJDDs.get(0);

        newJDD.setContract(contract);
        newJDD.setIsHoliday(existJDD.getIsHoliday());

        Optional<JourneyDiaryDaily> existWithDriverJDD = otherJDDs.stream()
                .filter(j -> !j.getIsSelfDrive())
                .findFirst();

        if (!newJDD.getIsSelfDrive() && existWithDriverJDD.isPresent()) {
            newJDD.setIsOverDay(existWithDriverJDD.get().getIsOverDay());
        }

        Long lastWorkingDayId = getLastWorkingDayId(contract, newJDD);

        if (otherJDDs.size() == 1) {
            otherJDDs.add(newJDD);
            createParentDiary(otherJDDs);
        } else {
            JourneyDiaryDaily parent = otherJDDs
                    .stream()
                    .filter(x -> Objects.isNull(x.getParent()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Not found parent JDD in date: " + payload.getDate().getTime()));

            List<JourneyDiaryDaily> currentChildren = parent.getChildren();
            currentChildren.add(newJDD);
            parent.setChildren(currentChildren);

            newJDD.setParent(parent);
        }

        checkUpdateIsWeekend(newJDD, payload.getIsWeekend(), lastWorkingDayId);

        updateSelfDrive(newJDD);
        checkAndUpdateOverDay(lastWorkingDayId, newJDD);

        updateOvernight(newJDD);
        updateKmNumber(newJDD, true);
        updateOverTime(newJDD, true);
        updateCosts(newJDD);

        return true;
    }

    @Override
    public JourneyDiaryDaily createEmptyJourneyDiaryDaily(Contract contract, Timestamp date) {
        if (contract.getContractType().getId().equals(WITH_DRIVER.value())) {
            return JourneyDiaryDaily.builder()
                    .contract(contract)
                    .date(date)
                    .isHoliday(workingCalendarService.isHoliday(date, contract.getWorkingDay().getId()))
                    .isWeekend(workingCalendarService.isWeekend(date, contract.getWorkingDay().getId()))
                    .build();
        } else {
            return JourneyDiaryDaily.builder()
                    .contract(contract)
                    .month(dateUtils.getFirstDayOfMonth(date))
                    .date(date)
                    .isHoliday(false)
                    .isWeekend(false)
                    .isSelfDrive(false)
                    .build();
        }
    }

    @Override
    public List<JourneyDiaryDaily> createEmptyJourneyDiaryDailies(Contract contract) {
        return createEmptyJourneyDiaryDailies(contract, dateUtils.getDatesBetween(contract.getFromDatetime(),
                dateUtils.subtract(dateUtils.startOfToday(), 1, DAYS)));
    }

    @Override
    public Boolean editWithoutDriverDiary(UpdateWithoutDriverDiaryPayload payload) {
        JourneyDiaryDaily jdd = findById(payload.getId());

        contractService.validContractType(ContractTypeEnum.WITHOUT_DRIVER,
                jdd.getContract().getContractType());
        validWithLockTime(jdd.getMonth());

        boolean needUpdate = validEditWithoutDriverDiary(payload, jdd);

        modelMapper.map(payload, jdd);
        save(jdd);

        if (needUpdate) {
            updateWithoutDriverDiaryKmNumber(jdd);
        }

        return true;
    }

    private void validWithLockTime(Timestamp date) {
        JourneyDiaryDailyLock journeyDiaryDailyLock = journeyDiaryDailyLockService.find();

        if (Objects.nonNull(journeyDiaryDailyLock) &&
                journeyDiaryDailyLock.getLockTime().compareTo(dateUtils.getFirstDayOfMonth(date)) >= 0) {
            throw new BadRequestException("Diary has been locked!!!")
                    .displayMessage(Translator.toLocale("jdd.error.diary_has_been_lock"));
        }
    }

    private void updateWithoutDriverDiaryKmNumber(JourneyDiaryDaily jdd) {
        if (Objects.nonNull(jdd.getKmStart()) && Objects.nonNull(jdd.getKmEnd())) {
            validUpdateKMWithoutDriver(jdd.getKmStart(), jdd.getKmEnd());
            jdd.setUsedKm(jdd.getKmEnd().subtract(jdd.getKmStart()).max(ZERO));
            jdd.setTotalKm(jdd.getUsedKm());
        } else if (Objects.nonNull(jdd.getKmStart())) {
            JourneyDiaryDaily endJDD = journeyDiaryDailyRepo
                    .findFirstByJourneyDiaryIdAndDeletedFalseOrderByIdDesc(jdd.getJourneyDiaryId());
            validUpdateKMWithoutDriver(jdd.getKmStart(), endJDD.getKmEnd());
            jdd.setUsedKm(endJDD.getKmEnd().subtract(jdd.getKmStart()).max(ZERO));
            jdd.setTotalKm(jdd.getUsedKm());
        } else if (Objects.nonNull(jdd.getKmEnd())) {
            JourneyDiaryDaily startJDD = journeyDiaryDailyRepo
                    .findFirstByJourneyDiaryIdAndDeletedFalseOrderById(jdd.getJourneyDiaryId());
            validUpdateKMWithoutDriver(startJDD.getKmStart(), jdd.getKmEnd());
            startJDD.setUsedKm(jdd.getKmEnd().subtract(startJDD.getKmStart()).max(ZERO));
            startJDD.setTotalKm(startJDD.getUsedKm());
        }

        if (Objects.nonNull(jdd.getParent())) {
            List<JourneyDiaryDaily> children = jdd.getParent().getChildren();
            jdd.getParent().setKmStart(children.get(0).getKmStart());
            jdd.getParent().setKmEnd(children.get(children.size() - 1).getKmEnd());
            jdd.getParent().setUsedKm(children.stream().map(JourneyDiaryDaily::getUsedKm)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(null));
        }
    }

    private void validUpdateKMWithoutDriver(BigDecimal kmStart, BigDecimal kmEnd) {
        if (kmEnd.compareTo(kmStart) < 0) {
            throw new BadRequestException("KmStart is greater than KmEnd")
                    .displayMessage(Translator.toLocale("jdd.error.km_start_bigger_than_km_end"));
        }
    }

    private boolean validEditWithoutDriverDiary(UpdateWithoutDriverDiaryPayload payload,
                                                JourneyDiaryDaily jdd) {
        boolean needUpdate = false;

        if (Objects.isNull(jdd.getKmStart()) && Objects.isNull(jdd.getKmEnd())) {
            if (Objects.nonNull(payload.getKmStart()) || Objects.nonNull(payload.getKmEnd())) {
                throw new BadRequestException("Cannot edit blank JDD!!!")
                        .displayMessage(Translator.toLocale("jdd.create_or_update_blank_diary.error.cannot_edit_system_created_diary"));
            }
        }

        checkAndThrowExceptionIfIsParent(jdd);

        if (Objects.nonNull(jdd.getKmStart())) {
            if (Objects.isNull(payload.getKmStart())) {
                throw new BadRequestException("KmStart is invalid!!!")
                        .displayMessage(Translator.toLocale("jdd.error.km_start_is_invalid"));
            } else if (!jdd.getKmStart().equals(payload.getKmStart())) {
                needUpdate = true;
            }
        } else if (Objects.nonNull(payload.getKmStart())) {
            throw new BadRequestException("KmStart is invalid!!!")
                    .displayMessage(Translator.toLocale("jdd.error.km_start_is_invalid"));
        }

        if (Objects.nonNull(jdd.getKmEnd())) {
            if (Objects.isNull(payload.getKmEnd())) {
                throw new BadRequestException("KmEnd is invalid!!!")
                        .displayMessage(Translator.toLocale("jdd.error.km_end_is_invalid"));
            } else if (!jdd.getKmEnd().equals(payload.getKmEnd())) {
                needUpdate = true;
            }
        } else if (Objects.nonNull(payload.getKmEnd())) {
            throw new BadRequestException("KmEnd is invalid!!!")
                    .displayMessage(Translator.toLocale("jdd.error.km_end_is_invalid"));
        }

        return needUpdate;
    }

    private void createParentDiary(List<JourneyDiaryDaily> childes) {
        JourneyDiaryDaily firstJDD = childes.get(0);
        JourneyDiaryDaily lastJDD = childes.get(childes.size() - 1);
        JourneyDiaryDaily parent = journeyDiaryDailyRepo.save(JourneyDiaryDaily.builder()
                .contract(firstJDD.getContract())
                .date(dateUtils.startOfDay(firstJDD.getDate()))
                .kmStart(firstJDD.getKmStart())
                .kmCustomerGetIn(firstJDD.getKmCustomerGetIn())
                .kmCustomerGetOut(lastJDD.getKmCustomerGetOut())
                .kmEnd(lastJDD.getKmEnd())
                .workingTimeAppFrom(firstJDD.getWorkingTimeAppFrom())
                .workingTimeAppTo(lastJDD.getWorkingTimeAppTo())
                .workingTimeGpsFrom(firstJDD.getWorkingTimeGpsFrom())
                .workingTimeGpsTo(lastJDD.getWorkingTimeGpsTo())
                .isHoliday(firstJDD.getIsHoliday())
                .isWeekend(firstJDD.getIsWeekend())
                .isSelfDrive(childes.stream().allMatch(JourneyDiaryDaily::getIsSelfDrive))
                .flagCreatedManually(true)
                .children(childes)
                .build());

        parent.setChildren(childes);
        childes.forEach(x -> x.setParent(parent));

        createEmptyCosts(parent);
    }

    private JourneyDiaryDaily createEmptyCosts(JourneyDiaryDaily journeyDiaryDaily) {
        List<JourneyDiaryDailyCostType> journeyDiaryDailyCostTypes = costTypeService.findAll()
                .stream()
                .filter(costType -> JourneyDiaryCostTypeEnum.codes().contains(costType.getCode()))
                .map(costType -> JourneyDiaryDailyCostType.builder()
                        .journeyDiaryDaily(journeyDiaryDaily)
                        .costType(costType)
                        .build())
                .collect(Collectors.toList());
        journeyDiaryDaily.setJourneyDiaryDailyCostTypes(journeyDiaryDailyCostTypes);
        journeyDiaryDailyCostTypeService.saveAll(journeyDiaryDailyCostTypes);
        return journeyDiaryDaily;
    }

    private void checkUpdateIsWeekend(JourneyDiaryDaily jdd, Boolean isWeekend, Long lastWorkingDayId) {
        Timestamp date = jdd.getDate();

        if (!lastWorkingDayId.equals(WorkingDayEnum.MON_TO_SAT_PLUS_2_SUN.getId())
                && !isWeekend.equals(workingCalendarService.isWeekend(date, lastWorkingDayId))) {
            throw new BadRequestException("Cannot update is weekend!!!")
                    .displayMessage(Translator.toLocale("jdd.edit_error.cannot_update_is_weekend"));
        }

        JourneyDiaryDaily parent = jdd.getParent();
        if (Objects.nonNull(parent)) {
            parent.setIsWeekend(isWeekend);
            parent.getChildren().forEach(x -> x.setIsWeekend(isWeekend));
        }

    }

    @Override
    public Timestamp getEndDate(Contract contract, Timestamp toDate) {
        return dateUtils.min(dateUtils.getLastDayOfMonth(toDate), contract.getToDatetime(),
                contract.getDateEarlyTermination());
    }

    @Override
    public Timestamp getStartDate(Contract contract, Timestamp fromDate) {
        return dateUtils.max(contract.getFromDatetime(), dateUtils.startOfDay(fromDate));
    }

    @Override
    public List<JourneyDiaryDaily> find(Long journeyDiaryId) {
        return journeyDiaryDailyRepo.findAllByJourneyDiaryIdAndDeletedFalseOrderByDate(journeyDiaryId);
    }

    @Override
    public JourneyDiaryDaily save(JourneyDiaryDaily journeyDiaryDaily) {
        return journeyDiaryDailyRepo.save(journeyDiaryDaily);
    }

    @Override
    public List<JourneyDiaryDaily> saveAll(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        if (CollectionUtils.isEmpty(journeyDiaryDailies)) {
            return Collections.emptyList();
        } else {
            return journeyDiaryDailyRepo.saveAll(journeyDiaryDailies);
        }
    }

    @Override
    public JourneyDiaryDaily updateSelfDrive(JourneyDiaryDaily journeyDiaryDaily) {
        if (Objects.nonNull(journeyDiaryDaily.getParent())) {
            updateSelfDrive(journeyDiaryDaily.getParent());
        } else if (!CollectionUtils.isEmpty(journeyDiaryDaily.getChildren())) {
            journeyDiaryDaily.setIsSelfDrive(journeyDiaryDaily.getChildren()
                    .stream()
                    .allMatch(JourneyDiaryDaily::getIsSelfDrive));
            save(journeyDiaryDaily);
        }

        if (journeyDiaryDaily.getIsSelfDrive()) {
            journeyDiaryDaily.setIsOverDay(false);
        }
        return journeyDiaryDaily;
    }

    @Override
    public JourneyDiaryDaily updateOverTime(JourneyDiaryDaily journeyDiaryDaily, boolean needUpdateParent) {
        return updateOverTime(journeyDiaryDaily, journeyDiaryDaily.getContract(), needUpdateParent);
    }

    @Override
    public JourneyDiaryDaily updateOverTime(JourneyDiaryDaily journeyDiaryDaily,
                                            Contract contract,
                                            boolean needUpdateParent) {

        Timestamp date = dateUtils.endOfDay(journeyDiaryDaily.getDate());

        Time contractWorkingTimeFrom = null;
        Time contractWorkingTimeTo = null;

        if (!journeyDiaryDaily.getIsSelfDrive()) {
            if (journeyDiaryDaily.getIsWeekend() || journeyDiaryDaily.getIsHoliday() || journeyDiaryDaily.getIsOverDay()) {
                contractWorkingTimeFrom = contractService.getContractValueAtTime(contract,
                        WORKING_TIME_WEEKEND_HOLIDAY_FROM.getName(), date, Time.class);
                contractWorkingTimeTo = contractService.getContractValueAtTime(contract,
                        WORKING_TIME_WEEKEND_HOLIDAY_TO.getName(), date, Time.class);
            } else {
                contractWorkingTimeFrom = contractService.getContractValueAtTime(contract,
                        WORKING_TIME_FROM.getName(), date, Time.class);
                contractWorkingTimeTo = contractService.getContractValueAtTime(contract,
                        WORKING_TIME_TO.getName(), date, Time.class);
            }
        }

        if (CollectionUtils.isEmpty(journeyDiaryDaily.getChildren())) {
            if (journeyDiaryDaily.getIsSelfDrive()) {
                journeyDiaryDaily.setOverTime(null);
            } else {
                if (journeyDiaryDaily.getFlagMultiDate()) {
                    List<JourneyDiaryDaily> inDayJourneys = find(journeyDiaryDaily.getJourneyDiaryId());
                    JourneyDiaryDaily firstJourney = inDayJourneys.get(0);
                    JourneyDiaryDaily lastJourney = inDayJourneys.get(inDayJourneys.size() - 1);
                    if (journeyDiaryDaily.equals(firstJourney)) {
                        Long overTime = dateUtils.getOverTime(firstJourney.getDate(), lastJourney.getDate(),
                                firstJourney.getWorkingTimeGpsFrom(), lastJourney.getWorkingTimeGpsTo(),
                                contractWorkingTimeFrom, contractWorkingTimeTo);
                        firstJourney.setOverTime(overTime);
                    } else {
                        updateOverTime(firstJourney, true);
                    }
                }
                if (Objects.nonNull(journeyDiaryDaily.getWorkingTimeGpsFrom()) &&
                        Objects.nonNull(journeyDiaryDaily.getWorkingTimeGpsTo())) {
                    Long overTime = dateUtils.getOverTime(journeyDiaryDaily.getWorkingTimeGpsFrom(), journeyDiaryDaily.getWorkingTimeGpsTo(),
                            contractWorkingTimeFrom, contractWorkingTimeTo);
                    journeyDiaryDaily.setOverTime(overTime);
                }
            }
        } else {
            List<JourneyDiaryDaily> driveChildren = journeyDiaryDaily.getChildren()
                    .stream()
                    .filter(child -> !child.getIsSelfDrive())
                    .collect(Collectors.toList());
            if (!driveChildren.isEmpty()) {
                JourneyDiaryDaily firstChild = driveChildren.get(0);
                JourneyDiaryDaily lastChild = driveChildren.get(driveChildren.size() - 1);
                Long overTime = dateUtils.getOverTime(firstChild.getDate(), lastChild.getDate(),
                        firstChild.getWorkingTimeGpsFrom(), lastChild.getWorkingTimeGpsTo(),
                        contractWorkingTimeFrom, contractWorkingTimeTo);
                journeyDiaryDaily.setIsSelfDrive(false);
                journeyDiaryDaily.setWorkingTimeGpsFrom(firstChild.getWorkingTimeGpsFrom());
                journeyDiaryDaily.setWorkingTimeGpsTo(lastChild.getWorkingTimeGpsTo());
                journeyDiaryDaily.setOverTime(overTime);
            } else {
                JourneyDiaryDaily firstChild = journeyDiaryDaily.getChildren().get(0);
                JourneyDiaryDaily lastChild = journeyDiaryDaily.getChildren().get(journeyDiaryDaily.getChildren().size() - 1);
                journeyDiaryDaily.setIsSelfDrive(true);
                journeyDiaryDaily.setWorkingTimeGpsFrom(firstChild.getWorkingTimeGpsFrom());
                journeyDiaryDaily.setWorkingTimeGpsTo(lastChild.getWorkingTimeGpsTo());
                journeyDiaryDaily.setOverTime(null);
            }
        }
        save(journeyDiaryDaily);
        if (Objects.nonNull(journeyDiaryDaily.getParent()) && needUpdateParent) {
            updateOverTime(journeyDiaryDaily.getParent(), contract, false);
        }
        return journeyDiaryDaily;
    }

    @Override
    public JourneyDiaryDaily updateOverKm(JourneyDiaryDaily journeyDiaryDaily, boolean needUpdateParent) {
        return updateOverKm(journeyDiaryDaily, journeyDiaryDaily.getContract(), needUpdateParent);
    }

    @Override
    public JourneyDiaryDaily updateOverKm(JourneyDiaryDaily journeyDiaryDaily, Contract contract, boolean needUpdateParent) {

        BigDecimal totalUsedKm = journeyDiaryDaily.getUsedKm();
        BigDecimal totalUsedKmSelfDrive = journeyDiaryDaily.getUsedKmSelfDrive();
        BigDecimal kmNorm, kmSelfDriveNorm;
        BigDecimal overKm = null, overKmSelfDrive = null;

        if (Objects.nonNull(journeyDiaryDaily.getParent())) {
            updateOverKm(journeyDiaryDaily.getParent(), contract, false);
        } else {
            if (journeyDiaryDaily.getIsHoliday()) {
                kmNorm = normListService.getContractNorm(contract, HOLIDAY_KM_NORM.code());
                kmSelfDriveNorm = normListService.getContractNorm(contract, SELF_DRIVE_HOLIDAY_KM_NORM.code());
            } else if (journeyDiaryDaily.getIsWeekend()) {
                kmNorm = normListService.getContractNorm(contract, WEEKEND_KM_NORM.code());
                kmSelfDriveNorm = normListService.getContractNorm(contract, SELF_DRIVE_WEEKEND_KM_NORM.code());
            } else if (journeyDiaryDaily.getIsOverDay()) {
                kmNorm = normListService.getContractNorm(contract, WEEKEND_KM_NORM.code());
                kmSelfDriveNorm = normListService.getContractNorm(contract, SELF_DRIVE_NORMAL_DAY_KM_NORM.code());
            } else {
                kmNorm = null;
                kmSelfDriveNorm = normListService.getContractNorm(contract, SELF_DRIVE_NORMAL_DAY_KM_NORM.code());
            }
            if (objUtils.nonNull(totalUsedKm, kmNorm)) {
                overKm = totalUsedKm.compareTo(kmNorm) > 0 ? totalUsedKm.subtract(kmNorm) : ZERO;
            }
            if (objUtils.nonNull(totalUsedKmSelfDrive, kmSelfDriveNorm)) {
                overKmSelfDrive = totalUsedKmSelfDrive.compareTo(kmSelfDriveNorm) > 0 ?
                        totalUsedKmSelfDrive.subtract(kmSelfDriveNorm) : ZERO;
            }
            journeyDiaryDaily.setOverKm(overKm);
            journeyDiaryDaily.setOverKmSelfDrive(overKmSelfDrive);

            journeyDiaryDaily.getChildren().forEach(child -> {
                child.setOverKm(null);
                child.setOverKmSelfDrive(null);
            });
            saveAll(journeyDiaryDaily.getChildren());
        }

        return save(journeyDiaryDaily);
    }

    @Override
    public JourneyDiaryDaily updateKmNumber(JourneyDiaryDaily journeyDiaryDaily, boolean needUpdateParent) {
        Contract contract = journeyDiaryDaily.getContract();
        if (CollectionUtils.isEmpty(journeyDiaryDaily.getChildren())) {
            if (objUtils.nonNull(journeyDiaryDaily.getKmStart(), journeyDiaryDaily.getKmCustomerGetIn(),
                    journeyDiaryDaily.getKmCustomerGetOut(), journeyDiaryDaily.getKmEnd())) {
                journeyDiaryDaily.setEmptyKm(
                        journeyDiaryDaily.getKmEnd().subtract(journeyDiaryDaily.getKmCustomerGetOut())
                                .add(journeyDiaryDaily.getKmCustomerGetIn().subtract(journeyDiaryDaily.getKmStart())));
                journeyDiaryDaily.setTotalKm(journeyDiaryDaily.getKmEnd().subtract(journeyDiaryDaily.getKmStart()));
                BigDecimal usedKm = contract.getIncludeEmptyKm() ? journeyDiaryDaily.getTotalKm() :
                        journeyDiaryDaily.getTotalKm().subtract(journeyDiaryDaily.getEmptyKm());
                if (journeyDiaryDaily.getIsSelfDrive()) {
                    journeyDiaryDaily.setUsedKm(null);
                    journeyDiaryDaily.setUsedKmSelfDrive(usedKm);
                } else {
                    journeyDiaryDaily.setUsedKm(usedKm);
                    journeyDiaryDaily.setUsedKmSelfDrive(null);
                }
            } else {
                journeyDiaryDaily.setEmptyKm(null);
                journeyDiaryDaily.setTotalKm(null);
                journeyDiaryDaily.setUsedKm(null);
                journeyDiaryDaily.setUsedKmSelfDrive(null);
            }
            if (Objects.nonNull(journeyDiaryDaily.getParent()) && needUpdateParent) {
                updateKmNumber(journeyDiaryDaily.getParent(), false);
            }
        } else {
            List<JourneyDiaryDaily> children = journeyDiaryDaily.getChildren()
                    .stream()
                    .filter(child -> child.getIsSelfDrive().equals(journeyDiaryDaily.getIsSelfDrive()))
                    .collect(Collectors.toList());
            JourneyDiaryDaily firstChild = children.get(0);
            JourneyDiaryDaily lastChild = children.get(children.size() - 1);
            journeyDiaryDaily.setKmStart(firstChild.getKmStart());
            journeyDiaryDaily.setKmCustomerGetIn(firstChild.getKmCustomerGetIn());
            journeyDiaryDaily.setKmCustomerGetOut(lastChild.getKmCustomerGetOut());
            journeyDiaryDaily.setKmEnd(lastChild.getKmEnd());
            journeyDiaryDaily.setEmptyKm(journeyDiaryDaily.getChildren()
                    .stream()
                    .map(JourneyDiaryDaily::getEmptyKm)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(null));
            journeyDiaryDaily.setTotalKm(journeyDiaryDaily.getChildren()
                    .stream()
                    .map(JourneyDiaryDaily::getTotalKm)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(null));
            journeyDiaryDaily.setUsedKm(journeyDiaryDaily.getChildren()
                    .stream()
                    .map(JourneyDiaryDaily::getUsedKm)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(null));
            journeyDiaryDaily.setUsedKmSelfDrive(journeyDiaryDaily.getChildren()
                    .stream()
                    .map(JourneyDiaryDaily::getUsedKmSelfDrive)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(null));
        }
        return updateOverKm(journeyDiaryDaily, needUpdateParent);
    }

    @Override
    public JourneyDiaryDaily updateCosts(JourneyDiaryDaily journeyDiaryDaily) {
        if (Objects.nonNull(journeyDiaryDaily.getParent())) {
            updateCosts(journeyDiaryDaily.getParent());
        } else if (!CollectionUtils.isEmpty(journeyDiaryDaily.getChildren())) {
            Map<CostType, BigDecimal> totalChildrenCosts = journeyDiaryDaily.getChildren()
                    .stream()
                    .map(JourneyDiaryDaily::getJourneyDiaryDailyCostTypes)
                    .flatMap(Collection::stream)
                    .filter(journeyDiaryDailyCostType -> Objects.nonNull(journeyDiaryDailyCostType.getValue()))
                    .collect(Collectors.groupingBy(JourneyDiaryDailyCostType::getCostType))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                            .map(JourneyDiaryDailyCostType::getValue)
                            .reduce(BigDecimal::add)
                            .orElse(ZERO)));
            List<JourneyDiaryDailyCostType> parentCosts = journeyDiaryDaily.getJourneyDiaryDailyCostTypes();
            parentCosts.forEach(journeyDiaryDailyCostType -> {
                BigDecimal value = totalChildrenCosts.getOrDefault(journeyDiaryDailyCostType.getCostType(), null);
                journeyDiaryDailyCostType.setValue(value);
            });
            journeyDiaryDailyCostTypeService.saveAll(parentCosts);
        }
        return journeyDiaryDaily;
    }

    @Override
    public JourneyDiaryDaily updateOvernight(JourneyDiaryDaily journeyDiaryDaily) {
        if (Objects.nonNull(journeyDiaryDaily.getParent())) {
            updateOvernight(journeyDiaryDaily.getParent());
        } else if (!CollectionUtils.isEmpty(journeyDiaryDaily.getChildren())) {
            journeyDiaryDaily.setOvernight(journeyDiaryDaily.getChildren()
                    .stream()
                    .filter(child -> !child.getIsSelfDrive())
                    .filter(child -> Objects.nonNull(child.getOvernight()))
                    .mapToInt(JourneyDiaryDaily::getOvernight)
                    .sum());
            save(journeyDiaryDaily);
        }
        return journeyDiaryDaily;
    }

    @Override
    public JourneyDiaryDaily updateWorkingCalendar(JourneyDiaryDaily journeyDiaryDaily) {
        Contract contract = journeyDiaryDaily.getContract();
        Timestamp date = journeyDiaryDaily.getDate();
        WorkingDay workingDay = workingDayService.getContractWorkingDayAtTime(contract, dateUtils.getLastDayOfMonth(date));
        return updateWorkingCalendar(journeyDiaryDaily, workingDay);
    }

    @Override
    public JourneyDiaryDaily updateWorkingCalendar(JourneyDiaryDaily journeyDiaryDaily, WorkingDay workingDay) {
        Timestamp date = journeyDiaryDaily.getDate();
        if (workingDay.getId().equals(MON_TO_SAT_PLUS_2_SUN.getId()) || workingDay.getId().equals(FLEXIBLE.getId())) {
            journeyDiaryDaily.setIsWeekend(false);
        } else {
            journeyDiaryDaily.setIsWeekend(workingCalendarService.isWeekend(date, workingDay.getId()));
        }
        journeyDiaryDaily.setIsHoliday(workingCalendarService.isHoliday(date, workingDay.getId()));
        return save(journeyDiaryDaily);
    }

    @Override
    public DateStatistic getDateStatistic(Contract contract, List<JourneyDiaryDaily> journeyDiaryDailies, Timestamp from, Timestamp to) {
        if (contract.getContractType().getId().equals(WITH_DRIVER.value())) {
            WorkingDay workingDay = workingDayService.getContractWorkingDayAtTime(contract, to);
            Integer workingDayValue = contractService.getContractValueAtTime(contract, WORKING_DAY.getName(), to, Integer.class);
            long contractWorkingDays = workingDayService.countContractWorkingDays(workingDay, workingDayValue, to);
            long realWorkingDays = journeyDiaryDailies.stream()
                    .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsSelfDrive())
                    .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getUsedKm()))
                    .count();
            return DateStatistic.builder()
                    .datesOfMonth(dateUtils.getMonthDays(from))
                    .contractWorkingDays(contractWorkingDays)
                    .realWorkingDays(realWorkingDays)
                    .build();
        } else {
            return DateStatistic.builder()
                    .datesOfMonth(dateUtils.getMonthDays(from))
                    .build();
        }
    }

    @Override
    public BigDecimal calculateTotalOverKm(Contract contract, List<JourneyDiaryDaily> journeyDiaryDailies, Timestamp from, Timestamp to) {
        BigDecimal kmNorm = logContractNormListService.getNormListAtTime(contract, CONTRACT_KM_NORM.code(), to);
        kmNorm = kmNorm.multiply(BigDecimal.valueOf(dateUtils.getDaysBetween(from, to) + 1))
                .divide(BigDecimal.valueOf(dateUtils.getMonthDays(from)), 0, HALF_DOWN);
        BigDecimal totalUsedKm = journeyDiaryDailies.stream()
                .filter(journeyDiaryDaily -> !(journeyDiaryDaily.getIsHoliday() || journeyDiaryDaily.getIsWeekend() ||
                        journeyDiaryDaily.getIsOverDay() || journeyDiaryDaily.getIsSelfDrive()))
                .map(JourneyDiaryDaily::getUsedKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);
        BigDecimal overKmWeekendHoliday = journeyDiaryDailies.stream()
                .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsSelfDrive())
                .map(JourneyDiaryDaily::getOverKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);
        BigDecimal overKmNormalDay = totalUsedKm.compareTo(kmNorm) > 0 ? totalUsedKm.subtract(kmNorm) : ZERO;
        return overKmNormalDay.add(overKmWeekendHoliday);
    }

    @Override
    public void fetchJourneyDiaryDailies(Collection<Contract> contracts, Timestamp from, Timestamp to) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream().map(Contract::getId).collect(Collectors.toSet());
            List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyRepo
                    .findByContractIdInAndDateOrMonthBetweenAndParentIsNullAndDeletedFalse(contractIds, from, to);
            Map<Long, List<JourneyDiaryDaily>> journeyDiaryDailiesMap = journeyDiaryDailies.stream()
                    .collect(Collectors.groupingBy(JourneyDiaryDaily::getContractId));
            fetchChildren(journeyDiaryDailies);
            contracts.forEach(contract -> contract.setJourneyDiaryDailies(
                    journeyDiaryDailiesMap.getOrDefault(contract.getId(), Lists.newArrayList())
            ));
        }
    }

    @Override
    public Map<Long, OvertimeInfo> findOvertimeInfos(Collection<Long> contractIds, Timestamp from, Timestamp to) {
        return journeyDiaryDailyRepo.findOvertimeInfos(contractIds, from, to).stream()
                .collect(Collectors.toMap(OvertimeInfo::getContractId, o -> o));
    }

    @Override
//    @Cacheable(cacheNames = "JDDFilterOptions", key = "#root.method.name")
    public JDDFilterOptionsWrapper getFilterOptions() {
        JourneyDiaryDailyLock journeyDiaryDailyLock = journeyDiaryDailyLockService.find();
        return JDDFilterOptionsWrapper.builder()
                .branchDTOS(branchService.getBranchInfos())
                .contractTypeDTOS(listUtils.mapAll(contractTypeService.findAll(), ContractTypeDTO.class))
                .journeyDiaryDailyLockDTO(Objects.isNull(journeyDiaryDailyLock) ? null :
                        modelMapper.map(journeyDiaryDailyLock, JourneyDiaryDailyLockDTO.class))
                .build();
    }

    @Override
    public void fetchDataSignature(List<JourneyDiaryDailyDTO> journeyDiaryDailyDTOS) {
        journeyDiaryDailyDTOS.forEach(jour -> {
            List<String> imageUrls = new ArrayList<>();
            List<String> commentList = new ArrayList<>();
            boolean signed = true;
            boolean hasJourneyDiary = false;
            if (jour.getJourneyDiaryId() != null) {
                JourneyDiary journeyDiary = journeyDiaryService.findById(jour.getJourneyDiaryId());
                JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiaryId(journeyDiary.getId());
                if (journeyDiarySignature != null) {
                    hasJourneyDiary = true;
                    if (journeyDiarySignature.getSignatureImageUrl() != null) {
                        imageUrls.add(journeyDiarySignature.getSignatureImageUrl());
                    }
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(journeyDiarySignature.getComment())) {
                        commentList.add(journeyDiarySignature.getComment());
                    }
                    signed = journeyDiarySignature.getStatus() != null ? journeyDiarySignature.getStatus() : false;
                    if (journeyDiarySignature.getMemberCustomer() != null) {
                        MemberCustomer memberCustomer = journeyDiarySignature.getMemberCustomer();
                        jour.setCustomerNameUsed(memberCustomer.getName());
                        jour.setCustomerDepartment(memberCustomer.getDepartment());
                    }

                }
            }

            if (jour.getChildren() != null) {
                for (JourneyDiaryDailyDTO jourChildren : jour.getChildren()) {
                    if (jourChildren.getJourneyDiaryId() != null) {
                        JourneyDiary journeyDiary = journeyDiaryService.findById(jourChildren.getJourneyDiaryId());
                        JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiaryId(journeyDiary.getId());
                        if (journeyDiarySignature != null) {
                            hasJourneyDiary = true;
                            if (journeyDiarySignature.getSignatureImageUrl() != null) {
                                jourChildren.setSignatureImageUrl(Collections.singletonList(journeyDiarySignature.getSignatureImageUrl()));
                                imageUrls.add(journeyDiarySignature.getSignatureImageUrl());
                            }
                            jourChildren.setSignatureStatus(journeyDiarySignature.getStatus());
                            if (signed)
                                signed = journeyDiarySignature.getStatus() != null ? journeyDiarySignature.getStatus() : false;
                        }
                    }
                }
            }

            jour.setSignatureImageUrl(imageUrls);
            jour.setSignatureStatus(hasJourneyDiary ? signed : null);
            jour.setSignatureComment(commentList);
        });
    }

    @Override
    public void fixTimeGPS() {
        int partitionSize = 100;
        for (int i = 0; ; i++) {
            try {
                Page<JourneyDiaryDaily> originJddPage = journeyDiaryDailyRepo.findByParentIsNotNullAndDateIsNotNull(
                        PageRequest.of(i, partitionSize, Sort.by("id").descending()));
                if (originJddPage.getContent().size() < 1) break;
                List<JourneyDiaryDaily> updateJddList
                        = originJddPage.stream().map(s -> {
                    //update time gps
                    List<VehicleActivity> vehicleActivities;
                    try {
                        String vehiclePlate;
                        if (Objects.nonNull(s.getVehicle())) vehiclePlate = s.getVehicle().getNumberPlate();
                        else vehiclePlate = s.getContract().getVehicle().getNumberPlate();
                        if (Objects.isNull(vehiclePlate)) throw new Exception("Vehicle plate is null");
                        Timestamp from = Objects.isNull(s.getWorkingTimeAppFrom()) ? dateUtils.startOfDay(s.getDate()) : timestampFromTimeAndTimestamp(s.getDate(), s.getWorkingTimeAppFrom());
                        Timestamp to = Objects.isNull(s.getWorkingTimeAppTo()) ? dateUtils.endOfDay(s.getDate()) : timestampFromTimeAndTimestamp(s.getDate(), s.getWorkingTimeAppTo());
                        vehicleActivities = trackingGpsService.getVehicleActivities(
                                vehiclePlate
                                , from
                                , to);
                        log.info("TOTAL ELEMENT OF LIST: " + vehicleActivities.size());
                    } catch (Exception e) {
                        log.error("Error when get vehicles activities: {}", ExceptionUtils.getRootCauseMessage(e));
                        vehicleActivities = new ArrayList<>();
                    }
                    VehicleWorkingTime vehicleWorkingTime = trackingGpsService.getVehicleWorkingTime(vehicleActivities);
                    s.setWorkingTimeGpsFrom(vehicleWorkingTime.getFrom());
                    s.setWorkingTimeGpsTo(vehicleWorkingTime.getTo());

                    //update overtime
                    if (Objects.isNull(s.getWorkingTimeGpsFrom()) && Objects.isNull(s.getWorkingTimeGpsTo()))
                        s.setOverTime(null);
                    else updateOverTime(s, false);

                    return s;
                }).collect(Collectors.toList());
                journeyDiaryDailyRepo.saveAll(updateJddList);
                log.info("Number element of list: " + originJddPage.getContent().size());
                log.info("First element id: " + originJddPage.getContent().get(0).getId());
                log.info("Last element index: " + originJddPage.getContent().get(originJddPage.getContent().size() - 1).getId());
                log.info("Number element updated: " + updateJddList.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void fixOverKM() {
        int partitionSize = 100;
        int[] remove = {0};
        int[] add = {0};
        for (int i = 0; ; i++) {
            Page<JourneyDiaryDaily> originPage = journeyDiaryDailyRepo.findByParentIsNullAndDateIsNotNull(PageRequest.of(i, partitionSize, Sort.by("id").descending()));
            log.info("Total elements: " + originPage.getTotalElements());
            if (originPage.getContent().size() < 1) break;

            List<JourneyDiaryDaily> jddUpdatedList = new ArrayList<>();
            originPage.stream().forEach(s -> {
                if (checkIsSaturdayAndOverKmWrong(s)) {
                    s.setOverKm(null);
                    jddUpdatedList.add(s);
                    log.info("Remove overKm of: " + dateUtils.format(s.getDate(), "EEEE dd/MM/yyyy"));
                    remove[0]++;
                }
                if (checkIsWeekendAndOverKmNull(s)) {
                    updateOverKm(s, false);
                    log.info("Add overKm of: " + dateUtils.format(s.getDate(), "EEEE dd/MM/yyyy"));
                    add[0]++;
                }
            });
            journeyDiaryDailyRepo.saveAll(jddUpdatedList);

            log.info("-----------------------------------------------------------------------------------");
        }
        log.info("Size of list remove: " + remove[0]);
        log.info("Size of list add: " + add[0]);
    }

    private boolean checkIsSaturdayAndOverKmWrong(JourneyDiaryDaily journeyDiaryDaily) {
        //check saturday
        boolean isSaturday = journeyDiaryDaily.getDate().toInstant().atZone(ZoneId.of(dateUtils.LOCAL_TIME_ZONE)).getDayOfWeek().getValue() == 6;

        //check weekend
        boolean notIsWeekend = Objects.isNull(journeyDiaryDaily.getIsWeekend()) || journeyDiaryDaily.getIsWeekend() == false;

        //check overKM not null
        boolean overKmNotNull = Objects.nonNull(journeyDiaryDaily.getOverKm());

        return overKmNotNull && isSaturday && notIsWeekend;
    }

    private boolean checkIsWeekendAndOverKmNull(JourneyDiaryDaily journeyDiaryDaily) {

        //check weekend
        boolean isWeekend = Objects.nonNull(journeyDiaryDaily.getIsWeekend()) && journeyDiaryDaily.getIsWeekend() == true;

        //check overKM not null
        boolean overKmIsNull = Objects.isNull(journeyDiaryDaily.getOverKm());

        //sunday
        boolean isMonToSat = false;
        try {
            isMonToSat = journeyDiaryDaily.getContract().getWorkingDay().getId().equals(WorkingDayEnum.MON_TO_SAT.getId());
        } catch (Exception e) {
        }

        return overKmIsNull && isWeekend && isMonToSat;
    }

    private Timestamp timestampFromTimeAndTimestamp(Timestamp timestamp, Time time) {
        try {
            String date = dateUtils.format(timestamp, "dd/MM/yyyy");
            String datetime = date + " " + convert(time);
            return new Timestamp(dateUtils.parseWithTimeZone(datetime, "dd/MM/yyyy HH:mm:ss").getTime());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private String convert(Time time) {
        String timeStr = "--:--";
        if (time != null) {
            String[] times = time.toString().split(":");
            if (times.length == 3) {
                timeStr = times[0] + ":" + times[1] + ":" + times[2].substring(0, 2);
            }
        }
        return timeStr;
    }

}
