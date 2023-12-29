package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum EndOfLifeCycleStatus implements CodeMapperType {
    DISMISS("Dismissed"),
    IN_COMMUNICATION("In Communication"),
    RESOLVE("Resolved"),
    NULL(null);

    private String title;

    EndOfLifeCycleStatus(String title) {
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
