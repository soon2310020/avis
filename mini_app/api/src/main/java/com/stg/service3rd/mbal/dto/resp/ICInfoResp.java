package com.stg.service3rd.mbal.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ICInfoResp {
    private String bpNumber;
    private String name;
    private String dob;
    private String gender;
}
