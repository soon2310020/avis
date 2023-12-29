package vn.com.twendie.avis.data.enumtype;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ContractStatusEnum {

    WAITING_ASSIGN_CAR(1, "waiting_assign_car", "Đang chờ điều xe"),
    ASSIGNED_CAR(2, "assigned_car", "Đã xếp xe"),
    IN_PROGRESS(3, "in_progress", "Đang diễn ra"),
    FINISHED(4, "finished", "Đã hoàn thành"),
    CANCELED(5, "canceled", "Đã huỷ");

    private final Integer code;

    private final String alias;

    private final String name;

    ContractStatusEnum(int code, String alias, String name) {
        this.code = code;
        this.alias = alias;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public static ContractStatusEnum valueOf(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
