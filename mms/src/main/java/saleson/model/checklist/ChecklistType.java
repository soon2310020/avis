package saleson.model.checklist;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ChecklistType implements CodeMapperType
{
    MAINTENANCE("Maintenance"),
    REJECT_RATE("Reject Rate"),
    GENERAL("General"),
    REFURBISHMENT("Refurbishment"),
    DISPOSAL("Disposal"),
    QUALITY_ASSURANCE("Quality Assurance")
    ;

    private String title;

    ChecklistType(String title) {
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
