package com.stg.service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetInstallElementResultRequest {
    private String requestId;
    private String sourceAppId;
}
