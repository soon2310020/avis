package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.RentalServiceTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.RentalServiceTypeService;
import vn.com.twendie.avis.data.model.RentalServiceType;

@Service
@CacheConfig(cacheNames = "RentalServiceType")
public class RentalServiceTypeServiceImpl implements RentalServiceTypeService {

    private final RentalServiceTypeRepo rentalServiceTypeRepo;

    public RentalServiceTypeServiceImpl(RentalServiceTypeRepo rentalServiceTypeRepo) {
        this.rentalServiceTypeRepo = rentalServiceTypeRepo;
    }

    @Override
    @Cacheable(key = "#code")
    public RentalServiceType findByCode(String code) {
        return rentalServiceTypeRepo.findByCode(code).orElseThrow(() ->
                new NotFoundException("Not found rental_service_type with code: " + code));
    }

    @Override
    @Cacheable(key = "#id")
    public RentalServiceType findByIdIgnoreDelete(Long id) {
        return rentalServiceTypeRepo.findById(id).orElseThrow(() ->
                new NotFoundException("Not found by id: " + id));
    }

}
