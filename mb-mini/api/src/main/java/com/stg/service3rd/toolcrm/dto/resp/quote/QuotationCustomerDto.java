package com.stg.service3rd.toolcrm.dto.resp.quote;

import com.stg.utils.Gender;
import com.stg.utils.IdentificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotationCustomerDto {
    private Long id;

    @NotBlank
    //@Size(min = 4, message = "Họ tên ít nhất 4 ký tự trở lên")
    private String fullName;

    @NotNull
    private LocalDate dob;

    @NotNull
    private Gender gender;

    //@Occupation
    private Integer occupationId;

    @NotNull
    private Boolean married;

    @NotNull
    private IdentificationType identificationType;

    @Size(max = 25)
    @NotNull
    private String identificationId;

    @Size(max = 12)
    @NotNull
    private String phoneNumber;

    private Integer insuranceAge;

    public void setIdentificationNumber(String identificationId) {
        this.identificationId = identificationId;
    }

    public String getIdentificationNumber() {
        return this.identificationId;
    }
}
