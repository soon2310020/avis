package vn.com.twendie.avis.api.controller;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.filter.UserFilter;
import vn.com.twendie.avis.api.model.payload.CreateAdminUserPayload;
import vn.com.twendie.avis.api.model.payload.UpdateAdminUserPayload;
import vn.com.twendie.avis.api.model.response.AdminUserDTO;
import vn.com.twendie.avis.api.model.response.CreateAdminUserOptionsWrapper;
import vn.com.twendie.avis.api.repository.UserRepo;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.FilterService;
import vn.com.twendie.avis.api.service.UserService;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepo userRepo;
    private final SpecificationBuilder<User> userSpecificationBuilder;

    private final FilterService<User, Long> userFilterService;
    private final UserService userService;

    private final ListUtils listUtils;
    private final ModelMapper modelMapper;

    public UserController(UserRepo userRepo,
                          SpecificationBuilder<User> userSpecificationBuilder,
                          FilterService<User, Long> userFilterService,
                          UserService userService,
                          ListUtils listUtils,
                          ModelMapper modelMapper) {
        this.userRepo = userRepo;
        this.userSpecificationBuilder = userSpecificationBuilder;
        this.userFilterService = userFilterService;
        this.userService = userService;
        this.listUtils = listUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/create_options")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> getCreateOptions() {
        CreateAdminUserOptionsWrapper wrapper = userService.getAdminUserCreateOptions();
        wrapper.setCode(userService.suggestDriverCode(AvisApiConstant.PrefixCode.ADMIN_USER));
        return ApiResponse.success(wrapper);
    }

    @PostMapping("/filter")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> filter(@Valid @RequestBody FilterWrapper<UserFilter> filterWrapper) {
        Page<User> users = userFilterService.filter(userRepo, userSpecificationBuilder, filterWrapper);
        Page<AdminUserDTO> adminUserDTOs = listUtils.mapAll(users, AdminUserDTO.class);
        return ApiResponse.success(adminUserDTOs);
    }

    @PostMapping("/create")
    @RequirePermission(acceptedRoles = {"Superuser"})
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ApiResponse<?> create(
            @Valid @RequestBody CreateAdminUserPayload payload,
            @CurrentUser UserDetails userDetails
    ) {
        User user = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(modelMapper.map(userService.createAdminUser(payload, user),
                AdminUserDTO.class));
    }

    @PutMapping("/update")
    @RequirePermission(acceptedRoles = {"Superuser"})
    @Transactional
    public ApiResponse<?> update(
            @Valid @RequestBody UpdateAdminUserPayload payload
    ) {
        return ApiResponse.success(modelMapper.map(userService.updateAdminUser(payload),
                AdminUserDTO.class));
    }

    @PostMapping("migrate-email-phone")
    public void migrateEmailPhone(){
        List<User> users = userRepo.findAll();
        users.forEach(u ->{
            if(StringUtils.isEmpty(u.getEmail()) && u.getMemberCustomer() != null){
                u.setEmail(u.getMemberCustomer().getEmail());
            }
        });

        userRepo.saveAll(users);
    }

    @PostMapping("/update-branch-of_drivers")
    public int updateBranchOfDrivers(@RequestParam("file") MultipartFile file){
        return userService.updateBranchOfDriver(file);
    }
}
