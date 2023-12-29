package com.stg.service.dto.external.request;

import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
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
    @Schema(description = "ngay_hieu_luc", required = true, example = "20/01/2024")
    private String ngay_hl; // dd/mm/yyyy
    @NotNull
    @JsonProperty("ng_sinh")
    @Schema(description = "ngay_sinh", required = true, example = "20/01/2000")
    private String ng_sinh; // dd/mm/yyyy
    @Schema(description = "nhom quyen loi MIC", required = true, example = "1,2,3,4,5")
    private String nhom;
    private String so_hd_bm;
    @Schema(description = "gcn_miccare_dkbs", required = true)
    private Common.GcnMicCareDkbs gcn_miccare_dkbs;

    @Schema(description = "ttin_hd_bme", required = true)
    @Valid
    private Common.ParentContract ttin_hd_bme;

}
