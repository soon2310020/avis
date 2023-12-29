package com.stg.service.dto.quotation.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RmExcelInfoResp {
    private boolean success;
    private Map<Integer, String> errorData;
}
