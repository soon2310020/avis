package vn.com.twendie.avis.api.converter;

import org.javatuples.Pair;
import org.modelmapper.AbstractConverter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.export.OvertimeReport;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static vn.com.twendie.avis.data.enumtype.ContractCostTypeEnum.*;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;
import static vn.com.twendie.avis.data.enumtype.NormListEnum.CONTRACT_KM_NORM;
import static vn.com.twendie.avis.data.enumtype.WorkingDayEnum.FLEXIBLE;

@Component
public class OvertimeReportConverter extends AbstractConverter<Pair<Contract, Timestamp>, OvertimeReport> {

    private final ContractService contractService;
    private final WorkingDayService workingDayService;
    private final LogContractCostTypeService logContractCostTypeService;
    private final LogContractNormListService logContractNormListService;

    public OvertimeReportConverter(@Lazy ContractService contractService,
                                   @Lazy WorkingDayService workingDayService,
                                   @Lazy LogContractCostTypeService logContractCostTypeService,
                                   @Lazy LogContractNormListService logContractNormListService) {
        this.contractService = contractService;
        this.workingDayService = workingDayService;
        this.logContractCostTypeService = logContractCostTypeService;
        this.logContractNormListService = logContractNormListService;
    }

    @Override
    protected OvertimeReport convert(Pair<Contract, Timestamp> contractTimestampPair) {

        Contract contract = contractTimestampPair.getValue0();
        Timestamp timestamp = contractTimestampPair.getValue1();

        Long driverId = contractService.getContractValueAtTime(contract, DRIVER_ID.getName(), timestamp, Long.class);
        Long vehicleId = contractService.getContractValueAtTime(contract, VEHICLE_ID.getName(), timestamp, Long.class);

        long realWorkingDay = contract.getJourneyDiaryDailies()
                .stream()
                .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsSelfDrive())
                .map(JourneyDiaryDaily::getUsedKm)
                .filter(Objects::nonNull)
                .count();

        Time workingTimeFrom = contractService
                .getContractValueAtTime(contract, WORKING_TIME_FROM.getName(), timestamp, Time.class);
        Time workingTimeTo = contractService
                .getContractValueAtTime(contract, WORKING_TIME_TO.getName(), timestamp, Time.class);

        WorkingDay workingDay = workingDayService.getContractWorkingDayAtTime(contract, timestamp);
        Integer workingDayValue = workingDay.getCode().equals(FLEXIBLE.getCode()) ?
                contractService.getContractValueAtTime(contract, WORKING_DAY.getName(), timestamp, Integer.class) : null;

        long overtimeMinutes = contract.getJourneyDiaryDailies()
                .stream()
                .map(JourneyDiaryDaily::getOverTime)
                .filter(Objects::nonNull)
                .reduce(Long::sum)
                .orElse(0L);

        BigDecimal overtime = BigDecimal.valueOf(overtimeMinutes)
                .divide(BigDecimal.valueOf(60), 2, ROUND_HALF_UP);

        long overnight = contract.getJourneyDiaryDailies()
                .stream()
                .map(JourneyDiaryDaily::getOvernight)
                .filter(Objects::nonNull)
                .reduce(Integer::sum)
                .orElse(0);

        long weekend = contract.getJourneyDiaryDailies()
                .stream()
                .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsSelfDrive())
                .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getUsedKm()))
                .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsHoliday())
                .filter(JourneyDiaryDaily::getIsWeekend)
                .count();

        long holiday = contract.getJourneyDiaryDailies()
                .stream()
                .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsSelfDrive())
                .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getUsedKm()))
                .filter(JourneyDiaryDaily::getIsHoliday)
                .count();

        return OvertimeReport.builder()
                .contractId(contract.getId())
                .contractCode(contract.getCode())
                .customerName(contract.getCustomer().getName())
                .vehicleId(vehicleId)
                .driverId(driverId)
                .kmNorm(logContractNormListService.getNormListAtTime(contract, CONTRACT_KM_NORM.code(), timestamp))
                .realWorkingDay(realWorkingDay)
                .workingTimeFrom(workingTimeFrom)
                .workingTimeTo(workingTimeTo)
                .workingDayValuePair(Pair.with(workingDay, workingDayValue))
                .overtime(overtime)
                .overnight(overnight)
                .weekend(weekend)
                .holiday(holiday)
                .overtimeSurcharge(logContractCostTypeService.getCostTypeAtTime(contract, OVERTIME_SURCHARGE.code(), timestamp))
                .overnightSurcharge(logContractCostTypeService.getCostTypeAtTime(contract, OVERNIGHT_SURCHARGE.code(), timestamp))
                .weekendSurcharge(logContractCostTypeService.getCostTypeAtTime(contract, WEEKEND_SURCHARGE.code(), timestamp))
                .holidaySurcharge(logContractCostTypeService.getCostTypeAtTime(contract, HOLIDAY_SURCHARGE.code(), timestamp))
                .build();
    }

}
