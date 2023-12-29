package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.data.enumtype.VehicleStatusEnum;

public class VehicleStatusMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return VehicleStatusEnum.valueOf(Integer.parseInt(String.valueOf(value))).getName();
    }

}
