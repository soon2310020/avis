package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.response.ContractChangeHistoryItemDTO;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractChangeHistory;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ContractChangeHistoryService {

    ContractChangeHistory save(ContractChangeHistory contractChangeHistory);

    List<ContractChangeHistory> saveAll(Collection<ContractChangeHistory> contractChangeHistories);

    List<ContractChangeHistory> buildContractChangeHistories(Contract oldContract, Contract newContract,
                                                             Timestamp fromDate, User createdBy);

    ContractChangeHistory buildContractChangeDriverHistory(Contract contract, User oldDriver, User newDriver,
                                                           Timestamp fromDate, User createdBy);

    ContractChangeHistory buildContractChangeVehicleHistory(Contract contract, Vehicle oldVehicle, Vehicle newVehicle,
                                                            Timestamp fromDate, User createdBy);

    GeneralPageResponse<ContractChangeHistoryItemDTO> findByContractId(Long contractId, int page);

    String findLastChangeOfField(Long contractId, Long fieldId, Timestamp timestamp);

    default List<ContractChangeHistory> getContractChangeHistories(Contract contract) {
        Set<ContractChangeHistory> contractChangeHistories;
        try {
            contractChangeHistories = contract.getContractChangeHistories();
        } catch (Exception e) {
            contractChangeHistories = fetchContractChangeHistories(contract);
        }
        return contractChangeHistories.stream()
                .sorted((history1, history2) -> {
                    if (!history1.getFromDate().equals(history2.getFromDate())) {
                        return -Long.compare(history1.getFromDate().getTime(), history2.getFromDate().getTime());
                    } else {
                        return -Long.compare(history1.getCreatedAt().getTime(), history2.getCreatedAt().getTime());
                    }
                })
                .collect(Collectors.toList());
    }

    default List<ContractChangeHistory> getContractChangeHistories(Contract contract, String field) {
        return getContractChangeHistories(contract).stream()
                .filter(history -> history.getMappingFieldCodeFontend().getFieldName().equals(field))
                .collect(Collectors.toList());
    }

    ContractChangeHistory findLastChangeOfField(Contract contract, String field, Timestamp timestamp);

    default ContractChangeHistory findLastChangeOfFieldInMonth(Contract contract, String field, Timestamp timestamp) {
        return findLastChangeOfField(contract, field, new DateUtils().getLastDayOfMonth(timestamp));
    }

    void fetchContractChangeHistories(Collection<Contract> contracts);

    default Set<ContractChangeHistory> fetchContractChangeHistories(Contract contract) {
        fetchContractChangeHistories(Collections.singleton(contract));
        return contract.getContractChangeHistories();
    }

    void validNewValueOfHistory(Contract contract, ContractChangeHistory history, Timestamp toDate);
}
