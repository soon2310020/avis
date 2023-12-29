package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.javatuples.Triplet;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ObjUtils;
import vn.com.twendie.avis.api.model.projection.PRLogContractCostProjection;
import vn.com.twendie.avis.api.model.response.PaymentRequestItemDTO;
import vn.com.twendie.avis.api.model.response.WeekendCounter;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.enumtype.ContractTimeUserPolicyEnum;
import vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum;
import vn.com.twendie.avis.data.enumtype.PaymentRequestEnum;
import vn.com.twendie.avis.data.enumtype.WorkingDayEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.data.model.LogContractNormList;
import vn.com.twendie.avis.data.model.LogContractPriceCostType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractCostTypeEnum.*;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum.PARKING_FEE;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum.TOLLS_FEE;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;
import static vn.com.twendie.avis.data.enumtype.NormListEnum.CONTRACT_KM_NORM;
import static vn.com.twendie.avis.data.enumtype.PaymentRequestEnum.*;

@Service
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private final DateUtils dateUtils;
    private final ObjUtils objUtils;

    private final LogContractCostTypeService logContractCostTypeService;
    private final LogContractNormListService logContractNormListService;
    private final ContractChangeHistoryService contractChangeHistoryService;
    private final ContractService contractService;
    private final NormListService normListService;

    public PaymentRequestServiceImpl(DateUtils dateUtils,
                                     ObjUtils objUtils,
                                     LogContractCostTypeService logContractCostTypeService,
                                     LogContractNormListService logContractNormListService,
                                     ContractChangeHistoryService contractChangeHistoryService,
                                     ContractService contractService,
                                     NormListService normListService) {
        this.dateUtils = dateUtils;
        this.objUtils = objUtils;
        this.logContractCostTypeService = logContractCostTypeService;
        this.logContractNormListService = logContractNormListService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.contractService = contractService;
        this.normListService = normListService;
    }

    @Override
    public PaymentRequestItemDTO getRentalPrice(Timestamp to,
                                                Contract contract,
                                                double diff) {
//        int lendingWeeks = Math.toIntExact(lendDays / 7);
//        int lendingWeekSurplus = Math.toIntExact(lendDays % 7);

//        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(from);
//        int monthWeeks = Math.toIntExact(monthDays / 7);
//        int monthWeekSurplus = Math.toIntExact(monthDays % 7);

//        WeekendCounter lendingWeekend = countWeekendDays(dateUtils.getDayOfWeek(from), lendingWeeks, lendingWeekSurplus);
//        WeekendCounter monthWeekend = countWeekendDays(dateUtils.getDayOfWeek(firstDayOfMonth), monthWeeks, monthWeekSurplus);
        LogContractPriceCostType logContractPriceCostType = logContractCostTypeService
                .findClosestLogBeforeDate(dateUtils.getTomorrow(dateUtils.getLastDayOfMonth(to)),
                        contract.getId(), CAR_RENTAL_COST.code());

//        ContractChangeHistory lastWorkingDayChange = contractChangeHistoryService.findLastChangeOfFieldInMonth(
//                contract.getId(), MappingFieldCodeFrontendEnum.WORKING_DAY.getId(), dateUtils.getTomorrow(to));
//        Integer workingDays = Objects.isNull(lastWorkingDayChange) ?
//                Objects.isNull(contract.getWorkingDayValue()) ? 0 : contract.getWorkingDayValue()
//                : Integer.parseInt(lastWorkingDayChange.getNewValue());
//        WorkingDayEnum workingDayEnum = getNewestWorkingDayEnum(contract, dateUtils.getTomorrow(to));

//        int lendingDayOffs = calculateDayOffs(workingDayEnum, lendingWeekend, dailyJourneyDiaries);
        return calculateRentalPrice(diff, logContractPriceCostType);
    }

    private long getLendDays(Timestamp from, Timestamp to, Contract contract) {
        Timestamp contractEndTime = Objects.isNull(contract.getDateEarlyTermination()) ?
                contract.getToDatetime() : dateUtils.min(contract.getDateEarlyTermination(), contract.getToDatetime());
        Timestamp lastDayOfMonth = dateUtils.getLastDayOfMonth(to);
        return dateUtils.getDaysBetween(from, dateUtils.min(lastDayOfMonth, contractEndTime)) + 1;
    }

    private WorkingDayEnum getNewestWorkingDayEnum(Contract contract, Timestamp to) {
        String workingDayHistoryValue = contractChangeHistoryService.findLastChangeOfField(
                contract.getId(), MappingFieldCodeFrontendEnum.WORKING_DAY_ID.getId(), dateUtils.getTomorrow(to));

        Long workingDayId = Objects.isNull(workingDayHistoryValue) ? contract.getWorkingDay().getId()
                : Long.parseLong(workingDayHistoryValue);

        WorkingDayEnum workingDayEnum = WorkingDayEnum.valueOf(workingDayId);

        if (Objects.isNull(workingDayEnum)) {
            throw new NotFoundException("Cannot find working day with id: " + workingDayId);
        }

        return workingDayEnum;
    }

    @Override
    public List<PaymentRequestItemDTO> getDriverFee(Contract contract, Timestamp from, Timestamp to,
                                                    List<JourneyDiaryDaily> diaries,
                                                    Map<String, List<PRLogContractCostProjection>> map,
                                                    Timestamp firstDayOD,
                                                    Double firstDayODPriceDiff) {
        List<PaymentRequestItemDTO> overTimeFee = getOverTimeFee(contract, from, to, diaries, map);
        List<PaymentRequestItemDTO> KMFee = getKMFee(contract, from, to, diaries, map, firstDayOD, firstDayODPriceDiff);
        overTimeFee.addAll(KMFee);
        return overTimeFee;
    }

    @Override
    public List<PaymentRequestItemDTO> getOvernightFee(List<JourneyDiaryDaily> diaries,
                                                       Timestamp from, Timestamp to,
                                                       Contract contract,
                                                       Map<String, List<PRLogContractCostProjection>> map) {
        List<JourneyDiaryDaily> temp = diaries.stream()
                .filter(x -> x.getOvernight() != null && x.getOvernight() > 0)
                .collect(Collectors.toList());
        List<PRLogContractCostProjection> logs = map.get(OVERNIGHT_SURCHARGE.code());
        List<PaymentRequestItemDTO> fees = new ArrayList<>();
        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(from);
        if (logs.size() > 1) {
            for (int i = 0; i < logs.size(); i++) {
                if (i == 0) {
                    calculateOverNightFee(fees, temp, logs.get(i), dateUtils.getTomorrow(to));
                } else {
                    calculateOverNightFee(fees, temp, logs.get(i), logs.get(i - 1).getFromDate());
                }

                if (logs.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection overKMSelfDriveLog = logs.get(0);
            calculateOverNightFee(fees, temp, overKMSelfDriveLog, null);
        }
        return fees;
    }

    @Override
    public PaymentRequestItemDTO getTollFee(List<JourneyDiaryDaily> diaries) {
        long totalPrice = diaries.stream()
                .map(JourneyDiaryDaily::getJourneyDiaryDailyCostTypes)
                .flatMap(Collection::stream)
                .filter(c -> TOLLS_FEE.code().equals(c.getCostType().getCode())
                        || PARKING_FEE.code().equals(c.getCostType().getCode()))
                .filter(x -> x.getValue() != null)
                .mapToLong(x -> x.getValue().longValue())
                .sum();
        return PaymentRequestItemDTO.builder()
                .id(TOLL_FEE.getId())
                .name(TOLL_FEE.getName())
                .unit(TOLL_FEE.getUnit())
                .totalPrice(totalPrice)
                .build();
    }

    @Override
    public PaymentRequestItemDTO getOtherFee() {
        return PaymentRequestItemDTO.builder()
                .id(OTHER_FEE.getId())
                .name(OTHER_FEE.getName())
                .unit(OTHER_FEE.getUnit())
                .totalPrice(0L)
                .build();
    }

    @Override
    public PaymentRequestItemDTO getOverKMContractWithOutDriver(Timestamp from, Timestamp to,
                                                                Contract contract,
                                                                List<JourneyDiaryDaily> dailyJourneyDiaries) {
        LogContractPriceCostType overKMWithDriverLog = logContractCostTypeService.findClosestLogBeforeDate(
                dateUtils.getTomorrow(to), contract.getId(), OVER_KM_SURCHARGE.code());
        LogContractNormList overKMQuota = logContractNormListService.findClosestLogBeforeDate(
                dateUtils.getTomorrow(to), contract.getId(), CONTRACT_KM_NORM.code());

        double KMCustomerUsedNormalDay = dailyJourneyDiaries.stream()
                .map(JourneyDiaryDaily::getUsedKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .doubleValue();

        double overKMWithDriverNormalDay = KMCustomerUsedNormalDay - calculateOverKM(overKMQuota, contract, from, to);
        return calculateKMFee(overKMWithDriverLog, Math.max(overKMWithDriverNormalDay, 0.0), OVER_KM_CONTRACT_WITHOUT_DRIVER);
    }

    private void calculateOverNightFee(List<PaymentRequestItemDTO> fees,
                                       List<JourneyDiaryDaily> temp,
                                       PRLogContractCostProjection logContractPriceCostType,
                                       Timestamp toDate) {
        long count = temp.stream().filter(x ->
                filterWithTimeRange(x, logContractPriceCostType.getFromDate(), toDate))
                .mapToLong(JourneyDiaryDaily::getOvernight).sum();
        fees.add(PaymentRequestItemDTO.builder()
                .id(OVER_NIGHT_FEE.getId())
                .name(OVER_NIGHT_FEE.getName())
                .unit(OVER_NIGHT_FEE.getUnit())
                .price(logContractPriceCostType.getPrice())
                .fromDate(logContractPriceCostType.getFromDate())
                .toDate(logContractPriceCostType.getFromDate())
                .count(new BigDecimal(count))
                .totalPrice(logContractPriceCostType.getPrice() * count)
                .build());
    }

    private List<PaymentRequestItemDTO> getKMFee(Contract contract,
                                                 Timestamp from, Timestamp to,
                                                 List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                 Map<String, List<PRLogContractCostProjection>> map,
                                                 Timestamp firstDayOD,
                                                 Double firstDayODPriceDiff) {
        Long contractId = contract.getId();
        List<PRLogContractCostProjection> overKMSelfDrive = map.get(SELF_DRIVE_OVER_KM_SURCHARGE.code());
        List<PaymentRequestItemDTO> fees = new ArrayList<>();
        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(from);
        if (overKMSelfDrive.size() > 1) {
            for (int i = 0; i < overKMSelfDrive.size(); i++) {
                if (i == 0) {
                    calculateOverKMSelfDriveFeeWithTimeRangeAndAddToList(fees, dailyJourneyDiaries, overKMSelfDrive.get(i), dateUtils.getTomorrow(to));
                } else {
                    calculateOverKMSelfDriveFeeWithTimeRangeAndAddToList(fees, dailyJourneyDiaries, overKMSelfDrive.get(i), overKMSelfDrive.get(i - 1).getFromDate());
                }

                if (overKMSelfDrive.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection overKMSelfDriveLog = overKMSelfDrive.get(0);
            calculateOverKMSelfDriveFeeWithTimeRangeAndAddToList(fees, dailyJourneyDiaries, overKMSelfDriveLog, null);
        }

        LogContractPriceCostType overKMWithDriverLog = logContractCostTypeService.findClosestLogBeforeDate(
                dateUtils.getTomorrow(to), contractId, OVER_KM_SURCHARGE.code());
        LogContractNormList overKMQuota = logContractNormListService.findClosestLogBeforeDate(
                dateUtils.getTomorrow(to), contractId, CONTRACT_KM_NORM.code());

        String workingTimeFromHistoryValue = contractChangeHistoryService.findLastChangeOfField(contractId,
                WORKING_TIME_WEEKEND_HOLIDAY_FROM.getId(), dateUtils.getTomorrow(to));
        String workingTimeToHistoryValue = contractChangeHistoryService.findLastChangeOfField(contractId,
                WORKING_TIME_WEEKEND_HOLIDAY_TO.getId(), dateUtils.getTomorrow(to));
        Time contractWorkingTimeFrom = Objects.isNull(workingTimeFromHistoryValue) ? contract.getWorkingTimeWeekendHolidayFrom()
                : Time.valueOf(workingTimeFromHistoryValue);
        Time contractWorkingTimeTo = Objects.isNull(workingTimeToHistoryValue) ? contract.getWorkingTimeWeekendHolidayTo()
                : Time.valueOf(workingTimeToHistoryValue);

        // calculate OK sunday
        List<PRLogContractCostProjection> overKMFeeChanges = map.get(OVER_KM_SURCHARGE.code());

        if (overKMFeeChanges.size() > 1) {
            for (int i = 0; i < overKMFeeChanges.size(); i++) {
                if (i == 0) {
                    calculateOKSundayWithDriverAndAddToList(fees, dailyJourneyDiaries, overKMFeeChanges.get(i), dateUtils.getTomorrow(to));
                } else {
                    calculateOKSundayWithDriverAndAddToList(fees, dailyJourneyDiaries, overKMFeeChanges.get(i), overKMFeeChanges.get(i - 1).getFromDate());
                }

                if (overKMFeeChanges.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection overKMFeeChange = overKMFeeChanges.get(0);
            calculateOKSundayWithDriverAndAddToList(fees, dailyJourneyDiaries, overKMFeeChange, null);
        }

        // calculate OK holiday fees
        if (overKMFeeChanges.size() > 1) {
            for (int i = 0; i < overKMFeeChanges.size(); i++) {
                if (i == 0) {
                    calculateOKHolidayWithDriverAndAddToList(fees, dailyJourneyDiaries, overKMFeeChanges.get(i), dateUtils.getTomorrow(to));
                } else {
                    calculateOKHolidayWithDriverAndAddToList(fees, dailyJourneyDiaries, overKMFeeChanges.get(i), overKMFeeChanges.get(i - 1).getFromDate());
                }

                if (overKMFeeChanges.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection overKMFeeChange = overKMFeeChanges.get(0);
            calculateOKHolidayWithDriverAndAddToList(fees, dailyJourneyDiaries, overKMFeeChange, null);
        }

        // calculate sunday fees
        List<JourneyDiaryDaily> diariesWithDriverInSunday = dailyJourneyDiaries.stream()
                .filter(x -> checkIsInSunday(x)
                        && !x.getIsSelfDrive())
                .collect(Collectors.toList());
        List<PRLogContractCostProjection> weekendWithDriverCostLogs = map.get(WEEKEND_SURCHARGE.code());
        fees.addAll(getKMFeeWithTimeUsePolicy(contract, diariesWithDriverInSunday,
                weekendWithDriverCostLogs, SUNDAY_FEE_NORMAL, to, contractWorkingTimeFrom, contractWorkingTimeTo, firstDayOD, firstDayODPriceDiff));

        // calculate holiday fees
        List<JourneyDiaryDaily> diariesWithDriverInHoliday = dailyJourneyDiaries.stream()
                .filter(x -> x.getIsHoliday()
                        && !x.getIsSelfDrive())
                .collect(Collectors.toList());
        List<PRLogContractCostProjection> holidayWithDriverCostLogs = map.get(HOLIDAY_SURCHARGE.code());
        fees.addAll(getKMFeeWithTimeUsePolicy(contract, diariesWithDriverInHoliday,
                holidayWithDriverCostLogs, HOLIDAY_FEE_NORMAL, to, contractWorkingTimeFrom, contractWorkingTimeTo, firstDayOD, firstDayODPriceDiff));

        calculateKMWithDriverNormalDayFeeAndAddToList(fees, dailyJourneyDiaries, overKMWithDriverLog,
                overKMQuota, contract, from, to);
        calculateKMSelfDriveFeeAndAddToList(fees, dailyJourneyDiaries, to, map, Objects.nonNull(firstDayOD));
        return fees;
    }

    private void calculateOKHolidayWithDriverAndAddToList(List<PaymentRequestItemDTO> fees,
                                                          List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                          PRLogContractCostProjection prLogContractCostProjection,
                                                          Timestamp toDate) {
        double overKMWithDriverHoliday = dailyJourneyDiaries.stream()
                .filter(x -> filterWithTimeRange(x, prLogContractCostProjection.getFromDate(), toDate))
                .filter(x -> x.getIsHoliday()
                        && !x.getIsSelfDrive())
                .filter(x -> x.getOverKm() != null)
                .mapToDouble(x -> x.getOverKm().doubleValue()).sum();
        fees.add(calculateKMFee(prLogContractCostProjection, Math.max(overKMWithDriverHoliday, 0.0), OVER_KM_HOLIDAY));
    }

    private void calculateOKSundayWithDriverAndAddToList(List<PaymentRequestItemDTO> fees,
                                                         List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                         PRLogContractCostProjection logContractCostProjection,
                                                         Timestamp toDate) {
        double overKMWithDriverSunday = dailyJourneyDiaries.stream()
                .filter(x -> filterWithTimeRange(x, logContractCostProjection.getFromDate(), toDate))
                .filter(x -> checkIsInSunday(x)
                        && !x.getIsSelfDrive())
                .filter(x -> x.getOverKm() != null)
                .mapToDouble(x -> x.getOverKm().doubleValue()).sum();
        fees.add(calculateKMFee(logContractCostProjection, Math.max(overKMWithDriverSunday, 0.0), OVER_KM_SUNDAY));
    }

    private void calculateKMSelfDriveFeeAndAddToList(List<PaymentRequestItemDTO> fees,
                                                     List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                     Timestamp to,
                                                     Map<String, List<PRLogContractCostProjection>> map,
                                                     boolean isFlexibleAndHaveOD) {
        List<PRLogContractCostProjection> selfDriveNormalDayLogs = map.get(SELF_DRIVE_NORMAL_DAY_SURCHARGE.code());

        List<PRLogContractCostProjection> selfDriveSundayLogs = map.get(SELF_DRIVE_WEEKEND_SURCHARGE.code());

        List<PRLogContractCostProjection> selfDriveHolidayLogs = map.get(SELF_DRIVE_HOLIDAY_SURCHARGE.code());

        List<JourneyDiaryDaily> selfDriveDiaries = dailyJourneyDiaries.stream()
                .filter(this::checkDiaryIsSelfDrive)
                .collect(Collectors.toList());

        List<JourneyDiaryDaily> diariesSelfDriveNormalDay = selfDriveDiaries.stream()
                .filter(j -> checkIsInNormalDay(j) || (isFlexibleAndHaveOD && !j.getIsHoliday()))
                .collect(Collectors.toList());
        fees.addAll(calculateKMSelfDriveFee(diariesSelfDriveNormalDay,
                selfDriveNormalDayLogs, NORMAL_DAY_FEE_SELF_DRIVE, to));

        List<JourneyDiaryDaily> diariesSelfDriveInSunday = selfDriveDiaries.stream()
                .filter(j -> checkIsInSunday(j) && !isFlexibleAndHaveOD)
                .collect(Collectors.toList());
        fees.addAll(calculateKMSelfDriveFee(diariesSelfDriveInSunday,
                selfDriveSundayLogs, SUNDAY_FEE_SELF_DRIVE, to));

        List<JourneyDiaryDaily> diariesSelfDriveInHoliday = selfDriveDiaries.stream()
                .filter(JourneyDiaryDaily::getIsHoliday)
                .collect(Collectors.toList());
        fees.addAll(calculateKMSelfDriveFee(diariesSelfDriveInHoliday,
                selfDriveHolidayLogs, HOLIDAY_FEE_SELF_DRIVE, to));
    }

    private boolean checkDiaryIsSelfDrive(JourneyDiaryDaily x) {
        return x.getIsSelfDrive() || (!x.getIsSelfDrive() && !CollectionUtils.isEmpty(x.getChildren())
                && x.getChildren().stream().anyMatch(JourneyDiaryDaily::getIsSelfDrive));
    }

    private List<PaymentRequestItemDTO> calculateKMSelfDriveFee(List<JourneyDiaryDaily> diariesSelfDriveNormalDay,
                                                                List<PRLogContractCostProjection> selfDriveNormalDayLogs,
                                                                PaymentRequestEnum paymentRequestEnum,
                                                                Timestamp to) {
        List<PaymentRequestItemDTO> items = new ArrayList<>();
        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(to);
        if (selfDriveNormalDayLogs.size() > 1) {
            for (int i = 0; i < selfDriveNormalDayLogs.size(); i++) {
                if (i == 0) {
                    calculateKMSelfDriveFeeWithTimeRangeAndAddToList(items, diariesSelfDriveNormalDay,
                            selfDriveNormalDayLogs.get(i), dateUtils.getTomorrow(to), paymentRequestEnum);
                } else {
                    calculateKMSelfDriveFeeWithTimeRangeAndAddToList(items, diariesSelfDriveNormalDay,
                            selfDriveNormalDayLogs.get(i), selfDriveNormalDayLogs.get(i - 1).getFromDate(), paymentRequestEnum);
                }

                if (selfDriveNormalDayLogs.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection overKMSelfDriveLog = selfDriveNormalDayLogs.get(0);
            calculateKMSelfDriveFeeWithTimeRangeAndAddToList(items, diariesSelfDriveNormalDay,
                    overKMSelfDriveLog, null, paymentRequestEnum);
        }

        return items;
    }

    private void calculateKMSelfDriveFeeWithTimeRangeAndAddToList(List<PaymentRequestItemDTO> items,
                                                                  List<JourneyDiaryDaily> diariesSelfDriveNormalDay,
                                                                  PRLogContractCostProjection overKMSelfDriveLog,
                                                                  Timestamp toDate,
                                                                  PaymentRequestEnum paymentRequestEnum) {
        long totalDays = diariesSelfDriveNormalDay.stream()
                .filter(x -> filterWithTimeRange(x, overKMSelfDriveLog.getFromDate(), toDate))
                .count();
        items.add(PaymentRequestItemDTO.builder()
                .id(paymentRequestEnum.getId())
                .name(paymentRequestEnum.getName())
                .unit(paymentRequestEnum.getUnit())
                .count(new BigDecimal(totalDays))
                .fromDate(overKMSelfDriveLog.getFromDate())
                .toDate(overKMSelfDriveLog.getToDate())
                .price(overKMSelfDriveLog.getPrice())
                .totalPrice(overKMSelfDriveLog.getPrice() * totalDays)
                .build());
    }

    private void calculateKMWithDriverNormalDayFeeAndAddToList(List<PaymentRequestItemDTO> fees,
                                                               List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                               LogContractPriceCostType overKMWithDriverLog,
                                                               LogContractNormList overKMQuota,
                                                               Contract contract, Timestamp from, Timestamp to) {
        double KMCustomerUsedNormalDay = dailyJourneyDiaries.stream()
                .filter(x -> checkIsInNormalDay(x)
                        && !x.getIsSelfDrive()
                        && x.getUsedKm() != null)
                .mapToDouble(x -> x.getUsedKm().doubleValue()).sum();

        double overKMWithDriverNormalDay = KMCustomerUsedNormalDay - calculateOverKM(overKMQuota, contract, from, to);
        fees.add(calculateKMFee(overKMWithDriverLog, Math.max(overKMWithDriverNormalDay, 0.0), OVER_KM_NORMAL_DAY));
    }

    public double calculateOverKM(LogContractNormList overKMQuota,
                                  Contract contract,
                                  Timestamp from,
                                  Timestamp to) {

//        WorkingDayEnum workingDayEnum = getNewestWorkingDayEnum(contract, dateUtils.getTomorrow(to));

//        int lendingWeeks = Math.toIntExact(lendDays / 7);
//        int lendingWeekSurplus = Math.toIntExact(lendDays % 7);

//        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(from);

//        WeekendCounter lendingWeekend = countWeekendDays(dateUtils.getDayOfWeek(from), lendingWeeks, lendingWeekSurplus);

//        int lendingDayOffs = calculateDayOffs(workingDayEnum, lendingWeekend, dailyJourneyDiaries);
        double diff = calculateDaysDiff(from, to);

        return overKMQuota.getQuota().doubleValue() * diff;
    }

    @Override
    public Map<String, List<PRLogContractCostProjection>> costTypeMap(Long contractId,
                                                                      Timestamp from,
                                                                      Timestamp to) {
        return logContractCostTypeService.findByContractIdAndWithinTimeAndCodeIn(contractId, from, to, AvisApiConstant.PR_COST_CODES)
                .stream()
                .collect(Collectors.groupingBy(PRLogContractCostProjection::getCostCode));
    }

    @Override
    public Triplet<List<JourneyDiaryDaily>, Timestamp, Double> checkAndConvertDiariesIfOverDay(Contract contract,
                                                                                               Timestamp toDate,
                                                                                               List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                                                               double diff) {

        String workingDayTypeValue = contractChangeHistoryService.findLastChangeOfField(
                contract.getId(), WORKING_DAY_ID.getId(), dateUtils.getTomorrow(toDate));

        Long lastWorkingDayId = Objects.isNull(workingDayTypeValue) ? contract.getWorkingDay().getId()
                : Long.parseLong(workingDayTypeValue);

        String workingDayValue = contractChangeHistoryService.findLastChangeOfField(
                contract.getId(), WORKING_DAY.getId(), dateUtils.getTomorrow(toDate));

        Integer lastWorkingDayValue = Objects.isNull(workingDayValue) ? contract.getWorkingDayValue()
                : Integer.valueOf(workingDayValue);

        Timestamp firstOD = null;
        Double diffValueFirstDay = null;

        long customerUseDays = dailyJourneyDiaries.stream().filter(j ->
                Objects.nonNull(j.getKmStart()) && !j.getIsSelfDrive()).count();

        if (lastWorkingDayId.equals(WorkingDayEnum.FLEXIBLE.getId())
                && customerUseDays > lastWorkingDayValue * diff) {
            double floor = Math.floor(lastWorkingDayValue * diff);
            List<JourneyDiaryDaily> temp = dailyJourneyDiaries
                    .stream()
                    .filter(JourneyDiaryDaily::getIsOverDay)
                    .collect(Collectors.toList());
            firstOD = temp.get(temp.size() - 1).getDate();
            diffValueFirstDay = lastWorkingDayValue * diff - floor;

            temp.forEach(j -> j.setIsWeekend(true));
        }
        return Triplet.with(dailyJourneyDiaries, firstOD, diffValueFirstDay);
    }

    private List<PaymentRequestItemDTO> getKMFeeWithTimeUsePolicy(Contract contract,
                                                                  List<JourneyDiaryDaily> diaries,
                                                                  List<PRLogContractCostProjection> logs,
                                                                  PaymentRequestEnum paymentRequestEnum,
                                                                  Timestamp to,
                                                                  Time contractWorkingTimeFrom,
                                                                  Time contractWorkingTimeTo,
                                                                  Timestamp firstDayOD,
                                                                  Double firstDayODPriceDiff) {
        boolean usingPolicy = ContractTimeUserPolicyEnum.USING.getId().equals(contract.getTimeUsePolicyGroupId());

        List<PaymentRequestItemDTO> fees = new ArrayList<>();
        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(to);
        if (logs.size() > 1) {
            for (int i = 0; i < logs.size(); i++) {
                if (i == 0) {
                    calculateKMFeeWithTimeUsePolicy(fees, diaries, logs.get(i), dateUtils.getTomorrow(to),
                            paymentRequestEnum, usingPolicy, contractWorkingTimeFrom, contractWorkingTimeTo, firstDayOD, firstDayODPriceDiff);
                } else {
                    calculateKMFeeWithTimeUsePolicy(fees, diaries, logs.get(i), logs.get(i - 1).getFromDate(),
                            paymentRequestEnum, usingPolicy, contractWorkingTimeFrom, contractWorkingTimeTo, firstDayOD, firstDayODPriceDiff);
                }

                if (logs.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection logContractPriceCostType = logs.get(0);
            calculateKMFeeWithTimeUsePolicy(fees, diaries, logContractPriceCostType, null,
                    paymentRequestEnum, usingPolicy, contractWorkingTimeFrom, contractWorkingTimeTo, firstDayOD, firstDayODPriceDiff);
        }
        return fees;
    }

    private void calculateKMFeeWithTimeUsePolicy(List<PaymentRequestItemDTO> fees,
                                                 List<JourneyDiaryDaily> diaries,
                                                 PRLogContractCostProjection logContractPriceCostType,
                                                 Timestamp toDate,
                                                 PaymentRequestEnum paymentRequestEnum,
                                                 boolean usingPolicy,
                                                 Time contractWorkingTimeFrom,
                                                 Time contractWorkingTimeTo,
                                                 Timestamp firstDayOD,
                                                 Double firstDayODPriceDiff) {
        double totalPrice = 0;
        double totalLowerPrice = 0;
        long price = logContractPriceCostType.getPrice();
        double lowerCount = 0;
        double count = 0;
        Boolean havingFirstDayODWithLessWorkingTime = null;

        List<JourneyDiaryDaily> temp = diaries.stream()
                .filter(x -> filterWithTimeRange(x, logContractPriceCostType.getFromDate(), toDate))
                .filter(x -> x.getWorkingTimeGpsFrom() != null)
                .filter(x -> x.getWorkingTimeGpsTo() != null || (x.getJourneyDiaryId() != null && x.getFlagMultiDate()))
                .collect(Collectors.toList());
        for (JourneyDiaryDaily x : temp) {

            Time endWorkingTime = Objects.isNull(x.getWorkingTimeGpsTo()) ?
                    new Time(24 * 3600 * 1000)
                    : x.getWorkingTimeGpsTo();

            long totalWorkingTime = x.getWorkingTimeGpsFrom().compareTo(endWorkingTime) < 0 ?
                    endWorkingTime.getTime() - x.getWorkingTimeGpsFrom().getTime() : 0;
            long workingTime;
            if (x.getFlagMultiDate()) {
                workingTime = Math.min(endWorkingTime.getTime(), contractWorkingTimeTo.getTime()) -
                        Math.max(contractWorkingTimeFrom.getTime(), x.getWorkingTimeGpsFrom().getTime());
            } else {
                workingTime = x.getOverTime() != null ? totalWorkingTime - x.getOverTime() * 60 * 1000
                        : totalWorkingTime;
            }

            if (usingPolicy) {
                if (workingTime <= 4 * 3600000) {
                    lowerCount++;
                    totalLowerPrice += price * 0.7;
                    havingFirstDayODWithLessWorkingTime = checkODAndWorkingTime(x, firstDayOD, true);
                } else {
                    count++;
                    totalPrice += price;
                    havingFirstDayODWithLessWorkingTime = checkODAndWorkingTime(x, firstDayOD, false);
                }
            } else {
                if (totalWorkingTime <= 4 * 3600000) {
                    lowerCount++;
                    totalLowerPrice += price * 0.7;
                    havingFirstDayODWithLessWorkingTime = checkODAndWorkingTime(x, firstDayOD, true);
                } else {
                    count++;
                    totalPrice += price;
                    havingFirstDayODWithLessWorkingTime = checkODAndWorkingTime(x, firstDayOD, false);
                }
            }
        }

        if (Objects.nonNull(havingFirstDayODWithLessWorkingTime)) {
            if (havingFirstDayODWithLessWorkingTime) {
                lowerCount -= firstDayODPriceDiff;
                totalLowerPrice = price * 0.7 * lowerCount;
            } else {
                count -= firstDayODPriceDiff;
                totalPrice = price * count;
            }
        }

        PaymentRequestItemDTO.PaymentRequestItemDTOBuilder builder = PaymentRequestItemDTO.builder()
                .id(paymentRequestEnum.getId())
                .name(paymentRequestEnum.getName())
                .unit(paymentRequestEnum.getUnit())
                .fromDate(logContractPriceCostType.getFromDate())
                .toDate(logContractPriceCostType.getToDate());

        fees.add(builder
                .count(BigDecimal.valueOf(count).setScale(2, RoundingMode.HALF_UP))
                .price(price)
                .totalPrice(Math.round(totalPrice))
                .build());

        fees.add(builder
                .name(paymentRequestEnum.getName().split("\\)")[0] + " - 70%)")
                .count(BigDecimal.valueOf(lowerCount).setScale(2, RoundingMode.HALF_UP))
                .price(Math.round(price * 0.7))
                .totalPrice(Math.round(totalLowerPrice))
                .build());
    }

    private Boolean checkODAndWorkingTime(JourneyDiaryDaily x, Timestamp firstDayOD, boolean isLessWorkingTime) {
        if (Objects.isNull(firstDayOD)) {
            return null;
        } else if (x.getDate().equals(firstDayOD)) {
            return isLessWorkingTime;
        } else {
            return null;
        }
    }

    private PaymentRequestItemDTO calculateKMFee(LogContractPriceCostType overKMWithDriverLog,
                                                 double overKMWithDriver,
                                                 PaymentRequestEnum paymentRequestEnum) {
        return PaymentRequestItemDTO.builder()
                .id(paymentRequestEnum.getId())
                .name(paymentRequestEnum.getName())
                .unit(paymentRequestEnum.getUnit())
                .count(new BigDecimal(Math.round(overKMWithDriver)))
                .price(overKMWithDriverLog.getPrice().longValue())
                .fromDate(overKMWithDriverLog.getFromDate())
                .toDate(overKMWithDriverLog.getToDate())
                .totalPrice(Math.round(overKMWithDriverLog.getPrice().longValue() * overKMWithDriver))
                .build();
    }

    private PaymentRequestItemDTO calculateKMFee(PRLogContractCostProjection prLogContractCostProjection,
                                                 double overKMWithDriver,
                                                 PaymentRequestEnum paymentRequestEnum) {
        return PaymentRequestItemDTO.builder()
                .id(paymentRequestEnum.getId())
                .name(paymentRequestEnum.getName())
                .unit(paymentRequestEnum.getUnit())
                .count(new BigDecimal(Math.round(overKMWithDriver)))
                .price(prLogContractCostProjection.getPrice())
                .fromDate(prLogContractCostProjection.getFromDate())
                .toDate(prLogContractCostProjection.getToDate())
                .totalPrice(Math.round(prLogContractCostProjection.getPrice() * overKMWithDriver))
                .build();
    }

    private void calculateOverKMSelfDriveFeeWithTimeRangeAndAddToList(List<PaymentRequestItemDTO> fees,
                                                                      List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                                      PRLogContractCostProjection logContractPriceCostType,
                                                                      Timestamp toDate) {
        BigDecimal overKMSelfDrive = dailyJourneyDiaries.stream()
                .filter(x -> filterWithTimeRange(x, logContractPriceCostType.getFromDate(), toDate))
                .map(JourneyDiaryDaily::getOverKmSelfDrive)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        fees.add(PaymentRequestItemDTO.builder()
                .id(OVER_KM_SELF_DRIVE.getId())
                .name(OVER_KM_SELF_DRIVE.getName())
                .unit(OVER_KM_SELF_DRIVE.getUnit())
                .fromDate(logContractPriceCostType.getFromDate())
                .toDate(logContractPriceCostType.getToDate())
                .count(overKMSelfDrive.setScale(0, RoundingMode.HALF_UP))
                .price(logContractPriceCostType.getPrice())
                .totalPrice(Math.round(logContractPriceCostType.getPrice() * overKMSelfDrive.doubleValue()))
                .build());
    }

    private List<PaymentRequestItemDTO> getOverTimeFee(Contract contract, Timestamp from, Timestamp to,
                                                       List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                       Map<String, List<PRLogContractCostProjection>> map) {
        List<PRLogContractCostProjection> overTimePrices = map.get(OVERTIME_SURCHARGE.code());
        List<PaymentRequestItemDTO> fees = new ArrayList<>();
        Timestamp firstDayOfMonth = dateUtils.getFirstDayOfMonth(from);
        if (overTimePrices.size() > 1) {
            for (int i = 0; i < overTimePrices.size(); i++) {
                if (i == 0) {
                    calculateOverTimeFeeWithTimeRangeAndAddToList(fees, dailyJourneyDiaries, overTimePrices.get(i), dateUtils.getTomorrow(to));
                } else {
                    calculateOverTimeFeeWithTimeRangeAndAddToList(fees, dailyJourneyDiaries, overTimePrices.get(i), overTimePrices.get(i - 1).getFromDate());
                }

                if (overTimePrices.get(i).getFromDate().equals(firstDayOfMonth)) {
                    break;
                }
            }
        } else {
            PRLogContractCostProjection logContractPriceCostType = overTimePrices.get(0);
            calculateOverTimeFeeWithTimeRangeAndAddToList(fees, dailyJourneyDiaries, logContractPriceCostType, null);
        }
        return fees;
    }

    private void calculateOverTimeFeeWithTimeRangeAndAddToList(List<PaymentRequestItemDTO> fees,
                                                               List<JourneyDiaryDaily> dailyJourneyDiaries,
                                                               PRLogContractCostProjection logContractPriceCostType,
                                                               Timestamp toDate) {

        long otHolidayCount = dailyJourneyDiaries.stream()
                .filter(x -> x.getIsHoliday()
                        && !x.getIsSelfDrive()
                        && x.getOverTime() != null
                        && filterWithTimeRange(x, logContractPriceCostType.getFromDate(), toDate))
                .mapToLong(JourneyDiaryDaily::getOverTime)
                .sum();
        fees.add(calculateOverTimeFee(logContractPriceCostType, otHolidayCount, OVERTIME_HOLIDAY));

        long otSundayCount = dailyJourneyDiaries.stream()
                .filter(x -> checkIsInSunday(x)
                        && !x.getIsSelfDrive()
                        && x.getOverTime() != null
                        && filterWithTimeRange(x, logContractPriceCostType.getFromDate(), toDate))
                .mapToLong(JourneyDiaryDaily::getOverTime)
                .sum();
        fees.add(calculateOverTimeFee(logContractPriceCostType, otSundayCount, OVERTIME_SUNDAY));

        long otNormalDay = dailyJourneyDiaries.stream()
                .filter(x -> checkIsInNormalDay(x)
                        && !x.getIsSelfDrive()
                        && x.getOverTime() != null
                        && filterWithTimeRange(x, logContractPriceCostType.getFromDate(), toDate))
                .mapToLong(JourneyDiaryDaily::getOverTime)
                .sum();
        fees.add(calculateOverTimeFee(logContractPriceCostType, otNormalDay, OVERTIME_NORMAL_DAY));
    }

    private boolean filterWithTimeRange(JourneyDiaryDaily x, Timestamp fromDate, Timestamp toDate) {
        if (Objects.isNull(toDate)) {
            return true;
        } else {
            return dateUtils.isBetweenButNotEqualMax(x.getDate(), fromDate, toDate);
        }
    }

    private boolean checkIsInNormalDay(JourneyDiaryDaily x) {
        return !x.getIsWeekend()
                && !x.getIsHoliday();
    }

    private boolean checkIsInSunday(JourneyDiaryDaily x) {
        return !x.getIsHoliday()
                && x.getIsWeekend();
    }

    private PaymentRequestItemDTO calculateOverTimeFee(PRLogContractCostProjection log, long count,
                                                       PaymentRequestEnum paymentRequestEnum) {
        return PaymentRequestItemDTO.builder()
                .id(paymentRequestEnum.getId())
                .name(paymentRequestEnum.getName())
                .unit(paymentRequestEnum.getUnit())
                .count(roundToTwoAfterComma(count * 1.0 / 60))
                .price(log.getPrice())
                .fromDate(log.getFromDate())
                .toDate(log.getToDate())
                .totalPrice(Math.round(log.getPrice() * count * 1.0 / 60))
                .build();
    }

    private List<LogContractPriceCostType> getListLogs(Contract contract, String code,
                                                       Timestamp from, Timestamp to) {
        Long contractId = contract.getId();
        List<LogContractPriceCostType> logs = logContractCostTypeService.findAllByContractIdWithinDate(contractId, code, from, to);

        if (CollectionUtils.isEmpty(logs)) {
            LogContractPriceCostType closestChange = logContractCostTypeService.findClosestLogBeforeDate(from, contractId, code);
            logs.add(closestChange);
        } else if (!logs.get(logs.size() - 1)
                .getFromDate().equals(dateUtils.getFirstDayOfMonth(from)) && !from.equals(contract.getFromDatetime())) {
            LogContractPriceCostType closestChange = logContractCostTypeService.findClosestLogBeforeDate(from, contractId, code);
            logs.add(closestChange);
        }

        return logs;
    }

    private PaymentRequestItemDTO calculateRentalPrice(double diff,
                                                       LogContractPriceCostType log) {
        double finalPrice;
        PaymentRequestItemDTO paymentRequestItemDTO = PaymentRequestItemDTO.builder()
                .count(roundToTwoAfterComma(diff))
                .unit(RENTAL_PRICE.getUnit())
                .name(RENTAL_PRICE.getName())
                .id(RENTAL_PRICE.getId())
                .price(log.getPrice().longValue())
                .fromDate(log.getFromDate())
                .toDate(log.getToDate())
                .build();
        finalPrice = log.getPrice().longValue() * diff;
        paymentRequestItemDTO.setTotalPrice(Math.round(finalPrice));
        return paymentRequestItemDTO;
    }

    public double calculateDaysDiff(Timestamp from, Timestamp to) {
        long lendDays = dateUtils.getDaysBetween(from, to) + 1;
        int monthDays = dateUtils.getMonthDays(from);
        return lendDays * 1.0 / monthDays;
    }

    private BigDecimal roundToTwoAfterComma(double input) {
        return new BigDecimal(String.valueOf(input)).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateDayOffs(WorkingDayEnum workingDayEnum, WeekendCounter weekendCounter,
                                 List<JourneyDiaryDaily> dailyJourneyDiaries) {
        int dayOffs = 0;
        switch (workingDayEnum) {
            case MON_TO_FRI:
                return weekendCounter.getSatCount() + weekendCounter.getSunCount();
            case MON_TO_SAT:
                return weekendCounter.getSunCount();
            case MON_TO_SUN:
            case FLEXIBLE:
                return dayOffs;
            case MON_TO_SAT_PLUS_2_SUN:
//                return weekendCounter.getSunCount() > 2 ? weekendCounter.getSunCount() - 2 : dayOffs;
                return Math.toIntExact(dailyJourneyDiaries.stream()
                        .filter(JourneyDiaryDaily::getIsWeekend)
                        .count());
            default:
                throw new NotFoundException("Cannot calculate with working day: " + workingDayEnum);
        }
    }

    private WeekendCounter countWeekendDays(DayOfWeek dayOfWeek, int weeks, int surplus) {
        WeekendCounter counter = WeekendCounter.builder()
                .satCount(weeks)
                .sunCount(weeks)
                .build();
        if (surplus > 0) {
            if (surplus + dayOfWeek.getValue() - 1 >= DayOfWeek.SATURDAY.getValue()) {
                counter.setSatCount(counter.getSatCount() + 1);
            }

            if (surplus + dayOfWeek.getValue() - 1 >= DayOfWeek.SUNDAY.getValue()) {
                counter.setSunCount(counter.getSunCount() + 1);
            }
        }
        return counter;
    }
}
