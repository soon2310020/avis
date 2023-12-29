package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.ContractCostTypeRepo;
import vn.com.twendie.avis.api.repository.CostTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.CostTypeService;
import vn.com.twendie.avis.api.service.LogContractCostTypeService;
import vn.com.twendie.avis.data.enumtype.ContractCostTypeEnum;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;

@Service
@CacheConfig(cacheNames = "CostType")
public class CostTypeServiceImpl implements CostTypeService {

    private final CostTypeRepo costTypeRepo;
    private final ContractCostTypeRepo contractCostTypeRepo;

    private final CostTypeService costTypeService;

    public CostTypeServiceImpl(CostTypeRepo costTypeRepo,
                               ContractCostTypeRepo contractCostTypeRepo,
                               @Lazy CostTypeService costTypeService) {
        this.costTypeRepo = costTypeRepo;
        this.contractCostTypeRepo = contractCostTypeRepo;
        this.costTypeService = costTypeService;
    }

    @Override
    @Cacheable(key = "'all'")
    public List<CostType> findAll() {
        return costTypeRepo.findAll();
    }

    @Override
    @Cacheable(key = "#p0", condition = "#p0 != null")
    public CostType findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return costTypeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found cost_type with id: " + id));
    }

    @Override
    @Cacheable(key = "#code")
    public CostType findByCode(String code) {
        return costTypeRepo.findByCode(code).orElseThrow(() ->
                new NotFoundException("Not found cost_type with code: " + code));
    }

    @Override
    public List<ContractCostType> getContractCostTypes(Contract contract) {
        try {
            return contract.getContractCostTypes();
        } catch (Exception e) {
            return fetchContractCostTypes(contract);
        }
    }

    @Override
    public BigDecimal getContractCost(Contract contract, String code) {
        return getContractCostTypes(contract).stream()
                .filter(contractCostType -> contractCostType.getId().getCostType().getCode().equals(code))
                .map(ContractCostType::getPrice)
                .findFirst()
                .orElse(ZERO);
    }

    @Override
    public void fetchContractCostTypes(Collection<Contract> contracts) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream().map(Contract::getId).collect(Collectors.toSet());
            List<ContractCostType> contractCostTypes = contractCostTypeRepo.findAllByIdContractIdIn(contractIds);
            contracts.forEach(contract -> contract.setContractCostTypes(
                    contractCostTypes.stream()
                            .filter(contractCostType -> contract.getId().equals(contractCostType.getId().getContractId()))
                            .collect(Collectors.toList())
            ));
        }
    }

    @Override
    public List<ContractCostType> saveAll(Collection<ContractCostType> contractCostTypes) {
        return contractCostTypeRepo.saveAll(contractCostTypes);
    }

    @Override
    public List<ContractCostType> buildContractCostTypes(Contract contract, List<CodeValueModel> costs) {
        List<CostType> costTypes = costTypeService.findAll();
        Set<String> contractCostTypeCodes = ContractCostTypeEnum.codes(
                Objects.requireNonNull(ContractTypeEnum.valueOf(contract.getContractType().getId())));

        return costTypes.stream()
                .filter(costType -> contractCostTypeCodes.contains(costType.getCode()))
                .map(costType -> {
                    BigDecimal value = costs.stream()
                            .filter(cost -> costType.getCode().equals(cost.getCode()))
                            .map(CodeValueModel::getValue)
                            .findAny()
                            .orElse(ZERO);
                    return ContractCostType.builder()
                            .id(ContractCostTypeId.builder()
                                    .contractId(contract.getId())
                                    .costType(costType)
                                    .build())
                            .price(value)
                            .build();
                }).collect(Collectors.toList());
    }

}
