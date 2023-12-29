package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.FuelTypeRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.FuelTypeService;
import vn.com.twendie.avis.data.enumtype.FuelGroupEnum;
import vn.com.twendie.avis.data.model.FuelType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "FuelType")
public class FuelTypeServiceImpl implements FuelTypeService {

    private final FuelTypeRepo fuelTypeRepo;

    public FuelTypeServiceImpl(FuelTypeRepo fuelTypeRepo) {
        this.fuelTypeRepo = fuelTypeRepo;
    }

    @Override
    @Cacheable(key = "#code")
    public FuelType findByCode(String code) {
        return fuelTypeRepo.findByCode(code).orElseThrow(() ->
                new NotFoundException("Not found fuel_type with code: " + code));
    }

    @Override
    @Cacheable(key = "#id")
    public FuelType findByIdIgnoreDelete(Long id) {
        return fuelTypeRepo.findById(id).orElseThrow(() ->
                new NotFoundException("Not found by id: " + id));
    }

    @Override
    public List<FuelType> findByGroupId(Long groupId) {
        return fuelTypeRepo.findByFuelTypeGroupIdAndDeletedFalse(groupId);
    }

}
