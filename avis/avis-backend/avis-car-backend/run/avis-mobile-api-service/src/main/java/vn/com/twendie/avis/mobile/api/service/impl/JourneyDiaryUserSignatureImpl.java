package vn.com.twendie.avis.mobile.api.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiarySignatureDTO;
import vn.com.twendie.avis.mobile.api.model.response.MemberCustomerDTO;
import vn.com.twendie.avis.mobile.api.repository.MemberCustomerRepo;
import vn.com.twendie.avis.mobile.api.service.*;
//import vn.com.twendie.avis.mobile.api.service.MemberCustomerService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JourneyDiaryUserSignatureImpl implements JourneyDiaryUserSignatureService {

    private ContractService contractService;

    private MemberCustomerRepo memberCustomerRepo;

    private JourneyDiarySignatureService journeyDiarySignatureService;

    private JourneyDiaryService journeyDiaryService;

    private DateUtils dateUtils;

    private MemberCustomerService memberCustomerService;

    private JourneyDiaryDailyService journeyDiaryDailyService;

    ModelMapper modelMapper = new ModelMapper();

    public JourneyDiaryUserSignatureImpl(ContractService contractService,
                                         MemberCustomerRepo memberCustomerRepo,
                                         MemberCustomerService memberCustomerService,
                                         JourneyDiaryService journeyDiaryService,
                                         JourneyDiaryDailyService journeyDiaryDailyService,
                                         DateUtils dateUtils,
                                         JourneyDiarySignatureService journeyDiarySignatureService) {
        this.contractService = contractService;
        this.memberCustomerRepo = memberCustomerRepo;
        this.journeyDiaryService = journeyDiaryService;
        this.journeyDiaryDailyService = journeyDiaryDailyService;
        this.memberCustomerService = memberCustomerService;
        this.dateUtils = dateUtils;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
    }

    @Override
    public List<MemberCustomerDTO> suggestionMember(User user, long contractId, String name) {

        Contract contract = contractService.findById(contractId);
        if (contract == null) {
            throw new BadRequestException("contract_id must not be null!!!")
                    .code(HttpStatus.BAD_REQUEST.value());
        }

        if(contract.getCustomer() == null){
            throw new NotFoundException("customer not found !!!")
                    .code(HttpStatus.NOT_FOUND.value());
        }
        Long memberCustomerId =null;
        if(contract.getMemberCustomer() != null){
            memberCustomerId = contract.getMemberCustomer().getId();
        }

        List<MemberCustomer> memberCustomers = memberCustomerRepo.findByParentIdAndNameLikeAndRoleIn(memberCustomerId, name,
                Arrays.asList(UserRoleEnum.SIGNATURE.getValue(), MemberCustomerRoleEnum.USER.getCode(), MemberCustomerRoleEnum.IGNORE.getCode()));
        if(memberCustomers.size() > 0){
            return memberCustomers.stream()
                    .map(e -> modelMapper.map(e, MemberCustomerDTO.class)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    @Override
    public JourneyDiarySignatureDTO getById(Long id){
        JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findById(id);
        return convert(journeyDiarySignature);
    }

    private JourneyDiarySignatureDTO convert(JourneyDiarySignature s){
        if (s.getJourneyDiary() == null) return null;
        JourneyDiary journeyDiary = s.getJourneyDiary();
        if (!JourneyDiaryStepEnum.FINISHED.value().equals(journeyDiary.getStep())) return null;

        JourneyDiarySignatureDTO signatureDTO = new JourneyDiarySignatureDTO();
        signatureDTO.setId(s.getId());
        signatureDTO.setTimeStart(journeyDiary.getTimeStart());
        signatureDTO.setTimeEnd(journeyDiary.getTimeEnd());
        signatureDTO.setStatus(s.getStatus() != null ? s.getStatus() : false);
        signatureDTO.setComment(s.getComment());
        List<JourneyDiaryDaily> journeyDiaryDaily = journeyDiaryDailyService.findByJourneyDiaryIdOrderByCreateAtAsc(journeyDiary.getId());

        long cost = 0L;
        StringBuilder tripItinerary = new StringBuilder();
        int totalKm = 0;
        long usedKmSelfDrive = 0;
        long usedKm = 0;
        if(journeyDiary.getKmStart() != null && journeyDiary.getKmEnd() != null){
            totalKm = journeyDiary.getKmEnd().intValue() - journeyDiary.getKmStart().intValue();
        }
        for (JourneyDiaryDaily daily : journeyDiaryDaily) {
            tripItinerary.append(daily.getTripItinerary()).append(", ");
            if (daily.getJourneyDiaryDailyCostTypes() == null) continue;
            for (JourneyDiaryDailyCostType costType : daily.getJourneyDiaryDailyCostTypes()) {
                if (JourneyDiaryCostTypeEnum.TOLLS_FEE.getCode().equals(costType.getCostType().getCode())
                        || JourneyDiaryCostTypeEnum.PARKING_FEE.getCode().equals(costType.getCostType().getCode())) {
                    cost += costType.getValue() != null ? costType.getValue().longValue() : 0;
                }
            }
            usedKmSelfDrive += daily.getUsedKmSelfDrive() != null ? daily.getUsedKmSelfDrive().intValue() : 0;
            usedKm += daily.getUsedKm() != null ? daily.getUsedKm().longValue() : 0;
        }
        if (tripItinerary.length() > 0) {
            tripItinerary.delete(tripItinerary.length() - 2, tripItinerary.length() - 1);
        }
        signatureDTO.setCost(cost);
        signatureDTO.setTotalKm(totalKm);
        signatureDTO.setSignatureImageUrl(s.getSignatureImageUrl());
        signatureDTO.setTripItinerary(tripItinerary.toString());
        signatureDTO.setUsedKmSelfDrive(usedKmSelfDrive);
        signatureDTO.setDriver(journeyDiary.getDriver() != null ? s.getJourneyDiary().getDriver().getName() : "");
        signatureDTO.setContractCode(journeyDiary.getContract() != null ? s.getJourneyDiary().getContract().getCode() : "");
        signatureDTO.setLicensePlate(journeyDiary.getVehicle() != null ? journeyDiary.getVehicle().getNumberPlate() : "");
        signatureDTO.setUsedKm(usedKm);
        return signatureDTO;
    }

    @Override
    public Page<JourneyDiarySignatureDTO> getJourneyDiarySignature(Long fromDate, Long toDate, User user, int page, int size) {
        Timestamp from = dateUtils.startOfDay(new Timestamp(fromDate));
        Timestamp to = dateUtils.endOfDay(new Timestamp(toDate));
        MemberCustomer memberCustomer = memberCustomerService.findByUserId(user.getId()).orElse(null);
        if (memberCustomer == null) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
        Page<JourneyDiarySignature> journeyDiarySignatureList = journeyDiarySignatureService
                .findByMemberCustomerIdAndTimeEndBetweenOrderByCreatedAtDesc(memberCustomer.getId(), from, to, page, size);

        List<JourneyDiarySignatureDTO> response = new ArrayList<>();

        for (JourneyDiarySignature s : journeyDiarySignatureList.getContent()) {
            JourneyDiarySignatureDTO signatureDTO = convert(s);
            if(signatureDTO != null) {
                response.add(signatureDTO);
            }
        }

        return new PageImpl<>(response, PageRequest.of(page, size), journeyDiarySignatureList.getTotalElements());

    }


}
