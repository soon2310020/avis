package com.stg.service.dto.external.responseV2;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IcInformationResp {
    private String bpNumber;
    private String name;
    private String dob;
    private String gender;
}
