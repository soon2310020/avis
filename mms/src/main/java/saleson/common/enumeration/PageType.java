package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum PageType  implements CodeMapperType {
    TOOLING_DASHBOARD("Tooling"),//system node
    PART_DASHBOARD("Part"),//system node
    PART_SETTING("Part"),//system node
    TOOLING_SETTING("Tooling"),//system node
    TERMINAL_SETTING("Terminal"),//system node
    COUNTER_SETTING("Counter"),//system node
    RELOCATION_ALERT("Relocation Alert"),//system node
    TOOLING_DISCONNECTION_ALERT("Tooling Disconnection Alert"),
    TERMINAL_DISCONNECTION_ALERT("Terminal Disconnection Alert"),
    CYCLE_TIME_ALERT("Cycle Time Alert"),//system node
    DOWNTIME_ALERT("Downtime Alert"),//system node
//    UPTIME_ALERT,
//    RESET_ALERT,
    DATA_APPROVAL_ALERT("Data Approval Alert"),
//    REFURBISHMENT_ALERT,
    CHECKLIST_MAINTENANCE("Checklist maintenance"),//system node
    CHECKLIST_REJECT_RATE("Checklist Reject Rate"),//system node
    CHECKLIST_GENERAL("Checklist General"),
    CHECKLIST_REFURBISHMENT("Checklist Refurbishment"),//system node
    CHECKLIST_DISPOSAL("Checklist Disposal"),
    CHECKLIST_QUALITY_ASSURANCE("Checklist Quality Assurance"),
    MAINTENANCE_ALERT("Maintenance Alert"),//system node
    CORRECTIVE_MAINTENANCE_ALERT("Corrective Maintenance Alert"),
    MACHINE_SETTING("Machine"),//system node
    CATEGORY_SETTING("Category"),//system node
    COMPANY_SETTING("Company"),//system node
    MACHINE_DOWNTIME_ALERT("Machine Downtime Alert"),
    OEE_PART_PRODUCED("OEE Part Produced"),
    DETACHMENT_ALERT("Detachment Alert"),//system node
    EFFICIENCY_ALERT("Efficiency Alert"),//system node
    REJECT_RATE("Reject Rate"),
    CYCLE_TIME_DEV("Cycle Time Deviation"),
    CYCLE_TIME_FLUCTUATION("Cycle Time Fluctuation"),
// system note add more
    ACCESS_GROUP("Access Group"),
    DATA_SUBMISSION_ALERT("Data Submission Alert"),
    DISCONNECTION_ALERT("Disconnection Alert"),
    LOCATION("Location"),
    REFURBISHMENT_ALERT("Refurbishment Alert"),
    RESET("Reset"),
    RESET_ALERT("Reset Alert"),
    UPTIME_ALERT("Uptime Alert"),
    USER("User"),
    SUPPORT_CENTER("Support Center"),
    END_OF_LIFE_CYCLE("End of Life Cycle"),
    CAPACITY_UTILIZATION_REPORT("Capacity Utilization Report"),
    CYCLE_TIME_REPORT("Cycle Time Report"),
    PRODUCTION_EFFICIENCY_REPORT("Production Efficiency Report"),
    CYCLE_TIME_ALERT_HISTORY("Cycle Time Alert History"),
    EFFICIENCY_ALERT_OUT_SIDE("Efficiency Alert Out Side"),
    MISCONFIGURED("misconfigured"),
    CHANGE_RELOCATION_STATUS("change relocation status"),
    WORK_ORDER("Work Order"),

    //Work Order tabs
    WORK_ORDER_ALL("Work Order - All"),
    WORK_ORDER_GENERAL("Work Order - General"),
    WORK_ORDER_MAINTENANCE("Work Order - Maintenance"),
    WORK_ORDER_EOL("Work Order - End Of Life"),
    WORK_ORDER_EMERGENCY("Work Order - Emergency"),
    WORK_ORDER_HISTORY("Work Order - History Log"),
    ;


    private String title;

    PageType(String title) {
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
