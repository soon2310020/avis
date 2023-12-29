package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MachineAvailabilityType  implements CodeMapperType {
    DAILY_WORKING_HOUR("Daily working hour"),
    PLANNED_DOWNTIME("Planned downtime"),
    UNPLANNED_DOWNTIME("Unplanned downtime");

    private String title;

    MachineAvailabilityType(String title) {
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
