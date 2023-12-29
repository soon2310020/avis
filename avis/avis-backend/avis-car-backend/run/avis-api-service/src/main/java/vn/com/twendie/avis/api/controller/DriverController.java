package vn.com.twendie.avis.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.enumtype.ForgotPasswordType;
import vn.com.twendie.avis.api.model.filter.DriverFilter;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.payload.CreateDriverPayload;
import vn.com.twendie.avis.api.model.payload.DriverSuggestionPayload;
import vn.com.twendie.avis.api.model.payload.EditDriverPayload;
import vn.com.twendie.avis.api.model.response.CreateDriverOptionsWrapper;
import vn.com.twendie.avis.api.model.response.DriverDetailDTO;
import vn.com.twendie.avis.api.repository.BranchRepo;
import vn.com.twendie.avis.api.repository.UserRepo;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.FilterService;
import vn.com.twendie.avis.api.service.ForgotPasswordService;
import vn.com.twendie.avis.api.service.UserService;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.security.annotation.RequirePermission;

import javax.validation.Valid;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final UserRepo userRepo;
    private final SpecificationBuilder<User> userSpecificationBuilder;
    private final FilterService<User, Long> userFilterService;
    private final UserService userService;
    private final BranchRepo branchRepo;
    private final ListUtils listUtils;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    public DriverController(UserRepo userRepo,
                            SpecificationBuilder<User> userSpecificationBuilder,
                            FilterService<User, Long> userFilterService,
                            UserService userService,
                            ListUtils listUtils,
                            BranchRepo branchRepo) {
        this.userRepo = userRepo;
        this.userSpecificationBuilder = userSpecificationBuilder;
        this.userFilterService = userFilterService;
        this.userService = userService;
        this.listUtils = listUtils;
        this.branchRepo = branchRepo;
    }

    @PostMapping("/filter")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> filter(@Valid @RequestBody FilterWrapper<DriverFilter> filterWrapper) {
        if(filterWrapper.getFilter() != null && filterWrapper.getFilter().getBranchId() != null){
            filterWrapper.getFilter().setBranch(branchRepo.findByIdAndDeletedFalse(filterWrapper.getFilter().getBranchId()).orElse(null));
        }
        Page<User> drivers = userFilterService.filter(userRepo, userSpecificationBuilder, filterWrapper);
        Page<DriverDetailDTO> driverDetailDTOs = listUtils.mapAll(drivers, DriverDetailDTO.class);
        return ApiResponse.success(driverDetailDTOs);
    }

    @PostMapping("/create_contract_suggestions")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getCreateContractWithDriverSuggestions(
            @RequestBody DriverSuggestionPayload payload) {
        return ApiResponse.success(userService.driverSuggestionsByName(payload, true));
    }

    @PostMapping("/create_contract_without_driver_suggestions")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getCreateContractWithoutDriverSuggestions(
            @RequestBody DriverSuggestionPayload payload) {
        return ApiResponse.success(userService.driverSuggestionsByName(payload, false));
    }

    @PostMapping("/create_driver")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> createDriver(
            @Valid @RequestBody CreateDriverPayload payload
    ) {
        userService.createDriver(payload);
        return ApiResponse.success(true);
    }

    @PutMapping("/update_driver")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> updateDriver(
            @Valid @RequestBody EditDriverPayload payload
    ) {
        userService.updateDriver(payload);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/delete_driver/{id}")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> deleteDriver(
            @PathVariable Long id
    ) {
        userService.deleteDriver(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/create_driver_options")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> getCreateDriverOptions() {
        CreateDriverOptionsWrapper wrapper = userService.getCreateDriverOptions();
        wrapper.setCode(userService.suggestDriverCode(AvisApiConstant.PrefixCode.DRIVER));
        return ApiResponse.success(wrapper);
    }

    @GetMapping("/filter_driver_options")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> getFilterDriverOptions() {
        return ApiResponse.success(userService.getFilterOptions());
    }
}
