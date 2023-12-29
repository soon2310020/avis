package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.FuelTypeGroupRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.FuelTypeGroupService;
import vn.com.twendie.avis.data.model.FuelTypeGroup;
import vn.com.twendie.avis.locale.config.Translator;

import java.util.List;

@Service
@CacheConfig(cacheNames = "FuelTypeGroup")
public class FuelTypeGroupServiceImpl implements FuelTypeGroupService {

    private final FuelTypeGroupRepo fuelTypeGroupRepo;

    public FuelTypeGroupServiceImpl(FuelTypeGroupRepo fuelTypeGroupRepo) {
        this.fuelTypeGroupRepo = fuelTypeGroupRepo;
    }

    @Override
    @Cacheable(key = "#id")
    public FuelTypeGroup findById(Long id) {
        return fuelTypeGroupRepo.findByIdAndDeletedFalse(id).orElseThrow(() ->
                new NotFoundException("Not found with id: " + id)
                        .displayMessage(Translator.toLocale("fuel_group.error.not_found")));
    }

    @Override
    public List<FuelTypeGroup> findByDeletedFalse() {
        return fuelTypeGroupRepo.findByDeletedFalse();
    }
}
