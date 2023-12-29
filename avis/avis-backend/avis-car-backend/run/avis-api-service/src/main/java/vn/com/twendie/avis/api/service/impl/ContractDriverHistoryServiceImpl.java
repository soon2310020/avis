package vn.com.twendie.avis.api.service.impl;

import org.javatuples.Quartet;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.repository.ContractDriverHistoryRepo;
import vn.com.twendie.avis.api.service.ContractDriverHistoryService;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractDriverHistory;
import vn.com.twendie.avis.data.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum.*;

@Service
public class ContractDriverHistoryServiceImpl implements ContractDriverHistoryService {

    private final ContractDriverHistoryRepo contractDriverHistoryRepo;

    private final DateUtils dateUtils;

    public ContractDriverHistoryServiceImpl(ContractDriverHistoryRepo contractDriverHistoryRepo,
                                            DateUtils dateUtils) {
        this.contractDriverHistoryRepo = contractDriverHistoryRepo;
        this.dateUtils = dateUtils;
    }

    @Override
    public void save(ContractDriverHistory contractDriverHistory) {
        contractDriverHistoryRepo.saveAndFlush(contractDriverHistory);
    }

    @Override
    public void saveAll(Collection<ContractDriverHistory> contractDriverHistories) {
        if (!CollectionUtils.isEmpty(contractDriverHistories)) {
            contractDriverHistoryRepo.saveAll(contractDriverHistories);
        }
    }

    @Override
    public ContractDriverHistory continueContract(Contract contract, User driver) {
        ContractDriverHistory contractDriverHistory = ContractDriverHistory.builder()
                .contract(contract)
                .driver(driver)
                .fromDate(dateUtils.now())
                .status(CONTINUE.code())
                .build();
        return contractDriverHistoryRepo.save(contractDriverHistory);
    }

    @Override
    public ContractDriverHistory pauseContract(Contract contract, User driver) {
        ContractDriverHistory contractDriverHistory = ContractDriverHistory.builder()
                .contract(contract)
                .driver(driver)
                .fromDate(dateUtils.now())
                .status(POSTPONE.code())
                .build();
        return contractDriverHistoryRepo.saveAndFlush(contractDriverHistory);
    }

    @Override
    public ContractDriverHistory buildContractDriverHistory(Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum> quartet) {
        return ContractDriverHistory.builder()
                .contract(quartet.getValue0())
                .driver(quartet.getValue1())
                .fromDate(dateUtils.now())
                .status(quartet.getValue2().code())
                .build();
    }

    @Override
    public List<ContractDriverHistory> buildContractDriverHistories(Collection<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets) {
        return quartets.stream()
                .filter(quartet -> Objects.nonNull(quartet.getValue2()))
                .map(this::buildContractDriverHistory)
                .collect(Collectors.toList());
    }

}
