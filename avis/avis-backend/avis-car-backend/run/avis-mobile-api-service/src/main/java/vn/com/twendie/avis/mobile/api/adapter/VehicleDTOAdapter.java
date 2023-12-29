package vn.com.twendie.avis.mobile.api.adapter;

import vn.com.twendie.avis.data.model.Vehicle;
import vn.com.twendie.avis.mobile.api.model.response.VehicleDTO;

import java.util.Objects;
import java.util.function.Function;

public class VehicleDTOAdapter implements Function<Vehicle, VehicleDTO> {

    @Override
    public VehicleDTO apply(Vehicle vehicle) {
        if (Objects.isNull(vehicle)) {
            return null;
        }

        return VehicleDTO.builder()
                .id(vehicle.getId())
                .numberPlate(vehicle.getNumberPlate())
                .build();
    }

}
