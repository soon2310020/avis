package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ContinentName implements CodeMapperType {
    ASIA("Asia"),
    EMEA("EMEA"),
    SOUTH_AMERICA("South America"),
    NORTH_AMERICA("North America"),
    OCEANIA("Oceania");

    private String title;

    ContinentName(String title) {
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
