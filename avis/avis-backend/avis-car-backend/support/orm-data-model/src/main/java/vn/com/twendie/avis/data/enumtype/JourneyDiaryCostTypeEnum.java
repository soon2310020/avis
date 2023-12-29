package vn.com.twendie.avis.data.enumtype;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum JourneyDiaryCostTypeEnum {

    TOLLS_FEE("CT007", "Chi phí cầu đường", "Chi phí"),
    PARKING_FEE("CT008", "Chi phí đỗ xe", "Chi phí"),
    TIRE_REPAIR_FEE("CT009", "Chi phí vá xăm lốp", "Chi phí"),
    CAR_WASH_FEE("CT010", "Chi phí rửa xe", "Chi phí"),
    NIGHT_STORAGE_FEE("CT011", "Chi phí lưu đêm", "Chi phí"),
    BREAKDOWN_FEE("CT012", "Chi phí kết thúc sớm chuyến đi", "Chi phí"),
    OTHER_FEE("CT013", "Chi phí khác", "Chi phí");

    private final String code;

    private final String name;

    private final String type;

    JourneyDiaryCostTypeEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String code() {
        return code;
    }

    public static Set<String> codes() {
        return Arrays.stream(values())
                .map(JourneyDiaryCostTypeEnum::code)
                .collect(Collectors.toSet());

    }

}
