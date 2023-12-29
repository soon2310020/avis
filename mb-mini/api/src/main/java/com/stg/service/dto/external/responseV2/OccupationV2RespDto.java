package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OccupationV2RespDto {

    private Long id;
    private String nameVn;
    private String nameEng;
    private Integer groupId;
    private Boolean active;

}
