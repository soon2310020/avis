package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DepartmentEnum {

    OPERATION_ADMIN(1L, "Admin điều hành"),
    UNIT_OPERATOR(2L, "Điều hành khối"),
    ACCOUNTANT(3L, "Kế toán");

    private final Long id;

    private final String name;

}
