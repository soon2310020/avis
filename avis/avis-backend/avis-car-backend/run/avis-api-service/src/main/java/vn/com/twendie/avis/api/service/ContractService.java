package vn.com.twendie.avis.api.service;

import lombok.SneakyThrows;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import vn.com.twendie.avis.api.model.payload.ContractClonePayload;
import vn.com.twendie.avis.api.model.payload.ContractCodeSuggestionPayload;
import vn.com.twendie.avis.api.model.projection.ContractInfoForNotiProjection;
import vn.com.twendie.avis.api.model.response.ContractCodeDTO;
import vn.com.twendie.avis.api.model.response.ContractDTO;
import vn.com.twendie.avis.api.model.response.ContractStatistic;
import vn.com.twendie.avis.api.model.response.CreateContractOptionsWrapper;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.enumtype.ContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractType;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.*;

public interface ContractService {

    Contract findById(Long id);

    default List<Contract> findContractsInTimeRange(Long contractTypeId, Timestamp from, Timestamp to) {
        return findContractsInTimeRange(Collections.singleton(contractTypeId), from, to);
    }

    List<Contract> findContractsInTimeRange(Collection<Long> contractTypeIds, Timestamp from, Timestamp to);

    CreateContractOptionsWrapper getOptions(Long id);

    ContractStatistic getContractStatistic(ContractTypeEnum contractTypeEnum);

    GeneralPageResponse<ContractCodeDTO> getContractCodeSuggestionByPrefix(ContractCodeSuggestionPayload payload);

    boolean existsCode(String code);

    String validateContractCode(String prefixCode, String suffixCode);

    Contract save(Contract contract);

    boolean cancelContract(Long id, User canceledBy);

    boolean inStatus(Contract contract, ContractStatusEnum... status);

    void assignTerm(Contract contract);

    void assignStatus(Contract contract);

    Boolean deleteContract(Long contractId);

    Boolean existByCustomerIdAndDeletedFalse(Long customerId);

    Boolean existByMemberCustomerIdAndDeletedFalse(Long memberId);

    Boolean existByListMemberCustomerIdAndDeletedFalse(List<Long> memberIds);

    List<ContractInfoForNotiProjection> findNeededInfoForNotiByListMemberId(List<Long> ids);

    List<ContractInfoForNotiProjection> findNeededInfoForNotiByCustomerId(Long id);

    ContractDTO getContractClone(ContractClonePayload payload);

    void handleCreateContract(Contract contract, User driver, Vehicle vehicle,
                              Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets);

    void handleChangeDriver(Contract contract, User oldDriver, User newDriver,
                            Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets);

    void handleChangeVehicle(Contract contract, Vehicle oldVehicle, Vehicle newVehicle,
                             Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets);

    void handleCreateContractWithoutDriver(Contract contract, User driver, Vehicle vehicle,
                                           Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets);

    void handleChangeDriverNoLending(Contract contract, User oldDriver, User newDriver,
                                     Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets);

    void handleChangeVehicleNoLending(Contract contract, Vehicle oldVehicle, Vehicle newVehicle,
                                      Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> changeQuartets);

    void retrieveDriver(Contract contract);

    void releaseDriver(Contract contract);

    void retrieveVehicle(Contract contract);

    void releaseVehicle(Contract contract);

    Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> cleanQuartets(
            Set<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets);

    List<Contract> findAllByContractTypeAndAssignedVehicle(Long typeId);

    @SneakyThrows
    <T> T getContractValueAtTime(Contract contract, String field, Timestamp timestamp, Class<T> clazz);

    default Pair<Time, Time> getWorkingTimeNormalDay(Contract contract, Timestamp timestamp) {
        return Pair.with(getContractValueAtTime(contract, WORKING_TIME_FROM.getName(), timestamp, Time.class),
                getContractValueAtTime(contract, WORKING_TIME_TO.getName(), timestamp, Time.class));
    }

    default Pair<Time, Time> getWorkingTimeWeekendHoliday(Contract contract, Timestamp timestamp) {
        return Pair.with(getContractValueAtTime(contract, WORKING_TIME_WEEKEND_HOLIDAY_FROM.getName(), timestamp, Time.class),
                getContractValueAtTime(contract, WORKING_TIME_WEEKEND_HOLIDAY_TO.getName(), timestamp, Time.class));
    }

    default Pair<Time, Time> getWorkingTime(Contract contract, Timestamp timestamp, boolean isWeekendHoliday) {
        if (isWeekendHoliday) {
            return getWorkingTimeWeekendHoliday(contract, timestamp);
        } else {
            return getWorkingTimeNormalDay(contract, timestamp);
        }
    }

    void validContractType(ContractTypeEnum contractTypeEnum, ContractType contractType);

    default void fetchFirstJourneyDiaryDate(Contract contract) {
        fetchFirstJourneyDiaryDate(Collections.singleton(contract));
    }

    void fetchFirstJourneyDiaryDate(Collection<Contract> contracts);
}
