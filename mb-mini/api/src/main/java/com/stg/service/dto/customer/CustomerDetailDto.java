package com.stg.service.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CustomerDetailDto {
    private Long id;

    private String mbId;

    // -fullName
    private String fullName;
    private String fullNameT24;

    //-gender
    private String gender;

    //-phone (bên mb)
    private String phone;

    //-identification (bên mb)
    private String identification;

    //-email
    @Email
    private String email;

    //-birthday
    private String birthday;

    //-nationality
    private String nationality;

    //-job (danh sách import)
    private String job;

    //-address
    private String address;

    private String idCardType;

    private String identificationDate;

    private String idIssuedPlace;

    private String managingUnit;

    public boolean isMBGroup() {
        return StringUtils.hasText(managingUnit);
    }

    private List<ContractDTO> insuranceContracts = new ArrayList<>();

    public <T> T accept(CustomerDtoVisitor<T> v) {
        return v.visit(this);
    }

}
