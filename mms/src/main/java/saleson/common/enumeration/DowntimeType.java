package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum DowntimeType implements CodeMapperType {
    MAINTENANCE("Maintenance"),
    BREAKDOWN("Breakdown"),
    OTHER("Other");

    private String title;

    DowntimeType(String title) {
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
