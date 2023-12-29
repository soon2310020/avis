package vn.com.twendie.avis.api.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.javatuples.Quartet;
import org.joda.time.DateTimeComparator;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.filter.ContractFilter;
import vn.com.twendie.avis.api.model.filter.ContractMemberCustomerFilter;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.payload.*;
import vn.com.twendie.avis.api.model.response.ContractDTO;
import vn.com.twendie.avis.api.model.response.CreateContractOptionsWrapper;
import vn.com.twendie.avis.api.repository.ContractRepo;
import vn.com.twendie.avis.api.repository.MemberCustomerRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.api.validation.ContractValidator;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

import static vn.com.twendie.avis.api.core.util.ValidUtils.normalizeString;
import static vn.com.twendie.avis.data.enumtype.ContractPeriodTypeEnum.LONG_TERM;
import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.*;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.*;
import static vn.com.twendie.avis.data.enumtype.DriverGroupEnum.*;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.WORKING_DAY;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.WORKING_DAY_ID;
import static vn.com.twendie.avis.data.enumtype.NotificationContentEnum.*;
import static vn.com.twendie.avis.data.enumtype.WorkingDayEnum.FLEXIBLE;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    private final MemberCustomerRepo memberCustomerRepo;
    private final ContractRepo contractRepo;
    private final SpecificationBuilder<Contract> contractSpecificationBuilder;

    private final FilterService<Contract, Long> contractFilterService;
    private final ContractService contractService;
    private final ContractPeriodTypeService contractPeriodTypeService;
    private final ContractTypeService contractTypeService;
    private final BranchService branchService;
    private final CustomerService customerService;
    private final MemberCustomerService memberCustomerService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final RentalServiceTypeService rentalServiceTypeService;
    private final WorkingDayService workingDayService;
    private final FuelTypeService fuelTypeService;
    private final CostTypeService costTypeService;
    private final NormListService normListService;
    private final JourneyDiaryService journeyDiaryService;
    private final ContractDriverHistoryService contractDriverHistoryService;
    private final ContractChangeHistoryService contractChangeHistoryService;
    private final LogContractCostTypeService logContractCostTypeService;
    private final LogContractNormListService logContractNormListService;
    private final NotificationService notificationService;
    private final JourneyDiaryDailyService journeyDiaryDailyService;
    private final JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService;

    private final ContractValidator contractValidator;

    private final ListUtils listUtils;
    private final DateUtils dateUtils;
    private final ModelMapper modelMapper;

    public ContractController(MemberCustomerRepo memberCustomerRepo,
                              ContractRepo contractRepo,
                              SpecificationBuilder<Contract> contractSpecificationBuilder,
                              FilterService<Contract, Long> contractFilterService,
                              ContractService contractService,
                              ContractPeriodTypeService contractPeriodTypeService,
                              ContractTypeService contractTypeService,
                              BranchService branchService,
                              CustomerService customerService,
                              MemberCustomerService memberCustomerService,
                              UserService userService,
                              VehicleService vehicleService,
                              RentalServiceTypeService rentalServiceTypeService,
                              WorkingDayService workingDayService,
                              FuelTypeService fuelTypeService,
                              CostTypeService costTypeService,
                              NormListService normListService,
                              JourneyDiaryService journeyDiaryService,
                              ContractDriverHistoryService contractDriverHistoryService,
                              ContractChangeHistoryService contractChangeHistoryService,
                              LogContractCostTypeService logContractCostTypeService,
                              LogContractNormListService logContractNormListService,
                              NotificationService notificationService,
                              JourneyDiaryDailyService journeyDiaryDailyService,
                              JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService,
                              ContractValidator contractValidator,
                              ListUtils listUtils,
                              DateUtils dateUtils,
                              ModelMapper modelMapper) {
        this.memberCustomerRepo = memberCustomerRepo;
        this.contractRepo = contractRepo;
        this.contractSpecificationBuilder = contractSpecificationBuilder;
        this.contractFilterService = contractFilterService;
        this.contractService = contractService;
        this.contractPeriodTypeService = contractPeriodTypeService;
        this.contractTypeService = contractTypeService;
        this.branchService = branchService;
        this.customerService = customerService;
        this.memberCustomerService = memberCustomerService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.rentalServiceTypeService = rentalServiceTypeService;
        this.workingDayService = workingDayService;
        this.fuelTypeService = fuelTypeService;
        this.costTypeService = costTypeService;
        this.normListService = normListService;
        this.journeyDiaryService = journeyDiaryService;
        this.contractDriverHistoryService = contractDriverHistoryService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.logContractCostTypeService = logContractCostTypeService;
        this.logContractNormListService = logContractNormListService;
        this.notificationService = notificationService;
        this.journeyDiaryDailyService = journeyDiaryDailyService;
        this.journeyDiaryDailyCostTypeService = journeyDiaryDailyCostTypeService;
        this.contractValidator = contractValidator;
        this.listUtils = listUtils;
        this.dateUtils = dateUtils;
        this.modelMapper = modelMapper;
    }


    @PostMapping("/filter")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale", "Customer"})
    public ApiResponse<?> filter(@Valid @RequestBody FilterWrapper<ContractFilter> filterWrapper,
                                 @CurrentUser UserDetails userDetails) {
        User user = ((UserPrincipal) userDetails).getUser();
        if (filterWrapper.getFilter().getMemberCustomer() == null) {
            filterWrapper.getFilter().setMemberCustomer(ContractMemberCustomerFilter.create());
        }
        if (user.getCustomer() != null) {
            filterWrapper.getFilter().getCustomer().setId(user.getCustomer().getId().intValue());
        }
        if (user.getMemberCustomer() != null) {
            List<MemberCustomer> children = memberCustomerRepo.findByParentIdAndDeletedFalse(user.getMemberCustomer().getId());
            List<Integer> memberCustomerIds = listUtils.transform(children, child -> child.getId().intValue());
            memberCustomerIds.add(user.getMemberCustomer().getId().intValue());
            filterWrapper.getFilter().getMemberCustomer().setIdIn(memberCustomerIds);
        }
        Page<Contract> contracts = contractFilterService.filter(contractRepo, contractSpecificationBuilder, filterWrapper);
        costTypeService.fetchContractCostTypes(contracts.getContent());
        normListService.fetchContractNormLists(contracts.getContent());
        Page<ContractDTO> contractDTOs = listUtils.transform(contracts, contract ->
                modelMapper.map(contract, ContractDTO.class));
        return ApiResponse.success(GeneralPageResponse.toResponse(contractDTOs));
    }

    @Transactional
    @PostMapping("/create-contract-with-driver")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> createContractWithDriver(
            @Valid @RequestBody ContractWithDriverPayload payload,
            @CurrentUser UserDetails userDetails) {

        payload.setPrefixCode(normalizeString(payload.getPrefixCode()).toUpperCase());
        String code = contractService.validateContractCode(payload.getPrefixCode(), payload.getSuffixCode());
        int term = dateUtils.getMonthsBetween(payload.getFromDatetime(), payload.getToDatetime());

        User driver = userService.findDriverByIdAndGroups(payload.getDriverId(), AVIS, SUB_CONTRACTOR);
        contractValidator.validateDriverNotInJourneyDiary(driver);
        contractValidator.validateDriverAvailable(driver);
        contractValidator.validateNoContractLendingDriver(driver);

        Vehicle vehicle = vehicleService.findById(payload.getVehicleId());
        contractValidator.validateVehicleNotInJourneyDiary(vehicle);
        contractValidator.validateVehicleAvailable(vehicle);
        contractValidator.validateNoContractLendingVehicle(vehicle);
        contractValidator.validateVehicleNotInContractWithoutDriver(vehicle);

        ContractPeriodType contractPeriodType = contractPeriodTypeService.findByIdIgnoreDelete(LONG_TERM.getId());
        ContractType contractType = contractTypeService.findById(WITH_DRIVER.value());
        Branch branch = branchService.findByCode(payload.getBranchCode());
        MemberCustomer memberCustomer = memberCustomerService.findById(payload.getMemberCustomerId());
        Customer customer = Objects.nonNull(memberCustomer) ? memberCustomer.getCustomer() :
                customerService.findById(payload.getCustomerId());
        contractValidator.validateCustomerInfo(customer, memberCustomer);
        RentalServiceType rentalServiceType = rentalServiceTypeService.findByCode(payload.getRentalServiceTypeCode());
        WorkingDay workingDay = workingDayService.findByCode(payload.getWorkingDayCode());
        FuelType fuelType = fuelTypeService.findByCode(payload.getFuelTypeCode());
        User createdBy = ((UserPrincipal) userDetails).getUser();

        int status = Objects.isNull(vehicle) ? WAITING_ASSIGN_CAR.getCode() :
                payload.getFromDatetime().after(dateUtils.now()) ? ASSIGNED_CAR.getCode() : IN_PROGRESS.getCode();

        Contract contract = modelMapper.map(payload, Contract.class).toBuilder()
                .code(code)
                .term(term)
                .contractPeriodType(contractPeriodType)
                .contractType(contractType)
                .branch(branch)
                .customer(customer)
                .memberCustomer(memberCustomer)
                .driverIsTransferredAnother(false)
                .vehicleIsTransferredAnother(false)
                .rentalServiceType(rentalServiceType)
                .workingDay(workingDay)
                .fuelType(fuelType)
                .createdBy(createdBy)
                .status(status)
                .build();

        contractService.save(contract);
        contract.setContractCostTypes(costTypeService.saveAll(costTypeService
                .buildContractCostTypes(contract, payload.getContractCosts())));
        contract.setContractNormLists(normListService.saveAll(normListService
                .buildContractNormList(contract, payload.getContractNorms())));

        Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets = new HashSet<>();
        contractService.handleCreateContract(contract, driver, vehicle, quartets);

        customerService.updateInContract(customer);
        memberCustomerService.updateInContract(memberCustomer);

        List<LogContractPriceCostType> logContractCostTypes = logContractCostTypeService
                .buildLogContractCostTypes(contract, dateUtils.startOfDay(contract.getFromDatetime()), createdBy);
        List<LogContractNormList> logContractNormLists = logContractNormListService
                .buildLogContractNormLists(contract, dateUtils.startOfDay(contract.getFromDatetime()), createdBy);
        logContractCostTypeService.saveAll(logContractCostTypes);
        logContractNormListService.saveAll(logContractNormLists);

        List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyService
                .saveAll(journeyDiaryDailyService.createEmptyJourneyDiaryDailies(contract));
        journeyDiaryDailyCostTypeService.saveAll(journeyDiaryDailyCostTypeService
                .createJourneyDiaryDailyCostTypes(journeyDiaryDailies));

        quartets = contractService.cleanQuartets(quartets);

        contractDriverHistoryService.saveAll(contractDriverHistoryService
                .buildContractDriverHistories(quartets));
        notificationService.saveAllAndPush(notificationService.build(quartets), true);
        notificationService.save(notificationService.buildCustomerNoti(contract));

        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);
        return ApiResponse.success(contractDTO);
    }

    @Transactional
    @PutMapping("{contract_id}/edit-contract-with-driver-immediate")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> editContractWithDriverImmediate(
            @PathVariable("contract_id") long contractId,
            @Valid @RequestBody EditContractWithDriverImmediatePayload payload,
            @CurrentUser UserDetails userDetails) {

        Timestamp now = dateUtils.now();

        Contract contract = contractService.findById(contractId);
        User oldDriver = contract.getDriver();
        Vehicle oldVehicle = contract.getVehicle();
        contractValidator.validateContractStatusCanEdit(contract);

        Branch branch = branchService.findByCode(payload.getBranchCode());
        FuelType fuelType = fuelTypeService.findByCode(payload.getFuelTypeCode());
        User updatedBy = ((UserPrincipal) userDetails).getUser();

        Contract tempContract = modelMapper.map(payload, Contract.class).toBuilder()
                .branch(branch)
                .fuelType(fuelType)
                .build();

        boolean changedDuration = !contract.getFromDatetime().equals(tempContract.getFromDatetime()) ||
                !contract.getToDatetime().equals(tempContract.getToDatetime());
        boolean changedDriver = Objects.nonNull(payload.getDriverId()) &&
                (Objects.isNull(oldDriver) || !oldDriver.getId().equals(payload.getDriverId()));
        boolean changedVehicle = Objects.nonNull(payload.getVehicleId()) &&
                (Objects.isNull(oldVehicle) || !oldVehicle.getId().equals(payload.getVehicleId()));

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryService
                .buildContractChangeHistories(contract, tempContract, dateUtils.startOfDay(now), updatedBy);

        Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets = new HashSet<>();

        if (changedDriver) {
            contractValidator.validateDriverNotInJourneyDiary(oldDriver);
            User newDriver = userService.findDriverByIdAndGroups(payload.getDriverId(), AVIS, SUB_CONTRACTOR);
            contractValidator.validateDriverNotInJourneyDiary(newDriver);
            contractValidator.validateDriverAvailable(newDriver);
            contractValidator.validateNoContractLendingDriver(newDriver);
            contractService.handleChangeDriver(contract, oldDriver, newDriver, changeQuartets);
        }

        if (changedVehicle) {
            contractValidator.validateVehicleNotInJourneyDiary(oldVehicle);
            Vehicle newVehicle = vehicleService.findById(payload.getVehicleId());
            contractValidator.validateVehicleNotInJourneyDiary(newVehicle);
            contractValidator.validateVehicleAvailable(newVehicle);
            contractValidator.validateNoContractLendingVehicle(newVehicle);
            contractValidator.validateVehicleNotInContractWithoutDriver(newVehicle);
            contractService.handleChangeVehicle(contract, oldVehicle, newVehicle, changeQuartets);
        }

        contractService.assignTerm(contract);
        contractService.assignStatus(contract);
        contract.setIncludeAppendix(true);
        contractService.save(contract);

        contractChangeHistoryService.saveAll(contractChangeHistories);

        changeQuartets = contractService.cleanQuartets(changeQuartets);

        contractDriverHistoryService.saveAll(contractDriverHistoryService.buildContractDriverHistories(changeQuartets));

        notificationService.saveAllAndPush(notificationService.build(changeQuartets), true);
        notificationService.saveAll(listUtils.transform(contractChangeHistories, notificationService::buildCustomerNoti));

        if (!changedDriver && Objects.nonNull(oldDriver) && BooleanUtils.isNotTrue(contract.getDriverIsTransferredAnother())) {
            if (changedDuration) {
                notificationService.saveAndPush(notificationService
                        .build(oldDriver.getId(), contract, CHANGE_CONTRACT_DURATION, contract.getCode()), true);
            }
            if (changedVehicle) {
                notificationService.saveAndPush(notificationService
                        .build(oldDriver.getId(), contract, CHANGE_VEHICLE), true);
            }
        }

        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);
        return ApiResponse.success(contractDTO);
    }

    @Transactional
    @PutMapping("{contract_id}/edit-contract-with-driver-effective-date")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> editContractWithDriverEffectiveDate(
            @PathVariable("contract_id") long contractId,
            @Valid @RequestBody EditContractWithDriverEffectiveDatePayload payload,
            @CurrentUser UserDetails userDetails) {

        Timestamp effectiveDate = dateUtils.startOfDay(payload.getEffectiveDate());

        Contract contract = contractService.findById(contractId);
        contractValidator.validateContractStatusCanEdit(contract);
        contractValidator.validateEffectiveDate(contract, effectiveDate);

        RentalServiceType rentalServiceType = rentalServiceTypeService.findByCode(payload.getRentalServiceTypeCode());
        WorkingDay workingDay = workingDayService.findByCode(payload.getWorkingDayCode());
        User updatedBy = ((UserPrincipal) userDetails).getUser();

        Contract tempContract = modelMapper.map(payload, Contract.class).toBuilder()
                .rentalServiceType(rentalServiceType)
                .workingDay(workingDay)
                .build();

        contractChangeHistoryService.findLastChangeOfFieldInMonth(contract, WORKING_DAY_ID.getName(), dateUtils.now());

        boolean changedWorkingDay = false;

        if (!contract.getWorkingDay().equals(tempContract.getWorkingDay()) ||
                !Objects.equals(contract.getWorkingDayValue(), tempContract.getWorkingDayValue())) {
            ContractChangeHistory workingDayChangeHistory = contractChangeHistoryService
                    .findLastChangeOfFieldInMonth(contract, WORKING_DAY_ID.getName(), dateUtils.now());
            if (Objects.isNull(workingDayChangeHistory) ||
                    (!effectiveDate.before(workingDayChangeHistory.getFromDate()) &&
                            !tempContract.getWorkingDay().getId().equals(Long.parseLong(workingDayChangeHistory.getNewValue())))) {
                changedWorkingDay = true;
            }
            ContractChangeHistory workingDayValueChangeHistory = contractChangeHistoryService
                    .findLastChangeOfFieldInMonth(contract, WORKING_DAY.getName(), dateUtils.now());
            if (Objects.isNull(workingDayValueChangeHistory) ||
                    (!effectiveDate.before(workingDayValueChangeHistory.getFromDate()) &&
                            !String.valueOf(tempContract.getWorkingDayValue())
                                    .equals(String.valueOf(workingDayValueChangeHistory.getNewValue())))) {
                changedWorkingDay = true;
            }
        }

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryService
                .buildContractChangeHistories(contract, tempContract, effectiveDate, updatedBy);
        List<LogContractPriceCostType> logContractCostTypes = logContractCostTypeService
                .buildLogContractCostTypes(contract, payload.getContractCosts(), effectiveDate, updatedBy);
        List<LogContractNormList> logContractNormLists = logContractNormListService
                .buildLogContractNormLists(contract, payload.getContractNorms(), effectiveDate, updatedBy);

        contract.setIncludeAppendix(true);
        contractService.save(contract);
        contractChangeHistoryService.saveAll(contractChangeHistories);
        logContractCostTypeService.saveAll(logContractCostTypes);
        logContractNormListService.saveAll(logContractNormLists);

        Timestamp now = dateUtils.now();
        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(now);
        Timestamp lastDayOfMonth = dateUtils.getLastDayOfMonth(now);
        if (dateUtils.isBetween(effectiveDate, firstDayOfMonth, lastDayOfMonth)) {
            contractChangeHistoryService.fetchContractChangeHistories(contract);
            List<JourneyDiaryDaily> journeyDiaryDailies = journeyDiaryDailyService.find(contract.getId(), firstDayOfMonth, lastDayOfMonth);
            journeyDiaryDailyService.fetchChildren(journeyDiaryDailies);
            if (changedWorkingDay) {
                if (tempContract.getWorkingDay().getId().equals(FLEXIBLE.getId())) {
                    Timestamp firstOverDay = journeyDiaryDailies.stream()
                            .sorted((j1, j2) -> DateTimeComparator.getInstance().compare(j1.getDate(), j2.getDate()))
                            .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getUsedKm()))
                            .skip(tempContract.getWorkingDayValue())
                            .map(JourneyDiaryDaily::getDate)
                            .findFirst()
                            .orElse(null);
                    journeyDiaryDailies.forEach(journeyDiaryDaily -> {
                        if (Objects.nonNull(journeyDiaryDaily.getUsedKm()) && Objects.nonNull(firstOverDay) &&
                                !journeyDiaryDaily.getDate().before(firstOverDay) && !journeyDiaryDaily.getIsSelfDrive()) {
                            journeyDiaryDaily.setIsOverDay(true);
                            journeyDiaryDaily.getChildren()
                                    .stream()
                                    .filter(child -> !child.getIsSelfDrive())
                                    .forEach(child -> child.setIsOverDay(true));
                        } else {
                            journeyDiaryDaily.setIsOverDay(false);
                            journeyDiaryDaily.getChildren().forEach(child -> child.setIsOverDay(false));
                        }
                    });
                } else {
                    journeyDiaryDailies.forEach(journeyDiaryDaily -> {
                        journeyDiaryDaily.setIsOverDay(false);
                        journeyDiaryDaily.getChildren().forEach(child -> child.setIsOverDay(false));
                    });
                }
                journeyDiaryDailies.stream()
                        .flatMap(journeyDiaryDaily -> {
                            if (CollectionUtils.isEmpty(journeyDiaryDaily.getChildren())) {
                                return Stream.of(journeyDiaryDaily);
                            } else {
                                return Stream.concat(journeyDiaryDaily.getChildren().stream(),
                                        Stream.of(journeyDiaryDaily));
                            }
                        })
                        .forEach(journeyDiaryDaily -> {
                            journeyDiaryDailyService.updateWorkingCalendar(journeyDiaryDaily, tempContract.getWorkingDay());
                            journeyDiaryDailyService.updateOverTime(journeyDiaryDaily, contract, false);
                            journeyDiaryDailyService.updateOverKm(journeyDiaryDaily, contract, false);
                        });
            }
        }

        List<Notification> customerNotifications = new ArrayList<Notification>() {{
            addAll(listUtils.transform(contractChangeHistories, notificationService::buildCustomerNoti));
            addAll(listUtils.transform(logContractCostTypes, notificationService::buildCustomerNoti));
            addAll(listUtils.transform(logContractNormLists, notificationService::buildCustomerNoti));
        }};
        notificationService.saveAll(customerNotifications);

        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);
        return ApiResponse.success(contractDTO);
    }

    @Transactional
    @PostMapping("/create-contract-without-driver")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> createContractWithoutDriver(
            @Valid @RequestBody ContractWithoutDriverPayload payload,
            @CurrentUser UserDetails userDetails) {

        payload.setPrefixCode(normalizeString(payload.getPrefixCode()).toUpperCase());
        String code = contractService.validateContractCode(payload.getPrefixCode(), payload.getSuffixCode());
        int term = dateUtils.getMonthsBetween(payload.getFromDatetime(), payload.getToDatetime());

        User driver = userService.findDriverByIdAndGroups(payload.getDriverId(), CUSTOMER);
        contractValidator.validateDriverNotInJourneyDiary(driver);
        contractValidator.validateDriverAvailable(driver);
        contractValidator.validateDriverNotInContract(driver);

        Vehicle vehicle = vehicleService.findById(payload.getVehicleId());
        contractValidator.validateVehicleNotInJourneyDiary(vehicle);
        contractValidator.validateVehicleAvailable(vehicle);
        contractValidator.validateVehicleNotInContract(vehicle);

        ContractPeriodType contractPeriodType = contractPeriodTypeService.findByIdIgnoreDelete(LONG_TERM.getId());
        ContractType contractType = contractTypeService.findById(WITHOUT_DRIVER.value());
        Branch branch = branchService.findByCode(payload.getBranchCode());
        MemberCustomer memberCustomer = memberCustomerService.findById(payload.getMemberCustomerId());
        Customer customer = Objects.nonNull(memberCustomer) ? memberCustomer.getCustomer() :
                customerService.findById(payload.getCustomerId());
        contractValidator.validateCustomerInfo(customer, memberCustomer);
        RentalServiceType rentalServiceType = rentalServiceTypeService.findByCode(payload.getRentalServiceTypeCode());
        FuelType fuelType = fuelTypeService.findByCode(payload.getFuelTypeCode());
        User createdBy = ((UserPrincipal) userDetails).getUser();

        int status = Objects.isNull(vehicle) ? WAITING_ASSIGN_CAR.getCode() :
                payload.getFromDatetime().after(dateUtils.now()) ? ASSIGNED_CAR.getCode() : IN_PROGRESS.getCode();

        Contract contract = modelMapper.map(payload, Contract.class).toBuilder()
                .code(code)
                .term(term)
                .contractPeriodType(contractPeriodType)
                .contractType(contractType)
                .branch(branch)
                .customer(customer)
                .memberCustomer(memberCustomer)
                .driverIsTransferredAnother(false)
                .vehicleIsTransferredAnother(false)
                .rentalServiceType(rentalServiceType)
                .fuelType(fuelType)
                .createdBy(createdBy)
                .status(status)
                .build();

        contractService.save(contract);
        contract.setContractCostTypes(costTypeService.saveAll(costTypeService
                .buildContractCostTypes(contract, payload.getContractCosts())));
        contract.setContractNormLists(normListService.saveAll(normListService
                .buildContractNormList(contract, payload.getContractNorms())));

        Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets = new HashSet<>();
        contractService.handleCreateContractWithoutDriver(contract, driver, vehicle, quartets);

        customerService.updateInContract(customer);
        memberCustomerService.updateInContract(memberCustomer);

        List<LogContractPriceCostType> logContractCostTypes = logContractCostTypeService
                .buildLogContractCostTypes(contract, dateUtils.startOfDay(contract.getFromDatetime()), createdBy);
        List<LogContractNormList> logContractNormLists = logContractNormListService
                .buildLogContractNormLists(contract, dateUtils.startOfDay(contract.getFromDatetime()), createdBy);
        logContractCostTypeService.saveAll(logContractCostTypes);
        logContractNormListService.saveAll(logContractNormLists);

        journeyDiaryDailyService.saveAll(journeyDiaryDailyService.createEmptyJourneyDiaryDailies(contract));

        quartets = contractService.cleanQuartets(quartets);

        contractDriverHistoryService.saveAll(contractDriverHistoryService
                .buildContractDriverHistories(quartets));
        notificationService.saveAllAndPush(notificationService.build(quartets), true);
        notificationService.save(notificationService.buildCustomerNoti(contract));

        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);
        return ApiResponse.success(contractDTO);

    }

    @Transactional
    @PutMapping("{contract_id}/edit-contract-without-driver-immediate")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> editContractWithoutDriverImmediate(
            @PathVariable("contract_id") long contractId,
            @Valid @RequestBody EditContractWithoutDriverImmediatePayload payload,
            @CurrentUser UserDetails userDetails) {

        Timestamp now = dateUtils.now();

        Contract contract = contractService.findById(contractId);
        User oldDriver = contract.getDriver();
        Vehicle oldVehicle = contract.getVehicle();
        contractValidator.validateContractStatusCanEdit(contract);

        Branch branch = branchService.findByCode(payload.getBranchCode());
        FuelType fuelType = fuelTypeService.findByCode(payload.getFuelTypeCode());
        User updatedBy = ((UserPrincipal) userDetails).getUser();

        Contract tempContract = modelMapper.map(payload, Contract.class).toBuilder()
                .branch(branch)
                .fuelType(fuelType)
                .build();

        boolean changedDuration = !contract.getFromDatetime().equals(tempContract.getFromDatetime()) ||
                !contract.getToDatetime().equals(tempContract.getToDatetime());
        boolean changedDriver = Objects.nonNull(payload.getDriverId()) &&
                (Objects.isNull(oldDriver) || !oldDriver.getId().equals(payload.getDriverId()));
        boolean changedVehicle = Objects.nonNull(payload.getVehicleId()) &&
                (Objects.isNull(oldVehicle) || !oldVehicle.getId().equals(payload.getVehicleId()));

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryService
                .buildContractChangeHistories(contract, tempContract, dateUtils.startOfDay(now), updatedBy);

        Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets = new HashSet<>();

        if (changedDriver) {
            contractValidator.validateDriverNotInJourneyDiary(oldDriver);
            User newDriver = userService.findDriverByIdAndGroups(payload.getDriverId(), CUSTOMER);
            contractValidator.validateDriverNotInJourneyDiary(newDriver);
            contractValidator.validateDriverAvailable(newDriver);
            contractValidator.validateDriverNotInContract(newDriver);
            contractService.handleChangeDriverNoLending(contract, oldDriver, newDriver, changeQuartets);
        }

        if (changedVehicle) {
            contractValidator.validateVehicleNotInJourneyDiary(oldVehicle);
            Vehicle newVehicle = vehicleService.findById(payload.getVehicleId());
            contractValidator.validateVehicleNotInJourneyDiary(newVehicle);
            contractValidator.validateVehicleAvailable(newVehicle);
            contractValidator.validateVehicleNotInContract(newVehicle);
            contractService.handleChangeVehicleNoLending(contract, oldVehicle, newVehicle, changeQuartets);
        }

        contractService.assignTerm(contract);
        contractService.assignStatus(contract);
        contract.setIncludeAppendix(true);
        contractService.save(contract);

        contractChangeHistoryService.saveAll(contractChangeHistories);

        changeQuartets = contractService.cleanQuartets(changeQuartets);

        contractDriverHistoryService.saveAll(contractDriverHistoryService
                .buildContractDriverHistories(changeQuartets));
        notificationService.saveAllAndPush(notificationService.build(changeQuartets), true);
        notificationService.saveAll(listUtils.transform(contractChangeHistories, notificationService::buildCustomerNoti));

        if (!changedDriver && Objects.nonNull(oldDriver)) {
            if (changedDuration) {
                notificationService.saveAndPush(notificationService
                        .build(oldDriver.getId(), contract, CHANGE_CONTRACT_DURATION, contract.getCode()), true);
            }
            if (changedVehicle) {
                notificationService.saveAndPush(notificationService
                        .build(oldDriver.getId(), contract, CHANGE_VEHICLE), true);
            }
        }

        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);
        return ApiResponse.success(contractDTO);
    }

    @Transactional
    @PutMapping("{contract_id}/edit-contract-without-driver-effective-date")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> editContractWithoutDriverEffectiveDate(
            @PathVariable("contract_id") long contractId,
            @Valid @RequestBody EditContractWithoutDriverEffectiveDatePayload payload,
            @CurrentUser UserDetails userDetails) {

        Timestamp effectiveDate = dateUtils.startOfDay(payload.getEffectiveDate());

        Contract contract = contractService.findById(contractId);
        contractValidator.validateContractStatusCanEdit(contract);
        contractValidator.validateEffectiveDate(contract, effectiveDate);

        RentalServiceType rentalServiceType = rentalServiceTypeService.findByCode(payload.getRentalServiceTypeCode());
        User updatedBy = ((UserPrincipal) userDetails).getUser();

        Contract tempContract = modelMapper.map(payload, Contract.class).toBuilder()
                .rentalServiceType(rentalServiceType)
                .build();

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryService
                .buildContractChangeHistories(contract, tempContract, effectiveDate, updatedBy);
        List<LogContractPriceCostType> logContractCostTypes = logContractCostTypeService
                .buildLogContractCostTypes(contract, payload.getContractCosts(), effectiveDate, updatedBy);
        List<LogContractNormList> logContractNormLists = logContractNormListService
                .buildLogContractNormLists(contract, payload.getContractNorms(), effectiveDate, updatedBy);

        contract.setIncludeAppendix(true);
        contractService.save(contract);
        contractChangeHistoryService.saveAll(contractChangeHistories);
        logContractCostTypeService.saveAll(logContractCostTypes);
        logContractNormListService.saveAll(logContractNormLists);

        List<Notification> customerNotifications = new ArrayList<Notification>() {{
            addAll(listUtils.transform(contractChangeHistories, notificationService::buildCustomerNoti));
            addAll(listUtils.transform(logContractCostTypes, notificationService::buildCustomerNoti));
            addAll(listUtils.transform(logContractNormLists, notificationService::buildCustomerNoti));
        }};
        notificationService.saveAll(customerNotifications);

        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);
        return ApiResponse.success(contractDTO);
    }

    @GetMapping("/statistic")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> statistic(@RequestParam("contract_type_id") long contractTypeId) {
        return ApiResponse.success(contractService.getContractStatistic(ContractTypeEnum.valueOf(contractTypeId)));
    }

    @GetMapping("/options")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<CreateContractOptionsWrapper> getCreateOptions(
            @RequestParam(value = "contract_type", required = false, defaultValue = "contract_with_driver") String type
    ) {
        CreateContractOptionsWrapper wrapper = contractService.getOptions(AvisApiConstant.CONTRACT_TYPE.get(type));
        return ApiResponse.success(wrapper);
    }

    @PostMapping("/contract_code_suggestion")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getContractCodeSuggestion(
            @RequestBody ContractCodeSuggestionPayload payload) {
        return ApiResponse.success(contractService.getContractCodeSuggestionByPrefix(payload));
    }

    @PostMapping("/clone")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getContractClone(
            @RequestBody ContractClonePayload payload) {
        return ApiResponse.success(contractService.getContractClone(payload));
    }

    @PutMapping("/cancel/{id}")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> cancelContract(
            @PathVariable("id") Long contractId,
            @CurrentUser UserDetails userDetails) {
        if (journeyDiaryService.checkContractHavingJourney(contractId)) {
            throw new BadRequestException("Contract is having a journey!!!")
                    .displayMessage(Translator.toLocale("contract.is_having_journey"));
        }
        User canceledBy = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(contractService.cancelContract(contractId, canceledBy));
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> deleteContract(
            @PathVariable("id") Long contractId) {
        return ApiResponse.success(contractService.deleteContract(contractId));
    }

    @GetMapping("/histories/{id}")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getChangeHistories(
            @PathVariable("id") Long contractId,
            @RequestParam(defaultValue = AvisApiConstant.DEFAULT_STARTER_PAGE_STRING, required = false) Integer page) {
        return ApiResponse.success(contractChangeHistoryService.findByContractId(contractId, page));
    }
}
