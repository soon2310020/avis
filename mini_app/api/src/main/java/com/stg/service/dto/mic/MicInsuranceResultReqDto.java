package com.stg.service.dto.mic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Getter
@Setter
@Accessors(chain = true)
public class MicInsuranceResultReqDto {

    private String ma_dvi;
    private String nsd;
    private String pas;
    @Pattern(regexp = "NG_SKC")
    private String nv;
    private String id_tras;
    private String kieu_hd;
    private String checksum;
    @JsonProperty("ma_sp")
    private String ma_sp;
    @NotNull
    @ApiModelProperty(notes = "ngay_hieu_luc", required = true, example = "20/01/2024")
    private String ngay_hl; // dd/mm/yyyy
    @NotNull
    @JsonProperty("ng_sinh")
    @ApiModelProperty(notes = "ngay_sinh", required = true, example = "20/01/2000")
    private String ng_sinh; // dd/mm/yyyy
    @ApiModelProperty(notes = "nhom quyen loi MIC", required = true, example = "1,2,3,4,5")
    private String nhom;
    private String so_hd_bm;
    @ApiModelProperty(notes = "gcn_miccare_dkbs", required = true)
    private FlexibleCommon.GcnMicCareDkbs gcn_miccare_dkbs;

    @ApiModelProperty(notes = "ttin_hd_bme", required = true)
    @Valid
    private FlexibleCommon.ParentContract ttin_hd_bme;

}
