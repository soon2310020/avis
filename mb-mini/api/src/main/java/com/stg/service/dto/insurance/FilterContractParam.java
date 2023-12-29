package com.stg.service.dto.insurance;

import com.stg.utils.ContractStatusType;
import com.stg.utils.DuePremiumType;
import com.stg.utils.SourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FilterContractParam {
    private String mbId;
    private String query;
    private ContractStatusType status;
    private DuePremiumType renewalStatus; // mbal
    private LocalDate dueDateFrom; // mbal
    private String dueDateTo; // mbal
    private SourceType source;
}
