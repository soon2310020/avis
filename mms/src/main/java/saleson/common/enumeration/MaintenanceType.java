package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MaintenanceType implements CodeMapperType {
    PREVENTIVE("Preventive"),
    CORRECTIVE("Corrective");

    private String title;

    MaintenanceType(String title) {
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
