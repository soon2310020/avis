package saleson.common.enumeration.customField;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum PropertyGroup implements CodeMapperType
{
    TOOLING_BASIC("Basic Information (Static)"),
    TOOLING_PHYSICAL("Physical Information"),
    TOOLING_RUNNER_SYSTEM("Runner System Information"),
    TOOLING_COST("Cost Information"),
    SL_DEPRECIATION("Straight Line (S.L.) Depreciation"),
    UP_DEPRECIATION("Units of Production (U.P.) Depreciation"),
    TOOLING_SUPPLIER("Supplier Information"),
    TOOLING_PRODUCTION("Production Information"),
    TOOLING_MAINTENANCE("Maintenance Information"),
    TOOLING_REFURBISHMENT("Refurbishment Information"),
    TOOLING_WORK_ORDER("Work Order Information"),
//    TOOLING_PART("Part"),
    ;

    private String title;

    PropertyGroup(String title) {
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
