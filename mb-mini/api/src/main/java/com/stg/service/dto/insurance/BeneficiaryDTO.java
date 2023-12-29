package com.stg.service.dto.insurance;

import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BeneficiaryDTO {

    private Long id;

    private String fullName;

    private String gender;

    private String email;

    private String birthday;

    private String phone;

    // giấy tờ tùy thân
    private String identification;

    private String idCardType;

    private String nationality;

    private String job;

    private String address;

    private String insuredId; // giá trị của mbal
}
