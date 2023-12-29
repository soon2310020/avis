package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum TopicType implements CodeMapperType {
    HELP_CENTER("Help center"),
    END_lIFE_CYCLE("End life cycle");

    private String title;

    TopicType(String title) {
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
