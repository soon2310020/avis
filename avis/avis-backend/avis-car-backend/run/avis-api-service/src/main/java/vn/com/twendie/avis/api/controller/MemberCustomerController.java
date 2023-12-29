package vn.com.twendie.avis.api.controller;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.filter.AdminCustomerFilter;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.filter.MemberCustomerFilter;
import vn.com.twendie.avis.api.model.response.MemberCustomerDTO;
import vn.com.twendie.avis.api.model.response.MemberCustomerDetailDTO;
import vn.com.twendie.avis.api.repository.AdminCustomerProjection;
import vn.com.twendie.avis.api.repository.MemberCustomerRepo;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.FilterService;
import vn.com.twendie.avis.api.service.MemberCustomerService;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.security.annotation.RequirePermission;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/member-customers")
public class MemberCustomerController {

    private final FilterService<MemberCustomer, Long> memberCustomerFilterService;
    private final MemberCustomerRepo memberCustomerRepo;
    private final SpecificationBuilder<MemberCustomer> memberCustomerSpecificationBuilder;
    private final MemberCustomerService memberCustomerService;

    private final ListUtils listUtils;
    private final ModelMapper modelMapper;

    public MemberCustomerController(FilterService<MemberCustomer, Long> memberCustomerFilterService,
                                    MemberCustomerRepo memberCustomerRepo,
                                    SpecificationBuilder<MemberCustomer> memberCustomerSpecificationBuilder,
                                    MemberCustomerService memberCustomerService,
                                    ListUtils listUtils,
                                    ModelMapper modelMapper) {
        this.memberCustomerFilterService = memberCustomerFilterService;
        this.memberCustomerRepo = memberCustomerRepo;
        this.memberCustomerSpecificationBuilder = memberCustomerSpecificationBuilder;
        this.memberCustomerService = memberCustomerService;
        this.listUtils = listUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{member_customer_id}")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getMemberCustomer(@PathVariable("member_customer_id") long memberCustomerId) {
        MemberCustomer memberCustomer = memberCustomerService.findById(memberCustomerId);
        MemberCustomerDetailDTO memberCustomerDetailDTO = modelMapper.map(memberCustomer, MemberCustomerDetailDTO.class);
        memberCustomerDetailDTO.setChildren(memberCustomer.getChildren().stream()
                .filter(member -> !member.isDeleted() && !MemberCustomerRoleEnum.IGNORE.getCode().equals(member.getRole()))
                .map(member -> modelMapper.map(member, MemberCustomerDTO.class))
                .collect(Collectors.toList())
        );
        return ApiResponse.success(memberCustomerDetailDTO);
    }

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getCustomers(@RequestBody FilterWrapper<MemberCustomerFilter> filter) {
        Page<MemberCustomer> memberCustomers = memberCustomerFilterService.filter(memberCustomerRepo, memberCustomerSpecificationBuilder, filter);
        Page<MemberCustomerDTO> customerDTOs = listUtils.transform(memberCustomers, memberCustomer ->
                modelMapper.map(memberCustomer, MemberCustomerDTO.class));
        return ApiResponse.success(GeneralPageResponse.toResponse(customerDTOs));
    }

    @PostMapping(value = "/filter-admin-customers", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> filterAdminCustomers(@RequestBody FilterWrapper<AdminCustomerFilter> filter) {
        Page<AdminCustomerProjection> adminCustomers = memberCustomerService.filterAdminCustomers(filter);
        return ApiResponse.success(GeneralPageResponse.toResponse(adminCustomers));
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> deleteMemberCustomer(@PathVariable("id") Long id) {
        return ApiResponse.success(memberCustomerService.deleteMemberCustomer(id));
    }

    @GetMapping("suggest_member_customer_code")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> suggestMemberCustomerCode() {
        return ApiResponse.success(memberCustomerService.suggestAdminMemberCode());
    }

    @GetMapping("suggest_user_member_customer_code")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> suggestUserMemberCode(@RequestParam(value = "count", defaultValue = "0") int count){
        return ApiResponse.success(memberCustomerService.suggestUserSignatureCode(count));
    }

    @PostMapping("migrate-user-signature")
    public void migrateUserSignature(){
        memberCustomerService.migrateUserSignature();
    }

    @GetMapping("suggestion-member-customer-ignore")
    public ApiResponse<?> suggest(@RequestParam("member_customer_id") Long memberCustomerId,
                                  @RequestParam(value = "name", defaultValue = "", required = false) String name){
        return ApiResponse.success(memberCustomerService.suggestionMemberCustomerIgnore(memberCustomerId, name));
    }

    @GetMapping("suggestion-member-customer")
    public ApiResponse<?> suggest(@RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "contract_id", required = false) Long contractId){
        return ApiResponse.success(memberCustomerService.suggestionMemberCustomer(name, contractId));
    }

}
