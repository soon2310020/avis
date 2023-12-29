package saleson.common.enumeration.productivity;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum  CompareType  implements CodeMapperType {
    TOOL("Compare by tooling"),
    SUPPLIER("Compare by supplier"),
    TOOLMAKER("Compare by toolmaker");

    private String title;

    CompareType(String title) {
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
