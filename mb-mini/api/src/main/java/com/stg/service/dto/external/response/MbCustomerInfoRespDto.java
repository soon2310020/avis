package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service.dto.external.SegmentDto;
import com.stg.service.dto.insurance.InstallmentPaymentPopupDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class MbCustomerInfoRespDto {

    @JsonProperty("cif")
    private String cif;
    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("fullname")
    private String fullname;
    //@JsonProperty("fullnameT24")
    private String fullnameT24; // for dto
    @JsonIgnore
    private boolean hasMustNameAccent;

    @JsonProperty("dob")
    private String dob;
    @JsonProperty("idCardNo")
    private String idCardNo;
    @JsonProperty("mobile")
    private String mobile;
    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address; /*origin address from T24*/
    @JsonProperty("line1")
    private String line1;
    @JsonProperty("provinceName")
    private String provinceName;
    @JsonProperty("districtName")
    private String districtName;
    @JsonProperty("wardName")
    private String wardName;

    @JsonProperty("idCardType")
    private String idCardType;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("masterSessionToken")
    private String masterSessionToken;

    @JsonProperty("sector")
    private String sector;
    private SegmentDto segment;

    @JsonProperty("idCardIssuedDate")
    private String identificationDate;
    @JsonProperty("idCardIssuedPlace")
    private String idIssuedPlace;

    private OauthToken oauthToken;

    private OauthToken mbalAccessToken;

    private Long customerId;

    private Boolean isSpecial; // true: 3.0, false: 2.0

    private InstallmentPaymentPopupDTO popupData;


    private String managingUnit;
    public boolean isMBGroup() {
        return StringUtils.hasText(managingUnit);
    }

}
