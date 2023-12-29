package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.ContractTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.ContractTypeService;
import vn.com.twendie.avis.data.model.ContractType;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheNames = "ContractType")
public class ContractTypeServiceImpl implements ContractTypeService {

    private final ContractTypeRepo contractTypeRepo;

    public ContractTypeServiceImpl(ContractTypeRepo contractTypeRepo) {
        this.contractTypeRepo = contractTypeRepo;
    }

    @Override
    @Cacheable(key = "#p0", condition = "#p0 != null")
    public ContractType findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return contractTypeRepo.findById(id).orElseThrow(() ->
                new NotFoundException("Not found contract_type with id: " + id));
    }

    @Override
    public List<ContractType> findAll() {
        return contractTypeRepo.findAllByDeletedFalse();
    }

}
