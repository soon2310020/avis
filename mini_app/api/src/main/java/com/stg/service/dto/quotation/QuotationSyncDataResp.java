package com.stg.service.dto.quotation;

import com.stg.constant.quotation.QuotationState;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationSyncDataResp {
    private boolean success;
    private String message;
    private QuotationState state;
}
