package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static com.stg.utils.CommonMessageError.MSG08;

@Data
@Accessors(chain = true)
public class MbalCustomerInfoReqDto {

    @NotNull(message = "Ngày sinh bắt buộc nhập")
    @Schema(description = "Ngày sinh khách hàng yyyy-mm-dd", example = "1991-01-01", required = true)
    private String dob;
    @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
    @NotNull(message = "Giới tính bắt buộc chọn")
    @Schema(required = true)
    private String gender;
    @Schema(required = true)
    private String occupation;
    @NotNull(message = "Mã nhóm nghề nghiệp bắt buộc nhập")
    @Schema(required = true)
    private Integer occupationClass;
    @Schema(required = true)
    private String productCode;
    @Schema(required = true)
    private String productName;
    @Pattern(regexp = "Việt Nam", message = MSG08)
    @Schema(required = true)
    private String nationality;
    @Schema(required = true)
    private String packageCode;
    @Schema(required = true)
    private String packageName;
    @NotEmpty(message = "Số điện thoại là bắt buộc, vui lòng kiểm tra lại thông tin tài khoản")
    @Schema(required = true)
    private String phone;
    @NotEmpty(message = "Email bắt buộc nhập")
    @Schema(required = true)
    private String email;
    @Schema(required = true)
    private String packageType;
    @Schema(description = "Phí bảo hiểm MBAL", required = true)
    @NotNull(message = "Phí bảo hiểm MBAL bắt buộc nhập")
    private Long insuranceFee;
    private String phoneRefer;
    private String rmCodeRefer;
    private String rmNameRefer;
    private String supporterPhone;
    private String supporterCode;
    private String supporterName;
    @NotEmpty(message = "Địa chỉ bắt buộc nhập")
    @Schema(required = true)
    private String address;
    @Schema(required = true)
    @NotEmpty(message = "Số giấy tờ cá nhân bắt buộc nhập")
    private String idCardNo;
    @Schema(required = true)
    @NotEmpty(message = "Fullname bắt buộc nhập")
    private String fullname;

    @Schema(description = "Dùng cho gen lấy GCNBH MIC, bằng mức phí bên MIC", required = true)
    private Long amount;

    @NotNull(message = "Type bắt buộc nhập")
    @Pattern(regexp = "FIX_COMBO|FREE_STYLE", message = "Type must be FREE_STYLE or FIX_COMBO")
    @Schema(description = "Loại gói bảo hiểm FIX_COMBO hoặc FREE_STYLE")
    private String type;

    @Schema(description = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
    @NotNull(message = "Nhóm gói bảo hiểm tính phí bên MIC bắt buộc nhập")
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

    @Override
    public String toString() {
        return "MbalCustomerInfoReqDto{" +
                "dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", occupation='" + occupation + '\'' +
                ", occupationClass=" + occupationClass +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", packageCode='" + packageCode + '\'' +
                ", packageName='" + packageName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", packageType='" + packageType + '\'' +
                ", insuranceFee=" + insuranceFee +
                ", phoneRefer='" + phoneRefer + '\'' +
                ", rmCodeRefer='" + rmCodeRefer + '\'' +
                ", rmNameRefer='" + rmNameRefer + '\'' +
                ", supporterPhone='" + supporterPhone + '\'' +
                ", supporterCode='" + supporterCode + '\'' +
                ", supporterName='" + supporterName + '\'' +
                ", address='" + address + '\'' +
                ", idCardNo='" + idCardNo + '\'' +
                ", fullname='" + fullname + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", nhom=" + nhom +
                ", bs1='" + bs1 + '\'' +
                ", bs2='" + bs2 + '\'' +
                ", bs3='" + bs3 + '\'' +
                ", bs4='" + bs4 + '\'' +
                '}';
    }
}
