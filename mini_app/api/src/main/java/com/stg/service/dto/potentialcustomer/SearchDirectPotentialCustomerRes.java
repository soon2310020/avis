package com.stg.service.dto.potentialcustomer;

import java.time.LocalDate;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.DirectState;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.service3rd.mbal.dto.FlexibleCommon;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
public class SearchDirectPotentialCustomerRes extends LeadStatusDto {

    private Long directId;
    private Long userComboId;
    private String userComboName;
    private String fullName;
    private DirectState directState;
    private AppStatus appStatus;
    private String phoneNumber;
    private String identificationId;
    private LocalDate createdDate;
    
    private String cif;
    private FlexibleCommon.ReferrerInput rmInfo;
    private FlexibleCommon.ReferrerInput icInfo;

    public static SearchDirectPotentialCustomerRes of(PotentialCustomerDirect direct) {
        SearchDirectPotentialCustomerRes res = SearchDirectPotentialCustomerRes.builder().directId(direct.getId())
                .fullName(direct.getPotentialCustomer().getFullName())
                .phoneNumber(direct.getPotentialCustomer().getPhoneNumber()).directState(direct.getState())
                .appStatus(direct.getAppStatus()).createdDate(direct.getCreatedDate().toLocalDate())
                .identificationId(direct.getPotentialCustomer().getIdentificationId())
                .cif(direct.getCifNumber()).build();

        res.setLeadInfo(direct.getLeadInfo());
        res.setLeadId(direct.getLeadId());
        
        if (direct.getUserCombo() != null) {
            res.setUserComboId(direct.getUserCombo().getId());
            res.setUserComboName(direct.getUserCombo().getComboName());
        }

        return res;
    }
}
