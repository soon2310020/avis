package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum PriorityType implements CodeMapperType {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private String title;

    PriorityType(String title) {
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
