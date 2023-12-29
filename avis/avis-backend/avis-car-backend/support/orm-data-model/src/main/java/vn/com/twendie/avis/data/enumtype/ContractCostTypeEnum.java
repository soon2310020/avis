package vn.com.twendie.avis.data.enumtype;

import java.util.*;
import java.util.stream.Collectors;

public enum ContractCostTypeEnum {

    CAR_RENTAL_COST("CT001", "Giá cho thuê xe", "Giá dịch vụ"),

    OVERTIME_SURCHARGE("CT002", "Phụ phí ngoài giờ", "Phụ phí"),
    OVER_KM_SURCHARGE("CT003", "Phụ phí vượt KM", "Phụ phí"),
    //    SUNDAY_SURCHARGE("CT004", "Phụ phí chủ nhật", "Phụ phí"),
    HOLIDAY_SURCHARGE("CT005", "Phụ phí ngày lễ", "Phụ phí"),
    OVERNIGHT_SURCHARGE("CT006", "Phụ phí qua đêm", "Phụ phí"),
    WEEKEND_SURCHARGE("CT014", "Phụ phí cuối tuần", "Phụ phí"),
    SELF_DRIVE_NORMAL_DAY_SURCHARGE("CT015", "Phụ phí tự lái ngày thường", "Phụ phí"),
    SELF_DRIVE_HOLIDAY_SURCHARGE("CT016", "Phụ phí tự lái ngày lễ", "Phụ phí"),
    SELF_DRIVE_WEEKEND_SURCHARGE("CT017", "Phụ phí tự lái cuối tuần", "Phụ phí"),
    SELF_DRIVE_OVER_KM_SURCHARGE("CT018", "Phụ phí vượt km tự lái", "Phụ phí"),

    FUEL_COST("CT019", "Giá nhiên liệu", "Giá"),
    CURRENT_FUEL_COST("CT020", "Giá nhiên liệu hiện tại", "Giá");

    private String code;

    private String name;

    private String type;

    ContractCostTypeEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String code() {
        return code;
    }

    public String tName() {
        return name;
    }

    public String type() {
        return type;
    }

    public static Set<String> codes(ContractTypeEnum contractType) {
        List<ContractCostTypeEnum> costTypes = Collections.emptyList();
        switch (contractType) {
            case WITH_DRIVER:
                costTypes = new ArrayList<>(Arrays.asList(
                        CAR_RENTAL_COST,
                        OVERTIME_SURCHARGE,
                        OVER_KM_SURCHARGE,
                        HOLIDAY_SURCHARGE,
                        OVERNIGHT_SURCHARGE,
                        WEEKEND_SURCHARGE,
                        SELF_DRIVE_NORMAL_DAY_SURCHARGE,
                        SELF_DRIVE_HOLIDAY_SURCHARGE,
                        SELF_DRIVE_WEEKEND_SURCHARGE,
                        SELF_DRIVE_OVER_KM_SURCHARGE,
                        CURRENT_FUEL_COST
                ));
                break;
            case WITHOUT_DRIVER:
                costTypes = new ArrayList<>(Arrays.asList(
                        CAR_RENTAL_COST,
                        OVER_KM_SURCHARGE,
                        FUEL_COST
                ));
                break;
        }
        return costTypes.stream()
                .map(ContractCostTypeEnum::code)
                .collect(Collectors.toSet());
    }

}
