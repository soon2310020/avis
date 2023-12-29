package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.data.enumtype.VehicleTransmissionTypeEnum;

public class VehicleTransmissionTypeMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return VehicleTransmissionTypeEnum.valueOf(Integer.parseInt(String.valueOf(value))).getName();
    }

}
