package vn.com.twendie.avis.api.controller;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.javatuples.Pair;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.adapter.ExportDiariesAdditionDataConverter;
import vn.com.twendie.avis.api.adapter.PaymentRequestAdditionDataConverter;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.core.util.ReflectUtils;
import vn.com.twendie.avis.api.core.util.StrUtils;
import vn.com.twendie.avis.api.mapping.DayOfWeekMapping;
import vn.com.twendie.avis.api.model.export.ExportConfig;
import vn.com.twendie.avis.api.model.export.KmReport;
import vn.com.twendie.avis.api.model.export.OvertimeReport;
import vn.com.twendie.avis.api.model.export.ReportName;
import vn.com.twendie.avis.api.model.filter.*;
import vn.com.twendie.avis.api.model.projection.JDDVehicleInfoProjection;
import vn.com.twendie.avis.api.model.projection.OvertimeInfo;
import vn.com.twendie.avis.api.model.projection.UserProjection;
import vn.com.twendie.avis.api.model.projection.VehicleProjection;
import vn.com.twendie.avis.api.model.response.*;
import vn.com.twendie.avis.api.repository.*;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.api.service.impl.ExportJourneyDiaryDailyServiceImpl;
import vn.com.twendie.avis.api.service.impl.ExportPaymentRequestImpl;
import vn.com.twendie.avis.api.service.impl.JourneyDiaryDailyServiceImpl;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.api.util.ReportNameUtil;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.DAYS;
import static vn.com.twendie.avis.api.core.util.DateUtils.MONTH_YEAR_PATTERN;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITHOUT_DRIVER;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;
import static vn.com.twendie.avis.data.enumtype.NormListEnum.CONTRACT_KM_NORM;
import static vn.com.twendie.avis.data.enumtype.UserRoleEnum.SUPERUSER;

@Controller
@RequestMapping("/export")
public class ExportController {

    private final ContractRepo contractRepo;
    private final VehicleRepo vehicleRepo;
    private final UserRepo userRepo;

    private final SpecificationBuilder<Vehicle> vehicleSpecificationBuilder;
    private final SpecificationBuilder<Contract> contractSpecificationBuilder;
    private final SpecificationBuilder<User> userSpecificationBuilder;
    private final FilterService<Contract, Long> contractFilterService;
    private final FilterService<Vehicle, Long> vehicleFilterService;
    private final FilterService<User, Long> userFilterService;

    private final MemberCustomerService memberCustomerService;
    private final CostTypeService costTypeService;
    private final NormListService normListService;
    private final ContractService contractService;
    private final JourneyDiaryDailyService journeyDiaryDailyService;
    private final ExportService exportService;
    private final LogContractCostTypeService logContractCostTypeService;
    private final LogContractNormListService logContractNormListService;
    private final ContractChangeHistoryService contractChangeHistoryService;
    private final WorkingCalendarService workingCalendarService;
    private final WorkingDayService workingDayService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final PaymentRequestService paymentRequestService;

    private final DayOfWeekMapping dayOfWeekMapping;
    private final ModelMapper modelMapper;

    private final ListUtils listUtils;
    private final DateUtils dateUtils;
    private final ReflectUtils reflectUtils;
    private final StrUtils strUtils;
    private final ExportJourneyDiaryDailyServiceImpl exportJourneyDiaryDailyService;
    private final ExportPaymentRequestImpl exportPaymentRequestImpl;
    private final CustomerPaymentRequestService customerPaymentRequestService;
    private final JourneyDiarySignatureService journeyDiarySignatureService;

    private final PaymentRequestController paymentRequestController;
    private final BranchRepo branchRepo;

    public ExportController(ContractRepo contractRepo,
                            VehicleRepo vehicleRepo,
                            UserRepo userRepo,
                            SpecificationBuilder<Vehicle> vehicleSpecificationBuilder,
                            SpecificationBuilder<Contract> contractSpecificationBuilder,
                            SpecificationBuilder<User> userSpecificationBuilder,
                            FilterService<Contract, Long> contractFilterService,
                            FilterService<Vehicle, Long> vehicleFilterService,
                            FilterService<User, Long> userFilterService,
                            MemberCustomerService memberCustomerService,
                            CostTypeService costTypeService,
                            NormListService normListService,
                            ContractService contractService,
                            JourneyDiaryDailyService journeyDiaryDailyService,
                            ExportService exportService,
                            LogContractCostTypeService logContractCostTypeService,
                            LogContractNormListService logContractNormListService,
                            ContractChangeHistoryService contractChangeHistoryService,
                            WorkingCalendarService workingCalendarService,
                            WorkingDayService workingDayService,
                            UserService userService,
                            VehicleService vehicleService,
                            PaymentRequestService paymentRequestService,
                            DayOfWeekMapping dayOfWeekMapping,
                            ModelMapper modelMapper,
                            ListUtils listUtils,
                            DateUtils dateUtils,
                            ReflectUtils reflectUtils,
                            StrUtils strUtils,
                            PaymentRequestController paymentRequestController,
                            ExportJourneyDiaryDailyServiceImpl exportJourneyDiaryDailyService,
                            CustomerPaymentRequestService customerPaymentRequestService,
                            ExportPaymentRequestImpl exportPaymentRequestImpl,
                            JourneyDiarySignatureService journeyDiarySignatureService,
                            BranchRepo branchRepo) {
        this.contractRepo = contractRepo;
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.vehicleSpecificationBuilder = vehicleSpecificationBuilder;
        this.contractSpecificationBuilder = contractSpecificationBuilder;
        this.userSpecificationBuilder = userSpecificationBuilder;
        this.contractFilterService = contractFilterService;
        this.vehicleFilterService = vehicleFilterService;
        this.userFilterService = userFilterService;
        this.memberCustomerService = memberCustomerService;
        this.costTypeService = costTypeService;
        this.normListService = normListService;
        this.contractService = contractService;
        this.journeyDiaryDailyService = journeyDiaryDailyService;
        this.exportService = exportService;
        this.logContractCostTypeService = logContractCostTypeService;
        this.logContractNormListService = logContractNormListService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.workingCalendarService = workingCalendarService;
        this.workingDayService = workingDayService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.paymentRequestService = paymentRequestService;
        this.dayOfWeekMapping = dayOfWeekMapping;
        this.modelMapper = modelMapper;
        this.listUtils = listUtils;
        this.dateUtils = dateUtils;
        this.reflectUtils = reflectUtils;
        this.strUtils = strUtils;
        this.paymentRequestController = paymentRequestController;
        this.exportJourneyDiaryDailyService = exportJourneyDiaryDailyService;
        this.exportPaymentRequestImpl = exportPaymentRequestImpl;
        this.customerPaymentRequestService = customerPaymentRequestService;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
        this.branchRepo = branchRepo;
    }

