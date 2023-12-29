package com.stg.service3rd.mbal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MbalRMDto {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String phoneNumber;
    @NotBlank
    private String email;
    @NotNull
    @Valid
    private MbalBranchDto branch;
}
