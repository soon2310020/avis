package vn.com.twendie.avis.mobile.api.adapter;

import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.mobile.api.model.response.ContractDTO;

import java.util.Objects;
import java.util.function.Function;

public class ContractDTOAdapter implements Function<Contract, ContractDTO> {

    @Override
    public ContractDTO apply(Contract contract) {
        if (Objects.isNull(contract)) {
            return null;
        }

        return ContractDTO.builder()
                .id(contract.getId())
                .code(contract.getCode())
//                .driverId(Objects.nonNull(contract.getDriver()) ? contract.getDriver().getId() : null)
                .vehicle(new VehicleDTOAdapter().apply(contract.getVehicle()))
                .build();
    }

}
