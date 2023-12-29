package vn.com.twendie.avis.api.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.javatuples.Quartet;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.adapter.ContractDriverHistoryAdapter;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.PageUtils;
import vn.com.twendie.avis.api.core.util.ReflectUtils;
import vn.com.twendie.avis.api.core.util.ValidUtils;
import vn.com.twendie.avis.api.model.payload.ContractClonePayload;
import vn.com.twendie.avis.api.model.payload.ContractCodeSuggestionPayload;
import vn.com.twendie.avis.api.model.projection.ContractCodeProjection;
import vn.com.twendie.avis.api.model.projection.ContractInfoForNotiProjection;
import vn.com.twendie.avis.api.model.projection.FirstJourneyDiaryDateProjection;
import vn.com.twendie.avis.api.model.response.*;
import vn.com.twendie.avis.api.predicate.ContractPredicateBuilder;
import vn.com.twendie.avis.api.repository.*;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.enumtype.*;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.notification.service.NotificationService;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.*;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITHOUT_DRIVER;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;
import static vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum.CONTINUE;
import static vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum.POSTPONE;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.EXTEND_STATUS;
import static vn.com.twendie.avis.data.enumtype.NotificationContentEnum.*;

@Service
@CacheConfig(cacheNames = "Contract")
public class ContractServiceImpl implements ContractService {

    private final ContractRepo contractRepo;
    private final BranchRepo branchRepo;
    private final FuelTypeRepo fuelTypeRepo;
    private final RentalServiceTypeRepo rentalServiceTypeRepo;
    private final WorkingDayRepo workingDayRepo;
    private final TimeUsePolicyRepo timeUsePolicyRepo;
    private final JourneyDiaryRepo journeyDiaryRepo;

    private final ContractDriverHistoryService contractDriverHistoryService;
    private final NotificationService notificationService;
    private final CustomerService customerService;
    private final MemberCustomerService memberCustomerService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final ContractChangeHistoryService contractChangeHistoryService;
    private final MappingFieldCodeFontendService mappingFieldCodeFontendService;

    private final ModelMapper modelMapper;
    private final DateUtils dateUtils;
    private final ReflectUtils reflectUtils;

    @Autowired
    public ContractServiceImpl(ContractRepo contractRepo,
                               BranchRepo branchRepo,
                               FuelTypeRepo fuelTypeRepo,
                               RentalServiceTypeRepo rentalServiceTypeRepo,
                               WorkingDayRepo workingDayRepo,
                               TimeUsePolicyRepo timeUsePolicyRepo,
                               JourneyDiaryRepo journeyDiaryRepo,
                               ContractDriverHistoryService contractDriverHistoryService,
                               NotificationService notificationService,
                               @Lazy CustomerService customerService,
                               @Lazy MemberCustomerService memberCustomerService,
                               UserService userService,
                               VehicleService vehicleService,
                               ContractChangeHistoryService contractChangeHistoryService,
                               MappingFieldCodeFontendService mappingFieldCodeFontendService,
                               ModelMapper modelMapper,
                               DateUtils dateUtils,
                               ReflectUtils reflectUtils) {
        this.contractRepo = contractRepo;
        this.branchRepo = branchRepo;
        this.fuelTypeRepo = fuelTypeRepo;
        this.rentalServiceTypeRepo = rentalServiceTypeRepo;
        this.workingDayRepo = workingDayRepo;
        this.timeUsePolicyRepo = timeUsePolicyRepo;
        this.journeyDiaryRepo = journeyDiaryRepo;
        this.contractDriverHistoryService = contractDriverHistoryService;
        this.notificationService = notificationService;
        this.customerService = customerService;
        this.memberCustomerService = memberCustomerService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.mappingFieldCodeFontendService = mappingFieldCodeFontendService;
        this.modelMapper = modelMapper;
        this.dateUtils = dateUtils;
        this.reflectUtils = reflectUtils;
    }

