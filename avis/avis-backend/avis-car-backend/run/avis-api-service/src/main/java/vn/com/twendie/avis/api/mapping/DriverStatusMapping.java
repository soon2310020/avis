package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.data.enumtype.DriverStatusEnum;

public class DriverStatusMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return DriverStatusEnum.valueOf(Integer.parseInt(String.valueOf(value))).getName();
    }

}
