package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RoleUserData implements CodeMapperType {
    ROLE_WEB_USER("ROLE_WEB_USER"),
    ROLE_REST_USER("ROLE_REST_USER");

    private String title;

    RoleUserData(String title) {
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
