package com.stg.service3rd.toolcrm.dto.resp.quote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DirectInfo {
    private long directId;
    private String leadId;

    private String productPackage;
    private String note;
}
