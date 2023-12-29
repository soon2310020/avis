package vn.com.twendie.avis.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.ContractPeriodTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.ContractPeriodTypeService;
import vn.com.twendie.avis.data.model.ContractPeriodType;

@Service
@CacheConfig(cacheNames = "ContractPeriodType")
public class ContractPeriodTypeServiceImpl implements ContractPeriodTypeService {

    @Autowired
    ContractPeriodTypeRepo contractPeriodTypeRepo;


    @Override
    @Cacheable(key = "#id")
    public ContractPeriodType findByIdIgnoreDelete(Long id) {
        return contractPeriodTypeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found by id: " + id));
    }
}
