package vn.com.twendie.avis.api.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.filter.CustomerFilter;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.payload.EnterpriseCustomerPayload;
import vn.com.twendie.avis.api.model.payload.IndividualCustomerPayload;
import vn.com.twendie.avis.api.model.payload.MemberCustomerPayload;
import vn.com.twendie.avis.api.model.response.CustomerDTO;
import vn.com.twendie.avis.api.repository.CustomerRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.CustomerService;
import vn.com.twendie.avis.api.service.FilterService;
import vn.com.twendie.avis.api.service.MemberCustomerService;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.model.Customer;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final FilterService<Customer, Long> customerFilterService;
    private final CustomerRepo customerRepo;
    private final SpecificationBuilder<Customer> customerSpecificationBuilder;
    private final CustomerService customerService;
    private final MemberCustomerService memberCustomerService;

    private final ListUtils listUtils;
    private final ModelMapper modelMapper;

    public CustomerController(FilterService<Customer, Long> customerFilterService,
                              CustomerRepo customerRepo,
                              SpecificationBuilder<Customer> customerSpecificationBuilder,
                              CustomerService customerService,
                              MemberCustomerService memberCustomerService,
                              ListUtils listUtils,
                              ModelMapper modelMapper) {
        this.customerFilterService = customerFilterService;
        this.customerRepo = customerRepo;
        this.customerSpecificationBuilder = customerSpecificationBuilder;
        this.customerService = customerService;
        this.memberCustomerService = memberCustomerService;
        this.listUtils = listUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping(value = "/{customer_id}")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getCustomer(@PathVariable("customer_id") long customerId) {
        Customer customer = customerService.findById(customerId);
        CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
        return ApiResponse.success(customerDTO);
    }

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getCustomers(@RequestBody FilterWrapper<CustomerFilter> filter) {
        Page<Customer> customers = customerFilterService.filter(customerRepo, customerSpecificationBuilder, filter);
        Page<CustomerDTO> customerDTOs = listUtils.transform(customers, customer ->
                modelMapper.map(customer, CustomerDTO.class));
        return ApiResponse.success(GeneralPageResponse.toResponse(customerDTOs));
    }

    @PostMapping("/enterprise/create_or_update")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ApiResponse<?> createEnterPriseCustomer(
            @Valid @RequestBody EnterpriseCustomerPayload payload,
            @CurrentUser UserDetails userDetails) {
        String customerCode = null;
        List<String> userMemberCodes = null;

        if (!payload.getAdminPayload().getActive()
                && !CollectionUtils.isEmpty(payload.getUserPayloads())
                && payload.getUserPayloads().stream().anyMatch(MemberCustomerPayload::isNew)) {
            throw new BadRequestException("Cannot create user with inactive Admin")
                    .displayMessage(Translator.toLocale("member_customer.error.cannot_create_user_of_inactive_admin"));
        }

        int count = CollectionUtils.isEmpty(payload.getUserPayloads()) ? 0
                : Math.toIntExact(payload.getUserPayloads().stream()
                .filter(MemberCustomerPayload::isNew)
                .count());

        if (payload.getCustomerPayload().isNew()) {
            customerCode = customerService.suggestCustomerCode();
        }

        if (count > 0) {
            userMemberCodes = memberCustomerService
                    .generateMemberCode(MemberCustomerRoleEnum.USER.getCode(), count);
        }

        User currentUser = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(customerService.createOrUpdateEnterpriseCustomer(payload, currentUser, customerCode, userMemberCodes));
    }

    @PostMapping("/individual/create")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ApiResponse<?> createIndividualCustomer(
            @Valid @RequestBody IndividualCustomerPayload payload,
            @CurrentUser UserDetails userDetails) {
        User currentUser = ((UserPrincipal) userDetails).getUser();
        Customer customer = customerService.createIndividualCustomer(payload, currentUser);
        return ApiResponse.success(modelMapper.map(customer, CustomerDTO.class));
    }

    @PutMapping("/individual/edit")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ApiResponse<?> editIndividualCustomer(
            @Valid @RequestBody IndividualCustomerPayload payload,
            @CurrentUser UserDetails userDetails) {
        if (payload.getId() == null) {
            throw new BadRequestException("Customer id is null!!!");
        }
        User currentUser = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(customerService.updateIndividualCustomer(payload, currentUser));
    }

    @DeleteMapping("/individual/delete/{id}")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    @Transactional
    public ApiResponse<?> deleteIndividualCustomer(@PathVariable("id") Long id) {
        return ApiResponse.success(customerService.deleteIndividualCustomer(id));
    }

    @GetMapping("suggest_customer_code")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> suggestCustomerCode() {
        return ApiResponse.success(customerService.suggestCustomerCode());
    }
}
