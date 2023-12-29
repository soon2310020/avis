package com.stg.service.dto.insurance;

import com.stg.service.dto.external.ControlStateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateControlStateReqDto {
    private ControlStateEnum controlState; // trạng thái đối soát

}
