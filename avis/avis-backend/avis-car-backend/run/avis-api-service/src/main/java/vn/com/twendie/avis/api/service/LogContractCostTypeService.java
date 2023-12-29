package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.projection.LogContractCostProjection;
import vn.com.twendie.avis.api.model.projection.PRLogContractCostProjection;
import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.LogContractPriceCostType;
import vn.com.twendie.avis.data.model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface LogContractCostTypeService {

    List<LogContractPriceCostType> saveAll(Collection<LogContractPriceCostType> logContractPriceCostTypes);

    List<LogContractPriceCostType> buildLogContractCostTypes(Contract contract, Timestamp effectiveDate, User createdBy);

    List<LogContractPriceCostType> buildLogContractCostTypes(Contract contract, List<CodeValueModel> contractCosts, Timestamp effectiveDate, User createdBy);

    List<LogContractCostProjection> findAllByContractId(Long id);

    LogContractPriceCostType findClosestLogBeforeDate(Timestamp timestamp, Long contractId, String costTypeCode);

    List<LogContractPriceCostType> findAllByContractIdWithinDate(Long contractId, String costCode, Timestamp from, Timestamp to);

    List<LogContractPriceCostType> getLogContractCostTypes(Contract contract);

    default List<LogContractPriceCostType> getLogContractCostTypes(Contract contract, String code) {
        return getLogContractCostTypes(contract).stream()
                .filter(log -> log.getCostType().getCode().equals(code))
                .collect(Collectors.toList());
    }

    BigDecimal getCostTypeAtTime(Contract contract, String code, Timestamp timestamp);

    default BigDecimal getCostTypeInMonth(Contract contract, String code, Timestamp timestamp) {
        return getCostTypeAtTime(contract, code, new DateUtils().getLastDayOfMonth(timestamp));
    }

    void fetchLogContractCostTypes(Collection<Contract> contracts);

    default Set<LogContractPriceCostType> fetchLogContractCostTypes(Contract contract) {
        fetchLogContractCostTypes(Collections.singleton(contract));
        return contract.getLogContractPriceCostTypes();
    }

    List<PRLogContractCostProjection> findByContractIdAndWithinTimeAndCodeIn(Long contractId, Timestamp from, Timestamp to, List<String> codes);

}
