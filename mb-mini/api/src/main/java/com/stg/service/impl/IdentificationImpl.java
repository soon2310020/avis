package com.stg.service.impl;

import com.stg.entity.Identification;
import com.stg.entity.InsuredMbal;
import com.stg.entity.customer.Customer;
import com.stg.repository.IdentificationRepository;
import com.stg.service.IdentificationService;
import com.stg.service.dto.customer.CustomerDto;
import com.stg.service.dto.insurance.IdentificationDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class IdentificationImpl implements IdentificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentificationImpl.class);
    private final IdentificationRepository identificationRepository;
    @Autowired
    private final ModelMapper mapper;

    @Override
    public List<IdentificationDetailDto> getIdentificationDetailDtos(InsuredMbal insuredMbal, Customer customer) {
        List<Identification> identificationsDb = new ArrayList<>();
        if (insuredMbal != null) {
            identificationsDb = identificationRepository.findByInsuredMbalId(insuredMbal.getId());
        }
        if (customer != null) {
            identificationsDb = identificationRepository.findByCustomerId(customer.getId());
        }
        List<IdentificationDetailDto> identificationsDto = new ArrayList<>();
        for (Identification identification : identificationsDb) {
            IdentificationDetailDto identificationDetailDto = new IdentificationDetailDto(identification);
            identificationsDto.add(identificationDetailDto);
        }
        return identificationsDto;
    }

    @Override
    public List<IdentificationDetailDto> getIdentificationDetailCustomer(CustomerDto customerDto) {
        List<IdentificationDetailDto> identificationsDto = new ArrayList<>();
        IdentificationDetailDto identificationDetailDto = new IdentificationDetailDto();
        identificationDetailDto.setType(customerDto.getIdCardType());
        identificationDetailDto.setIdentification(customerDto.getIdentification());
        identificationDetailDto.setIssuePlace(customerDto.getIdIssuedPlace());
        identificationsDto.add(identificationDetailDto);
        return identificationsDto;
    }
}
