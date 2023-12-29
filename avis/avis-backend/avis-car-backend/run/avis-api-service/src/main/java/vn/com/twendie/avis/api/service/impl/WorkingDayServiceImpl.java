package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.repository.WorkingDayRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.ContractChangeHistoryService;
import vn.com.twendie.avis.api.service.WorkingCalendarService;
import vn.com.twendie.avis.api.service.WorkingDayService;
import vn.com.twendie.avis.data.enumtype.WorkingDayEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractChangeHistory;
import vn.com.twendie.avis.data.model.WorkingDay;

import java.sql.Timestamp;
import java.util.Objects;

import static java.time.DayOfWeek.SUNDAY;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.WORKING_DAY_ID;

@Service
@CacheConfig(cacheNames = "WorkingDay")
public class WorkingDayServiceImpl implements WorkingDayService {

    private final WorkingDayRepo workingDayRepo;

    private final WorkingDayService workingDayService;
    private final WorkingCalendarService workingCalendarService;
    private final ContractChangeHistoryService contractChangeHistoryService;

    private final DateUtils dateUtils;

    public WorkingDayServiceImpl(WorkingDayRepo workingDayRepo,
                                 @Lazy WorkingDayService workingDayService,
                                 WorkingCalendarService workingCalendarService,
                                 @Lazy ContractChangeHistoryService contractChangeHistoryService,
                                 DateUtils dateUtils) {
        this.workingDayRepo = workingDayRepo;
        this.workingDayService = workingDayService;
        this.workingCalendarService = workingCalendarService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.dateUtils = dateUtils;
    }

    @Override
    @Cacheable(key = "#code")
    public WorkingDay findByCode(String code) {
        return workingDayRepo.findByCode(code).orElseThrow(() ->
                new NotFoundException("Not found working_day with code: " + code));
    }

    @Override
    @Cacheable(key = "#id")
    public WorkingDay findByIdIgnoreDelete(Long id) {
        return workingDayRepo.findById(id).orElseThrow(() ->
                new NotFoundException("Not found by id: " + id));
    }

    @Override
    @Cacheable(cacheNames = "ContractWorkingDayAtTime", key = "#contract.id + '::' + " +
            "new vn.com.twendie.avis.api.core.util.DateUtils().getDate(#timestamp)",
            condition = "new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday().after(#timestamp)")
    public WorkingDay getContractWorkingDayAtTime(Contract contract, Timestamp timestamp) {
        ContractChangeHistory history = contractChangeHistoryService
                .findLastChangeOfField(contract, WORKING_DAY_ID.getName(), timestamp);
        if (Objects.nonNull(history)) {
            if (!timestamp.before(history.getFromDate())) {
                return workingDayService.findByIdIgnoreDelete(Long.parseLong(history.getNewValue()));
            } else if (Objects.nonNull(history.getOldValue())) {
                return workingDayService.findByIdIgnoreDelete(Long.parseLong(history.getOldValue()));
            }
        }
        return contract.getWorkingDay();
    }

    @Override
    public long countContractWorkingDays(WorkingDay workingDay, Integer workingDayValue, Timestamp month) {
        switch (WorkingDayEnum.valueOf(workingDay.getId())) {
            case FLEXIBLE:
                return workingDayValue;
            case MON_TO_SAT_PLUS_1_SUN:
                return dateUtils.getDatesBetween(dateUtils.getFirstDayOfMonth(month), dateUtils.getLastDayOfMonth(month))
                        .stream()
                        .filter(date -> !dateUtils.getDayOfWeek(date).equals(SUNDAY))
                        .count() + 1;
            case MON_TO_SAT_PLUS_2_SUN:
                return dateUtils.getDatesBetween(dateUtils.getFirstDayOfMonth(month), dateUtils.getLastDayOfMonth(month))
                        .stream()
                        .filter(date -> !dateUtils.getDayOfWeek(date).equals(SUNDAY))
                        .count() + 2;
            default:
                return workingCalendarService.countWorkingDay(
                        dateUtils.getFirstDayOfMonth(month), dateUtils.getLastDayOfMonth(month), workingDay.getId());
        }
    }

}
