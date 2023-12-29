package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum DashboardGroupType implements CodeMapperType {
    ASSET_MANAGEMENT_DASHBOARD("Asset Management Dashboard", true),
    SUPPLIER_PERFORMANCE_MANAGEMENT_DASHBOARD("Supplier Performance Management Dashboard", true),
    INDIVIDUAL_DASHBOARDS("Individual Dashboards", true);


    private String title;
    private Boolean enabled;

    DashboardGroupType(String title, Boolean enabled) {
        this.title = title;
        this.enabled = enabled;
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
        return enabled;
    }

}
