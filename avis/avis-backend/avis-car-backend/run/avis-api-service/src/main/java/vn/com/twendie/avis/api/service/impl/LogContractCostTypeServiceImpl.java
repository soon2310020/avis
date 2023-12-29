package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.projection.LogContractCostProjection;
import vn.com.twendie.avis.api.model.projection.PRLogContractCostProjection;
import vn.com.twendie.avis.api.repository.ContractCostTypeRepo;
import vn.com.twendie.avis.api.repository.LogContractCostTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.CostTypeService;
import vn.com.twendie.avis.api.service.LogContractCostTypeService;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

@Service
public class LogContractCostTypeServiceImpl implements LogContractCostTypeService {

    private final ContractCostTypeRepo contractCostTypeRepo;
    private final LogContractCostTypeRepo logContractCostTypeRepo;

    private final CostTypeService costTypeService;

    private final DateUtils dateUtils;

    public LogContractCostTypeServiceImpl(ContractCostTypeRepo contractCostTypeRepo,
                                          LogContractCostTypeRepo logContractCostTypeRepo,
                                          CostTypeService costTypeService,
                                          DateUtils dateUtils) {
        this.contractCostTypeRepo = contractCostTypeRepo;
        this.logContractCostTypeRepo = logContractCostTypeRepo;
        this.costTypeService = costTypeService;
        this.dateUtils = dateUtils;
    }

    @Override
    public List<LogContractPriceCostType> saveAll(Collection<LogContractPriceCostType> logContractPriceCostTypes) {
        return logContractCostTypeRepo.saveAll(logContractPriceCostTypes);
    }

    @Override
    public List<LogContractPriceCostType> buildLogContractCostTypes(Contract contract, Timestamp effectiveDate, User createdBy) {
        return costTypeService.getContractCostTypes(contract).stream()
                .map(contractCostType -> LogContractPriceCostType.builder()
                        .contract(contract)
                        .costType(contractCostType.getId().getCostType())
                        .price(contractCostType.getPrice())
                        .fromDate(effectiveDate)
                        .createdBy(createdBy)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LogContractPriceCostType> buildLogContractCostTypes(Contract contract, List<CodeValueModel> costs, Timestamp effectiveDate, User createdBy) {
        List<ContractCostType> contractCostTypes = new ArrayList<>();
        List<LogContractPriceCostType> logContractCostTypes = costTypeService
                .getContractCostTypes(contract).stream()
                .map(contractCostType -> {
                    CodeValueModel codeValue = costs.stream()
                            .filter(cost -> contractCostType.getId().getCostType().getCode().equals(cost.getCode()))
                            .filter(cost -> contractCostType.getPrice().compareTo(cost.getValue().setScale(0, HALF_UP)) != 0)
                            .findAny()
                            .orElse(null);
                    if (Objects.nonNull(codeValue) && effectiveDate.before(dateUtils.now())) {
                        contractCostType.setPrice(codeValue.getValue());
                        contractCostTypes.add(contractCostType);
                    }
                    return Objects.isNull(codeValue) ? null : LogContractPriceCostType.builder()
                            .contract(contract)
                            .costType(contractCostType.getId().getCostType())
                            .price(codeValue.getValue())
                            .fromDate(effectiveDate)
                            .createdBy(createdBy)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!contractCostTypes.isEmpty()) {
            contractCostTypeRepo.saveAll(contractCostTypes);
        }
        return logContractCostTypes;
    }

    @Override
    public List<LogContractCostProjection> findAllByContractId(Long id) {
        return logContractCostTypeRepo.findAllByContractId(id);
    }

    @Override
    public LogContractPriceCostType findClosestLogBeforeDate(Timestamp timestamp, Long contractId, String costTypeCode) {
        LogContractPriceCostType logContractPriceCostType = logContractCostTypeRepo
                .findFirstByContractIdAndCostTypeCodeAndFromDateBeforeAndDeletedFalseOrderByFromDateDescIdDesc(
                        contractId, costTypeCode, timestamp);
        if (Objects.isNull(logContractPriceCostType)) {
            throw new NotFoundException("Cannot find log with contract id:" + contractId
                    + " and timestamp:" + timestamp.getTime() + " and costCode: " + costTypeCode);
        }
        return logContractPriceCostType;
    }

    @Override
    public List<LogContractPriceCostType> findAllByContractIdWithinDate(Long contractId, String costCode, Timestamp from, Timestamp to) {
        return logContractCostTypeRepo.findByContractAndDateWithin(contractId, costCode, from, to);
    }

    @Override
    public List<LogContractPriceCostType> getLogContractCostTypes(Contract contract) {
        Set<LogContractPriceCostType> logContractCostTypes;
        try {
            logContractCostTypes = contract.getLogContractPriceCostTypes();
        } catch (Exception e) {
            logContractCostTypes = fetchLogContractCostTypes(contract);
        }
        return logContractCostTypes.stream()
                .sorted((log1, log2) -> {
                    if (!log1.getFromDate().equals(log2.getFromDate())) {
                        return -Long.compare(log1.getFromDate().getTime(), log2.getFromDate().getTime());
                    } else {
                        return -Long.compare(log1.getCreatedAt().getTime(), log2.getCreatedAt().getTime());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "ContractCostAtTime", key = "#contract.id + '::' + #code + '::' + " +
            "new vn.com.twendie.avis.api.core.util.DateUtils().getDate(#timestamp)",
            condition = "new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday().after(#timestamp)")
    public BigDecimal getCostTypeAtTime(Contract contract, String code, Timestamp timestamp) {
        List<LogContractPriceCostType> logContractCostTypes = getLogContractCostTypes(contract, code);
        LogContractPriceCostType logContractCostType = logContractCostTypes.stream()
                .filter(log -> !log.getFromDate().after(dateUtils.endOfDay(timestamp)))
                .findFirst()
                .orElse(logContractCostTypes.stream()
                        .findFirst()
                        .orElse(null));
        return Objects.nonNull(logContractCostType) ? logContractCostType.getPrice() :
                costTypeService.getContractCost(contract, code);
    }

    @Override
    public void fetchLogContractCostTypes(Collection<Contract> contracts) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream()
                    .map(Contract::getId)
                    .collect(Collectors.toSet());
            List<LogContractPriceCostType> logContractCostTypes = logContractCostTypeRepo.findAllByContractIdIn(contractIds);
            contracts.forEach(contract -> contract.setLogContractPriceCostTypes(
                    logContractCostTypes.stream()
                            .filter(logContractCostType -> contract.getId().equals(logContractCostType.getContractId()))
                            .collect(Collectors.toSet())
            ));
        }
    }

    @Override
    public List<PRLogContractCostProjection> findByContractIdAndWithinTimeAndCodeIn(Long contractId,
                                                                                    Timestamp from,
                                                                                    Timestamp to,
                                                                                    List<String> codes) {
        return logContractCostTypeRepo.findByContractIdAndWithinTimeAndCodeIn(contractId, from, to, codes);
    }

}
