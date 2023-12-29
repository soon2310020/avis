package com.stg.service.impl.visitor.customer;

import com.stg.entity.customer.Customer;
import com.stg.entity.customer.CustomerVisitor;
import com.stg.service.dto.customer.CustomerDto;
import com.stg.utils.DateUtil;


public class ToDtoCustomerVisitor implements CustomerVisitor<CustomerDto> {

    public ToDtoCustomerVisitor() {
        // empty constructor
    }

    @Override
    public CustomerDto visit(Customer customer) {
        CustomerDto customerDto = new CustomerDto();
        setCustomerField(customer, customerDto);
        return customerDto;
    }

    private void setCustomerField(Customer customer, CustomerDto customerDto) {
        customerDto.setId(customer.getId());
        customerDto.setFullName(customer.getFullName());
        customerDto.setFullNameT24(customer.getFullNameT24());
        customerDto.setEmail(customer.getEmail());customerDto.setMbId(customer.getMbId());
        if (customer.getBirthday() != null) {
            customerDto.setBirthday(DateUtil.localDateTimeToString(DateUtil.DATE_YYYY_MM_DD_HH_MM, customer.getBirthday()));
        }
        customerDto.setIdCardType(customer.getIdCardType());
        customerDto.setSegment(customer.getSegment() != null ? customer.getSegment().getGain() : ""); // phân lợi
        customerDto.setSource(customer.getSource() == null ? "Cổng BH MB" : customer.getSource());
       if (customer.getLastUpdated()!= null){
           customerDto.setLastUpdated(DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01,customer.getLastUpdated()));
       }

        customerDto.setManagingUnit(customer.getManagingUnit());
    }

}
