package com.stg.service3rd.mbal.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ProductInfoDto {

    private BigDecimal ape;
    private String productPackage;
    private String note;
}
