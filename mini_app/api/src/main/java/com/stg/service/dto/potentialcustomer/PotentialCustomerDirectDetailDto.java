package com.stg.service.dto.potentialcustomer;

import java.time.LocalDate;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.DirectState;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service3rd.mbal.dto.FlexibleCommon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PotentialCustomerDirectDetailDto extends LeadStatusDto {
    private Long id;
    private PotentialCustomerDto potentialCustomer;
    private QuotationDto quotation;
    private DirectState directState;
    private Long userComboId;
    private String userComboName;
    private AppStatus appStatus;
    private LocalDate createdDate;

    private String cif;
    private FlexibleCommon.ReferrerInput rmInfo;
    private FlexibleCommon.ReferrerInput icInfo;

    public static PotentialCustomerDirectDetailDto of(PotentialCustomerDirect entity) {
        PotentialCustomerDirectDetailDto dto = new PotentialCustomerDirectDetailDto();
        dto.setId(entity.getId());
        dto.setPotentialCustomer(PotentialCustomerDto.of(entity.getPotentialCustomer()));
        dto.setDirectState(entity.getState());
        if (entity.getQuotationHeader() != null) {
            dto.setQuotation(new QuotationDto(entity.getQuotationHeader(), entity));
        }
        if (entity.getUserCombo() != null) {
            dto.setUserComboId(entity.getUserCombo().getId());
            dto.setUserComboName(entity.getUserCombo().getComboName());
        }
        dto.setAppStatus(entity.getAppStatus());
        dto.setCreatedDate(entity.getCreatedDate().toLocalDate());

        dto.setLeadInfo(entity.getLeadInfo());
        dto.setLeadId(entity.getLeadId());

        dto.setCif(entity.getCifNumber());
        
        return dto;
    }
}
