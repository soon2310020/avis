package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RevisionObjectType implements CodeMapperType {
    USER("User", true),
    ROLE("Role", true),
    COMPANY("Company", true),
    LOCATION("Location", true),
    CATEGORY("Category", true),
    PART("Part", true),
    TERMINAL("Terminal", true),
    MOLD("Mold", true),
    COUNTER("Counter", true),
    MACHINE("Machine", true),
    PICK_LIST("Picklist", true),
    CHECKLIST_MAINTENANCE("Checklist maintenance", true),
    CHECKLIST_REJECT_RATE("Checklist reject rate", true),
    CHECKLIST_GENERAL("Checklist general", true),
    CHECKLIST_REFURBISHMENT("Checklist maintenance", true),
    CHECKLIST_DISPOSAL("Checklist disposal", true),
    CHECKLIST_QUALITY_ASSURANCE("Checklist quality assurance", true)
    ;


    private String title;
    private boolean enabled;

    RevisionObjectType(String title, boolean enabled) {
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
        return enabled;
    }
}
