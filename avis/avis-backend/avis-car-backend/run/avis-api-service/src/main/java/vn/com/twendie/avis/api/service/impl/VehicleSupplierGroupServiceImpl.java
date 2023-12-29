package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.VehicleSupplierGroupRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.VehicleSupplierGroupService;
import vn.com.twendie.avis.data.model.VehicleSupplierGroup;
import vn.com.twendie.avis.locale.config.Translator;

import java.util.List;

@Service
@CacheConfig(cacheNames = "VehicleSupplierGroup")
public class VehicleSupplierGroupServiceImpl implements VehicleSupplierGroupService {

    private final VehicleSupplierGroupRepo vehicleSupplierGroupRepo;

    public VehicleSupplierGroupServiceImpl(VehicleSupplierGroupRepo vehicleSupplierGroupRepo) {
        this.vehicleSupplierGroupRepo = vehicleSupplierGroupRepo;
    }

    @Override
    @Cacheable(key = "#id")
    public VehicleSupplierGroup findById(Long id) {
        return vehicleSupplierGroupRepo.findByIdAndDeletedFalse(id).orElseThrow(() ->
                new NotFoundException("Not found with id: " + id)
                        .displayMessage(Translator.toLocale("vehicle_supplier_group.error.not_found")));
    }

    @Override
    public List<VehicleSupplierGroup> findByDeletedFalse() {
        return vehicleSupplierGroupRepo.findByDeletedFalse();
    }
}
