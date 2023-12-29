package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.payload.CreateOrUpdateBlankDiaryPayload;
import vn.com.twendie.avis.api.model.payload.EditAppDiaryDailyPayload;
import vn.com.twendie.avis.api.model.payload.UpdateWithoutDriverDiaryPayload;
import vn.com.twendie.avis.api.model.projection.JDDVehicleInfoProjection;
import vn.com.twendie.avis.api.model.projection.OvertimeInfo;
import vn.com.twendie.avis.api.model.response.DateStatistic;
import vn.com.twendie.avis.api.model.response.JDDFilterOptionsWrapper;
import vn.com.twendie.avis.api.model.response.JourneyDiaryDailyDTO;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.data.model.WorkingDay;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface JourneyDiaryDailyService {

    List<JourneyDiaryDaily> findAll();

    List<JourneyDiaryDaily> find(Long contractId, Timestamp fromDate, Timestamp toDate);

    List<JourneyDiaryDaily> findAndName(String name, Long contractId, Timestamp fromDate,
                                        Timestamp toDate, List<Long> memberCustomerId);

    List<JourneyDiaryDaily> findNameInMonth(String name, Long contractId, Timestamp month, List<Long> memberCustomerIds);

    List<JourneyDiaryDaily> findInMonth(Long contractId, Timestamp month);

    default List<JDDVehicleInfoProjection> findJDDTicketFeeInfo(Timestamp fromDate, Timestamp toDate, String branchCode) {
        return findJDDTicketFeeInfo(fromDate, toDate, branchCode, null);
    }

    List<JDDVehicleInfoProjection> findJDDTicketFeeInfo(Timestamp fromDate, Timestamp toDate, String branchCode,
                                                        Collection<String> vehicleNumberPlates);

    JourneyDiaryDaily findById(Long id);

    void fetchChildren(Collection<JourneyDiaryDaily> journeyDiaryDailies);

    default List<JourneyDiaryDaily> fetchChildren(JourneyDiaryDaily journeyDiaryDaily) {
        fetchChildren(Collections.singleton(journeyDiaryDaily));
        return journeyDiaryDaily.getChildren();
    }

    Boolean editAppDiary(EditAppDiaryDailyPayload payload);

    Boolean editManuallyCreatedDiary(CreateOrUpdateBlankDiaryPayload payload);

    Boolean delete(Long jddId);

    Boolean create(CreateOrUpdateBlankDiaryPayload wrapper);

    JourneyDiaryDaily createEmptyJourneyDiaryDaily(Contract contract, Timestamp date);

    default List<JourneyDiaryDaily> createEmptyJourneyDiaryDailies(Contract contract, Collection<Timestamp> dates) {
        return dates.stream()
                .map(date -> createEmptyJourneyDiaryDaily(contract, date))
                .collect(Collectors.toList());
    }

    List<JourneyDiaryDaily> createEmptyJourneyDiaryDailies(Contract contract);

    Boolean editWithoutDriverDiary(UpdateWithoutDriverDiaryPayload payload);

    Timestamp getEndDate(Contract contract, Timestamp toDate);

    Timestamp getStartDate(Contract contract, Timestamp fromDate);

    List<JourneyDiaryDaily> find(Long journeyDiaryId);

    JourneyDiaryDaily save(JourneyDiaryDaily journeyDiaryDaily);

    List<JourneyDiaryDaily> saveAll(Collection<JourneyDiaryDaily> journeyDiaryDailies);

    JourneyDiaryDaily updateSelfDrive(JourneyDiaryDaily journeyDiaryDaily);

    JourneyDiaryDaily updateOverTime(JourneyDiaryDaily journeyDiaryDaily, boolean needUpdateParent);

    JourneyDiaryDaily updateOverTime(JourneyDiaryDaily journeyDiaryDaily, Contract contract, boolean needUpdateParent);

    JourneyDiaryDaily updateOverKm(JourneyDiaryDaily journeyDiaryDaily, boolean needUpdateParent);

    JourneyDiaryDaily updateOverKm(JourneyDiaryDaily journeyDiaryDaily, Contract contract, boolean needUpdateParent);

    JourneyDiaryDaily updateKmNumber(JourneyDiaryDaily journeyDiaryDaily, boolean needUpdateParent);

    JourneyDiaryDaily updateCosts(JourneyDiaryDaily journeyDiaryDaily);

    JourneyDiaryDaily updateOvernight(JourneyDiaryDaily journeyDiaryDaily);

    JourneyDiaryDaily updateWorkingCalendar(JourneyDiaryDaily journeyDiaryDaily);

    JourneyDiaryDaily updateWorkingCalendar(JourneyDiaryDaily journeyDiaryDaily, WorkingDay workingDay);

    DateStatistic getDateStatistic(Contract contract, List<JourneyDiaryDaily> journeyDiaryDailies, Timestamp from, Timestamp to);

    BigDecimal calculateTotalOverKm(Contract contract, List<JourneyDiaryDaily> journeyDiaryDailies, Timestamp from, Timestamp to);

    void fetchJourneyDiaryDailies(Collection<Contract> contracts, Timestamp from, Timestamp to);

    Map<Long, OvertimeInfo> findOvertimeInfos(Collection<Long> contractIds, Timestamp from, Timestamp to);

    JDDFilterOptionsWrapper getFilterOptions();

    void fetchDataSignature(List<JourneyDiaryDailyDTO> journeyDiaryDailyDTOS);

    void fixTimeGPS();

    void fixOverKM();
}
