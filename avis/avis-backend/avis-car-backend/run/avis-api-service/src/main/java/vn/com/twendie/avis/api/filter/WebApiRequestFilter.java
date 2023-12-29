package vn.com.twendie.avis.api.filter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.security.core.filter.RoleRequestFilter;

import java.util.HashSet;
import java.util.Set;

import static vn.com.twendie.avis.data.enumtype.UserRoleEnum.*;

@Component
public class WebApiRequestFilter extends RoleRequestFilter {

    @Override
    protected Set<String> acceptRoles() {
        return new HashSet<String>() {{
            add(ADMIN.value());
            add(SALE.value());
            add(CUSTOMER.value());
            add(SUPERUSER.value());
        }};
    }

}
