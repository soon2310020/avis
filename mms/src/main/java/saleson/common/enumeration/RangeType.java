package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RangeType  implements CodeMapperType {
    CUSTOM_RANGE("Custom Range", true),
//    DAILY("Daily", true),
    WEEKLY("Weekly", true),
    MONTHLY("Monthly", true),
    YEARLY("Yearly", true);



    private String title;
    private boolean enabled;

    RangeType(String title, boolean enabled) {
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
