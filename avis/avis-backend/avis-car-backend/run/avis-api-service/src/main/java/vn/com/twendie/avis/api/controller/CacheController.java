package vn.com.twendie.avis.api.controller;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.security.annotation.RequirePermission;

import java.util.Objects;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PutMapping("/clear")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> clearCache() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(
                        cacheManager.getCache(cacheName)).clear());
        return ApiResponse.success(true);
    }

    @PutMapping("/clear_vehicle_and_driver_options_cache")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> clearGetOptionsCache() {
        Cache operationAdmin = cacheManager.getCache("OperationAdmin");
        if (operationAdmin != null) {
            operationAdmin.clear();
        }

        Cache unitOperator = cacheManager.getCache("UnitOperator");
        if (unitOperator != null) {
            unitOperator.clear();
        }

        Cache accountant = cacheManager.getCache("Accountant");
        if (accountant != null) {
            accountant.clear();
        }

        Cache createVehicleOptions = cacheManager.getCache("CreateVehicleOptions");
        if (createVehicleOptions != null) {
            createVehicleOptions.clear();
        }

        Cache createDriverOptions = cacheManager.getCache("CreateDriverOptions");
        if (createDriverOptions != null) {
            createDriverOptions.clear();
        }

        return ApiResponse.success(true);
    }

}
