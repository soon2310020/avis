package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.data.enumtype.ParkingPlaceEnum;

import java.util.Objects;

public class ContractParkingMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        ParkingPlaceEnum placeEnum = ParkingPlaceEnum.valueOf(Long.parseLong(String.valueOf(value)));
        return Objects.isNull(placeEnum) ? "" : placeEnum.getName();
    }

}
