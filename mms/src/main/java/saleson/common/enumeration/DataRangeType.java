package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum DataRangeType implements CodeMapperType {
    BELOW("Below"), WITHIN("Within"), ABOVE("Above");

    private String title;

    DataRangeType(String title) {
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
