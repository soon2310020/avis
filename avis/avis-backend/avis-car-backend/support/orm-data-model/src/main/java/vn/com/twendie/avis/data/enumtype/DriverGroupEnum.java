package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DriverGroupEnum {

    AVIS(1L, "avis", "AVIS"),
    SUB_CONTRACTOR(2L, "subcontractor", "Thầu phụ"),
    CUSTOMER(3L, "customer", "Khách hàng");

    private Long id;
    private String code;
    private String name;
}
