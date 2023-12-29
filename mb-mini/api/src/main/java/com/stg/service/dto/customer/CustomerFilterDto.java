package com.stg.service.dto.customer;

import lombok.Data;

@Data
public class CustomerFilterDto {
    private int page = 1;
    private int size = 10;
    private String queryName;
    private String queryInsurance;
}
