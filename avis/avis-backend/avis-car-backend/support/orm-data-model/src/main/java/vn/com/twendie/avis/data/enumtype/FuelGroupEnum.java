package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FuelGroupEnum {

    GASOLINE(1L, "Xăng"),
    OIL(2L, "Dầu");

    private Long id;
    private String name;
}
