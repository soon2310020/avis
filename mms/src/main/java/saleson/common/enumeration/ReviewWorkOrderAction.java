package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ReviewWorkOrderAction  implements CodeMapperType {

    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled")
    ;


    private String title;

    ReviewWorkOrderAction(String title) {
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
