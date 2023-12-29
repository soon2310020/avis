package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum FrequentUsage implements CodeMapperType {
    ALL("All Rates", true),
    FREQUENTLY("Frequently", true),
    OCCASIONALLY("Occasionally", true),
    RARELY("Rarely", true),
    NEVER("Never", true);


    private String title;
    private boolean enabled;

    FrequentUsage(String title, boolean enabled) {
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
