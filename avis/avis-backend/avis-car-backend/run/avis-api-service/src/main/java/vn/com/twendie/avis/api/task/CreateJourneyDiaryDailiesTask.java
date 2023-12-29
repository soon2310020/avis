package vn.com.twendie.avis.api.task;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javatuples.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.StrUtils;
import vn.com.twendie.avis.api.repository.ContractRepo;
import vn.com.twendie.avis.api.repository.JourneyDiaryCostTypeRepo;
import vn.com.twendie.avis.api.repository.JourneyDiaryRepo;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.tracking.model.tracking.VehicleActivity;
import vn.com.twendie.avis.tracking.model.tracking.VehicleWorkingTime;
import vn.com.twendie.avis.tracking.service.TrackingGpsService;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.DAYS;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum.NIGHT_STORAGE_FEE;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;
import static vn.com.twendie.avis.data.enumtype.NormListEnum.CONTRACT_KM_NORM;

@Service
@Slf4j
public class CreateJourneyDiaryDailiesTask {

    private final ContractRepo contractRepo;
    private final JourneyDiaryRepo journeyDiaryRepo;
    private final JourneyDiaryCostTypeRepo journeyDiaryCostTypeRepo;

    private final ContractService contractService;
    private final ContractChangeHistoryService contractChangeHistoryService;
    private final LogContractNormListService logContractNormListService;
    private final JourneyDiaryDailyService journeyDiaryDailyService;
    private final JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService;
    private final WorkingDayService workingDayService;
    private final WorkingCalendarService workingCalendarService;
    private final TrackingGpsService trackingGpsService;

    private final StrUtils strUtils;
    private final DateUtils dateUtils;

    private final CreateJourneyDiaryDailiesTask createJourneyDiaryDailiesTask;

