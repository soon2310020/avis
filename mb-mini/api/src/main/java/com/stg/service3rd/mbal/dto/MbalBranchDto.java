package com.stg.service3rd.mbal.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MbalBranchDto {

    @NotBlank
    private String code;
    @NotBlank
    private String name;
}
