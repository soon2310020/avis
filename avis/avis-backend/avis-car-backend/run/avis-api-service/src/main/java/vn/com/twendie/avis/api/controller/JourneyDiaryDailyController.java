package vn.com.twendie.avis.api.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.payload.CreateOrUpdateBlankDiaryPayload;
import vn.com.twendie.avis.api.model.payload.CreateOrUpdateJDDLockPayload;
import vn.com.twendie.avis.api.model.payload.EditAppDiaryDailyPayload;
import vn.com.twendie.avis.api.model.payload.UpdateWithoutDriverDiaryPayload;
import vn.com.twendie.avis.api.model.response.*;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;

@RestController
@RequestMapping("/journey_diary_dailies")
public class JourneyDiaryDailyController {

    private final JourneyDiaryDailyService journeyDiaryDailyService;
    private final JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService;
    private final WorkingDayService workingDayService;
    private final ContractService contractService;
    private final JourneyDiaryDailyLockService journeyDiaryDailyLockService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final BranchService branchService;

    private final ListUtils listUtils;
    private final DateUtils dateUtils;
    private final ModelMapper modelMapper;

    private final JourneyDiaryService journeyDiaryService;
    private final JourneyDiarySignatureService journeyDiarySignatureService;

    public JourneyDiaryDailyController(JourneyDiaryDailyService journeyDiaryDailyService,
                                       JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService,
                                       WorkingDayService workingDayService,
                                       ContractService contractService,
                                       JourneyDiaryDailyLockService journeyDiaryDailyLockService,
                                       UserService userService,
                                       VehicleService vehicleService,
                                       BranchService branchService,
                                       ListUtils listUtils,
                                       DateUtils dateUtils,
                                       ModelMapper modelMapper,
                                       JourneyDiaryService journeyDiaryService,
                                       JourneyDiarySignatureService journeyDiarySignatureService) {
        this.journeyDiaryDailyService = journeyDiaryDailyService;
        this.journeyDiaryDailyCostTypeService = journeyDiaryDailyCostTypeService;
        this.workingDayService = workingDayService;
        this.contractService = contractService;
        this.journeyDiaryDailyLockService = journeyDiaryDailyLockService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.branchService = branchService;
        this.listUtils = listUtils;
        this.dateUtils = dateUtils;
        this.modelMapper = modelMapper;
        this.journeyDiaryService = journeyDiaryService;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
    }

    @GetMapping("/filter")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Customer"})
    public ApiResponse<?> filter(@RequestParam("contract_id") long contractId,
                                 @RequestParam("from_date") long fromDate,
                                 @RequestParam("to_date") long toDate,
                                 @RequestParam(name = "name", required = false) String name,
                                 @RequestParam(value = "member_customer_ids", required = false) String memberCustomerIds) {

        List<Long> memberCustomerIdList = null;
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(memberCustomerIds)){
            try {
                memberCustomerIdList = Arrays.stream(memberCustomerIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
            }catch (NumberFormatException e){
                throw new BadRequestException("member customer id incorrect").code(HttpStatus.BAD_REQUEST.value());
            }
        }

        Contract contract = contractService.findById(contractId);
        Timestamp from = dateUtils.max(dateUtils.getFirstDayOfMonth(new Timestamp(fromDate)), contract.getFromDatetime());
        Timestamp to = dateUtils.min(dateUtils.getLastDayOfMonth(new Timestamp(toDate)), contract.getToDatetime(), contract.getDateEarlyTermination());
        List<JourneyDiaryDaily> journeyDiaryDailies;
        if (WITH_DRIVER.value().equals(contract.getContractType().getId())) {
//            journeyDiaryDailies = journeyDiaryDailyService.find(contractId, from, to);
            journeyDiaryDailies = journeyDiaryDailyService.findAndName(name != null ? name : "",contractId, from, to, memberCustomerIdList);
        } else {
//            journeyDiaryDailies = journeyDiaryDailyService.findInMonth(contractId, from);
            journeyDiaryDailies = journeyDiaryDailyService.findNameInMonth(name != null ? name : "", contractId, from, memberCustomerIdList);
        }
        journeyDiaryDailyService.fetchChildren(journeyDiaryDailies);
        journeyDiaryDailyCostTypeService.fetchJourneyDiaryDailyCostTypes(journeyDiaryDailies);
        journeyDiaryDailyCostTypeService.fetchJourneyDiaryDailyStationFees(journeyDiaryDailies);
        Branch branch = branchService.getContractBranchAtTime(contract, to);
        String vehicleWorkingArea = contractService.getContractValueAtTime(contract, VEHICLE_WORKING_AREA.getName(), to, String.class);
        User driver = userService.getContractDriverAtTime(contract, to);
        Vehicle vehicle = vehicleService.getContractVehicleAtTime(contract, to);
        WorkingDay workingDay = workingDayService.getContractWorkingDayAtTime(contract, to);
        ContractDTO contractDTO = ContractDTO.builder()
                .branch(Objects.nonNull(branch) ? modelMapper.map(branch, BranchDTO.class) : null)
                .vehicleWorkingArea(vehicleWorkingArea)
                .driver(Objects.nonNull(driver) ? modelMapper.map(driver, DriverDTO.class) : null)
                .vehicle(Objects.nonNull(vehicle) ? modelMapper.map(vehicle, VehicleDTO.class) : null)
                .workingTimeFrom(contractService.getContractValueAtTime(contract, WORKING_TIME_FROM.getName(), to, Time.class))
                .workingTimeTo(contractService.getContractValueAtTime(contract, WORKING_TIME_TO.getName(), to, Time.class))
                .workingTimeWeekendHolidayFrom(contractService.getContractValueAtTime(contract, WORKING_TIME_WEEKEND_HOLIDAY_FROM.getName(), to, Time.class))
                .workingTimeWeekendHolidayTo(contractService.getContractValueAtTime(contract, WORKING_TIME_WEEKEND_HOLIDAY_TO.getName(), to, Time.class))
                .workingDay(Objects.nonNull(workingDay) ? modelMapper.map(workingDay, WorkingDayDTO.class) : null)
                .build();
        DateStatistic dateStatistic = journeyDiaryDailyService.getDateStatistic(contract, journeyDiaryDailies, from, to);
        BigDecimal totalOverKm = journeyDiaryDailyService.calculateTotalOverKm(contract, journeyDiaryDailies, from, to);
        TotalRow totalRow = new TotalRow(totalOverKm);
        List<JourneyDiaryDailyDTO> journeyDiaryDailyDTOs = listUtils.mapAll(journeyDiaryDailies, JourneyDiaryDailyDTO.class);


        journeyDiaryDailyService.fetchDataSignature(journeyDiaryDailyDTOs);

        return ApiResponse.success(JourneyDiaryDailyDetail.builder()
                .contract(contractDTO)
                .dateStatistic(dateStatistic)
                .journeyDiaryDailies(journeyDiaryDailyDTOs)
                .totalRow(totalRow)
                .build());
    }

