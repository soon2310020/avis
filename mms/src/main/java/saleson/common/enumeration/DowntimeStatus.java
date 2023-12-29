package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum DowntimeStatus implements CodeMapperType {
    DOWNTIME("Downtime"),
    CONFIRMED("Confirmed");

    private String title;

    DowntimeStatus(String title) {
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
