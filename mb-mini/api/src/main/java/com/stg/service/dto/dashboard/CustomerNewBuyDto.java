package com.stg.service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerNewBuyDto {
    private String id;
    private String firstName;
    private String lastName;
    private String packageBh;
    private Long totalFeeBh;
}
