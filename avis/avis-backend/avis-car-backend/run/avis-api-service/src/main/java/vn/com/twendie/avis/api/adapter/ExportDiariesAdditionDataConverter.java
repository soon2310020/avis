package vn.com.twendie.avis.api.adapter;

import lombok.AllArgsConstructor;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.response.DateStatistic;
import vn.com.twendie.avis.api.model.response.ExportDiariesAdditionDataWrapper;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.enumtype.ContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.model.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static vn.com.twendie.avis.api.core.util.DateUtils.HOUR_SHORT_PATTERN;
import static vn.com.twendie.avis.api.core.util.DateUtils.UTC_TIME_ZONE;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;

@AllArgsConstructor
public class ExportDiariesAdditionDataConverter implements Function<Contract, ExportDiariesAdditionDataWrapper> {

    private Timestamp from;
    private Timestamp to;
    private Timestamp endTime;
    private ContractChangeHistoryService contractChangeHistoryService;
    private WorkingCalendarService workingCalendarService;
    private WorkingDayService workingDayService;
    private UserService userService;
    private VehicleService vehicleService;

    private DateUtils dateUtils;
    private List<JourneyDiaryDaily> journeyDiaryDailies;
    private MemberCustomerService memberCustomerService;
    private List<Long> memberCustomerIds;
    @Override
    public ExportDiariesAdditionDataWrapper apply(Contract contract) {
        MemberCustomer memberCustomer = contract.getMemberCustomer();
        int contractPeriod = dateUtils.getMonthsBetween(contract.getFromDatetime(), contract.getToDatetime());

        DateStatistic dateStatistic = DateStatistic.builder()
                .datesOfMonth(dateUtils.getMonthDays(from))
                .build();

        User driver = userService.getContractDriverAtTime(contract, endTime);
        Vehicle vehicle = vehicleService.getContractVehicleAtTime(contract, endTime);

        ExportDiariesAdditionDataWrapper wrapper = ExportDiariesAdditionDataWrapper
                .builder()
                .diariesFrom(from)
                .diariesTo(to)
                .branchName(Objects.isNull(contract.getBranch()) ? "" : contract.getBranch().getName())
                .vehicleWorkingArea(contract.getVehicleWorkingArea())
                .contractCode(contract.getCode())
                .customerName(Objects.isNull(contract.getCustomer()) ? "" : contract.getCustomer().getName())
                .adminName(Objects.isNull(memberCustomer) ? "" : memberCustomer.getRole()
                        .equals(MemberCustomerRoleEnum.ADMIN.getCode()) ? memberCustomer.getName() : "")
                .contractStatus(ContractStatusEnum.valueOf(contract.getStatus()).getName())
                .contractFromDate(contract.getFromDatetime())
                .contractToDate(contract.getToDatetime())
                .contractPeriod(String.format("%d tháng", contractPeriod))
                .driverName(Objects.nonNull(driver) ? driver.getName() : null)
                .vehicleNumberPlate(Objects.nonNull(vehicle) ? vehicle.getNumberPlate() : null)
                .build();

        if (ContractTypeEnum.WITH_DRIVER.value().equals(contract.getContractType().getId())) {
            long realWorkingDays = journeyDiaryDailies.stream()
                    .filter(journeyDiaryDaily -> !journeyDiaryDaily.getIsSelfDrive())
                    .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getUsedKm()))
                    .count();

            WorkingDay workingDay = contract.getWorkingDay();
            String workingDayHistoryValue = contractChangeHistoryService
                    .findLastChangeOfField(contract.getId(), WORKING_DAY_ID.getId(), dateUtils.getTomorrow(endTime));
            if (Objects.nonNull(workingDayHistoryValue)) {
                workingDay = workingDayService
                        .findByIdIgnoreDelete(Long.parseLong(workingDayHistoryValue));
            }

            String workingTimeFrom = getLastValue(contract.getId(), contract.getWorkingTimeFrom(),
                    WORKING_TIME_FROM.getId(), endTime);

            String workingTimeTo = getLastValue(contract.getId(), contract.getWorkingTimeTo(),
                    WORKING_TIME_TO.getId(), endTime);

            String workingTimeHolidayFrom = getLastValue(contract.getId(), contract.getWorkingTimeWeekendHolidayFrom(),
                    WORKING_TIME_WEEKEND_HOLIDAY_FROM.getId(), endTime);

            String workingTimeHolidayTo = getLastValue(contract.getId(), contract.getWorkingTimeWeekendHolidayTo(),
                    WORKING_TIME_WEEKEND_HOLIDAY_TO.getId(), endTime);

            dateStatistic.setContractWorkingDays(workingCalendarService.countWorkingDay(
                    dateUtils.getFirstDayOfMonth(from), dateUtils.getLastDayOfMonth(from), workingDay.getId()));
            dateStatistic.setRealWorkingDays(realWorkingDays);

            wrapper.setWorkingTime(workingTimeFrom + " - " + workingTimeTo);
            wrapper.setWorkingTimeHoliday(workingTimeHolidayFrom + " - " + workingTimeHolidayTo);
        }

        if(memberCustomerIds != null && memberCustomerIds.size() > 0){
            wrapper.setNameFinds(memberCustomerService.getNameByIdIn(memberCustomerIds));
        }

        wrapper.setDateStatistic(dateStatistic);

        return wrapper;
    }

    private String getLastValue(Long contractId, Time time, Long id, Timestamp to) {
        String historyValue = contractChangeHistoryService.findLastChangeOfField(contractId, id,
                dateUtils.getTomorrow(to));

        return Objects.isNull(historyValue) ? dateUtils.format(time, HOUR_SHORT_PATTERN, UTC_TIME_ZONE)
                : dateUtils.dateWithTimeZone(historyValue, HOUR_SHORT_PATTERN, HOUR_SHORT_PATTERN, UTC_TIME_ZONE);
    }
}
