package saleson.common;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum FrameType implements CodeMapperType {
    PART("PART"),
    TOOLING("TOOLING"),
    CATEGORY("CATEGORY"),
    COMPANY("COMPANY"),
    PLANT("PLANT"),
    MACHINE("MACHINE"),
    WORKORDER("WORKORDER"),
    TERMINAL("TERMINAL"),
    SENSOR("SENSOR"),
    USER("USER")
    ;
    private String title;

    FrameType(String title) {
        this.title =title;
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
