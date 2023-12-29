package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.CustomerTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.CustomerTypeService;
import vn.com.twendie.avis.data.model.CustomerType;

@Service
@CacheConfig(cacheNames = "CustomerType")
public class CustomerTypeServiceImpl implements CustomerTypeService {

    private final CustomerTypeRepo customerTypeRepo
            ;

    public CustomerTypeServiceImpl(CustomerTypeRepo customerTypeRepo) {
        this.customerTypeRepo = customerTypeRepo;
    }

    @Override
    @Cacheable(key = "#p0", condition = "#p0 != null")
    public CustomerType findById(Long id) {
        return customerTypeRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Cannot find Customer Type with id: " + id));
    }
}
