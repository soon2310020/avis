package com.stg.service.dto.external.request;

import com.stg.service.dto.external.requestFlexible.MicAdditionalProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static com.stg.utils.Constants.PHONE_PATTERN;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema
@Valid
public class MicParentReqDto {

    @Schema
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String fullName;

    @Schema
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String identificationNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
    private String phoneNumber;

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema
    @Valid
    private MicAdditionalProduct micRequest;
}
