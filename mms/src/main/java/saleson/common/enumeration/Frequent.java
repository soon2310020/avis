package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum Frequent implements CodeMapperType {
    TEN_MINUTE("Ten Minute", true),
    HOURLY("Hourly", true),
    DAILY("Daily", true),
    WEEKLY("Weekly", true),
    MONTHLY("Monthly", true),
    QUARTERLY("Quarterly", true),
    YEARLY("Yearly", true);


    private String title;
    private boolean enabled;

    Frequent(String title, boolean enabled) {
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
