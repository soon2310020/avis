package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ReportType implements CodeMapperType {
    CAPACITY_UTILIZATION("Capacity Utilization", true),
    OEE("Overall Equipment Effectiveness", true);


    private String title;
    private boolean enabled;

    ReportType(String title, boolean enabled) {
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
        return enabled;
    }
}
