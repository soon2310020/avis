package vn.com.twendie.avis.api.adapter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.service.UserGroupService;
import vn.com.twendie.avis.api.service.UserRoleService;
import vn.com.twendie.avis.api.util.PhoneNumberUtil;
import vn.com.twendie.avis.data.enumtype.DriverGroupEnum;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;

import java.util.Objects;
import java.util.function.BiFunction;

@Component
public class CustomerAdminMemberUserAdapter implements BiFunction<MemberCustomer, User, User> {

    private final UserRoleService userRoleService;
    private final UserGroupService userGroupService;

    public CustomerAdminMemberUserAdapter(UserRoleService userRoleService,
                                          UserGroupService userGroupService) {
        this.userRoleService = userRoleService;
        this.userGroupService = userGroupService;
    }

    @Override
    public User apply(MemberCustomer memberCustomer, User existUser) {
        User.UserBuilder builder;

        if (Objects.isNull(existUser)) {
            builder = User.builder();
        } else {
            builder = existUser.toBuilder();
        }

        return builder
                .address(memberCustomer.getCustomer().getAddress())
                .iso2(memberCustomer.getIso2())
                .countryCode(memberCustomer.getCountryCode())
                .mobile(memberCustomer.getMobile())
                .mobileFull(PhoneNumberUtil.toMobileFullPattern(memberCustomer.getCountryCode(),
                        memberCustomer.getMobile()))
                .name(memberCustomer.getName())
                .email(memberCustomer.getEmail())
                .username(memberCustomer.getCode())
                .customer(memberCustomer.getCustomer())
                .memberCustomer(memberCustomer)
                .code(memberCustomer.getCode())
                .active(memberCustomer.isActive())
                .userRole(userRoleService.findById(UserRoleEnum.CUSTOMER.getId()))
                .userGroup(userGroupService.findById(DriverGroupEnum.CUSTOMER.getId()))
                .build();
    }
}
