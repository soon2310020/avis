package com.stg.service.dto.external.requestV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SendMailV2ReqDto {
    private String recipient;
    private Long customerId;

}
