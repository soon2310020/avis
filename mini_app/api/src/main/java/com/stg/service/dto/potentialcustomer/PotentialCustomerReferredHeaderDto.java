package com.stg.service.dto.potentialcustomer;

import java.time.LocalDate;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.PotentialCustomerRefer;
import com.stg.entity.potentialcustomer.PotentialCustomerReferState;
import com.stg.service3rd.mbal.dto.FlexibleCommon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PotentialCustomerReferredHeaderDto extends LeadStatusDto {
    private Long id;
    private String potentialCustomerFullName;
    private String potentialCustomerPhoneNumber;
    private String potentialCustomerIdentificationId;
    private PotentialCustomerReferState state;
    private FlexibleCommon.ReferrerInput sale;
    private LocalDate createdDate;

    private AppStatus appStatus;

    private FlexibleCommon.ReferrerInput rmInfo;
    private FlexibleCommon.ReferrerInput icInfo;

    public static PotentialCustomerReferredHeaderDto of(PotentialCustomerRefer entity) {
        PotentialCustomerReferredHeaderDto dto = new PotentialCustomerReferredHeaderDto();
        dto.setId(entity.getId());
        dto.setPotentialCustomerFullName(entity.getPotentialCustomer().getFullName());
        dto.setPotentialCustomerPhoneNumber(entity.getPotentialCustomer().getPhoneNumber());
        dto.setPotentialCustomerIdentificationId(entity.getPotentialCustomer().getIdentificationId());
        dto.setState(entity.getState());
        dto.setCreatedDate(entity.getCreatedDate().toLocalDate());
        FlexibleCommon.ReferrerInput sale = new FlexibleCommon.ReferrerInput();
        sale.setName(entity.getIcFullName());
        sale.setPhoneNumber(entity.getIcPhoneNumber());
        dto.setSale(sale);
        dto.setLeadInfo(entity.getLeadInfo());
        dto.setLeadId(entity.getLeadId());
        dto.setAppStatus(entity.getAppStatus());
        return dto;
    }
}
