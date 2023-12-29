package com.stg.service.dto.external.requestV2;

import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateQuoteV2ReqDto {

    private String quotationDate;

    private CreateProcessV2ReqDto.Customer customer;

    private String productPackageCode;

    @NotNull(message = "Thông tin sale bắt buộc nhập")
    @Schema(name = "Thông tin IC")
    private Common.Sale sale;

    @Schema(description = "Thông tin để tính phí MIC và gen GCNBH")
    @NotNull(message = "Thông tin tính phí MIC là bắt buộc")
    private MiniMicRequestDto micRequestDto;

    @NotNull(message = "Type bắt buộc nhập")
    @Pattern(regexp = "FIX_COMBO|FREE_STYLE", message = "Type must be FREE_STYLE or FIX_COMBO")
    @Schema(description = "Loại gói bảo hiểm FIX_COMBO hoặc FREE_STYLE")
    private String type;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class MiniMicRequestDto {
        @Schema(description = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
        @NotNull(message = "Nhóm gói bảo hiểm tính phí bên MIC bắt buộc nhập")
        @Max(value = 5, message = "Giá trị lớn nhất là 5")
        @Min(value = 1, message = "Giá trị nhỏ nhất là 1")
        private int nhom;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String bs1;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String bs2;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String bs3;
        @Schema(required = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String bs4;
    }
}
