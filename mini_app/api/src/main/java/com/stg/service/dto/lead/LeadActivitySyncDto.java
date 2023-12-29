package com.stg.service.dto.lead;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.Instant;

@Getter
@Setter
public class LeadActivitySyncDto {
    @NotBlank
    private String action;
    @NotBlank
    private String title;
    private String leadName;
    private String customerName;
    //@NotNull
    private Instant startDate;
    //@NotNull
    private Instant endDate;
    private String note;
}
