package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum SpecialAlertType implements CodeMapperType {
    L2("L2"),
    L1L2("L1+L2"),
    OVERDUE("Overdue"),
    UPCOMING("Upcoming"),
    UPCOMING_OVERDUE("Upcoming + Overdue"),
    MEDIUM_HIGH("Medium + High"),
    HIGH("High");

    private String title;

    SpecialAlertType(String title) {
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
