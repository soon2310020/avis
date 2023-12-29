package com.stg.service.dto.lead;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadSyncDto {
    @NotBlank
    private String leadId;
    @NotBlank
    private String leadStatus;
    @Valid
    private List<LeadActivitySyncDto> activities;
}
