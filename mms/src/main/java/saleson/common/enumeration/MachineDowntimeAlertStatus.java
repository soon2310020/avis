package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MachineDowntimeAlertStatus implements CodeMapperType {
    DOWNTIME("Downtime"),
    REGISTERED("Registered"),
    UNCONFIRMED("Unconfirmed"),
    CONFIRMED("Confirmed");

    private String title;

    MachineDowntimeAlertStatus(String title) {
        this.title = title;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
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
