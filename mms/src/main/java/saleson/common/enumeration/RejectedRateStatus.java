package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RejectedRateStatus implements CodeMapperType {
    UNCOMPLETED("Uncompleted"),
    COMPLETED("Completed");

    private String title;

    RejectedRateStatus(String title) {
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
