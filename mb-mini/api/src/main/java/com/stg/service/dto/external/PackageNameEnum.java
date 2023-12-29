package com.stg.service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum PackageNameEnum {

    CONFIDENT("Gói Tự Tin", "PROTECT1", "PROTECT1"), EQUANIMITY("Gói An Nhiên", "PROTECT2", "PROTECT2"),
    PEACEFUL("Gói Bình An", "PROTECT3", "PROTECT3"), HEALTHY("Gói Vui Khỏe", "PROTECT4", "PROTECT4"),
    HAPPY("Gói Hạnh Phúc", "PROTECT5", "PROTECT5"), ENJOY("Gói Tận Hưởng", "PROTECT6", "PROTECT6"),
    LUCKY("Gói Như Ý", "PROTECT7", "PROTECT7"), UR_STYLE("Gói Phong Cách", "", ""),
    BRONZE("GÓI ĐỒNG", "", ""),
    SILVER("GÓI BẠC", "", ""),
    GOLD("GÓI VÀNG", "", ""),
    PLATINUM("GÓI BẠCH KIM", "", ""),
    DIAMOND("GÓI KIM CƯƠNG", "", "");

    private String val;
    private String codeMbal;
    private String codePav;

    public static PackageNameEnum getPackageNumFromVal(String val) {
        Optional<PackageNameEnum> packageNameEnumOptional = Arrays.stream(PackageNameEnum.values()).filter(packageNameEnum ->
                packageNameEnum.getVal().equals(val)).findFirst();
        if (packageNameEnumOptional.isPresent()) {
            return packageNameEnumOptional.get();
        }
        return UR_STYLE;
    }

}