    @Override
//    @Cacheable(key = "#p0", condition = "#p0 != null")
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Contract findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return contractRepo.findByIdAndDeletedFalse(id).orElseThrow(() ->
                new NotFoundException("Not found contract with id: " + id)
                        .displayMessage(Translator.toLocale("contract.not_found")));
    }

    @Override
    public List<Contract> findContractsInTimeRange(Collection<Long> contractTypeIds, Timestamp from, Timestamp to) {
        return contractRepo.findAllByContractTypeIdInAndToDatetimeAfterAndFromDatetimeBeforeAndDeletedFalse(
                contractTypeIds, from, to);
    }

    @Override
//    @Cacheable(cacheNames = "CreateContractOptionsWrapper", key = "#p0", condition = "#p0 != null")
    public CreateContractOptionsWrapper getOptions(Long id) {
        List<BranchDTO> branches = branchRepo.findAllByDeletedFalse().stream()
                .map(branch -> modelMapper.map(branch, BranchDTO.class)).collect(Collectors.toList());
        List<FuelTypeDTO> fuelTypes = fuelTypeRepo.findAllByDeletedFalseOrderById().stream()
                .map(fuel -> modelMapper.map(fuel, FuelTypeDTO.class)).collect(Collectors.toList());
        List<RentalServiceTypeDTO> rentalServiceTypes = rentalServiceTypeRepo.findAllByDeletedFalseAndContractTypeIdOrderById(id).stream()
                .map(rst -> modelMapper.map(rst, RentalServiceTypeDTO.class)).collect(Collectors.toList());
        List<WorkingDayDTO> workingDayTypes = workingDayRepo.findAllByDeletedFalseOrderById().stream()
                .map(wdt -> modelMapper.map(wdt, WorkingDayDTO.class)).collect(Collectors.toList());
        List<TimeUsePolicyDTO> overTimePolicies = timeUsePolicyRepo.findAllByDeletedFalseOrderById().stream()
                .map(otp -> modelMapper.map(otp, TimeUsePolicyDTO.class)).collect(Collectors.toList());
        return CreateContractOptionsWrapper.builder()
                .branches(branches)
                .fuelTypes(fuelTypes)
                .policyWrapper(OverTimePolicyWrapper.builder()
                        .overTimePolicies(overTimePolicies)
                        .groupId(overTimePolicies.get(0).getGroupId())
                        .build())
                .rentalServiceTypes(rentalServiceTypes)
                .workingDayTypes(workingDayTypes)
                .build();
    }

    @Override
    public ContractStatistic getContractStatistic(ContractTypeEnum contractTypeEnum) {
        switch (contractTypeEnum) {
            case WITH_DRIVER:
                return ContractStatistic.builder()
                        .waitingAssignCar(contractRepo.countByContractTypeIdAndStatusAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), WAITING_ASSIGN_CAR.getCode(), false, false))
                        .assignedCar(contractRepo.countByContractTypeIdAndStatusAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), ASSIGNED_CAR.getCode(), false, false))
                        .inProgress(contractRepo.countByContractTypeIdAndStatusAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), IN_PROGRESS.getCode(), false, false))
                        .canceled(contractRepo.countByContractTypeIdAndStatusAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), CANCELED.getCode(), false, false))
                        .finished(contractRepo.countByContractTypeIdAndStatusAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), FINISHED.getCode(), false, false))
                        .lendingCar(contractRepo.countByContractTypeIdAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), true, false))
                        .lendingDriver(contractRepo.countByContractTypeIdAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), false, true))
                        .lendingCarAndDriver(contractRepo.countByContractTypeIdAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
                                WITH_DRIVER.value(), true, true))
                        .build();
            case WITHOUT_DRIVER:
                return ContractStatistic.builder()
                        .waitingAssignCar(contractRepo.countByContractTypeIdAndStatusAndDeletedFalse(
                                WITHOUT_DRIVER.value(), WAITING_ASSIGN_CAR.getCode()))
                        .assignedCar(contractRepo.countByContractTypeIdAndStatusAndDeletedFalse(
                                WITHOUT_DRIVER.value(), ASSIGNED_CAR.getCode()))
                        .inProgress(contractRepo.countByContractTypeIdAndStatusAndDeletedFalse(
                                WITHOUT_DRIVER.value(), IN_PROGRESS.getCode()))
                        .canceled(contractRepo.countByContractTypeIdAndStatusAndDeletedFalse(
                                WITHOUT_DRIVER.value(), CANCELED.getCode()))
                        .finished(contractRepo.countByContractTypeIdAndStatusAndDeletedFalse(
                                WITHOUT_DRIVER.value(), FINISHED.getCode()))
                        .build();
            default:
                return null;
        }
    }

    @Override
    public GeneralPageResponse<ContractCodeDTO> getContractCodeSuggestionByPrefix(ContractCodeSuggestionPayload payload) {
        if (StringUtils.isBlank(payload.getCodePrefix())) {
            return GeneralPageResponse.toResponse(Page.empty());
        }

        String prefixLike = "%" + ValidUtils.normalizeString(payload.getCodePrefix().trim().toUpperCase()) + "%";

        List<ContractCodeProjection> queryResult = contractRepo.getContractCodeByPrefixLikeAndDeleted(
                prefixLike, payload.getDeleted());
        List<ContractCodeDTO> finalResult = queryResult.stream()
                .map(entity -> ContractCodeDTO.builder()
                        .code(entity.getCode())
                        .suffixCode(String.format("%03d", Integer.parseInt(entity.getSubCode()) + 1))
                        .build()).collect(Collectors.toList());

        return GeneralPageResponse.toResponse(PageUtils
                .toPage(payload.getPage(), payload.getSize(), finalResult));
    }

    @Override
    public boolean existsCode(String code) {
        return contractRepo.existsByCode(code);
    }

    @Override
    public String validateContractCode(String prefixCode, String suffixCode) {
        String code = String.format("%s - %s", prefixCode.trim(), suffixCode);
        if (existsCode(code)) {
            throw new BadRequestException("Already exist contract with code: " + code)
                    .displayMessage(Translator.toLocale("contract.contract_code_exists"));
        }
        return code;
    }

    @Override
