package vn.com.twendie.avis.api.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.model.response.BranchDTO;
import vn.com.twendie.avis.api.repository.BranchRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.BranchService;
import vn.com.twendie.avis.api.service.ContractChangeHistoryService;
import vn.com.twendie.avis.data.model.Branch;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractChangeHistory;
import vn.com.twendie.avis.locale.config.Translator;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.BRANCH_ID;

@Service
@CacheConfig(cacheNames = "Branch")
public class BranchServiceImpl implements BranchService {

    private final BranchRepo branchRepo;

    private final ContractChangeHistoryService contractChangeHistoryService;

    private final ModelMapper modelMapper;

    public BranchServiceImpl(BranchRepo branchRepo,
                             @Lazy ContractChangeHistoryService contractChangeHistoryService,
                             ModelMapper modelMapper) {
        this.branchRepo = branchRepo;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Cacheable(key = "#code")
    public Branch findByCode(String code) {
        return branchRepo.findByCode(code).orElseThrow(() ->
                new NotFoundException("Not found branch with code: " + code));
    }

    @Override
    public List<BranchDTO> getBranchInfos() {
        List<Branch> branches = branchRepo.findAllByDeletedFalse();
        return branches.stream().map(entity -> modelMapper.map(entity, BranchDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "#id")
    public Branch findByIdIgnoreDelete(Long id) {
        return branchRepo.findById(id)
                .orElseThrow(() -> new BadRequestException("Wrong Branch Id!!!"));
    }

    @Override
    @Cacheable(key = "#root.method.name + #id")
    public Branch findById(Long id) {
        return branchRepo.findByIdAndDeletedFalse(id).orElseThrow(() ->
                new NotFoundException("Not found branch with id: " + id)
                        .displayMessage(Translator.toLocale("branch.error.not_found")));
    }

    @Override
    public Branch getContractBranchAtTime(Contract contract, Timestamp timestamp) {
        ContractChangeHistory history = contractChangeHistoryService
                .findLastChangeOfField(contract, BRANCH_ID.getName(), timestamp);
        if (Objects.nonNull(history)) {
            if (!timestamp.before(history.getFromDate())) {
                return findByIdIgnoreDelete(Long.parseLong(history.getNewValue()));
            } else if (Objects.nonNull(history.getOldValue())) {
                return findByIdIgnoreDelete(Long.parseLong(history.getOldValue()));
            } else {
                return null;
            }
        } else {
            return contract.getBranch();
        }
    }

}
