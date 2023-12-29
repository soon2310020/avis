package vn.com.twendie.avis.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.filter.VehicleFilter;
import vn.com.twendie.avis.api.model.payload.VehiclePayload;
import vn.com.twendie.avis.api.model.payload.VehicleSuggestionPayload;
import vn.com.twendie.avis.api.model.response.VehicleDetailDTO;
import vn.com.twendie.avis.api.repository.BranchRepo;
import vn.com.twendie.avis.api.repository.VehicleRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.FilterService;
import vn.com.twendie.avis.api.service.VehicleService;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.data.model.Vehicle;
import vn.com.twendie.avis.security.annotation.RequirePermission;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleRepo vehicleRepo;
    private final SpecificationBuilder<Vehicle> vehicleSpecificationBuilder;
    private final FilterService<Vehicle, Long> vehicleFilterService;
    private final VehicleService vehicleService;
    private final BranchRepo branchRepo;

    private final ListUtils listUtils;

    public VehicleController(VehicleRepo vehicleRepo,
                             SpecificationBuilder<Vehicle> vehicleSpecificationBuilder,
                             FilterService<Vehicle, Long> vehicleFilterService,
                             VehicleService vehicleService,
                             ListUtils listUtils,
                             BranchRepo branchRepo) {
        this.vehicleRepo = vehicleRepo;
        this.vehicleSpecificationBuilder = vehicleSpecificationBuilder;
        this.vehicleFilterService = vehicleFilterService;
        this.vehicleService = vehicleService;
        this.listUtils = listUtils;
        this.branchRepo = branchRepo;
    }

    @PostMapping("/filter")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> getVehicles(@RequestBody FilterWrapper<VehicleFilter> filter) {
        if(filter.getFilter() != null && filter.getFilter().getBranchId() != null){
            filter.getFilter().setBranch(branchRepo.findByIdAndDeletedFalse(filter.getFilter().getBranchId()).orElse(null));
        }
        Page<Vehicle> vehicles = vehicleFilterService.filter(vehicleRepo, vehicleSpecificationBuilder, filter);
        Page<VehicleDetailDTO> vehicleDetailDTOS = listUtils.mapAll(vehicles, VehicleDetailDTO.class);
        return ApiResponse.success(GeneralPageResponse.toResponse(vehicleDetailDTOS));
    }

    @PostMapping("/create_contract_suggestions")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getCreateContractSuggestions(@RequestBody VehicleSuggestionPayload payload) {
        return ApiResponse.success(vehicleService.vehicleSuggestionsByNumberPlate(payload));
    }

    @GetMapping("/filter_options")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> getFilterOptions() {
        return ApiResponse.success(vehicleService.getFilterOptions());
    }

    @GetMapping("/create_vehicle_options")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> getCreateOptions() {
        return ApiResponse.success(vehicleService.getCreateOptions());
    }

    @PostMapping("/create")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> createVehicle(
            @Valid @RequestBody VehiclePayload payload
    ) {
        return ApiResponse.success(vehicleService.createVehicle(payload));
    }

    @PutMapping("/update")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> updateVehicle(
            @Valid @RequestBody VehiclePayload payload
    ) {
        if (Objects.isNull(payload.getId())) {
            throw new BadRequestException("Request id is null!!!");
        }
        return ApiResponse.success(vehicleService.updateVehicle(payload));
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> deleteVehicle(
            @PathVariable("id") Long id
    ) {
        return ApiResponse.success(vehicleService.deleteVehicle(id));
    }
}
