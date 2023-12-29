package com.stg.service3rd.mbal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class MicSandboxContractInfoRespDto extends MicSandboxResp {

    private ContractInfo data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContractInfo {
        private String ten;
        private String cmt;
        private String ng_sinh;
        private String mobi;
        private String tt_bm;

        private String nhom;
        private String bs1;
        private String bs2;
        private String bs3;
        private String bs4;
    }

}
