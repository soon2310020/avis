package com.stg.service;

import com.stg.entity.InsuredMbal;
import com.stg.entity.customer.Customer;
import com.stg.service.dto.customer.CustomerDto;
import com.stg.service.dto.insurance.IdentificationDetailDto;

import java.util.List;

public interface IdentificationService {

    List<IdentificationDetailDto> getIdentificationDetailDtos(InsuredMbal insuredMbal, Customer customer);

    List<IdentificationDetailDto> getIdentificationDetailCustomer(CustomerDto customerDto);
}
