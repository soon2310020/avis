package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRoleEnum {

    ADMIN(1L, "Admin"),
    SALE(2L, "Sale"),
    DRIVER(3L, "Driver"),
    CUSTOMER(4L, "Customer"),
    SUPERUSER(5L, "Superuser"),
    SIGNATURE(6L, "Signature");

    private final long id;

    private final String value;

    public String value() {
        return value;
    }

}
