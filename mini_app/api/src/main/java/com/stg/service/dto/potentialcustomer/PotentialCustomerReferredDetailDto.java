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
public class PotentialCustomerReferredDetailDto extends LeadStatusDto {
    private Long id;
    private PotentialCustomerDto potentialCustomer;
    private FlexibleCommon.Referrer referrer;
    private FlexibleCommon.ReferrerInput sale;
    private String reason;
    private PotentialCustomerReferState state;
    private AppStatus appStatus;
    
    private LocalDate createdDate;
    
    private FlexibleCommon.ReferrerInput rmInfo;
    private FlexibleCommon.ReferrerInput icInfo;

    public static PotentialCustomerReferredDetailDto of(PotentialCustomerRefer entity) {
        PotentialCustomerReferredDetailDto dto = new PotentialCustomerReferredDetailDto();
        dto.setId(entity.getId());
        dto.setPotentialCustomer(PotentialCustomerDto.of(entity.getPotentialCustomer()));
        dto.setReferrer(new FlexibleCommon.Referrer(entity.getRmCode(), null, null, null));
        FlexibleCommon.ReferrerInput sale = new FlexibleCommon.ReferrerInput();
        sale.setCode(entity.getIcCode());
        sale.setName(entity.getIcFullName());
        sale.setPhoneNumber(entity.getIcPhoneNumber());
        sale.setEmail(entity.getIcEmail());
        sale.setBranchCode(entity.getIcBranchCode());
        sale.setBranchName(entity.getIcBranchName());
        dto.setSale(sale);
        dto.setReason(entity.getReason());
        dto.setState(entity.getState());
        dto.setAppStatus(entity.getAppStatus());
        dto.setCreatedDate(entity.getCreatedDate().toLocalDate());
        dto.setLeadInfo(entity.getLeadInfo());
        dto.setLeadId(entity.getLeadId());
        return dto;
    }
}
