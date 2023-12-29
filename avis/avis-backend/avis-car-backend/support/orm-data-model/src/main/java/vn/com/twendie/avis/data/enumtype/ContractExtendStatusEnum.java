package vn.com.twendie.avis.data.enumtype;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;

@Getter
public enum ContractExtendStatusEnum {

    WAITING_ASSIGN_CAR(1, "waiting_assign_car", "Đang chờ điều xe"),
    ASSIGNED_CAR(2, "assigned_car", "Đã điều xe"),
    IN_PROGRESS(3, "in_progress", "Đang diễn ra"),
    FINISHED(4, "finished", "Đã hoàn thành"),
    CANCELED(5, "canceled", "Đã huỷ"),
    LENDING_DRIVER(6, "lending_driver", "Đang cho mượn tài"),
    LENDING_VEHICLE(7, "lending_vehicle", "Đang cho mượn xe"),
    LENDING_DRIVER_AND_VEHICLE(8, "lending_driver_and_vehilce", "Đang cho mượn tài và xe");

    private final Integer code;

    private final String alias;

    private final String name;

    ContractExtendStatusEnum(int code, String alias, String name) {
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

    public static ContractExtendStatusEnum valueOf(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static ContractExtendStatusEnum parseStatus(Integer status, Boolean lendingDriver, Boolean lendingVehicle) {
        lendingDriver = ObjectUtils.defaultIfNull(lendingDriver, false);
        lendingVehicle = ObjectUtils.defaultIfNull(lendingVehicle, false);
        if (lendingDriver) {
            if (lendingVehicle) {
                return LENDING_DRIVER_AND_VEHICLE;
            } else {
                return LENDING_DRIVER;
            }
        } else {
            if (lendingVehicle) {
                return LENDING_VEHICLE;
            } else {
                return valueOf(status);
            }
        }
    }

}
