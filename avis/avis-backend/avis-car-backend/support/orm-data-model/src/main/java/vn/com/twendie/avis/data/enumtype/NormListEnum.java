package vn.com.twendie.avis.data.enumtype;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public enum NormListEnum {

    CONTRACT_KM_NORM("OLQ001", "Định mức km hợp đồng", "km/tháng"),
    SELF_DRIVE_NORMAL_DAY_KM_NORM("OLQ002", "Định mức km tự lái ngày thường", "km/tháng"),
    SELF_DRIVE_HOLIDAY_KM_NORM("OLQ003", "Định mức km tự lái ngày lễ", "km/tháng"),
    SELF_DRIVE_WEEKEND_KM_NORM("OLQ004", "Định mức km tự lái cuối tuần", "km/tháng"),
    FUEL_NORM("OLQ005", "Định mức nhiên liệu", "lít/100km"),
    HOLIDAY_KM_NORM("OLQ006", "Định mức km có lái ngày lễ", "km/tháng"),
    WEEKEND_KM_NORM("OLQ007", "Định mức km có lái cuối tuần", "km/tháng");

    private String code;

    private String name;

    private String unit;

    NormListEnum(String code, String name, String unit) {
        this.code = code;
        this.name = name;
        this.unit = unit;
    }

    public String code() {
        return code;
    }

    public static Set<String> codes() {
        return Arrays.stream(values())
                .map(NormListEnum::code)
                .collect(Collectors.toSet());
    }

    public static Set<String> codes(ContractTypeEnum contractType) {
        List<NormListEnum> costTypes = Collections.emptyList();
        switch (contractType) {
            case WITH_DRIVER:
                costTypes = new ArrayList<>(Arrays.asList(
                        CONTRACT_KM_NORM,
                        SELF_DRIVE_NORMAL_DAY_KM_NORM,
                        SELF_DRIVE_HOLIDAY_KM_NORM,
                        SELF_DRIVE_WEEKEND_KM_NORM,
                        FUEL_NORM,
                        HOLIDAY_KM_NORM,
                        WEEKEND_KM_NORM
                ));
                break;
            case WITHOUT_DRIVER:
                costTypes = new ArrayList<>(Arrays.asList(
                        CONTRACT_KM_NORM,
                        FUEL_NORM
                ));
                break;
        }
        return costTypes.stream()
                .map(NormListEnum::code)
                .collect(Collectors.toSet());
    }
}
