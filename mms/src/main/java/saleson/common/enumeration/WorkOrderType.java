package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum WorkOrderType  implements CodeMapperType {

    GENERAL("General"),
    INSPECTION("Inspection"),
    EMERGENCY("Emergency"),
    PREVENTATIVE_MAINTENANCE("Preventative Maintenance"),
    CORRECTIVE_MAINTENANCE("Corrective Maintenance"),
    REFURBISHMENT("Refurbishment"),
    DISPOSAL("Disposal")
    ;


    private String title;

    WorkOrderType(String title) {
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
