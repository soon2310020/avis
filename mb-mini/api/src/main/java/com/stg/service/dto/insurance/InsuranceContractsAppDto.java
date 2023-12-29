package com.stg.service.dto.insurance;

import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractsAppDto {

    private Long id;

    //Tên gói combo
    private String packageName;

    private String logo;

    // Tên BMBH
    private String policyholderName;

    // Trạng thái tái tục
    private String renewalStatus;

    // Trạng thái hợp đồng
    private String status;

    //Tổng phí bảo hiểm
    private String strInsuranceFee;

    // ngày tham gia
    private String createdDate;

    // ngày đến hạn (Hiển thị trước 30 ngày khi đến ngày đóng phí )
    private String dueFromDate;

    // Thời gian tái tục còn lại: x ngày (Đếm ngược 10 ngày kể từ ngày đến hạn)
    private String remainingRenewalTime;

    // Được cung cấp bởi
    private String providedBy;

    // Loại Sống vui, Sống khỏe
    private String type;

    private Constants.PackageType PackageType;

    // ngày hiệu lực
    private String inceptionDate;

    // ngày tái tục
    private String dueToDate;

    //số tiền phí BH tái tục
    private BigDecimal dueAmount;

    //Tên sản phẩm MBAL
    private String mbalProductName;

    // số HĐBH
    private String mbalPolicyNumber;

    //định kỳ đóng phí mbal
    private String mbalPeriodicFeePaymentTime;

    // Phí bảo hiểm định kỳ
    private String periodicPrem;

    // Đã thanh toán tái tục hay chưa
    private boolean isPaid;

    // Phí đóng thêm
    private String additionalFee;

    // đơn vị tiền tệ
    private String currency;

    private String agentCode;

    private String agentName;

    private String agentNum;

    private String premiumType; //Loại phí: OVERDUE: Phí quá hạn, DUE: Phí tới hạn, NDUE: Kỳ phí sắp tới (Next Due)

    private String mbalAppNo;

    private String insuredBp; // BP number của người được bảo hiểm mbal

    private String insuredName; // Tên người được bảo hiểm mbal

    private String insuredDob; // Ngày sinh người được bảo hiểm mbal

    private String phName; // Ten nguoi mua bao hiem

    public void setInceptionDate(LocalDateTime inceptionDate) {
        if (inceptionDate != null) {
            this.inceptionDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, inceptionDate);
        }
    }

    public void setInceptionDate(String inceptionDate) {
        this.inceptionDate = inceptionDate;
    }

    public void setDueToDate(LocalDateTime renewalDate) {
        if (renewalDate != null) {
            this.dueToDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, renewalDate);
        }
    }

    public void setDueToDate(String renewalDate) {
        this.dueToDate = renewalDate;
    }
}