//    @CachePut(key = "#result.id")
    public Contract save(Contract contract) {
        return contractRepo.save(contract);
    }

    @Override
    public boolean inStatus(Contract contract, ContractStatusEnum... status) {
        return ContractPredicateBuilder.inStatus(status).test(contract);
    }

    @Override
    public void assignTerm(Contract contract) {
        int term = dateUtils.getMonthsBetween(contract.getFromDatetime(), contract.getToDatetime());
        contract.setTerm(term);
    }

    @Override
    public void assignStatus(Contract contract) {
        int status = Objects.isNull(contract.getVehicle()) ? WAITING_ASSIGN_CAR.getCode() :
                dateUtils.startOfDay(contract.getFromDatetime()).after(dateUtils.now()) ? ASSIGNED_CAR.getCode() :
                        dateUtils.endOfDay(contract.getToDatetime()).after(dateUtils.now()) ?
                                IN_PROGRESS.getCode() : FINISHED.getCode();
        contract.setStatus(status);
    }

    @Override
    public Boolean deleteContract(Long contractId) {
        Contract contract = contractRepo.findById(contractId).orElseThrow(() ->
                new NotFoundException("Not found contract with id: " + contractId));

        if (contract.isDeleted()) {
            throw new BadRequestException("Contract already deleted!!!")
                    .displayMessage(Translator.toLocale("contract.already_deleted"));
        }

        if (!contract.isDeleted() && !contract.getDriverIsTransferredAnother()
                && !contract.getVehicleIsTransferredAnother()
                && !ContractStatusEnum.CANCELED.getCode().equals(contract.getStatus())
                && dateUtils.now().before(dateUtils.endOfDay(contract.getToDatetime()))
                && (
                contract.getVehicle() == null
                        || contract.getDriver() == null
        )) {
            contract.setDeleted(true);

            // customer noti
            notificationService.save(notificationService.buildCustomerNoti(contract));

            updateInContractStatusOfCustomer(contract);

            return true;
        } else throw new BadRequestException("Contract status is invalid to delete!!!")
                .displayMessage(Translator.toLocale("contract.delete_invalid_contract_status"));
    }

    private void updateInContractStatusOfCustomer(Contract contract) {
        if (!Objects.isNull(contract.getMemberCustomer())) {
            if (!existByMemberCustomerIdAndDeletedFalse(contract.getMemberCustomer().getId())) {
                MemberCustomer memberCustomer = memberCustomerService.findById(contract.getMemberCustomer().getId());
                memberCustomer.setInContract(false);
            }
        } else if (!Objects.isNull(contract.getCustomer())) {
            if (!existByCustomerIdAndDeletedFalse(contract.getCustomer().getId())) {
                Customer customer = customerService.findById(contract.getCustomer().getId());
                customer.setInContract(false);
            }
        }
    }

    @Override
    public Boolean existByCustomerIdAndDeletedFalse(Long customerId) {
        return contractRepo.existsByCustomerIdAndDeletedFalse(customerId);
    }

    @Override
    public Boolean existByMemberCustomerIdAndDeletedFalse(Long memberId) {
        return contractRepo.existsByMemberCustomerIdAndDeletedFalse(memberId);
    }

    @Override
    public Boolean existByListMemberCustomerIdAndDeletedFalse(List<Long> memberIds) {
        List<Long> queryResults = contractRepo.findContractIdsByMemberCustomerIdIn(memberIds);
        return !CollectionUtils.isEmpty(queryResults);
    }

    @Override
    public List<ContractInfoForNotiProjection> findNeededInfoForNotiByListMemberId(List<Long> ids) {
        return contractRepo.findNeededInfoForNotiByListMemberId(ids, CANCELED.getCode());
    }

    @Override
    public List<ContractInfoForNotiProjection> findNeededInfoForNotiByCustomerId(Long id) {
        return contractRepo.findNeededInfoForNotiByCustomerId(id, CANCELED.getCode());
    }

    @Override
    public ContractDTO getContractClone(ContractClonePayload payload) {
        if (StringUtils.isBlank(payload.getCodePrefix())) {
            return null;
        } else {
            Contract result = contractRepo.findContractClone(
                    ValidUtils.normalizeString(payload.getCodePrefix().trim().toUpperCase()),
                    AvisApiConstant.CONTRACT_TYPE.get(payload.getContractType()));
            return Objects.isNull(result) ? null : modelMapper.map(result, ContractDTO.class);
        }
    }

    @Override
    public boolean cancelContract(Long id, User canceledBy) {
        Contract contract = contractRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Contract not found by id: " + id)
                        .displayMessage(Translator.toLocale("contract.not_found")));

        Integer oldExtendStatus = contract.getExtendStatus();

        if (CANCELED.getCode().equals(contract.getStatus())) {
            throw new BadRequestException("This contract has already been canceled!!!")
                    .displayMessage(Translator.toLocale("contract.already_canceled"));
        }

        if (FINISHED.getCode().equals(contract.getStatus())
                || dateUtils.endOfDay(contract.getToDatetime()).before(dateUtils.now())) {
            throw new BadRequestException("This contract is finished!!!")
                    .displayMessage("contract.is_finished");
        }

        contract.setDriverIsTransferredAnother(false);
        contract.setVehicleIsTransferredAnother(false);
        contract.setStatus(ContractStatusEnum.CANCELED.getCode());
        contract.setDateEarlyTermination(new Timestamp(System.currentTimeMillis()));

        ContractChangeHistory contractChangeHistory = ContractChangeHistory.builder()
                .contract(contract)
                .mappingFieldCodeFontend(mappingFieldCodeFontendService
                        .findByTableAndField("contract", EXTEND_STATUS.getName()))
                .oldValue(String.valueOf(oldExtendStatus))
                .newValue(String.valueOf(contract.getExtendStatus()))
                .fromDate(dateUtils.now())
                .createdBy(canceledBy)
                .build();
        contractChangeHistoryService.save(contractChangeHistory);
        // customer noti
        notificationService.save(notificationService.buildCustomerNoti(contract));

        Contract lendDriver = null;
        Contract lendVehicle = null;

        if (contract.getDriver() != null) {
            lendDriver = returnDriverToPreviousState(contract.getDriver().getId());

            // push canceled contract notification to driver app
            if (BooleanUtils.isNotTrue(contract.getDriverIsTransferredAnother())) {
                pushNotiToApp(contract.getDriver().getId(), contract, CANCELED_CONTRACT);
            }

            if (!Objects.isNull(lendDriver)) {
                contract.getDriver().setCurrentContractId(lendDriver.getId());
            } else if (contract.getId().equals(contract.getDriver().getCurrentContractId())) {
                contract.getDriver().setStatus(DriverStatusEnum.WAITING.getValue());
                contract.getDriver().setCurrentContractId(null);
            }

            contract.getDriver().setLendingContractId(null);
        }

        if (contract.getVehicle() != null) {
            lendVehicle = returnVehicleToPreviousState(contract.getVehicle().getId());

            if (!Objects.isNull(lendVehicle)) {
                contract.getVehicle().setCurrentContractId(lendVehicle.getId());
            } else if (contract.getId().equals(contract.getVehicle().getCurrentContractId())) {
                contract.getVehicle().setStatus(VehicleStatusEnum.WAITING.getValue());
                contract.getVehicle().setCurrentContractId(null);
            }

            contract.getVehicle().setLendingContractId(null);
        }

        if (lendDriver != null && lendVehicle != null
                && lendDriver.getId().equals(lendVehicle.getId())) {
            pushNotiToApp(lendDriver.getDriver().getId(),
                    lendDriver, NEW_CONTRACT);
        } else {
            if (lendDriver != null && !lendDriver.getDriverIsTransferredAnother()
                    && !lendDriver.getVehicleIsTransferredAnother()) {
                pushNotiToApp(lendDriver.getDriver().getId(),
                        lendDriver, NEW_CONTRACT);
            }

            if (lendVehicle != null && !lendVehicle.getVehicleIsTransferredAnother()
                    && !lendVehicle.getDriverIsTransferredAnother()) {
                pushNotiToApp(lendVehicle.getDriver().getId(),
                        lendVehicle, NEW_CONTRACT);
            }
        }
        return true;
    }

    private Contract returnVehicleToPreviousState(Long id) {
        Contract lendVehicle = contractRepo.findContractLendingVehicle(
                id, ContractStatusEnum.CANCELED.getCode());
        if (lendVehicle != null) {
            lendVehicle.setVehicleIsTransferredAnother(false);
            if (!lendVehicle.getVehicleIsTransferredAnother()
                    && !lendVehicle.getDriverIsTransferredAnother()) {
                ContractDriverHistory contractDriverHistory = new ContractDriverHistoryAdapter(dateUtils)
                        .apply(lendVehicle);
                contractDriverHistoryService.save(contractDriverHistory);
            }
        }

        return lendVehicle;
    }

    private Contract returnDriverToPreviousState(Long id) {
        Contract lendDriver = contractRepo.findContractLendingDriver(
                id, ContractStatusEnum.CANCELED.getCode());
        if (lendDriver != null) {
            lendDriver.setDriverIsTransferredAnother(false);
            if (!lendDriver.getVehicleIsTransferredAnother()
                    && !lendDriver.getDriverIsTransferredAnother()) {
                ContractDriverHistory contractDriverHistory = new ContractDriverHistoryAdapter(dateUtils)
                        .apply(lendDriver);
                contractDriverHistoryService.save(contractDriverHistory);
            }
        }
        return lendDriver;
    }

    private void pushNotiToApp(Long id, Contract contract, NotificationContentEnum content) {
        Notification notification = notificationService
                .build(id, contract, content);
        notificationService.saveAndPush(notification, true);
    }

    @Override
    public void handleCreateContract(Contract contract, User driver, Vehicle vehicle,
                                     Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets) {

        if (Objects.isNull(driver) || Objects.isNull(vehicle)) {
            return;
        }
        handleChangeDriver(contract, null, driver, changeQuartets);
        handleChangeVehicle(contract, null, vehicle, changeQuartets);
    }

    @Override
    public void handleChangeDriver(Contract contract, User oldDriver, User newDriver,
                                   Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets) {

        Contract contractLendingOldDriver = Objects.isNull(oldDriver) ? null : oldDriver.getLendingContract();
        Contract contractLendNewDriver = newDriver.getCurrentContract();

        assignDriver(contract, newDriver);
        retrieveDriver(contractLendingOldDriver);
        lendDriver(contractLendNewDriver);

        userService.assignToContract(newDriver, contract);
        userService.unAssignFromContract(oldDriver, contract);

        changeQuartets.add(Quartet.with(contractLendNewDriver, newDriver, POSTPONE, PAUSE_CONTRACT));
        changeQuartets.add(Quartet.with(contract, newDriver, CONTINUE, NEW_CONTRACT));
        changeQuartets.add(Quartet.with(contract, oldDriver, POSTPONE, PAUSE_CONTRACT));
        if (Objects.nonNull(contractLendingOldDriver) && !contractLendingOldDriver.getVehicleIsTransferredAnother()) {
            changeQuartets.add(Quartet.with(contractLendingOldDriver, oldDriver, CONTINUE, NEW_CONTRACT));
        } else {
            changeQuartets.add(Quartet.with(contractLendingOldDriver, oldDriver, null, NEW_CONTRACT));
        }
    }

    @Override
    public void handleChangeVehicle(Contract contract, Vehicle oldVehicle, Vehicle newVehicle,
                                    Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets) {

        Contract contractLendingOldVehicle = Objects.isNull(oldVehicle) ? null : oldVehicle.getLendingContract();
        Contract contractLendNewVehicle = newVehicle.getCurrentContract();

        assignVehicle(contract, newVehicle);
        retrieveVehicle(contractLendingOldVehicle);
        lendVehicle(contractLendNewVehicle);

        vehicleService.assignToContract(newVehicle, contract);
        vehicleService.unAssignFromContract(oldVehicle, contract);

        if (Objects.nonNull(contractLendNewVehicle) && !contractLendNewVehicle.getDriverIsTransferredAnother()) {
            changeQuartets.add(Quartet.with(contractLendNewVehicle, contractLendNewVehicle.getDriver(), POSTPONE, PAUSE_CONTRACT));
        }
        if (Objects.nonNull(contractLendingOldVehicle) && !contractLendingOldVehicle.getDriverIsTransferredAnother()) {
            changeQuartets.add(Quartet.with(contractLendingOldVehicle, contractLendingOldVehicle.getDriver(), CONTINUE, NEW_CONTRACT));
        }
    }

    @Override
    public void handleCreateContractWithoutDriver(Contract contract, User driver, Vehicle vehicle,
                                                  Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets) {
        if (Objects.isNull(driver) || Objects.isNull(vehicle)) {
            return;
        }
        handleChangeDriverNoLending(contract, null, driver, changeQuartets);
        handleChangeVehicleNoLending(contract, null, vehicle, changeQuartets);
    }

    @Override
    public void handleChangeDriverNoLending(Contract contract, User oldDriver, User newDriver,
                                            Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets) {

        assignDriver(contract, newDriver);

        userService.assignToContract(newDriver, contract);
        userService.unAssignFromContract(oldDriver, contract);

        changeQuartets.add(Quartet.with(contract, newDriver, CONTINUE, NEW_CONTRACT));
        changeQuartets.add(Quartet.with(contract, oldDriver, POSTPONE, PAUSE_CONTRACT));
    }

    @Override
    public void handleChangeVehicleNoLending(Contract contract, Vehicle oldVehicle, Vehicle newVehicle,
                                             Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets) {

        assignVehicle(contract, newVehicle);

        vehicleService.assignToContract(newVehicle, contract);
        vehicleService.unAssignFromContract(oldVehicle, contract);
    }

    public void assignDriver(Contract contract, User driver) {
        releaseDriver(contract);
        contract.setDriver(driver);
    }

    public void lendDriver(Contract contract) {
        if (Objects.nonNull(contract)) {
            contract.setDriverIsTransferredAnother(true);
            contract.setDriverIsTransferredAnotherAt(dateUtils.now());
        }
    }

    @Override
    public void retrieveDriver(Contract contract) {
        if (Objects.nonNull(contract)) {
            contract.setDriverIsTransferredAnother(false);
            contract.setDriverIsTransferredAnotherAt(null);
        }
    }

    @Override
    public void releaseDriver(Contract contract) {
        if (Objects.nonNull(contract)) {
            contract.setDriverIsTransferredAnother(false);
            contract.setDriverIsTransferredAnotherAt(null);
        }
    }

    public void assignVehicle(Contract contract, Vehicle vehicle) {
        releaseVehicle(contract);
        contract.setVehicle(vehicle);
    }

    public void lendVehicle(Contract contract) {
        if (Objects.nonNull(contract)) {
            contract.setVehicleIsTransferredAnother(true);
            contract.setVehicleIsTransferredAnotherAt(dateUtils.now());
        }
    }

    @Override
    public void retrieveVehicle(Contract contract) {
        if (Objects.nonNull(contract)) {
            contract.setVehicleIsTransferredAnother(false);
            contract.setVehicleIsTransferredAnotherAt(null);
        }
    }

    @Override
    public void releaseVehicle(Contract contract) {
        if (Objects.nonNull(contract)) {
            contract.setVehicleIsTransferredAnother(false);
            contract.setVehicleIsTransferredAnotherAt(null);
        }
    }

    @Override
    public Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> cleanQuartets(
            Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets) {
        return quartets.stream()
                .filter(quartet -> Objects.nonNull(quartet.getValue0()) && Objects.nonNull(quartet.getValue1()))
                .collect(Collectors.toSet());
    }

    @Override
    public List<Contract> findAllByContractTypeAndAssignedVehicle(Long typeId) {
        String id = Objects.isNull(typeId) ? "%" : String.valueOf(typeId);
        return contractRepo.findAllByContractTypeAndAssignedVehicle(id);
    }

    @Override
    @SneakyThrows
    @Cacheable(cacheNames = "ContractValueAtTime", key = "#contract.id + '::' + #field + '::' + " +
            "new vn.com.twendie.avis.api.core.util.DateUtils().getDate(#timestamp)",
            condition = "new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday().after(#timestamp)")
    public <T> T getContractValueAtTime(Contract contract, String field, Timestamp timestamp, Class<T> clazz) {
        ContractChangeHistory history = contractChangeHistoryService.findLastChangeOfField(contract, field, timestamp);
        if (Objects.nonNull(history)) {
            String stringValue;
            if (!timestamp.before(history.getFromDate())) {
                stringValue = history.getNewValue();
            } else if (Objects.nonNull(history.getOldValue())) {
                stringValue = history.getOldValue();
            } else {
                return null;
            }
            if (clazz.equals(String.class)) {
                return clazz.cast(stringValue);
            } else {
                return clazz.cast(clazz.getDeclaredMethod("valueOf", String.class).invoke(null, stringValue));
            }
        } else {
            String modelFieldName = reflectUtils.getFieldByColumn(Contract.class, field).getName();
            return clazz.cast(reflectUtils.getGetterMethod(Contract.class, modelFieldName).invoke(contract));
        }
    }

    @Override
    public void validContractType(ContractTypeEnum contractTypeEnum, ContractType contractType) {
        if (!contractTypeEnum.value().equals(contractType.getId())) {
            throw new BadRequestException("Contract type is invalid!!!")
                    .displayMessage(Translator.toLocale("contract.error.contract_type_is_invalid"));
        }
    }

    @Override
    public void fetchFirstJourneyDiaryDate(Collection<Contract> contracts) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream()
                    .map(Contract::getId)
                    .collect(Collectors.toSet());
            List<FirstJourneyDiaryDateProjection> firstJourneyDiaryDateProjections = journeyDiaryRepo
                    .findFirstJourneyDiaryDate(contractIds);
            Map<Long, Timestamp> firstJourneyDiaryDateProjectionsMap = firstJourneyDiaryDateProjections.stream()
                    .collect(Collectors.toMap(FirstJourneyDiaryDateProjection::getContractId,
                            FirstJourneyDiaryDateProjection::getFirstJourneyDiaryDate));
            contracts.forEach(contract -> contract.setFirstJourneyDiaryDate(
                    firstJourneyDiaryDateProjectionsMap.get(contract.getId())
            ));
        }
    }
}
