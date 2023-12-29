package com.stg.service.dto.potentialcustomer;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stg.constant.Gender;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.service3rd.mbal.dto.FlexibleCommon;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
public class SearchPotentialCustomerRes {

    private Long id;
    private String fullName;
    private Gender gender;
    private String phoneNumber;
    private LocalDate createdDate;
    private String createdBy;
    
    private Integer totalRefer;
    private Integer totalDirect;
    private BigDecimal inputAmount;
    private String note;
    
    private FlexibleCommon.ReferrerInput rmInfo;
    private FlexibleCommon.ReferrerInput icInfo;
    
    private String cif;
    
    public static SearchPotentialCustomerRes of(PotentialCustomer customer) {
        SearchPotentialCustomerRes res = SearchPotentialCustomerRes.builder().id(customer.getId())
                .fullName(customer.getFullName()).gender(customer.getGender())
                .phoneNumber(customer.getPhoneNumber()).createdDate(customer.getCreatedDate().toLocalDate())
                .build();
        return res;
    }

}
