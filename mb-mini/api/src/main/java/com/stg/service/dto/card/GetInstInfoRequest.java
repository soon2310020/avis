package com.stg.service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetInstInfoRequest {
    private String cardId;
    private String sourceAppId;
    private String cusName;
    private String merchant;
    private String fromDate;
    private String toDate;
}
