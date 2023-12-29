package com.stg.service.dto.mbal;

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
