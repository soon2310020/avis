package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum OnBoardingFeature  implements CodeMapperType {
    ACTION_BAR("Action Bar"),
    TOOLING_TAB("Tooling Tab"),
    NUMBER_OF_PART("Number Of Part"),
    SHOW_WARNING_DELETE_TAB("Show Warning Delete Tab"),
    VERSION_HISTORY("Version history"),
    DATA_TABLE_TAB("Data table tab"),
    SHOW_WARNING_DELETE_NOTE("Show Warning Delete Note"),
    DATA_REQUEST_INVALID_CHECK("Data request invalid check"),
    ;

    private String title;

    OnBoardingFeature(String title) {
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
