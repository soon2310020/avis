package com.stg.service.dto.card;

import com.stg.utils.InsurancePackageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetInstFeeRequest {
    @Schema(description = "Kỳ hạn trả góp", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Pattern(regexp = "3|6|9|12", message = "Chỉ được phép chọn kỳ hạn 3, 6, 9, 12 tháng")
    private String period;
    @Schema(description = "Giá trị giao dịch CĐTG", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String valueTransaction;
    @Schema(description = "Loại thẻ thanh toán", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String cardClass;

    @Schema(description = "Process mua bảo hiểm", required = true)
    @NotEmpty(message = "ProcessId bắt buộc nhập")
    private String processId;

    @Schema(description = "Loại gói bảo hiểm COMBO|FLEXIBLE", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private InsurancePackageType type;

    @Schema(description = "Mã khách hàng. Không bắt buộc")
    private String cif;

    @Schema(description = "Số điện thoại khách hàng. Không bắt buộc")
    private String phone;

    @Schema(description = "Tên khách hàng. Không bắt buộc")
    private String fullName;

}