    @PostMapping("/contracts")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ResponseEntity<?> exportContracts(
            @Valid @RequestBody FilterWrapper<ContractFilter> filter,
            @CurrentUser UserDetails userDetails) throws IOException {

        User user = ((UserPrincipal) userDetails).getUser();

        Page<Contract> contracts = contractFilterService.filter(contractRepo, contractSpecificationBuilder, filter);
        costTypeService.fetchContractCostTypes(contracts.getContent());
        normListService.fetchContractNormLists(contracts.getContent());

        if (!user.getUserRole().getName().equals(SUPERUSER.getValue())) {
            contracts = listUtils.mapAll(contracts, Contract.class);
            contracts.getContent().stream()
                    .filter(contract -> !contract.getCreatedBy().equals(user))
                    .forEach(reflectUtils::hideInfo);
        }

        final InputStream inputStream;
        ContractTypeFilter contractTypeFilter = filter.getFilter().getContractType();
        if (!Objects.isNull(contractTypeFilter)
                && ContractTypeEnum.WITHOUT_DRIVER.value().equals(contractTypeFilter.getId())) {
            inputStream = Objects.requireNonNull(getClass().getClassLoader()
                    .getResourceAsStream("templates/excel/contract_without_driver.xlsx"));
        } else {
            inputStream = Objects.requireNonNull(getClass().getClassLoader()
                    .getResourceAsStream("templates/excel/contract_with_driver.xlsx"));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(contracts.getContent(), outputStream, inputStream);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("contracts.xlsx"))
                .body(outputStream.toByteArray());
    }

