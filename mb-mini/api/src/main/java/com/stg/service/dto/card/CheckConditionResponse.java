package com.stg.service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CheckConditionResponse {

    private String clientMessageId;
    private String errorCode;
    private List<String> errorDesc;
    private DataResp data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataResp {

        private String status; // Kết quả check điều kiện

    }
}
