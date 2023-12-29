package saleson.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import saleson.common.enumeration.mapper.CodeMapperType;

@AllArgsConstructor
@Getter
@Deprecated
public enum MoldStatus implements CodeMapperType {
    IN_PRODUCTION("In Production"),
    IDLE("Idle"),
    NOT_WORKING("Inactive"),
    SENSOR_OFFLINE("Sensor Offline"),
    SENSOR_DETACHED("Sensor Detached"),
    NO_SENSOR("No Sensor"),
    ON_STANDBY("On Standby")
    ;

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getDescription() {
        return "";
    }
    @Override
    public Boolean isEnabled() {
        return true;
    }
}
