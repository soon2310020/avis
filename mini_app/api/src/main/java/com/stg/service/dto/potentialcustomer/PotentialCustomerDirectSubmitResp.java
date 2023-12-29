package com.stg.service.dto.potentialcustomer;

import com.stg.entity.potentialcustomer.DirectSubmitStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PotentialCustomerDirectSubmitResp {

    private DirectSubmitStatus status;

}
