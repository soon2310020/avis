package com.stg.service.dto.customer;

import com.stg.service.dto.insurance.IdentificationDetailDto;
import com.stg.service.dto.validator.MbalDateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CustomerDto {

    private Long id;
    private String fullName;
    private String fullNameT24;
    @Email
    private String email;
    @Schema(description = "Ngày sinh khách hàng")
    @NotNull
    @MbalDateTimeFormat
    private String birthday;
    @Schema(description = "ID Khách hàng bên MB")
    @NotNull
    private String mbId;
    private String identification;
    private String idCardType;
    @NotNull
    @Schema(description = "Giới tính khách hàng")
    @Pattern(regexp = "MALE|FEMALE", message = "Giới tính MALE or FEMALE")
    private String gender;
    private String nationality;
    private String job;
    private String address;
    private String phone;
    private String segment;

    @Schema(description = "Nơi cấp")
    private String idIssuedPlace;

    @Schema(description = "Ngày cấp")
    @NotNull
    @MbalDateTimeFormat
    private String identificationDate;

    private String source;

    private List<IdentificationDetailDto> identifications;

    private String provinceName;

    private String districtName;

    private String wardName;
    private String lastUpdated;

    private String managingUnit;

    public boolean isMBGroup() {
        return StringUtils.hasText(managingUnit);
    }
}
