package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VehicleSupplierGroupEnum {

    AVIS(1L, "VSG001", "AVIS"),
    SUBCONTRACTOR(2L, "VSG002", "Thầu phụ"),
    COLLABORATOR(3L, "VSG003", "Hợp tác kinh doanh");

    private Long id;
    private String code;
    private String name;
}
