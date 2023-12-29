package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ObjectType implements CodeMapperType
{
    COUNTER("Counter"),
    PART("Part"),
    TERMINAL("Terminal"),
    TOOLING("Tooling"),
    USER("User"),
    //option for item of topic
    ACCESS_FEATURE("Access Feature"),
    ACCESS_GROUP("Access Group"),
    CATEGORY("Category"),
    COMPANY("Company"),
    LOCATION("Location"),
    MACHINE("Machine")
    ;

    private String title;

    ObjectType(String title) {
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