    public CreateJourneyDiaryDailiesTask(ContractRepo contractRepo,
                                         JourneyDiaryRepo journeyDiaryRepo,
                                         JourneyDiaryCostTypeRepo journeyDiaryCostTypeRepo,
                                         ContractService contractService,
                                         ContractChangeHistoryService contractChangeHistoryService,
                                         LogContractNormListService logContractNormListService,
                                         JourneyDiaryDailyService journeyDiaryDailyService,
                                         JourneyDiaryDailyCostTypeService journeyDiaryDailyCostTypeService,
                                         WorkingDayService workingDayService,
                                         WorkingCalendarService workingCalendarService,
                                         TrackingGpsService trackingGpsService,
                                         StrUtils strUtils,
                                         DateUtils dateUtils,
                                         @Lazy CreateJourneyDiaryDailiesTask createJourneyDiaryDailiesTask) {
        this.contractRepo = contractRepo;
        this.journeyDiaryRepo = journeyDiaryRepo;
        this.journeyDiaryCostTypeRepo = journeyDiaryCostTypeRepo;
        this.contractService = contractService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.logContractNormListService = logContractNormListService;
        this.journeyDiaryDailyService = journeyDiaryDailyService;
        this.journeyDiaryDailyCostTypeService = journeyDiaryDailyCostTypeService;
        this.workingDayService = workingDayService;
        this.workingCalendarService = workingCalendarService;
        this.trackingGpsService = trackingGpsService;
        this.strUtils = strUtils;
        this.dateUtils = dateUtils;
        this.createJourneyDiaryDailiesTask = createJourneyDiaryDailiesTask;
    }

//    @Scheduled(cron = "0 0 19 * * *")
    @Scheduled(fixedDelay = 8 * 60 * 60 * 1000, initialDelay = 0)
    public void createJourneyDiaryDailies() {
        log.info("Start task: createJourneyDiaryDailies");
        createJourneyDiaryDailiesTask.createJourneyDiaryDailiesWithDriver();
        createJourneyDiaryDailiesTask.createJourneyDiaryDailiesWithoutDriver();
    }
    @Scheduled(cron = "0 0 0 30 6/6 *")
    public void importWorkingCalendar() {
        log.info("Start task: importWorkingCalendar");
        workingCalendarService.importWorkingCalendar(Calendar.getInstance().get(Calendar.YEAR)+1);

    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createJourneyDiaryDailiesWithDriver() {
        Timestamp today = dateUtils.startOfToday();
        List<Object[]> results = contractRepo.findContractsWithDriverMissingJourneyDiaryDaily(dateUtils.subtract(today, 1, DAYS));
        for (Object[] result : results) {
            Long contractId = ((Integer) result[0]).longValue();
            Timestamp lastJddDate = (Timestamp) result[1];
            Contract contract = contractService.findById(contractId);
            Timestamp startDate = Objects.nonNull(lastJddDate) ?
                    dateUtils.add(lastJddDate, 1, DAYS) : contract.getFromDatetime();
            List<JourneyDiary> journeyDiaries = journeyDiaryRepo
                    .findByContractIdAndTimeStartAfterAndDeletedFalseOrderById(contractId, startDate);
            Timestamp endDate = dateUtils.min(dateUtils.subtract(today, 1, DAYS),
                    contract.getToDatetime(), contract.getDateEarlyTermination());
            if (!journeyDiaries.isEmpty()) {
                JourneyDiary lastJourneyDiary = journeyDiaries.get(journeyDiaries.size() - 1);
                if (Objects.isNull(lastJourneyDiary.getTimeEnd()) || lastJourneyDiary.getTimeEnd().after(today)) {
                    JourneyDiary firstIgnoredDiary = journeyDiaries.stream()
                            .filter(journeyDiary -> Objects.isNull(journeyDiary.getTimeEnd()) ||
                                    journeyDiary.getTimeEnd().after(dateUtils.startOfDay(lastJourneyDiary.getTimeStart())))
                            .findFirst()
                            .orElse(lastJourneyDiary);

                    //skip error journey do not completed before
                    List<JourneyDiary> calcDiaryList = journeyDiaries.stream()
                            .filter(journeyDiary -> Objects.nonNull(journeyDiary.getTimeEnd()) &&
                                    journeyDiary.getTimeEnd().before(dateUtils.startOfDay(lastJourneyDiary.getTimeStart())))
                            .collect(Collectors.toList());
                    JourneyDiary endCal=!calcDiaryList.isEmpty()?calcDiaryList.get(calcDiaryList.size()-1):null;
                    if(endCal!=null && (journeyDiaries.indexOf(endCal)+1<journeyDiaries.size())){
                        firstIgnoredDiary = journeyDiaries.get(journeyDiaries.indexOf(endCal)+1);
                    }

                    endDate = dateUtils.subtract(dateUtils.startOfDay(firstIgnoredDiary.getTimeStart()), 1, DAYS);
                    journeyDiaries = journeyDiaries.stream()
                            .filter(journeyDiary -> Objects.nonNull(journeyDiary.getTimeEnd()) &&
                                    journeyDiary.getTimeEnd().before(dateUtils.startOfDay(lastJourneyDiary.getTimeStart())))
                            .collect(Collectors.toList());
                }
            }
            if (!endDate.before(startDate)) {
                createJourneyDiaryDailiesWithDriver(contract, journeyDiaries, startDate, endDate);
            }
        }
    }

    public List<JourneyDiaryDaily> createJourneyDiaryDailiesWithDriver(Contract contract,
                                                                       Collection<JourneyDiary> journeyDiaries,
                                                                       Timestamp startDate,
                                                                       Timestamp endDate) {
        List<JourneyDiaryDaily> journeyDiaryDailies = createJourneyDiaryDailiesWithDriver(contract, journeyDiaries);
        updateOverTime(contract, journeyDiaryDailies);
        Set<Timestamp> dates = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getDate)
                .collect(Collectors.toSet());
        journeyDiaryDailies.addAll(dateUtils.getDatesBetween(startDate, endDate)
                .stream()
                .filter(date -> !dates.contains(date))
                .map(date -> createEmptyJourneyDiaryWithDriver(contract, date))
                .collect(Collectors.toList()));
        List<JourneyDiaryDaily> parents = createParentWithDriver(journeyDiaryDailies)
                .stream()
                .filter(journeyDiaryDaily -> dateUtils.isBetween(journeyDiaryDaily.getDate(), startDate, endDate))
                .collect(Collectors.toList());
        journeyDiaryDailies = journeyDiaryDailies.stream()
                .filter(journeyDiaryDaily -> dateUtils.isBetween(journeyDiaryDaily.getDate(), startDate, endDate))
                .collect(Collectors.toList());
        if (!parents.isEmpty()) {
            journeyDiaryDailyService.saveAll(parents);
            List<JourneyDiaryDailyCostType> parentCosts = journeyDiaryDailyCostTypeService.saveAll(journeyDiaryDailyCostTypeService
                    .createJourneyDiaryDailyCostTypes(parents));
            parents.forEach(parent -> parent.setJourneyDiaryDailyCostTypes(parentCosts.stream()
                    .filter(cost -> cost.getJourneyDiaryDaily().equals(parent))
                    .collect(Collectors.toList())));
        }
        if (!journeyDiaryDailies.isEmpty()) {
            journeyDiaryDailies = journeyDiaryDailyService.saveAll(journeyDiaryDailies);
            List<JourneyDiaryDailyCostType> costs = journeyDiaryDailyCostTypeService.saveAll(journeyDiaryDailyCostTypeService
                    .createJourneyDiaryDailyCostTypes(journeyDiaryDailies));
            journeyDiaryDailies.forEach(journeyDiaryDaily -> {
                if (Objects.nonNull(journeyDiaryDaily.getUsedKm())) {
                    journeyDiaryDailyService.updateOverKm(journeyDiaryDaily, contract, false);
                }
            });
            journeyDiaryDailies.forEach(journeyDiaryDaily -> journeyDiaryDaily.setJourneyDiaryDailyCostTypes(costs.stream()
                    .filter(cost -> cost.getJourneyDiaryDaily().equals(journeyDiaryDaily))
                    .collect(Collectors.toList())));
        }
        List<JourneyDiaryDaily> children = journeyDiaryDailies;
        parents.forEach(parent -> parent.setChildren(children.stream()
                .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getParent()))
                .filter(journeyDiaryDaily -> journeyDiaryDaily.getParent().equals(parent))
                .collect(Collectors.toList())));
        parents.forEach(journeyDiaryDaily -> {
            journeyDiaryDailyService.updateCosts(journeyDiaryDaily);
            if (Objects.nonNull(journeyDiaryDaily.getUsedKm())) {
                journeyDiaryDailyService.updateOverKm(journeyDiaryDaily, contract, false);
            }
        });
        return parents;
    }

    public List<JourneyDiaryDaily> createJourneyDiaryDailiesWithDriver(Contract contract, Collection<JourneyDiary> journeyDiaries) {
        return journeyDiaries.stream()
                .map(journeyDiary -> createJourneyDiaryDailiesWithDriver(contract, journeyDiary))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<JourneyDiaryDaily> createJourneyDiaryDailiesWithDriver(Contract contract, JourneyDiary journeyDiary) {
        if (ObjectUtils.allNotNull(journeyDiary.getTimeStart(), journeyDiary.getTimeEnd())) {
            return dateUtils.getDatesBetween(journeyDiary.getTimeStart(), journeyDiary.getTimeEnd())
                    .stream()
                    .map(date -> createJourneyDiaryDailyWithDriver(contract, journeyDiary, date))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public JourneyDiaryDaily createJourneyDiaryDailyWithDriver(Contract contract, JourneyDiary journeyDiary, Timestamp date) {

        WorkingDay workingDay = workingDayService.getContractWorkingDayAtTime(contract, dateUtils.getLastDayOfMonth(date));

        Time from = date.equals(dateUtils.startOfDay(journeyDiary.getTimeStart())) ? dateUtils.getTime(journeyDiary.getTimeStart()) : null;
        Time to = date.equals(dateUtils.startOfDay(journeyDiary.getTimeEnd())) ? dateUtils.getTime(journeyDiary.getTimeEnd()) : null;

        List<VehicleActivity> vehicleActivities;
        try {
            Timestamp min = date.equals(dateUtils.startOfDay(journeyDiary.getTimeStart())) ? journeyDiary.getTimeStart() : dateUtils.startOfDay(date);
            Timestamp max = date.equals(dateUtils.startOfDay(journeyDiary.getTimeEnd())) ? journeyDiary.getTimeEnd() : dateUtils.endOfDay(date);
            vehicleActivities = trackingGpsService.getVehicleActivities(journeyDiary.getVehicle().getNumberPlate(), min, max);
        } catch (Exception e) {
            log.error("Error when get vehicles activities: {}", ExceptionUtils.getRootCauseMessage(e));
            vehicleActivities = new ArrayList<>();
        }

        VehicleWorkingTime vehicleWorkingTime = trackingGpsService.getVehicleWorkingTime(vehicleActivities);
        String tripItinerary = StringUtils.left(createTripItinerary(trackingGpsService.getVehicleTripItinerary(vehicleActivities)), 1000);

        User driver = null;
        Vehicle vehicle = null;

        String customerNameUsed = null, customerDepartment = null;

        BigDecimal kmStart = null, kmCustomerGetIn = null, kmCustomerGetOut = null, kmEnd = null;
        BigDecimal totalKm = null, emptyKm = null, usedKm = null;

        int overnight = 0;

        String imageOdoLinks = null, imageCustomerGetInLink = null, imageCustomerGetOutLink = null, stationFeeImages = null, confirmationScreenshot = null;

        if (Objects.nonNull(from)) {
            driver = journeyDiary.getDriver();
            vehicle = journeyDiary.getVehicle();

            customerNameUsed = journeyDiary.getCustomerNameUsed();
            customerDepartment = journeyDiary.getCustomerDepartment();

            kmStart = journeyDiary.getKmStart();
            kmCustomerGetIn = journeyDiary.getKmCustomerGetIn();
            kmCustomerGetOut = journeyDiary.getKmCustomerGetOut();
            kmEnd = journeyDiary.getKmEnd();

            List<JourneyDiaryCostType> journeyDiaryDailyCostTypes = journeyDiaryCostTypeRepo
                    .findByJourneyDiaryIdAndDeletedFalse(journeyDiary.getId());
            if (journeyDiaryDailyCostTypes.stream()
                    .map(JourneyDiaryCostType::getCostType)
                    .map(CostType::getCode)
                    .anyMatch(code -> NIGHT_STORAGE_FEE.code().equals(code))) {
                overnight = 1;
            }

            imageOdoLinks = StringUtils.trimToNull(journeyDiary.getRecognitionFailedOdoLinks()
                    .stream()
                    .map(strUtils::getFileName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
            imageCustomerGetInLink = strUtils.getFileName(journeyDiary.getImageCustomerGetInLink());
            imageCustomerGetOutLink = strUtils.getFileName(journeyDiary.getImageCustomerGetOutLink());

            stationFeeImages = StringUtils.trimToNull(String.join("\n", journeyDiary.getStationFeeImages()));
            confirmationScreenshot = strUtils.getFileName(journeyDiary.getScreenshot());
        }

        if (ObjectUtils.allNotNull(kmStart, kmEnd)) {
            totalKm = kmEnd.subtract(kmStart);
            BigDecimal firstEmptyKm = Objects.nonNull(kmCustomerGetIn) ? kmCustomerGetIn.subtract(kmStart) : ZERO;
            BigDecimal lastEmptyKm = Objects.nonNull(kmCustomerGetOut) ? kmEnd.subtract(kmCustomerGetOut) : ZERO;
            emptyKm = firstEmptyKm.add(lastEmptyKm);
            usedKm = contract.getIncludeEmptyKm() ? totalKm : totalKm.subtract(emptyKm);
        }

        boolean flagMultiDate = Objects.isNull(from) || Objects.isNull(to);

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryService.getContractChangeHistories(contract)
                .stream()
                .filter(history -> history.getFromDate().equals(date))
                .collect(Collectors.toList());

        boolean flagChangedVehicle = contractChangeHistories.stream()
                .anyMatch(history -> Objects.nonNull(history.getOldValue()) &&
                        history.getMappingFieldCodeFontend().getFieldName().equals(VEHICLE_ID.getName()));
        boolean flagChangedWorkingDay = contractChangeHistories.stream()
                .anyMatch(history -> history.getMappingFieldCodeFontend().getFieldName().equals(WORKING_DAY_ID.getName()) ||
                        history.getMappingFieldCodeFontend().getFieldName().equals(WORKING_DAY.getName()));
        boolean flagChangedKmNorm = logContractNormListService.getLogContractNormLists(contract, CONTRACT_KM_NORM.code())
                .stream()
                .sorted(Comparator.comparingLong(LogContractNormList::getId))
                .skip(1)
                .anyMatch(log -> log.getFromDate().equals(date));

        boolean flagUnavailableVehicle = Objects.nonNull(from) &&
                Objects.nonNull(journeyDiary.getFlagUnavailableVehicle()) &&
                journeyDiary.getFlagUnavailableVehicle();

        return JourneyDiaryDaily.builder()
                .journeyDiary(journeyDiary)
                .contract(contract)
                .driver(driver)
                .vehicle(vehicle)
                .date(date)
                .customerNameUsed(customerNameUsed)
                .customerDepartment(customerDepartment)
                .tripItinerary(tripItinerary)
                .kmStart(kmStart)
                .kmCustomerGetIn(kmCustomerGetIn)
                .kmCustomerGetOut(kmCustomerGetOut)
                .kmEnd(kmEnd)
                .totalKm(totalKm)
                .emptyKm(emptyKm)
                .usedKm(usedKm)
                .workingTimeAppFrom(from)
                .workingTimeAppTo(to)
                .workingTimeGpsFrom(Objects.nonNull(from) ? vehicleWorkingTime.getFrom() : null)
                .workingTimeGpsTo(Objects.nonNull(to) ? vehicleWorkingTime.getTo() : null)
                .overnight(overnight)
                .isHoliday(workingCalendarService.isHoliday(date, workingDay.getId()))
                .isWeekend(workingCalendarService.isWeekend(date, workingDay.getId()))
                .imageOdoLinks(imageOdoLinks)
                .imageCustomerGetInLink(imageCustomerGetInLink)
                .imageCustomerGetOutLink(imageCustomerGetOutLink)
                .stationFeeImages(stationFeeImages)
                .confirmationScreenshot(confirmationScreenshot)
                .flagOdoRecognitionFailed(StringUtils.isNotBlank(imageOdoLinks))
                .flagMultiDate(flagMultiDate)
                .flagUnavailableVehicle(flagUnavailableVehicle)
                .flagChangedVehicle(flagChangedVehicle)
                .flagChangedKmNorm(flagChangedKmNorm)
                .flagChangedWorkingDay(flagChangedWorkingDay)
                .note(Objects.nonNull(from) ? journeyDiary.getNote() : null)
                .build();
    }

    public JourneyDiaryDaily createEmptyJourneyDiaryWithDriver(Contract contract, Timestamp date) {
        WorkingDay workingDay = workingDayService.getContractWorkingDayAtTime(contract, dateUtils.getLastDayOfMonth(date));

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryService.getContractChangeHistories(contract)
                .stream()
                .filter(history -> history.getFromDate().equals(date))
                .collect(Collectors.toList());

        boolean flagChangedVehicle = contractChangeHistories.stream()
                .anyMatch(history -> Objects.nonNull(history.getOldValue()) &&
                        history.getMappingFieldCodeFontend().getFieldName().equals(VEHICLE_ID.getName()));
        boolean flagChangedWorkingDay = contractChangeHistories.stream()
                .anyMatch(history -> history.getMappingFieldCodeFontend().getFieldName().equals(WORKING_DAY_ID.getName()) ||
                        history.getMappingFieldCodeFontend().getFieldName().equals(WORKING_DAY.getName()));
        boolean flagChangedKmNorm = logContractNormListService.getLogContractNormLists(contract, CONTRACT_KM_NORM.code())
                .stream()
                .sorted(Comparator.comparingLong(LogContractNormList::getId))
                .skip(1)
                .anyMatch(log -> log.getFromDate().equals(date));

        return JourneyDiaryDaily.builder()
                .contract(contract)
                .date(date)
                .isHoliday(workingCalendarService.isHoliday(date, workingDay.getId()))
                .isWeekend(workingCalendarService.isWeekend(date, workingDay.getId()))
                .flagChangedVehicle(flagChangedVehicle)
                .flagChangedKmNorm(flagChangedKmNorm)
                .flagChangedWorkingDay(flagChangedWorkingDay)
                .build();
    }

    public List<JourneyDiaryDaily> createParentWithDriver(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        return journeyDiaryDailies.stream()
                .collect(Collectors.groupingBy(JourneyDiaryDaily::getDate))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    Timestamp date = entry.getKey();
                    List<JourneyDiaryDaily> children = entry.getValue()
                            .stream()
                            .filter(child -> !(child.getFlagMultiDate() && Objects.isNull(child.getWorkingTimeAppFrom())))
                            .collect(Collectors.toList());
                    JourneyDiaryDaily firstDiary = children.get(0);
                    JourneyDiaryDaily lastDiary = children.get(children.size() - 1);

                    Contract contract = firstDiary.getContract();

                    BigDecimal emptyKm = children.stream()
                            .map(JourneyDiaryDaily::getEmptyKm)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal::add)
                            .orElse(null);
                    BigDecimal totalKm = children.stream()
                            .map(JourneyDiaryDaily::getTotalKm)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal::add)
                            .orElse(null);
                    BigDecimal usedKm = children.stream()
                            .map(JourneyDiaryDaily::getUsedKm)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal::add)
                            .orElse(null);

                    Integer overnight = children.stream()
                            .map(JourneyDiaryDaily::getOvernight)
                            .filter(Objects::nonNull)
                            .reduce(Integer::sum)
                            .orElse(0);

                    Boolean flagOdoRecognitionFailed = children.stream()
                            .map(JourneyDiaryDaily::getFlagOdoRecognitionFailed)
                            .filter(Objects::nonNull)
                            .anyMatch(b -> b);

                    Boolean flagMultiDate = children.stream()
                            .map(JourneyDiaryDaily::getFlagMultiDate)
                            .filter(Objects::nonNull)
                            .anyMatch(b -> b);

                    JourneyDiaryDaily parent = JourneyDiaryDaily.builder()
                            .contract(contract)
                            .date(date)
                            .kmStart(firstDiary.getKmStart())
                            .kmCustomerGetIn(firstDiary.getKmCustomerGetIn())
                            .kmCustomerGetOut(lastDiary.getKmCustomerGetOut())
                            .kmEnd(lastDiary.getKmEnd())
                            .emptyKm(emptyKm)
                            .totalKm(totalKm)
                            .usedKm(usedKm)
                            .workingTimeAppFrom(firstDiary.getWorkingTimeAppFrom())
                            .workingTimeAppTo(lastDiary.getWorkingTimeAppTo())
                            .workingTimeGpsFrom(firstDiary.getWorkingTimeGpsFrom())
                            .workingTimeGpsTo(lastDiary.getWorkingTimeGpsTo())
                            .overnight(overnight)
                            .isHoliday(firstDiary.getIsHoliday())
                            .isWeekend(firstDiary.getIsWeekend())
                            .flagOdoRecognitionFailed(flagOdoRecognitionFailed)
                            .flagMultiDate(flagMultiDate)
                            .flagChangedVehicle(firstDiary.getFlagChangedVehicle())
                            .flagChangedKmNorm(firstDiary.getFlagChangedKmNorm())
                            .flagChangedWorkingDay(firstDiary.getFlagChangedWorkingDay())
                            .build();

                    boolean isWeekendHoliday = parent.getIsWeekend() || parent.getIsHoliday() || parent.getIsOverDay();
                    Pair<Time, Time> contractWorkingTime = contractService.getWorkingTime(contract, date, isWeekendHoliday);

                    Long overTime = dateUtils.getOverTime(parent.getWorkingTimeGpsFrom(), parent.getWorkingTimeGpsTo(),
                            contractWorkingTime.getValue0(), contractWorkingTime.getValue1());

                    if (lastDiary.getFlagMultiDate()) {
                        JourneyDiaryDaily lastJourneyDairyDailyMultiDate = findLastJourneyDairyDailyMultiDate(journeyDiaryDailies, lastDiary);
                        if (Objects.nonNull(lastJourneyDairyDailyMultiDate)) {
                            overTime = dateUtils.getOverTime(firstDiary.getDate(), lastJourneyDairyDailyMultiDate.getDate(),
                                    firstDiary.getWorkingTimeGpsFrom(), lastJourneyDairyDailyMultiDate.getWorkingTimeGpsTo(),
                                    contractWorkingTime.getValue0(), contractWorkingTime.getValue1());
                        }
                    }

                    parent.setOverTime(overTime);

                    entry.getValue().forEach(child -> child.setParent(parent));
                    return parent;
                })
                .collect(Collectors.toList());
    }

    private void updateOverTime(Contract contract, List<JourneyDiaryDaily> journeyDiaryDailies) {
        journeyDiaryDailies.stream()
                .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getWorkingTimeGpsFrom()))
                .forEach(journeyDiaryDaily -> {
                    Long overTime = null;

                    Timestamp date = dateUtils.endOfDay(journeyDiaryDaily.getDate());

                    boolean isWeekendHoliday = journeyDiaryDaily.getIsWeekend() || journeyDiaryDaily.getIsHoliday() || journeyDiaryDaily.getIsOverDay();
                    Pair<Time, Time> contractWorkingTime = contractService.getWorkingTime(contract, date, isWeekendHoliday);

                    if (journeyDiaryDaily.getFlagMultiDate()) {
                        JourneyDiaryDaily lastJourneyDiaryDaily = findLastJourneyDairyDailyMultiDate(journeyDiaryDailies, journeyDiaryDaily);
                        if (Objects.nonNull(lastJourneyDiaryDaily)) {
                            overTime = dateUtils.getOverTime(journeyDiaryDaily.getDate(), lastJourneyDiaryDaily.getDate(),
                                    journeyDiaryDaily.getWorkingTimeGpsFrom(), lastJourneyDiaryDaily.getWorkingTimeGpsTo(),
                                    contractWorkingTime.getValue0(), contractWorkingTime.getValue1());
                        }
                    } else {
                        overTime = dateUtils.getOverTime(journeyDiaryDaily.getWorkingTimeGpsFrom(), journeyDiaryDaily.getWorkingTimeGpsTo(),
                                contractWorkingTime.getValue0(), contractWorkingTime.getValue1());
                    }
                    journeyDiaryDaily.setOverTime(overTime);
                });
    }

    public JourneyDiaryDaily findLastJourneyDairyDailyMultiDate(Collection<JourneyDiaryDaily> journeyDiaryDailies,
                                                                JourneyDiaryDaily journeyDiaryDaily) {
        return journeyDiaryDailies.stream()
                .filter(jdd -> journeyDiaryDaily.getJourneyDiary().equals(jdd.getJourneyDiary()))
                .filter(jdd -> Objects.nonNull(jdd.getWorkingTimeGpsTo()))
                .findFirst()
                .orElse(null);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createJourneyDiaryDailiesWithoutDriver() {
        Timestamp today = dateUtils.startOfToday();
        List<Object[]> results = contractRepo.findContractsWithoutDriverMissingJourneyDiaryDaily(dateUtils.subtract(today, 1, DAYS));
        for (Object[] result : results) {
            Long contractId = ((Integer) result[0]).longValue();
            Timestamp lastJddDate = (Timestamp) result[1];
            Integer maxJourneyDiaryId = (Integer) result[2];
            Contract contract = contractService.findById(contractId);
            Timestamp startDate = Objects.nonNull(lastJddDate) ?
                    dateUtils.add(lastJddDate, 1, DAYS) : contract.getFromDatetime();
            List<JourneyDiary> journeyDiaries;
            if (Objects.isNull(maxJourneyDiaryId)) {
                journeyDiaries = journeyDiaryRepo
                        .findByContractIdAndDeletedFalseOrderById(contractId);
            } else {
                journeyDiaries = journeyDiaryRepo
                        .findByContractIdAndIdGreaterThanAndDeletedFalseOrderById(contractId, maxJourneyDiaryId.longValue());
            }
            Timestamp endDate = dateUtils.min(dateUtils.subtract(today, 1, DAYS),
                    contract.getToDatetime(), contract.getDateEarlyTermination());
            if (!journeyDiaries.isEmpty()) {
                JourneyDiary lastJourneyDiary = journeyDiaries.get(journeyDiaries.size() - 1);
                if (Objects.isNull(lastJourneyDiary.getTimeEnd()) || lastJourneyDiary.getTimeEnd().after(today)) {
                    endDate = dateUtils.subtract(dateUtils.startOfDay(lastJourneyDiary.getTimeStart()), 1, DAYS);
                    journeyDiaries.remove(lastJourneyDiary);
                }
            }
            if (!endDate.before(startDate) || !journeyDiaries.isEmpty()) {
                createJourneyDiaryDailiesWithoutDriver(contract, journeyDiaries, startDate, endDate);
            }
        }
    }

    List<JourneyDiaryDaily> createJourneyDiaryDailiesWithoutDriver(Contract contract,
                                                                   Collection<JourneyDiary> journeyDiaries,
                                                                   Timestamp startDate,
                                                                   Timestamp endDate) {
        List<JourneyDiaryDaily> journeyDiaryDailies = createJourneyDiaryDailiesWithoutDriver(contract, journeyDiaries);
        Set<Timestamp> dates = journeyDiaryDailies.stream()
                .map(JourneyDiaryDaily::getDate)
                .collect(Collectors.toSet());
        journeyDiaryDailies.addAll(dateUtils.getDatesBetween(startDate, endDate)
                .stream()
                .filter(date -> !dates.contains(date))
                .map(date -> createEmptyJourneyDiaryWithoutDriver(contract, date))
                .collect(Collectors.toList()));
        List<JourneyDiaryDaily> parents = createParentWithoutDriver(journeyDiaryDailies);
        if (!parents.isEmpty()) {
            journeyDiaryDailyService.saveAll(parents);
        }
        if (!journeyDiaryDailies.isEmpty()) {
            journeyDiaryDailyService.saveAll(journeyDiaryDailies);
        }
        return parents;
    }

    public List<JourneyDiaryDaily> createJourneyDiaryDailiesWithoutDriver(Contract contract, Collection<JourneyDiary> journeyDiaries) {
        return journeyDiaries.stream()
                .map(journeyDiary -> createJourneyDiaryDailiesWithoutDriver(contract, journeyDiary))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<JourneyDiaryDaily> createJourneyDiaryDailiesWithoutDriver(Contract contract, JourneyDiary journeyDiary) {
        if (ObjectUtils.allNotNull(journeyDiary.getTimeStart(), journeyDiary.getTimeEnd())) {
            return dateUtils.getDatesBetween(journeyDiary.getTimeStart(), journeyDiary.getTimeEnd())
                    .stream()
                    .map(date -> createJourneyDiaryDailyWithoutDriver(contract, journeyDiary, date))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public JourneyDiaryDaily createJourneyDiaryDailyWithoutDriver(Contract contract, JourneyDiary journeyDiary, Timestamp date) {

        List<VehicleActivity> vehicleActivities;
        try {
            vehicleActivities = trackingGpsService.getVehicleActivities(journeyDiary.getVehicle().getNumberPlate(), date);
        } catch (Exception e) {
            log.error("Error when get vehicles activities: {}", ExceptionUtils.getRootCauseMessage(e));
            vehicleActivities = new ArrayList<>();
        }

        String tripItinerary = StringUtils.left(createTripItinerary(trackingGpsService.getVehicleTripItinerary(vehicleActivities)), 1000);

        User driver = null;
        Vehicle vehicle = null;

        BigDecimal kmStart = null, kmEnd = null;
        BigDecimal totalKm = null;

        String imageOdoLinks = null;

        if (date.equals(dateUtils.startOfDay(journeyDiary.getTimeStart()))) {
            driver = journeyDiary.getDriver();
            vehicle = journeyDiary.getVehicle();


            kmStart = journeyDiary.getKmStart();
            totalKm = journeyDiary.getKmEnd().subtract(kmStart);

            imageOdoLinks = StringUtils.trimToNull(journeyDiary.getRecognitionFailedOdoLinks()
                    .stream()
                    .map(strUtils::getFileName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        if (date.equals(dateUtils.startOfDay(journeyDiary.getTimeEnd()))) {
            kmEnd = journeyDiary.getKmEnd();
        }

        return JourneyDiaryDaily.builder()
                .journeyDiary(journeyDiary)
                .contract(contract)
                .driver(driver)
                .vehicle(vehicle)
                .date(date)
                .month(findJourneyDailyMonth(journeyDiary.getTimeStart(), journeyDiary.getTimeEnd()))
                .tripItinerary(tripItinerary)
                .kmStart(kmStart)
                .kmEnd(kmEnd)
                .totalKm(totalKm)
                .usedKm(totalKm)
                .imageOdoLinks(imageOdoLinks)
                .flagOdoRecognitionFailed(StringUtils.isNotBlank(imageOdoLinks))
                .build();
    }

    public JourneyDiaryDaily createEmptyJourneyDiaryWithoutDriver(Contract contract, Timestamp date) {
        return JourneyDiaryDaily.builder()
                .contract(contract)
                .date(date)
                .month(dateUtils.getFirstDayOfMonth(date))
                .build();
    }

    public List<JourneyDiaryDaily> createParentWithoutDriver(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        return journeyDiaryDailies.stream()
                .filter(journeyDiaryDaily -> ObjectUtils.allNotNull(journeyDiaryDaily.getKmStart(), journeyDiaryDaily.getKmEnd()))
                .collect(Collectors.groupingBy(JourneyDiaryDaily::getDate))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    Timestamp date = entry.getKey();
                    List<JourneyDiaryDaily> children = entry.getValue();
                    JourneyDiaryDaily firstDiary = children.get(0);
                    JourneyDiaryDaily lastDiary = children.get(children.size() - 1);

                    BigDecimal totalKm = children.stream()
                            .map(JourneyDiaryDaily::getTotalKm)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal::add)
                            .orElse(null);

                    JourneyDiaryDaily parent = JourneyDiaryDaily.builder()
                            .contract(firstDiary.getContract())
                            .date(date)
                            .month(dateUtils.getFirstDayOfMonth(date))
                            .kmStart(firstDiary.getKmStart())
                            .kmEnd(lastDiary.getKmEnd())
                            .totalKm(totalKm)
                            .usedKm(totalKm)
                            .build();
                    children.forEach(child -> child.setParent(parent));
                    return parent;
                })
                .collect(Collectors.toList());
    }

    private Timestamp findJourneyDailyMonth(Timestamp startTime, Timestamp endTime) {
        Timestamp startMonth = dateUtils.getFirstDayOfMonth(startTime);
        Timestamp nextMonth = dateUtils.add(dateUtils.getLastDayOfMonth(startTime), 1, DAYS);
        if (nextMonth.getTime() - startTime.getTime() >= endTime.getTime() - nextMonth.getTime()) {
            return startMonth;
        } else {
            return nextMonth;
        }
    }

    private String createTripItinerary(List<Pair<String, String>> list) {
        List<Pair<List<String>, String>> groups = new ArrayList<>();
        list.forEach(item -> {
            if (groups.isEmpty() || !groups.get(groups.size() - 1).getValue1().equals(item.getValue1())) {
                groups.add(Pair.with(Lists.newArrayList(item.getValue0()), item.getValue1()));
            } else {
                groups.get(groups.size() - 1).getValue0().add(item.getValue0());
            }
        });
        return groups.stream()
                .map(group -> String.format("%s - %s",
                        group.getValue0()
                                .stream()
                                .distinct()
                                .collect(Collectors.joining(", ")),
                        group.getValue1()))
                .collect(Collectors.joining("; "));
    }

}
