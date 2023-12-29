package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.projection.LogContractNormProjection;
import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.LogContractNormList;
import vn.com.twendie.avis.data.model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface LogContractNormListService {

    List<LogContractNormList> saveAll(Collection<LogContractNormList> logContractNormLists);

    List<LogContractNormList> buildLogContractNormLists(Contract contract, Timestamp effectiveDate, User createdBy);

    List<LogContractNormList> buildLogContractNormLists(Contract contract, List<CodeValueModel> norms, Timestamp effectiveDate, User createdBy);

    List<LogContractNormProjection> findAllByContractId(Long id);

    LogContractNormList findClosestLogBeforeDate(Timestamp timestamp, Long contractId, String normListCode);

    List<LogContractNormList> getLogContractNormLists(Contract contract);

    default List<LogContractNormList> getLogContractNormLists(Contract contract, String code) {
        return getLogContractNormLists(contract).stream()
                .filter(log -> log.getNormList().getCode().equals(code))
                .collect(Collectors.toList());
    }

    BigDecimal getNormListAtTime(Contract contract, String code, Timestamp timestamp);

    default BigDecimal getNormListInMonth(Contract contract, String code, Timestamp timestamp) {
        return getNormListAtTime(contract, code, new DateUtils().getLastDayOfMonth(timestamp));
    }

    void fetchLogContractNormLists(Collection<Contract> contracts);

    default Set<LogContractNormList> fetchLogContractNormLists(Contract contract) {
        fetchLogContractNormLists(Collections.singleton(contract));
        return contract.getLogContractNormLists();
    }
}
