package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberCustomerRoleEnum {

    ADMIN("Admin"), USER("User"), IGNORE("Ignore");

    private final String code;

}
