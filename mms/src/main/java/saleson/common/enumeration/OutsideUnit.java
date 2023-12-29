package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;
import saleson.common.util.StringUtils;

import java.util.Arrays;

public enum OutsideUnit implements CodeMapperType {
    PERCENTAGE("%"),
    SECOND("second");

    private String title;

    OutsideUnit(String title) {
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

    public static String convertToTitle(String title){
        if(!StringUtils.isEmpty(title)){
            if(Arrays.asList("s",SECOND.title).contains(title.trim().toLowerCase())) return SECOND.title;
            if(Arrays.asList("percentage",PERCENTAGE.title).contains(title.trim().toLowerCase())) return PERCENTAGE.title;
        }
        return title;
    }
}
