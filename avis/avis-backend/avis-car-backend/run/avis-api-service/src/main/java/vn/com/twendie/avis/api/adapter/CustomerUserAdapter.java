package vn.com.twendie.avis.api.adapter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.service.UserGroupService;
import vn.com.twendie.avis.api.service.UserRoleService;
import vn.com.twendie.avis.api.util.PhoneNumberUtil;
import vn.com.twendie.avis.data.enumtype.DriverGroupEnum;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.Customer;
import vn.com.twendie.avis.data.model.User;

import java.util.Objects;
import java.util.function.BiFunction;

@Component
public class CustomerUserAdapter implements BiFunction<Customer, User, User> {

    private final UserRoleService userRoleService;
    private final UserGroupService userGroupService;

    public CustomerUserAdapter(UserRoleService userRoleService,
                               UserGroupService userGroupService) {
        this.userRoleService = userRoleService;
        this.userGroupService = userGroupService;
    }

    @Override
    public User apply(Customer customer, User existUser) {
        User.UserBuilder builder;

        if (Objects.isNull(existUser)) {
            builder = User.builder();
        } else {
            builder = existUser.toBuilder();
        }

        return builder
                .address(customer.getAddress())
                .iso2(customer.getIso2())
                .countryCode(customer.getCountryCode())
                .mobile(customer.getMobile())
                .mobileFull(PhoneNumberUtil.toMobileFullPattern(customer.getCountryCode(),
                        customer.getMobile()))
                .idCard(customer.getIdCard())
                .cardType(customer.getCardType())
                .name(customer.getName())
                .email(customer.getEmail())
                .username(customer.getCode())
                .customer(customer)
                .code(customer.getCode())
                .active(customer.isActive())
                .userRole(userRoleService.findById(UserRoleEnum.CUSTOMER.getId()))
                .userGroup(userGroupService.findById(DriverGroupEnum.CUSTOMER.getId()))
                .build();
    }
}
