package com.stg.service3rd.mbal.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OccupationResp {
    private Long id;
    private String nameVn;
    private String nameEng;
    private Integer groupId;
    private Boolean active;
}
