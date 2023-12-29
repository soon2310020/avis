package com.stg.service.dto.potentialcustomer;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.stg.constant.Gender;
import com.stg.service.dto.IdentificationDto;
import com.stg.service.dto.constraint.Dob;
import com.stg.service.dto.constraint.Modulus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePotentialCustomerReq extends IdentificationDto {

    @NotNull
    private Integer occupationId;

    @NotNull
    private String fullName;

    @NotNull
    @Dob
    private LocalDate dob;

    @NotNull
    private Gender gender;

    private String email;

    private String phoneNumber;

    @NotNull
    @Min(10 * 1000 * 1000)
    @Modulus(division = 1000 * 1000, message = "Phải là bội của 1 triệu")
    private BigDecimal inputAmount;

    private CaredProductDto caredProduct;

    private String note;

    private Long id;

}
