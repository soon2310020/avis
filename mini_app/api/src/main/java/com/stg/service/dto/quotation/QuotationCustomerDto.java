package com.stg.service.dto.quotation;

import com.stg.entity.quotation.QuotationCustomer;
import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.service.dto.quotation.validation.Occupation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotationCustomerDto {
    private Long id;

    @NotBlank
    @Size(min = 4, message = "Họ tên ít nhất 4 ký tự trở lên")
    private String fullName;

    @NotNull
    private LocalDate dob;

    @NotNull
    private Gender gender;

    @Occupation
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

    public QuotationCustomerDto(QuotationCustomer customer) {
        setFullName(customer.getFullName());
        setDob(customer.getDob());
        setGender(customer.getGender());
        setOccupationId(customer.getOccupationId());
        setMarried(customer.getMarried());
        setIdentificationType(customer.getIdentificationType());
        setIdentificationId(customer.getIdentificationId());
        setPhoneNumber(customer.getPhoneNumber());
        setInsuranceAge(calculateInsuranceAge(customer.getDob(), customer.getCreatedDate()));
    }

    public int calculateInsuranceAge(LocalDate birthday, LocalDateTime startDateInsurance) {
        int insuranceAge;
        LocalDate dateInsurance = startDateInsurance.toLocalDate();
        LocalDate firstDayOfMonth = LocalDate.of(startDateInsurance.getYear(), birthday.getMonth(), 1);
        LocalDate lastDayOfMonth = firstDayOfMonth
                .withDayOfMonth(firstDayOfMonth.getMonth().length(firstDayOfMonth.isLeapYear()));
        LocalDate dboThisYear;
        if (lastDayOfMonth.getDayOfMonth() < birthday.getDayOfMonth()) {
            dboThisYear = lastDayOfMonth;
        } else {
            dboThisYear = LocalDate.of(startDateInsurance.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        }
        if (dboThisYear.isBefore(dateInsurance) || dboThisYear.isEqual(dateInsurance)) {
            insuranceAge = startDateInsurance.getYear() - birthday.getYear();
        } else {
            insuranceAge = startDateInsurance.getYear() - birthday.getYear() - 1;
        }
        return insuranceAge;
    }
}
