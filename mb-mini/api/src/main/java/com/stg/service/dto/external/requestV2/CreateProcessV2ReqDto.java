package com.stg.service.dto.external.requestV2;

import com.stg.service.dto.external.responseV2.CreateQuoteV2RespDto;
import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.stg.utils.CommonMessageError.REGEX_DATE;
import static com.stg.utils.Constants.PHONE_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateProcessV2ReqDto {

    @Valid
    private Customer customer;

    private Integer productPackageId;

    private String productPackageCode;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Customer {
        private Long id;
        private String fullName;

        @Pattern(regexp = REGEX_DATE, message = "Ngày tạo bảng minh họa, format: yyyy-MM-dd")
        private String dob;

        @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
        private String gender;

        private String email;

        private CreateQuoteV2RespDto.Address address;

        private Integer occupationId;
        // tên nghề nghiệp
        private String occupationName;

        //@Pattern(regexp = "NATIONAL_ID|CITIZEN_ID|PASSPORT|MILITARY_ID|BIRTH_CERTIFICATE|CMT|CMND", message = "Loại giấy tờ tùy thân")
        private String identificationType;

        private String identificationNumber;

        private String nationalityCode;
        // tên nước
        private String nationality;

        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        @NotBlank(message = "Thông tin ngày cấp là bắt buộc")
        private String identificationDate;

        @NotBlank(message = "Thông tin nơi cấp là bắt buộc")
        private String idIssuedPlace;

    }

}
