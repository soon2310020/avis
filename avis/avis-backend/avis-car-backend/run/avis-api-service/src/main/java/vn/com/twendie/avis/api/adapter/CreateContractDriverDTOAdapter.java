package vn.com.twendie.avis.api.adapter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.model.response.ContractContainedDTO;
import vn.com.twendie.avis.api.model.response.CreateContractDriverDTO;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.User;

import java.util.Objects;
import java.util.function.Function;

@Component
public class CreateContractDriverDTOAdapter implements Function<User, CreateContractDriverDTO> {

    @Override
    public CreateContractDriverDTO apply(User user) {
        Contract currentContract = user.getCurrentContract();
        CreateContractDriverDTO dto = CreateContractDriverDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .idCard(user.getIdCard())
                .mobile(user.getMobile())
                .countryCode(user.getCountryCode())
                .name(user.getName())
                .status(user.getStatus())
                .inJourney(Objects.nonNull(user.getCurrentJourneyDiaryId()))
                .build();
        dto.setContract(Objects.isNull(currentContract) ? null : ContractContainedDTO.builder()
                .id(currentContract.getId())
                .code(currentContract.getCode())
                .driverId(currentContract.getDriverId())
                .vehicleId(currentContract.getVehicleId())
                .contractType(AvisApiConstant.CONTRACT_TYPE_REVERSE
                        .get(currentContract.getContractType().getId()))
                .isLend(Objects.nonNull(user.getLendingContractId()))
                .build());
        return dto;
    }

}
