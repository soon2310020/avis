package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RoleUserTopic implements CodeMapperType {
    OWNER("owner"),
    NORMAL("normal"),
    HOST("host"),
    HOST_OWNER("host owner");

    private String title;

    RoleUserTopic(String title) {
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
