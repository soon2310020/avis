package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.projection.LogContractNormProjection;
import vn.com.twendie.avis.api.repository.ContractNormListRepo;
import vn.com.twendie.avis.api.repository.LogContractNormListRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.LogContractNormListService;
import vn.com.twendie.avis.api.service.NormListService;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

@Service
public class LogContractNormListServiceImpl implements LogContractNormListService {

    private final ContractNormListRepo contractNormListRepo;
    private final LogContractNormListRepo logContractNormListRepo;

    private final NormListService normListService;

    private final DateUtils dateUtils;

    public LogContractNormListServiceImpl(ContractNormListRepo contractNormListRepo,
                                          LogContractNormListRepo logContractNormListRepo,
                                          NormListService normListService,
                                          DateUtils dateUtils) {
        this.contractNormListRepo = contractNormListRepo;
        this.logContractNormListRepo = logContractNormListRepo;
        this.normListService = normListService;
        this.dateUtils = dateUtils;
    }


    @Override
    public List<LogContractNormList> saveAll(Collection<LogContractNormList> logContractNormLists) {
        return logContractNormListRepo.saveAll(logContractNormLists);
    }

    @Override
    public List<LogContractNormList> buildLogContractNormLists(Contract contract, Timestamp effectiveDate, User createdBy) {
        return normListService.getContractNormLists(contract).stream()
                .map(contractNormList -> LogContractNormList.builder()
                        .contract(contract)
                        .normList(contractNormList.getId().getNormList())
                        .quota(contractNormList.getQuota())
                        .fromDate(effectiveDate)
                        .createdBy(createdBy)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LogContractNormList> buildLogContractNormLists(Contract contract, List<CodeValueModel> norms, Timestamp effectiveDate, User createdBy) {
        List<ContractNormList> contractNormLists = new ArrayList<>();
        List<LogContractNormList> logContractNormLists = normListService
                .getContractNormLists(contract)
                .stream()
                .map(contractNormList -> {
                    CodeValueModel codeValue = norms.stream()
                            .filter(norm -> contractNormList.getId().getNormList().getCode().equals(norm.getCode()))
                            .filter(norm -> contractNormList.getQuota().compareTo(norm.getValue().setScale(1, HALF_UP)) != 0)
                            .findAny()
                            .orElse(null);
                    if (Objects.nonNull(codeValue) && effectiveDate.before(dateUtils.now())) {
                        contractNormList.setQuota(codeValue.getValue());
                        contractNormLists.add(contractNormList);
                    }
                    return Objects.isNull(codeValue) ? null : LogContractNormList.builder()
                            .contract(contract)
                            .normList(contractNormList.getId().getNormList())
                            .quota(codeValue.getValue())
                            .fromDate(effectiveDate)
                            .createdBy(createdBy)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!contractNormLists.isEmpty()) {
            contractNormListRepo.saveAll(contractNormLists);
        }
        return logContractNormLists;
    }

    @Override
    public List<LogContractNormProjection> findAllByContractId(Long id) {
        return logContractNormListRepo.findAllByContractId(id);
    }

    @Override
    public LogContractNormList findClosestLogBeforeDate(Timestamp timestamp, Long contractId, String normListCode) {
        LogContractNormList logContractNormList = logContractNormListRepo
                .findFirstByContractIdAndNormListCodeAndFromDateBeforeAndDeletedFalseOrderByFromDateDescIdDesc(
                        contractId, normListCode, timestamp);
        if (Objects.isNull(logContractNormList)) {
            throw new NotFoundException("Cannot find log with contract id:" + contractId
                    + " and timestamp:" + timestamp.getTime() + " and normListCode: " + normListCode);
        }
        return logContractNormList;
    }

    @Override
    public List<LogContractNormList> getLogContractNormLists(Contract contract) {
        Set<LogContractNormList> logContractNormLists;
        try {
            logContractNormLists = contract.getLogContractNormLists();
        } catch (Exception e) {
            logContractNormLists = fetchLogContractNormLists(contract);
        }
        return logContractNormLists.stream()
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
    @Cacheable(cacheNames = "ContractNormAtTime", key = "#contract.id + '::' + #code + '::' + " +
            "new vn.com.twendie.avis.api.core.util.DateUtils().getDate(#timestamp)",
            condition = "new vn.com.twendie.avis.api.core.util.DateUtils().startOfToday().after(#timestamp)")
    public BigDecimal getNormListAtTime(Contract contract, String code, Timestamp timestamp) {
        List<LogContractNormList> logContractNormLists = getLogContractNormLists(contract, code);
        LogContractNormList logContractNormList = logContractNormLists.stream()
                .filter(log -> !log.getFromDate().after(dateUtils.endOfDay(timestamp)))
                .findFirst()
                .orElse(logContractNormLists.stream()
                        .findFirst()
                        .orElse(null));
        return Objects.nonNull(logContractNormList) ? logContractNormList.getQuota() :
                normListService.getContractNorm(contract, code);
    }

    @Override
    public void fetchLogContractNormLists(Collection<Contract> contracts) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream()
                    .map(Contract::getId)
                    .collect(Collectors.toSet());
            List<LogContractNormList> logContractNormLists = logContractNormListRepo.findAllByContractIdIn(contractIds);
            contracts.forEach(contract -> contract.setLogContractNormLists(
                    logContractNormLists.stream()
                            .filter(logContractNormList -> contract.getId().equals(logContractNormList.getContractId()))
                            .collect(Collectors.toSet())
            ));
        }
    }
}
