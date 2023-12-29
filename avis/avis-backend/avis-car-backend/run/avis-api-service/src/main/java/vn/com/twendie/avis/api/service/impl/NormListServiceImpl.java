package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.ContractNormListRepo;
import vn.com.twendie.avis.api.repository.NormListRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.NormListService;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.NormListEnum;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;

@Service
@CacheConfig(cacheNames = "NormList")
public class NormListServiceImpl implements NormListService {

    private final NormListRepo normListRepo;
    private final ContractNormListRepo contractNormListRepo;

    private final NormListService normListService;

    public NormListServiceImpl(NormListRepo normListRepo,
                               ContractNormListRepo contractNormListRepo,
                               @Lazy NormListService normListService) {
        this.normListRepo = normListRepo;
        this.contractNormListRepo = contractNormListRepo;
        this.normListService = normListService;
    }

    @Override
    @Cacheable(key = "'all'")
    public List<NormList> findAll() {
        return normListRepo.findAll();
    }

    @Override
    @Cacheable(key = "#p0", condition = "#p0 != null")
    public NormList findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return normListRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found norm_list with id: " + id));
    }

    @Override
    @Cacheable(key = "#code")
    public NormList findByCode(String code) {
        return normListRepo.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Not found norm_list with code: " + code));
    }

    @Override
    public List<ContractNormList> getContractNormLists(Contract contract) {
        try {
            return contract.getContractNormLists();
        } catch (Exception e) {
            return fetchContractNormLists(contract);
        }
    }

    @Override
    public BigDecimal getContractNorm(Contract contract, String code) {
        return getContractNormLists(contract).stream()
                .filter(contractNormList -> contractNormList.getId().getNormList().getCode().equals(code))
                .map(ContractNormList::getQuota)
                .findFirst()
                .orElse(ZERO);
    }

    @Override
    public void fetchContractNormLists(Collection<Contract> contracts) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream().map(Contract::getId).collect(Collectors.toSet());
            List<ContractNormList> contractNormLists = contractNormListRepo.findAllByIdContractIdIn(contractIds);
            contracts.forEach(contract -> contract.setContractNormLists(
                    contractNormLists.stream()
                            .filter(contractNormList -> contract.getId().equals(contractNormList.getId().getContractId()))
                            .collect(Collectors.toList())
            ));
        }
    }

    @Override
    public List<ContractNormList> saveAll(Collection<ContractNormList> contractNormLists) {
        return contractNormListRepo.saveAll(contractNormLists);
    }

    @Override
    public List<ContractNormList> buildContractNormList(Contract contract, List<CodeValueModel> norms) {
        List<NormList> normLists = normListService.findAll();
        Set<String> normListCodes = NormListEnum.codes(
                Objects.requireNonNull(ContractTypeEnum.valueOf(contract.getContractType().getId())));

        return normLists.stream()
                .filter(normList -> normListCodes.contains(normList.getCode()))
                .map(normList -> {
                    BigDecimal value = norms.stream()
                            .filter(norm -> normList.getCode().equals(norm.getCode()))
                            .map(CodeValueModel::getValue)
                            .findAny()
                            .orElse(ZERO);
                    return ContractNormList.builder()
                            .id(ContractNormListId.builder()
                                    .contractId(contract.getId())
                                    .normList(normList)
                                    .build())
                            .quota(value)
                            .build();
                }).collect(Collectors.toList());
    }

}
