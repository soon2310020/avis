package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PavImportRespDto {

    private Long id;

    private String packageCode;

    private Integer age;

    private String gender;

    // nhom nghe
    private Integer groupOccupationClass;

    // nam hop dong
    private Integer contractYear;

    //Tuổi NĐBH
    private Integer ageInsured;

    //Lãi suất minh họa - Giá trị tài khoản
    private String illustratedAccountValue;

    //Lãi suất minh họa - Giá trị hoàn lại
    private String illustratedRefundValue;

    //Lãi suất cam kết - GTTK
    private String committedAccountValue;

    //Lãi suất Cam kết - Giá trị hoàn lại
    private String committedRefundValue;

}