    @GetMapping("/vehicles_long_period_report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportVehiclesLongPeriodReport() throws IOException {
        List<Contract> contracts = contractService.findAllByContractTypeAndAssignedVehicle(WITH_DRIVER.value());
        costTypeService.fetchContractCostTypes(contracts);
        normListService.fetchContractNormLists(contracts);

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/vehicles_long_period_report.xlsx"));

        List<VehicleLongPeriodReportModel> models = listUtils.mapAll(contracts, VehicleLongPeriodReportModel.class);

        String reportName = ReportNameUtil.reportNameWithTimeRange(models.get(models.size() - 1).getCreatedAt(),
                models.get(0).getCreatedAt(), dateUtils);

        Workbook workbook = exportService.export(models, inputStream, ExportConfig.builder()
                .columnIndex(0)
                .keyRowIndex(2)
                .sampleRowIndex(4)
                .build());

        Workbook workbook1 = exportService.export(ReportName.builder()
                .name(reportName)
                .build(), workbook, 18, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(outputStream, workbook1);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("vehicles_long_period_report.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping("/vehicles_long_period_report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportVehiclesLongPeriodReport(
            @Valid @RequestBody FilterWrapper<ContractFilter> filter) throws IOException {
        List<Contract> contracts = contractFilterService.filter(contractRepo, contractSpecificationBuilder, filter)
                .getContent();
        costTypeService.fetchContractCostTypes(contracts);
        normListService.fetchContractNormLists(contracts);
        contractService.fetchFirstJourneyDiaryDate(contracts);

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/vehicles_long_period_report.xlsx"));

        List<VehicleLongPeriodReportModel> models = listUtils.mapAll(contracts, VehicleLongPeriodReportModel.class);

        Timestamp from = ObjectUtils.defaultIfNull(filter.getFilter().getSignDateGte(), new Timestamp(0));
        Timestamp to = ObjectUtils.defaultIfNull(filter.getFilter().getSignDateLte(), dateUtils.now());
        String reportName = ReportNameUtil.reportNameWithTimeRange(from, to, dateUtils);

        Workbook workbook = exportService.export(models, inputStream, ExportConfig.builder()
                .columnIndex(0)
                .keyRowIndex(2)
                .sampleRowIndex(4)
                .build());

        Workbook workbook1 = exportService.export(ReportName.builder()
                .name(reportName)
                .build(), workbook, 18, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(outputStream, workbook1);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("vehicles_long_period_report.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping("/vehicles")
    public ResponseEntity<byte[]> exportVehicles(@RequestBody FilterWrapper<VehicleFilter> filter)
            throws Exception {
        if(filter.getFilter() != null && filter.getFilter().getBranchId() != null){
            filter.getFilter().setBranch(branchRepo.findByIdAndDeletedFalse(filter.getFilter().getBranchId()).orElse(null));
        }
        Page<Vehicle> vehicles = vehicleFilterService.filter(vehicleRepo, vehicleSpecificationBuilder, filter);
        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/vehicles.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(vehicles.getContent(), outputStream, inputStream);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("vehicles.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping(value = "/customers", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ResponseEntity<?> exportAdminCustomers(@RequestBody FilterWrapper<AdminCustomerFilter> filter)
            throws IOException {
        Page<AdminCustomerProjection> adminCustomers = memberCustomerService.filterAdminCustomers(filter);
        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/admin-customers.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(adminCustomers.getContent(), outputStream, inputStream);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("customers.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping("/drivers")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportDrivers(@Valid @RequestBody FilterWrapper<DriverFilter> filterWrapper)
            throws IOException {
        if(filterWrapper.getFilter() != null && filterWrapper.getFilter().getBranchId() != null){
            filterWrapper.getFilter().setBranch(branchRepo.findByIdAndDeletedFalse(filterWrapper.getFilter().getBranchId()).orElse(null));
        }
        Page<User> drivers = userFilterService.filter(userRepo, userSpecificationBuilder, filterWrapper);
        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/drivers.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(drivers.getContent(), outputStream, inputStream);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("drivers.xlsx"))
                .body(outputStream.toByteArray());
    }

    @GetMapping("/overtime-report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportOvertimeReport(@RequestParam("time") long timestampMillis) throws IOException {
        Timestamp timestamp = new Timestamp(timestampMillis);
        Timestamp from = dateUtils.getFirstDayOfMonth(timestamp);
        Timestamp to = dateUtils.getLastDayOfMonth(timestamp);

        List<Contract> contracts = contractService.findContractsInTimeRange(WITH_DRIVER.value(),
                dateUtils.subtract(from, 1, DAYS), dateUtils.add(to, 1, DAYS));
        List<Long> contractIds = listUtils.transform(contracts, Contract::getId);
        journeyDiaryDailyService.fetchJourneyDiaryDailies(contracts, from, to);
        costTypeService.fetchContractCostTypes(contracts);
        normListService.fetchContractNormLists(contracts);
        contractChangeHistoryService.fetchContractChangeHistories(contracts);
        logContractCostTypeService.fetchLogContractCostTypes(contracts);
        logContractNormListService.fetchLogContractNormLists(contracts);

        List<Pair<Contract, Timestamp>> contractTimestampPairs = listUtils.transform(contracts, contract -> Pair.with(contract, to));
        List<OvertimeReport> overtimeReports = listUtils.mapAll(contractTimestampPairs, OvertimeReport.class);
        Map<Long, OvertimeInfo> overtimeInfos = journeyDiaryDailyService.findOvertimeInfos(contractIds, from, to);
//        overtimeReports.forEach(overtimeReport -> {
//            OvertimeInfo overtimeInfo = overtimeInfos.get(overtimeReport.getContractId());
//            overtimeReport.setRealWorkingDay(Objects.nonNull(overtimeInfo) ? overtimeInfo.getRealWorkingDay() : 0L);
//            overtimeReport.setOvertime(Objects.nonNull(overtimeInfo) ? overtimeInfo.getOvertime() : 0L);
//            overtimeReport.setOvernight(Objects.nonNull(overtimeInfo) ? overtimeInfo.getOverNight() : 0L);
//            overtimeReport.setWeekend(Objects.nonNull(overtimeInfo) ? overtimeInfo.getWeekend() : 0L);
//            overtimeReport.setHoliday(Objects.nonNull(overtimeInfo) ? overtimeInfo.getHoliday() : 0L);
//        });

        Set<Long> vehicleIds = overtimeReports.stream().map(OvertimeReport::getVehicleId).collect(Collectors.toSet());
        Set<Long> driverIds = overtimeReports.stream().map(OvertimeReport::getDriverId).collect(Collectors.toSet());
        Map<Long, VehicleProjection> vehicles = vehicleService.findByIdIn(vehicleIds);
        Map<Long, UserProjection> drivers = userService.findByIdIn(driverIds);
        overtimeReports.forEach(overtimeReport -> {
            if (Objects.nonNull(overtimeReport.getVehicleId())) {
                VehicleProjection vehicleProjection = vehicles.get(overtimeReport.getVehicleId());
                if (Objects.nonNull(vehicleProjection)) {
                    overtimeReport.setVehicleNumberPlate(vehicleProjection.getNumberPlate());
                    overtimeReport.setVehicleModel(vehicleProjection.getModel());
                    overtimeReport.setVehicleOperationAdmin(vehicleProjection.getOperationAdminName());
                }
            }
            if (Objects.nonNull(overtimeReport.getDriverId())) {
                UserProjection userProjection = drivers.get(overtimeReport.getDriverId());
                if (Objects.nonNull(userProjection)) {
                    overtimeReport.setDriverName(userProjection.getName());
                }
            }
        });

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/overtime-report.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(overtimeReports, outputStream, inputStream,
                ExportConfig.builder()
                        .columnIndex(0)
                        .sampleRowIndex(4)
                        .copyRowHeight(true)
                        .sheetName(dateUtils.format(timestamp, MONTH_YEAR_PATTERN).toUpperCase())
                        .build());

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("overtime-report.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping("/overtime-report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportOvertimeReport(@Valid @RequestBody OvertimeReportFilter overtimeReportFilter) throws IOException {
        Timestamp timestamp = overtimeReportFilter.getTime();
        Timestamp from = dateUtils.getFirstDayOfMonth(timestamp);
        Timestamp to = dateUtils.getLastDayOfMonth(timestamp);

        List<Contract> contracts = contractFilterService.filter(contractRepo, contractSpecificationBuilder, overtimeReportFilter)
                .getContent();

        journeyDiaryDailyService.fetchJourneyDiaryDailies(contracts, from, to);
        costTypeService.fetchContractCostTypes(contracts);
        normListService.fetchContractNormLists(contracts);
        contractChangeHistoryService.fetchContractChangeHistories(contracts);
        logContractCostTypeService.fetchLogContractCostTypes(contracts);
        logContractNormListService.fetchLogContractNormLists(contracts);

        List<Pair<Contract, Timestamp>> contractTimestampPairs = listUtils.transform(contracts, contract -> Pair.with(contract, to));
        List<OvertimeReport> overtimeReports = listUtils.mapAll(contractTimestampPairs, OvertimeReport.class);
//        Map<Long, OvertimeInfo> overtimeInfos = journeyDiaryDailyService.findOvertimeInfos(contractIds, from, to);
//        overtimeReports.forEach(overtimeReport -> {
//            OvertimeInfo overtimeInfo = overtimeInfos.get(overtimeReport.getContractId());
//            overtimeReport.setRealWorkingDay(Objects.nonNull(overtimeInfo) ? overtimeInfo.getRealWorkingDay() : 0L);
//            overtimeReport.setOvertime(Objects.nonNull(overtimeInfo) ? overtimeInfo.getOvertime() : 0L);
//            overtimeReport.setOvernight(Objects.nonNull(overtimeInfo) ? overtimeInfo.getOverNight() : 0L);
//            overtimeReport.setWeekend(Objects.nonNull(overtimeInfo) ? overtimeInfo.getWeekend() : 0L);
//            overtimeReport.setHoliday(Objects.nonNull(overtimeInfo) ? overtimeInfo.getHoliday() : 0L);
//        });

        Set<Long> vehicleIds = overtimeReports.stream().map(OvertimeReport::getVehicleId).collect(Collectors.toSet());
        Set<Long> driverIds = overtimeReports.stream().map(OvertimeReport::getDriverId).collect(Collectors.toSet());
        Map<Long, VehicleProjection> vehicles = vehicleService.findByIdIn(vehicleIds);
        Map<Long, UserProjection> drivers = userService.findByIdIn(driverIds);
        overtimeReports.forEach(overtimeReport -> {
            if (Objects.nonNull(overtimeReport.getVehicleId())) {
                VehicleProjection vehicleProjection = vehicles.get(overtimeReport.getVehicleId());
                if (Objects.nonNull(vehicleProjection)) {
                    overtimeReport.setVehicleNumberPlate(vehicleProjection.getNumberPlate());
                    overtimeReport.setVehicleModel(vehicleProjection.getModel());
                    overtimeReport.setVehicleOperationAdmin(vehicleProjection.getOperationAdminName());
                }
            }
            if (Objects.nonNull(overtimeReport.getDriverId())) {
                UserProjection userProjection = drivers.get(overtimeReport.getDriverId());
                if (Objects.nonNull(userProjection)) {
                    overtimeReport.setDriverName(userProjection.getName());
                }
            }
        });

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/overtime-report.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(overtimeReports, outputStream, inputStream,
                ExportConfig.builder()
                        .columnIndex(0)
                        .sampleRowIndex(4)
                        .copyRowHeight(true)
                        .sheetName(dateUtils.format(timestamp, MONTH_YEAR_PATTERN).toUpperCase())
                        .build());

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("overtime-report.xlsx"))
                .body(outputStream.toByteArray());
    }

    @GetMapping("/km-report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportKmReport(@RequestParam("time") long timestampMillis) throws IOException {
        Timestamp timestamp = new Timestamp(timestampMillis);
        Timestamp from = dateUtils.getFirstDayOfMonth(timestamp);
        Timestamp to = dateUtils.getLastDayOfMonth(timestamp);

        Set<Long> contractTypeIds = Sets.newHashSet(WITH_DRIVER.value(), WITHOUT_DRIVER.value());
        List<Contract> contracts = contractService.findContractsInTimeRange(contractTypeIds,
                dateUtils.subtract(from, 1, DAYS), dateUtils.add(to, 1, DAYS));
        journeyDiaryDailyService.fetchJourneyDiaryDailies(contracts, from, to);
        contractChangeHistoryService.fetchContractChangeHistories(contracts);

        List<Pair<Contract, Timestamp>> contractTimestampPairs = listUtils.transform(contracts, contract -> Pair.with(contract, to));
        List<KmReport> kmReports = listUtils.mapAll(contractTimestampPairs, KmReport.class);

        Set<Long> vehicleIds = kmReports.stream().map(KmReport::getVehicleId).collect(Collectors.toSet());
        Set<Long> driverIds = kmReports.stream().map(KmReport::getDriverId).collect(Collectors.toSet());
        Map<Long, VehicleProjection> vehicles = vehicleService.findByIdIn(vehicleIds);
        Map<Long, UserProjection> drivers = userService.findByIdIn(driverIds);
        kmReports.forEach(kmReport -> {
            if (Objects.nonNull(kmReport.getVehicleId())) {
                VehicleProjection vehicleProjection = vehicles.get(kmReport.getVehicleId());
                if (Objects.nonNull(vehicleProjection)) {
                    kmReport.setVehicleNumberPlate(vehicleProjection.getNumberPlate());
                    kmReport.setVehicleType(vehicleProjection.getType());
                    kmReport.setVehicleOwner(vehicleProjection.getOwner());
                    kmReport.setVehicleOperationAdmin(vehicleProjection.getOperationAdminName());
                }
            }
            if (Objects.nonNull(kmReport.getDriverId())) {
                UserProjection userProjection = drivers.get(kmReport.getDriverId());
                if (Objects.nonNull(userProjection)) {
                    kmReport.setDriverName(userProjection.getName());
                }
            }
        });

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/km-report.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(kmReports, outputStream, inputStream,
                ExportConfig.builder()
                        .columnIndex(0)
                        .keyRowIndex(4)
                        .sampleRowIndex(7)
                        .copyRowHeight(true)
                        .sheetName(dateUtils.format(timestamp, MONTH_YEAR_PATTERN).toUpperCase())
                        .build());

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("km-report.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping("/km-report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportKmReport(@Valid @RequestBody KmReportFilter kmReportFilter) throws IOException {
        Timestamp timestamp = kmReportFilter.getTime();
        Timestamp from = dateUtils.getFirstDayOfMonth(timestamp);
        Timestamp to = dateUtils.getLastDayOfMonth(timestamp);

        List<Contract> contracts = contractFilterService.filter(contractRepo, contractSpecificationBuilder, kmReportFilter)
                .getContent();
        journeyDiaryDailyService.fetchJourneyDiaryDailies(contracts, from, to);
        contractChangeHistoryService.fetchContractChangeHistories(contracts);

        List<Pair<Contract, Timestamp>> contractTimestampPairs = listUtils.transform(contracts, contract -> Pair.with(contract, to));
        List<KmReport> kmReports = listUtils.mapAll(contractTimestampPairs, KmReport.class);

        Set<Long> vehicleIds = kmReports.stream().map(KmReport::getVehicleId).collect(Collectors.toSet());
        Set<Long> driverIds = kmReports.stream().map(KmReport::getDriverId).collect(Collectors.toSet());
        Map<Long, VehicleProjection> vehicles = vehicleService.findByIdIn(vehicleIds);
        Map<Long, UserProjection> drivers = userService.findByIdIn(driverIds);
        kmReports.forEach(kmReport -> {
            if (Objects.nonNull(kmReport.getVehicleId())) {
                VehicleProjection vehicleProjection = vehicles.get(kmReport.getVehicleId());
                if (Objects.nonNull(vehicleProjection)) {
                    kmReport.setVehicleNumberPlate(vehicleProjection.getNumberPlate());
                    kmReport.setVehicleType(vehicleProjection.getType());
                    kmReport.setVehicleOwner(vehicleProjection.getOwner());
                    kmReport.setVehicleOperationAdmin(vehicleProjection.getOperationAdminName());
                }
            }
            if (Objects.nonNull(kmReport.getDriverId())) {
                UserProjection userProjection = drivers.get(kmReport.getDriverId());
                if (Objects.nonNull(userProjection)) {
                    kmReport.setDriverName(userProjection.getName());
                }
            }
        });

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/km-report.xlsx"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(kmReports, outputStream, inputStream,
                ExportConfig.builder()
                        .columnIndex(0)
                        .keyRowIndex(4)
                        .sampleRowIndex(7)
                        .copyRowHeight(true)
                        .sheetName(dateUtils.format(timestamp, MONTH_YEAR_PATTERN).toUpperCase())
                        .build());

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("km-report.xlsx"))
                .body(outputStream.toByteArray());
    }

    @GetMapping("/diaries")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Customer"})
    public ResponseEntity<?> exportDiaries(@RequestParam("contract_id") long contractId,
                                           @RequestParam("from_date") long fromDate,
                                           @RequestParam("to_date") long toDate,
                                           @RequestParam(value = "type", required = false) String type,
                                           @RequestParam(value = "name", required = false) String name,
                                           @RequestParam(value = "member_customer_ids", required = false) String memberCustomerIds) throws Exception {
        Contract contract = contractService.findById(contractId);
        Timestamp from = new Timestamp(fromDate);
        Timestamp to = new Timestamp(toDate);

        Timestamp begin = journeyDiaryDailyService.getStartDate(contract, from);
        Timestamp end = journeyDiaryDailyService.getEndDate(contract, to);
        List<JourneyDiaryDaily> journeyDiaryDailies;

        List<Long> memberCustomerIdList = null;
        String nameFinds = "Tất cả người sử dụng";
        if(StringUtils.isNotEmpty(memberCustomerIds)){
            try {
                memberCustomerIdList = Arrays.stream(memberCustomerIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
                String names = vn.com.twendie.avis.api.util.StringUtils.convertListToString(memberCustomerService.getNameByIdIn(memberCustomerIdList));
                if(StringUtils.isNotEmpty(names)){
                    nameFinds = names;
                }
            }catch (NumberFormatException e){
                throw new BadRequestException("member customer id incorrect").code(HttpStatus.BAD_REQUEST.value());
            }
        }

//        export pdf
        if("pdf".equals(type)){
            journeyDiaryDailies = journeyDiaryDailyService.findAndName(name != null ? name : "", contract.getId(), begin, end, memberCustomerIdList);
            ExportDiariesAdditionDataWrapper dataWrapper = new ExportDiariesAdditionDataConverter(from, to, end,
                    contractChangeHistoryService, workingCalendarService, workingDayService, userService, vehicleService,
                    dateUtils, journeyDiaryDailies, memberCustomerService, memberCustomerIdList).apply(contract);
            BigDecimal totalOverKm = journeyDiaryDailyService.calculateTotalOverKm(contract, journeyDiaryDailies, from, to);
            exportJourneyDiaryDailyService.buildHtmlJourneyDiaryDailies(dataWrapper, journeyDiaryDailies, totalOverKm.longValue());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exportJourneyDiaryDailyService.export(outputStream, 3850, -1);
            return ResponseEntity.ok()
                    .headers(buildHttpHeaders("diaries.pdf"))
                    .body(outputStream.toByteArray());
        }

        WorkbookWrapper wrapper;
        if (WITH_DRIVER.value().equals(contract.getContractType().getId())) {
            journeyDiaryDailies = journeyDiaryDailyService.findAndName(name != null ? name : "", contract.getId(), begin, end, memberCustomerIdList);
            wrapper = exportDiariesToExcel("templates/excel/diaries.xlsx", contract,
                    begin, end, journeyDiaryDailies, 15, 18, 0, nameFinds);
        } else {
            journeyDiaryDailies = journeyDiaryDailyService.findNameInMonth(name != null ? name : "",contractId, begin, memberCustomerIdList);
            wrapper = exportDiariesToExcel("templates/excel/diaries_without_driver.xlsx", contract,
                    begin, end, journeyDiaryDailies, 15, 18, 0, nameFinds);
        }

        ExportDiariesAdditionDataWrapper dataWrapper = new ExportDiariesAdditionDataConverter(from, to, end,
                contractChangeHistoryService, workingCalendarService, workingDayService, userService, vehicleService,
                dateUtils, journeyDiaryDailies, memberCustomerService, memberCustomerIdList).apply(contract);

        Workbook workbook1 = exportService.export(dataWrapper, wrapper.getWorkbook(), 2, 13);

        Workbook workbook2 = exportService.export(dataWrapper, workbook1, 5, 13);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(outputStream, workbook2);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("diaries.xlsx"))
                .body(outputStream.toByteArray());
    }

    @GetMapping("/payment_request")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportPaymentRequest(@RequestParam long from,
                                                  @RequestParam long to,
                                                  @RequestParam("contract_id") Long contractId,
                                                  @RequestParam(value = "type", required = false) String type,
                                                  @RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "member_customer_ids", required = false) String memberCustomerIds) throws Exception {
        Contract contract = contractService.findById(contractId);
        Timestamp fromDate = new Timestamp(from);
        Timestamp toDate = new Timestamp(to);
        Timestamp begin = journeyDiaryDailyService.getStartDate(contract, fromDate);
        Timestamp end = journeyDiaryDailyService.getEndDate(contract, toDate);
//        List<JourneyDiaryDaily> journeyDiaryDailies =
//                journeyDiaryDailyService.find(contract.getId(), begin, end);

        List<Long> memberCustomerIdList = null;
        String nameFinds = "Tất cả người sử dụng";

        if(StringUtils.isNotEmpty(memberCustomerIds)){
            try {
                memberCustomerIdList = Arrays.stream(memberCustomerIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
                String names = vn.com.twendie.avis.api.util.StringUtils.convertListToString(memberCustomerService.getNameByIdIn(memberCustomerIdList));
                if(StringUtils.isNotEmpty(names)){
                    nameFinds = names;
                }
            }catch (NumberFormatException e){
                throw new BadRequestException("member customer id incorrect").code(HttpStatus.BAD_REQUEST.value());
            }
        }

        List<JourneyDiaryDaily> journeyDiaryDailies =
                journeyDiaryDailyService.findAndName(name != null ? name : "", contractId, fromDate, toDate, memberCustomerIdList);
        List<PaymentRequestItemDTO> dtos = paymentRequestController.getListPaymentRequestItem(begin,
                end, contract, journeyDiaryDailies);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileName = "payment_request";
        WorkbookWrapper exportDiariesWrapper = exportDiariesToExcel("templates/excel/payment_request.xlsx", contract,
                begin, end, journeyDiaryDailies, 17, 20, 0, nameFinds);
        if("pdf".equals(type)){
            fileName+=".pdf";
            exportJourneyDiaryDailyService.buildHtmlPaymentRequest(journeyDiaryDailies, dtos, contract, 0L);
            exportJourneyDiaryDailyService.export(outputStream, 4500, 100000);
        }else {
            fileName+= ".xlsx";
            Workbook workbook = exportPR(exportDiariesWrapper, 15, dtos, fromDate, toDate, contract, nameFinds);

            exportService.exportToStream(outputStream, workbook);
        }

        return ResponseEntity.ok()
                .headers(buildHttpHeaders(fileName))
                .body(outputStream.toByteArray());
    }

    @GetMapping("/payment_request_without_driver")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportPaymentRequestWithoutDriver(@RequestParam long from,
                                                               @RequestParam long to,
                                                               @RequestParam("contract_id") Long contractId) throws Exception {
        Contract contract = contractService.findById(contractId);
        Timestamp fromDate = new Timestamp(from);
        Timestamp toDate = new Timestamp(to);

        PRWithoutDriverResponseWrapper wrapper = paymentRequestController
                .getPaymentRequestWithoutDriver(from, to, contractId)
                .data();
        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/payment_request_without_driver.xlsx"));

        List<PRWithoutDriverDiary> diaries = wrapper.getDiaries().subList(0, wrapper.getDiaries().size() - 1);
        List<PRWithoutDriverDiary> totalRowDiary = wrapper.getDiaries().subList(wrapper.getDiaries().size() - 1, wrapper.getDiaries().size());
        totalRowDiary.get(0).setDate("Total");

        WorkbookWrapper exportDiariesWrapper = exportService.exportToWrapper(diaries, inputStream, ExportConfig.builder()
                .keyRowIndex(17)
                .sampleRowIndex(20)
                .columnIndex(0)
                .build());

        WorkbookWrapper exportDiariesTotalRow = exportService.exportToWrapper(totalRowDiary, exportDiariesWrapper.getWorkbook(), ExportConfig.builder()
                .keyRowIndex(17)
                .sampleRowIndex(exportDiariesWrapper.getLastRowIndexWithData() + 1)
                .columnIndex(null)
                .build());

        Workbook workbook = exportPR(exportDiariesTotalRow, 15, wrapper.getPaymentRequestItemDTOS(),
                fromDate, toDate, contract, "");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(outputStream, workbook);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("payment_request.xlsx"))
                .body(outputStream.toByteArray());
    }

    private Workbook exportPR(WorkbookWrapper exportDiariesTotalRow, int keyRowIndex,
                              List<PaymentRequestItemDTO> paymentRequestItemDTOS,
                              Timestamp fromDate, Timestamp toDate, Contract contract, String nameFinds) throws IOException {
        List<PaymentRequestItemDTO> items = paymentRequestItemDTOS.subList(0, paymentRequestItemDTOS.size() - 1);
        List<PaymentRequestItemDTO> totalRow = paymentRequestItemDTOS.subList(paymentRequestItemDTOS.size() - 1,
                paymentRequestItemDTOS.size());

        WorkbookWrapper exportPR = exportService.exportToWrapper(items, exportDiariesTotalRow.getWorkbook(), ExportConfig.builder()
                .keyRowIndex(keyRowIndex)
                .sampleRowIndex(exportDiariesTotalRow.getLastRowIndexWithData() + 3)
                .columnIndex(null)
                .build());

        WorkbookWrapper exportPRTotalRow = exportService.exportToWrapper(totalRow, exportPR.getWorkbook(), ExportConfig.builder()
                .keyRowIndex(keyRowIndex)
                .sampleRowIndex(exportPR.getLastRowIndexWithData() + 1)
                .columnIndex(null)
                .build());

        final String title = "PAYMENT REQUEST IN " + dateUtils.format(fromDate, MONTH_YEAR_PATTERN).toUpperCase();
        final String totalPriceInWords = StringUtils.capitalize(strUtils.convertNumberToWords(totalRow.get(0).getTotalPrice())) + " (VNĐ)";

        PaymentRequestAdditionDataWrapper additionDataWrapper = new PaymentRequestAdditionDataConverter(
                title, fromDate, toDate, totalPriceInWords, userService, vehicleService, dateUtils, nameFinds).apply(contract);
        Workbook workbook = exportPRTotalRow.getWorkbook();
        workbook = exportService.export(additionDataWrapper, workbook, 5, 4, 4);
        workbook = exportService.export(additionDataWrapper, workbook, 1, 13);
        workbook = exportService.export(additionDataWrapper, workbook, 2, exportPR.getLastRowIndexWithData() + 1, exportPR.getLastRowIndexWithData() + 1);
        return workbook;
    }

    @GetMapping("/ticket_fee_report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportTicketFeeReport(
            @RequestParam(value = "time", required = false) Long time,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to,
            @RequestParam(value = "branch_code", required = false) String branchCode
    ) throws IOException {
        Timestamp timestamp = new Timestamp(ObjectUtils.defaultIfNull(time, System.currentTimeMillis()));
        Timestamp fromTimestamp = new Timestamp(ObjectUtils.defaultIfNull(from, dateUtils.getFirstDayOfMonth(timestamp).getTime()));
        Timestamp toTimestamp = new Timestamp(ObjectUtils.defaultIfNull(to, dateUtils.getLastDayOfMonth(timestamp).getTime()));

        List<JDDVehicleInfoProjection> projections = journeyDiaryDailyService
                .findJDDTicketFeeInfo(fromTimestamp, toTimestamp, branchCode);
        Collection<TicketFeeReportItem> ticketFeeReportItems = calculateTicketFee(projections);

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/ticket_fee.xlsx"));

        Workbook workbook = exportService.export(ticketFeeReportItems,
                inputStream, ExportConfig.builder()
                        .keyRowIndex(2)
                        .sampleRowIndex(5)
                        .columnIndex(0)
                        .build());

        String reportName = ReportNameUtil.reportNameWithTimeRange(fromTimestamp, toTimestamp, dateUtils);

        exportService.export(ReportName.builder()
                        .name(reportName)
                        .build(),
                workbook, 7, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(outputStream, workbook);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("ticket_fee.xlsx"))
                .body(outputStream.toByteArray());
    }

    @PostMapping("/ticket_fee_report")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ResponseEntity<?> exportTicketFeeReport(
            @RequestBody TicketFeeReportFilter ticketFeeReportFilter) throws IOException {
        Timestamp fromTimestamp = ObjectUtils.defaultIfNull(ticketFeeReportFilter.getFrom(), new Timestamp(0));
        Timestamp toTimestamp = ObjectUtils.defaultIfNull(ticketFeeReportFilter.getTo(), dateUtils.getLastDayOfMonth(dateUtils.now()));

        List<JDDVehicleInfoProjection> projections = journeyDiaryDailyService
                .findJDDTicketFeeInfo(fromTimestamp, toTimestamp,
                        ticketFeeReportFilter.getBranchCode(),
                        ticketFeeReportFilter.getVehicleNumberPlates());
        Collection<TicketFeeReportItem> ticketFeeReportItems = calculateTicketFee(projections);

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("templates/excel/ticket_fee.xlsx"));

        Workbook workbook = exportService.export(ticketFeeReportItems,
                inputStream, ExportConfig.builder()
                        .keyRowIndex(2)
                        .sampleRowIndex(5)
                        .columnIndex(0)
                        .build());

        String reportName = ReportNameUtil.reportNameWithTimeRange(fromTimestamp, toTimestamp, dateUtils);

        exportService.export(ReportName.builder()
                        .name(reportName)
                        .build(),
                workbook, 7, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportToStream(outputStream, workbook);

        return ResponseEntity.ok()
                .headers(buildHttpHeaders("ticket_fee.xlsx"))
                .body(outputStream.toByteArray());
    }

    private Collection<TicketFeeReportItem> calculateTicketFee(List<JDDVehicleInfoProjection> projections) {
        Map<String, TicketFeeReportItem> map = new HashMap<>();
        for (JDDVehicleInfoProjection projection : projections) {
            String numberPlate = Objects.isNull(projection.getNumberPlate()) ?
                    Objects.isNull(projection.getCustomNumberPlate()) ? null : projection.getCustomNumberPlate()
                    : projection.getNumberPlate();

            if (Objects.nonNull(numberPlate)) {
                TicketFeeReportItem ticketFeeReportItem;

                BigDecimal tollFee = JourneyDiaryCostTypeEnum.TOLLS_FEE.code()
                        .equals(projection.getCostCode()) ?
                        Objects.isNull(projection.getCostValue()) ? ZERO : projection.getCostValue()
                        : ZERO;

                BigDecimal parkingFee = JourneyDiaryCostTypeEnum.PARKING_FEE.code()
                        .equals(projection.getCostCode()) ?
                        Objects.isNull(projection.getCostValue()) ? ZERO : projection.getCostValue()
                        : ZERO;

                BigDecimal tireRepairFee = JourneyDiaryCostTypeEnum.TIRE_REPAIR_FEE.code()
                        .equals(projection.getCostCode()) ?
                        Objects.isNull(projection.getCostValue()) ? ZERO : projection.getCostValue()
                        : ZERO;

                BigDecimal carWashFee = JourneyDiaryCostTypeEnum.CAR_WASH_FEE.code()
                        .equals(projection.getCostCode()) ?
                        Objects.isNull(projection.getCostValue()) ? ZERO : projection.getCostValue()
                        : ZERO;

                if (Objects.nonNull(map.get(numberPlate))) {
                    ticketFeeReportItem = map.get(numberPlate);
                    ticketFeeReportItem.setTotalTollFee(ticketFeeReportItem.getTotalTollFee().add(tollFee));
                    ticketFeeReportItem.setTotalParkingFee(ticketFeeReportItem.getTotalParkingFee().add(parkingFee));
                    ticketFeeReportItem.setTotalTireRepairFee(ticketFeeReportItem.getTotalTireRepairFee().add(tireRepairFee));
                    ticketFeeReportItem.setTotalCarWashFee(ticketFeeReportItem.getTotalCarWashFee().add(carWashFee));
                    ticketFeeReportItem.setTotalTireRepairAndCarWashFee(ticketFeeReportItem.getTotalTireRepairAndCarWashFee().add(carWashFee).add(tireRepairFee));
                    ticketFeeReportItem.setTotalTollAndParkingFee(ticketFeeReportItem.getTotalTollAndParkingFee().add(tollFee).add(parkingFee));
                } else {
                    ticketFeeReportItem = TicketFeeReportItem.builder()
                            .numberPlate(numberPlate)
                            .totalTollFee(tollFee)
                            .totalParkingFee(parkingFee)
                            .totalCarWashFee(carWashFee)
                            .totalTireRepairFee(tireRepairFee)
                            .totalTireRepairAndCarWashFee(tireRepairFee.add(carWashFee))
                            .totalTollAndParkingFee(tollFee.add(parkingFee))
                            .build();
                    map.put(numberPlate, ticketFeeReportItem);
                }

                if (StringUtils.isBlank(ticketFeeReportItem.getAccountantName())) {
                    ticketFeeReportItem.setAccountantName(
                            StringUtils.defaultIfBlank("", projection.getAccountantName()));
                }
            }
        }
        return map.values();
    }

    private WorkbookWrapper exportDiariesToExcel(String resourceFilePath, Contract contract, Timestamp from, Timestamp to,
                                                 List<JourneyDiaryDaily> journeyDiaryDailies, int keyRowIndex, int sampleRowIndex, Integer identityColumnIndex,
                                                 String nameFinds) throws IOException {

        List<JourneyDiaryDailyDTO> dtos = listUtils.mapAll(journeyDiaryDailies, JourneyDiaryDailyDTO.class);

        dtos.forEach(x -> {
            x.setDayOfWeek(dayOfWeekMapping.map(x.getDate()));
            JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiaryId(x.getJourneyDiaryId());
            if(journeyDiarySignature != null && journeyDiarySignature.getMemberCustomer() != null){
                MemberCustomer memberCustomer = journeyDiarySignature.getMemberCustomer();
                x.setCustomerNameUsed(memberCustomer.getName());
                x.setCustomerDepartment(memberCustomer.getDepartment());
            }
        });

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream(resourceFilePath));

        WorkbookWrapper wrapper = exportService.exportToWrapper(dtos, inputStream, ExportConfig.builder()
                .keyRowIndex(keyRowIndex)
                .sampleRowIndex(sampleRowIndex)
                .columnIndex(identityColumnIndex)
                .build());

        JourneyDiaryDailyDTO totalRow;

        if (WITH_DRIVER.value().equals(contract.getContractType().getId())) {
            totalRow = calculateTotalRow(from, to, contract, journeyDiaryDailies);
            totalRow.setNameFinds(nameFinds);
        } else {
            totalRow = calculateTotalRowJDDWithoutDriver(from, to, contract, journeyDiaryDailies);
            totalRow.setNameFinds(nameFinds);
        }

        return exportService.exportToWrapper(Collections.singletonList(totalRow), wrapper.getWorkbook(), ExportConfig.builder()
                .keyRowIndex(keyRowIndex)
                .sampleRowIndex(wrapper.getLastRowIndexWithData() + 1)
                .columnIndex(null)
                .build());
    }

    public JourneyDiaryDailyDTO calculateTotalRow(Timestamp from, Timestamp to,
                                                  Contract contract,
                                                  List<JourneyDiaryDaily> journeyDiaryDailies) {

        JourneyDiaryDaily totalRow = JourneyDiaryDaily.builder().build();
        BigDecimal totalOverKm = journeyDiaryDailyService.calculateTotalOverKm(contract, journeyDiaryDailies, from, to);

        Integer totalOverTime = Math.toIntExact(journeyDiaryDailies.stream()
                .filter(x -> Objects.nonNull(x.getOverTime())
                        && !x.getIsSelfDrive())
                .mapToLong(JourneyDiaryDaily::getOverTime)
                .sum());

        BigDecimal totalOverKMSelfDrive = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getOverKmSelfDrive)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        Integer totalOvernight = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getOvernight)
                .filter(Objects::nonNull)
                .reduce(Integer::sum)
                .orElse(0);

        List<CodeValueModel> codeValueModels = costTypeService.findAll()
                .stream()
                .filter(costType -> JourneyDiaryCostTypeEnum.codes().contains(costType.getCode()))
                .map(costType -> CodeValueModel.builder()
                        .code(costType.getCode())
                        .value(new BigDecimal(0))
                        .build())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(journeyDiaryDailies)) {
            List<JourneyDiaryDailyCostType> totalCosts = journeyDiaryDailies
                    .stream()
                    .map(JourneyDiaryDaily::getJourneyDiaryDailyCostTypes)
                    .flatMap(Collection::stream)
                    .filter(journeyDiaryDailyCostType -> Objects.nonNull(journeyDiaryDailyCostType.getValue()))
                    .collect(Collectors.groupingBy(JourneyDiaryDailyCostType::getCostType))
                    .entrySet()
                    .stream()
                    .map(x -> JourneyDiaryDailyCostType.builder()
                            .value(x.getValue().stream()
                                    .map(JourneyDiaryDailyCostType::getValue)
                                    .filter(Objects::nonNull)
                                    .reduce(BigDecimal::add)
                                    .orElse(ZERO))
                            .costType(x.getKey())
                            .build())
                    .collect(Collectors.toList());

            codeValueModels = listUtils.mapAll(totalCosts, CodeValueModel.class);
        }

        BigDecimal totalKm = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getTotalKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);
        BigDecimal usedKm = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getUsedKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);
        BigDecimal usedKmSelfDrive = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getUsedKmSelfDrive)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        JourneyDiaryDailyDTO totalRowDTO = modelMapper.map(totalRow, JourneyDiaryDailyDTO.class);
        totalRowDTO.setOverKm(totalOverKm);
        totalRowDTO.setOverKmSelfDrive(totalOverKMSelfDrive);
        totalRowDTO.setOverTime(totalOverTime);
        totalRowDTO.setOvernight(totalOvernight);
        totalRowDTO.setDayOfWeek("Total");
        totalRowDTO.setJourneyDiaryDailyCostTypes(codeValueModels);
        //
        totalRowDTO.setTotalKm(totalKm);
        totalRowDTO.setUsedKm(usedKm);
        totalRowDTO.setUsedKmSelfDrive(usedKmSelfDrive);

        return totalRowDTO;
    }

    public JourneyDiaryDailyDTO calculateTotalRowJDDWithoutDriver(Timestamp from, Timestamp to,
                                                                  Contract contract,
                                                                  List<JourneyDiaryDaily> journeyDiaryDailies) {
        LogContractNormList logContractNormList = logContractNormListService
                .findClosestLogBeforeDate(dateUtils.getTomorrow(to), contract.getId(), CONTRACT_KM_NORM.code());

        BigDecimal totalUsedKM = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getUsedKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        BigDecimal kmNorm = BigDecimal.valueOf(paymentRequestService.calculateOverKM(logContractNormList, contract, from, to));

        BigDecimal overKmNormalDay = totalUsedKM.compareTo(kmNorm) > 0 ?
                totalUsedKM.subtract(kmNorm)
                        .setScale(0, RoundingMode.HALF_UP) : ZERO;
        return JourneyDiaryDailyDTO.builder()
                .dayOfWeek("Total")
                .usedKm(totalUsedKM)
                .overKm(overKmNormalDay)
                .build();
    }

    private HttpHeaders buildHttpHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName)
                .build());
        return headers;
    }


    @GetMapping("/customer_payment_request/{id}")
    public ResponseEntity<?> exportPaymentRequest(@PathVariable("id") Long id){
        CustomerPaymentRequest customerPaymentRequest = customerPaymentRequestService.findById(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            CustomerPaymentRequestWrapperDTO customerPaymentRequestWrapperDTO
                    = customerPaymentRequestService.convertToDTO(customerPaymentRequest);

            exportPaymentRequestImpl.buildHtml(customerPaymentRequestWrapperDTO);
            exportPaymentRequestImpl.export(outputStream, 2000, -1, "/templates/images/avis-logo.png");

        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .headers(buildHttpHeaders("payment_request_customer.pdf"))
                .body(outputStream.toByteArray());
    }

}
