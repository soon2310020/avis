package com.stg.service.dto.insurance;

import com.stg.utils.Constants;
import com.stg.service.dto.customer.CustomerDto;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class IllustrationTableDto {

    private String illustrationNumber;

    private CustomerDto customer;

    private Constants.PackageType packageType;

    private InsurancePackageDto insurancePackage;

    private MbalPackageDto mbalPackage;

    private MicPackageDto micPackage;

    private String creationTime;

    private String packageCode;

    private String deathNoAccidentFrom;

    private String strDeathNoAccidentFrom;

    private String deathNoAccidentTo;

    private String strDeathNoAccidentTo;

    private String deathAccident;

    private String strDeathAccident;

    private String deathAccidentYesTraffic;

    private String deathAccidentNoTraffic;

    private String supInpatientHospitalFee;

    private String insuranceFee;

    private String baseInsuranceFee;

    private String topupInsuranceFee;

    private String payFrequency;

    private String timeFrequency;

    private String hsTimeFrequency;

    private String hscrFee;

    private long amount;

    private List<PavTableDto> pavTables = new ArrayList<>();

    private String segment;

    // Tổng phí bảo hiểm MBAL + MIC
    private String mixInsuranceFee;

    private String bs1;

    private String bs2;

    private String bs3;

    private String bs4;

    private List<AdditionalProductDTO> customerAdditionalProductDTOS;

    private PrimaryInsuredDTO primaryInsuredDTO;

    private PrimaryProductDTO primaryProductDTO;

    private List<AdditionalProductDTO> primaryInsuredAdditionalProductDTOS;

    private List<AdditionalInsuredDTO> additionalAssuredOutputs;

    private List<BeneficiaryDTO> beneficiaryDTOS;

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, creationTime);
    }

}
