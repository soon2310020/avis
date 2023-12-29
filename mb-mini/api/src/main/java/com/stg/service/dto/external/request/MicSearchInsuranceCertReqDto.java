package com.stg.service.dto.external.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;


@Data
@Accessors(chain = true)
public class MicSearchInsuranceCertReqDto {

    private String ma_dvi;
    private String nsd;
    private String pas;
    @Pattern(regexp = "NG_SKC")
    private String nv;
    private String kieu_hd;
    private String checksum;
    private String id_tras;


}
