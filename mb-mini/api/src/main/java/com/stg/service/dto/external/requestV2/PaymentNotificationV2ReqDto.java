package com.stg.service.dto.external.requestV2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentNotificationV2ReqDto {

    private String applicationNumber;
    private boolean success;
    private String note;

    private Transaction transaction;

    private Source source;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Transaction {
        private BigDecimal amount;

        private String code;

        private String description;

        private String completedAt;

        private String paymentId;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Source {

        private String phoneNumber;

        private String accountId;

        private String cif;

        private String name;

    }

}
