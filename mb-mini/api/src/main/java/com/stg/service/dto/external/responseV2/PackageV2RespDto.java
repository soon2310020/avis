package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PackageV2RespDto {

    private String code;    // Mã gói
    private String groupName;   // Tên gói
    private String expiredDate; // Ngày hết hạn
    private Long sumAssured;    //Số tiền bảo hiểm
    private Long accidentBenefit;    //Quyền lợi bảo hiểm tử vong/thương tật.. do tai nạn
    private Long inpatientTreatmentBenefit;    //Quyền lợi bảo hiểm điều trị nội trú do ốm đau bệnh tật
    private String paymentPeriod;   // Định kì đóng phí: ONCE|ANNUAL
    private String type;   // Loại gói: ULLP_PROTECT|ULLP_INVEST|ULLP_PROTECT_HSCR
    private String insuranceFee;   // Phí bảo hiểm

}
