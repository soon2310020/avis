package vn.com.twendie.avis.api.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.twendie.avis.api.model.response.CustomerPaymentRequestWrapperDTO;
import vn.com.twendie.avis.api.repository.CustomerPaymentRequestRepo;
import vn.com.twendie.avis.api.repository.MemberCustomerRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.CustomerPaymentRequestService;
import vn.com.twendie.avis.api.service.JourneyDiarySignatureService;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.CustomerTypeEnum.INDIVIDUAL;

@Service
public class CustomerPaymentRequestServiceImpl implements CustomerPaymentRequestService {

    private final CustomerPaymentRequestRepo customerPaymentRequestRepo;
    private final MemberCustomerRepo memberCustomerRepo;
    private final ModelMapper modelMapper;
    private final JourneyDiarySignatureService journeyDiarySignatureService;

    public CustomerPaymentRequestServiceImpl(CustomerPaymentRequestRepo customerPaymentRequestRepo,
                                             MemberCustomerRepo memberCustomerRepo,
                                             ModelMapper modelMapper,
                                             JourneyDiarySignatureService journeyDiarySignatureService) {
        this.customerPaymentRequestRepo = customerPaymentRequestRepo;
        this.memberCustomerRepo = memberCustomerRepo;
        this.modelMapper = modelMapper;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<CustomerPaymentRequest> findAllByCustomerUser(User user) {
        Customer customer = user.getCustomer();
        if (customer.getCustomerType().getCode().equals(INDIVIDUAL.code())) {
            return customerPaymentRequestRepo
                    .findByContractCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customer.getId());
        } else {
            MemberCustomer memberCustomer = user.getMemberCustomer();
            List<MemberCustomer> children = memberCustomerRepo.findByParentId(memberCustomer.getId());
            List<Long> ids = new ArrayList<>();
            ids.add(memberCustomer.getId());
            ids.addAll(children.stream()
                    .map(MemberCustomer::getId)
                    .collect(Collectors.toList()));
            return customerPaymentRequestRepo
                    .findByContractMemberCustomerIdInAndDeletedFalseOrderByCreatedAtDesc(ids);
        }
    }

    @Override
    public CustomerPaymentRequest findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return customerPaymentRequestRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot find customer payment request with id: " + id)
                        .displayMessage(Translator.toLocale("customer_payment_request.error.not_found")));
    }

    @Override
    public CustomerPaymentRequest save(CustomerPaymentRequest customerPaymentRequest) {
        return customerPaymentRequestRepo.save(customerPaymentRequest);
    }

    @Override
    public CustomerPaymentRequestWrapperDTO convertToDTO(CustomerPaymentRequest customerPaymentRequest) {
        CustomerPaymentRequestWrapperDTO customerPaymentRequestWrapperDTO
                = modelMapper.map(customerPaymentRequest, CustomerPaymentRequestWrapperDTO.class);

        if(customerPaymentRequestWrapperDTO.getCustomerJourneyDiaryDailies() != null){
            customerPaymentRequestWrapperDTO.getCustomerJourneyDiaryDailies().forEach(e ->{
                if(e.getJourneyDiaryId() != null){
                    JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiaryId(e.getJourneyDiaryId());
                    if(journeyDiarySignature != null && journeyDiarySignature.getMemberCustomer() != null){
                        e.setCustomerDepartment(journeyDiarySignature.getMemberCustomer().getDepartment());
                        e.setCustomerNameUsed(journeyDiarySignature.getMemberCustomer().getName());
                    }
                }
            });
        }

        if(customerPaymentRequest.getMemberCustomerIds() != null){
            try{
                List<Long> ids = Arrays.stream(customerPaymentRequest.getMemberCustomerIds().split(","))
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                List<String> names = memberCustomerRepo.findNameByIdIn(ids);
                customerPaymentRequestWrapperDTO.setNameFinds(names);
            }catch (NumberFormatException e){

            }
        }
        return customerPaymentRequestWrapperDTO;
    }
}
