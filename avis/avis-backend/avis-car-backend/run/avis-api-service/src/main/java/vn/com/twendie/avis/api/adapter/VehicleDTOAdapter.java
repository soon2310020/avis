package vn.com.twendie.avis.api.adapter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.model.response.ContractContainedDTO;
import vn.com.twendie.avis.api.model.response.VehicleDTO;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.Vehicle;

import java.util.Objects;
import java.util.function.Function;

@Component
public class VehicleDTOAdapter implements Function<Vehicle, VehicleDTO> {

    @Override
    public VehicleDTO apply(Vehicle vehicle) {
        Contract currentContract = vehicle.getCurrentContract();
        VehicleDTO dto = VehicleDTO.builder()
                .id(vehicle.getId())
                .color(vehicle.getColor())
                .model(vehicle.getModel())
                .numberPlate(vehicle.getNumberPlate())
                .numberSeat(vehicle.getNumberSeat())
                .type(vehicle.getType())
                .fuelTypeGroupId(vehicle.getFuelTypeGroup().getId())
                .status(vehicle.getStatus())
                .inJourney(Objects.nonNull(vehicle.getCurrentJourneyDiaryId()))
                .build();
        dto.setContract(Objects.isNull(currentContract) ? null : ContractContainedDTO.builder()
                .id(currentContract.getId())
                .code(currentContract.getCode())
                .driverId(currentContract.getDriverId())
                .vehicleId(currentContract.getVehicleId())
                .contractType(AvisApiConstant.CONTRACT_TYPE_REVERSE
                        .get(currentContract.getContractType().getId()))
                .isLend(Objects.nonNull(vehicle.getLendingContractId()))
                .build());
        return dto;
    }

}
