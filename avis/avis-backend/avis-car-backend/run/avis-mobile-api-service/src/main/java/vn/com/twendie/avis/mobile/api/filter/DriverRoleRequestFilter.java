package vn.com.twendie.avis.mobile.api.filter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.security.core.filter.RoleRequestFilter;

import java.util.HashSet;
import java.util.Set;

import static vn.com.twendie.avis.data.enumtype.UserRoleEnum.*;

@Component
public class DriverRoleRequestFilter extends RoleRequestFilter {

    @Override
    protected Set<String> acceptRoles() {
        return new HashSet<String>() {{
            add(DRIVER.value());
            add(SIGNATURE.value());
            add(CUSTOMER.value());
        }};
    }

}
