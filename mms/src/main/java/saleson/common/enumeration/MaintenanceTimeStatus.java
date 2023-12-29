package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MaintenanceTimeStatus implements CodeMapperType {
    ON_TIME("On time"),
    OVERDUE("Overdue");

    private String title;

    MaintenanceTimeStatus(String title) {
        this.title = title;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Boolean isEnabled() {
        return null;
    }
}
