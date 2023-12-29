package com.stg.service.dto.customer;

import com.stg.entity.InsuranceContract;
import com.stg.entity.customer.Customer;
import com.stg.utils.DateUtil;
import com.stg.utils.InsuranceCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.stg.service.dto.external.PackageNameEnum.UR_STYLE;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ContractDTO {
    private Long id;
    private Customer customer;
    private String nameInsurance;

    // Số BMH
    private String illustrationNumber;

    // Số HSYCBH
    private String numberHsycbh;

    // Số GCNBH
    private String micContractNum;

    // Số HSYCBH
    private String mbalAppNo;

    // Giá trị hợp đồng
    private String contractValue;

    // Đơn vị phát hành
    private String issuer;

    // Ngày phát hành
    private String releaseDate;

    //Thời hạn
    private String mbalFeePaymentTime;

    private String category;

    public ContractDTO(InsuranceContract contract) {
        this.id = contract.getId();
        this.nameInsurance = contract.getInsurancePackage() != null ? contract.getInsurancePackage().getPackageName() : UR_STYLE.getVal();
        this.illustrationNumber = "";
        this.micContractNum = contract.getMicContractNum();
        this.mbalAppNo = contract.getMbalAppNo();
        this.mbalFeePaymentTime = contract.getMbalFeePaymentTime() + " - theo MBAL";
        this.contractValue = contract.getStrInsuranceFee();
        this.issuer = contract.getInsurancePackage() != null ? contract.getInsurancePackage().getIssuer() : "MBAL + MIC";
        this.releaseDate = (DateUtil.localDateTimeToString(DateUtil.DATE_DMY, contract.getMicIssueDate()));
        this.category = contract.getInsurancePackage() != null ? contract.getInsurancePackage().getCategory() : "";
    }
}