    @PutMapping("/edit_app_diary")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> editAppDiary(
            @Valid @RequestBody EditAppDiaryDailyPayload payload
    ) {
        return ApiResponse.success(journeyDiaryDailyService.editAppDiary(payload));
    }

    @PostMapping("/create_or_update_blank_journey_diary_daily")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ApiResponse<?> createJourneyDiaryDaily(
            @Valid @RequestBody CreateOrUpdateBlankDiaryPayload payload
    ) {
        if (payload.getIsNew()) {
            return ApiResponse.success(journeyDiaryDailyService.create(payload));
        } else {

            if (Objects.isNull(payload.getId())) {
                throw new BadRequestException("Jdd id is null!!!")
                        .displayMessage(Translator.toLocale("error.blank_input"));
            }

            return ApiResponse.success(journeyDiaryDailyService.editManuallyCreatedDiary(payload));
        }
    }

    @PutMapping("/update_without_driver_diary")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    @Transactional
    public ApiResponse<?> updateWithoutDriverDiary(
            @Valid @RequestBody UpdateWithoutDriverDiaryPayload payload
    ) {
        return ApiResponse.success(journeyDiaryDailyService.editWithoutDriverDiary(payload));
    }

    @DeleteMapping("/delete")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> delete(
            @RequestParam("jdd_id") Long jddId
    ) {
        return ApiResponse.success(journeyDiaryDailyService.delete(jddId));
    }

    @GetMapping("/get_filter_options")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Customer"})
    public ApiResponse<?> getFilterOptions() {
        return ApiResponse.success(journeyDiaryDailyService.getFilterOptions());
    }

    @PostMapping("/create_or_update_diaries_lock")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> createOrUpdateDiariesLock(
            @RequestBody CreateOrUpdateJDDLockPayload payload,
            @CurrentUser UserDetails userDetails
    ) {
        if (payload.getLockTime().compareTo(dateUtils.getFirstDayOfMonth(dateUtils.now())) >= 0) {
            throw new BadRequestException("Lock time is invalid!!!")
                    .displayMessage(Translator.toLocale("jdd.error.lock_time_is_invalid"));
        }

        User user = ((UserPrincipal) userDetails).getUser();
        JourneyDiaryDailyLock journeyDiaryDailyLock = journeyDiaryDailyLockService.createOrUpdate(payload, user);

        return ApiResponse.success(modelMapper.map(journeyDiaryDailyLock, JourneyDiaryDailyLockDTO.class));
    }

    @RequirePermission(acceptedRoles = {"Superuser"})
    @GetMapping("/fix-working-time-gps")
    public ApiResponse<?> fixWorkingTimeGps(){
        journeyDiaryDailyService.fixTimeGPS();
        return ApiResponse.success("Successfully");
    }

    @RequirePermission(acceptedRoles = {"Superuser"})
    @GetMapping("/fix-over-km")
    public ApiResponse<?> fixOverKM(){
        journeyDiaryDailyService.fixOverKM();
        return ApiResponse.success("Successfully");
    }
}
