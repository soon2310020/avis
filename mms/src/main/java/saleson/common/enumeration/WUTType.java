package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum  WUTType implements CodeMapperType {
    WARM_UP_TIME("Warm-up time"),
    NORMAL_TIME("Normal time"),
    ABNORMAL_TIME("Abnormal time"),
    COOL_DOWN_TIME("Cool down time"),
    DOWN_TIME("Down time"),
    CHANGE_IN_NORMAL_TIME("Change in normal time"),
    ;

    private String title;

    WUTType(String title) {
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

    public static WUTType getEnumByTitle(String title){
        for(WUTType e:WUTType.values()){
            if(e.title.equalsIgnoreCase(title)) return e;
        }
        return null;
    }
}
