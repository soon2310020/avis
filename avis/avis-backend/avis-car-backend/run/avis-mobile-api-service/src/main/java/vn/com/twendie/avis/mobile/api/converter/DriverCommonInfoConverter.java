package vn.com.twendie.avis.mobile.api.converter;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.data.enumtype.CommonStatusEnum;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.model.response.*;
import vn.com.twendie.avis.mobile.api.service.ContractService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryService;

import java.util.Objects;

import static vn.com.twendie.avis.data.enumtype.CommonStatusEnum.DISABLE;
import static vn.com.twendie.avis.data.enumtype.CommonStatusEnum.ENABLE;
import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.IN_PROGRESS;

@Component
public class DriverCommonInfoConverter extends AbstractConverter<User, DriverCommonInfo> {

    private final ContractService contractService;
    private final JourneyDiaryService journeyDiaryService;

    private final ModelMapper modelMapper;

    public DriverCommonInfoConverter(@Lazy ContractService contractService,
                                     @Lazy JourneyDiaryService journeyDiaryService,
                                     @Lazy ModelMapper modelMapper) {
        this.contractService = contractService;
        this.journeyDiaryService = journeyDiaryService;
        this.modelMapper = modelMapper;
    }

    @Override
    protected DriverCommonInfo convert(User user) {

        JourneyDiary journeyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());
        Contract contract = Objects.nonNull(journeyDiary) ? journeyDiary.getContract() :
                contractService.findById(user.getCurrentContractId());

        UserGroupDTO userGroupDTO = Objects.isNull(user.getUserGroup()) ? null :
                modelMapper.map(user.getUserGroup(), UserGroupDTO.class);
        ContractDTO contractDTO = Objects.isNull(contract) ? null :
                modelMapper.map(contract, ContractDTO.class);
        MemberCustomerDTO memberCustomerDTO = contractDTO != null ? contractDTO.getMemberCustomer() : null;
        if(memberCustomerDTO != null && contract.getMemberCustomer() != null){
            memberCustomerDTO.setIsAdmin(MemberCustomerRoleEnum.ADMIN.getCode().equals(contract.getMemberCustomer().getRole()));
        }
        JourneyDiaryDTO journeyDiaryDTO = Objects.isNull(journeyDiary) ? null :
                modelMapper.map(journeyDiary, JourneyDiaryDTO.class);

        CommonStatusEnum status = Objects.nonNull(journeyDiary) || (Objects.nonNull(contract)
                && IN_PROGRESS.getCode().equals(contract.getStatus()) && !contract.getVehicleIsTransferredAnother())
                ? ENABLE : DISABLE;
        String message = null;

        if (status.equals(DISABLE)) {
            if (Objects.isNull(contract)) {
                message = Translator.toLocale("contract.has_no_contract");
            } else if (contract.getVehicleIsTransferredAnother()) {
                message = Translator.toLocale("contract.contract_postpone");
            } else if (!contract.getStatus().equals(IN_PROGRESS.getCode())) {
                message = Translator.toLocale("contract.contract_not_in_progress");
            }
        }

        return DriverCommonInfo.builder()
                .id(user.getId())
                .code(user.getCode())
                .name(user.getName())
                .userGroup(userGroupDTO)
                .contract(contractDTO)
                .journeyDiary(journeyDiaryDTO)
                .status(status.value())
                .message(message)
                .build();
    }

}
