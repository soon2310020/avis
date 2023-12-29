package com.stg.service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GetCardListRequest {

    private String requestType;

    private String requestNumber;

    private String customerName;

    private String phoneNumber;
}