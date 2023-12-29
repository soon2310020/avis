package com.stg.service.dto.card;

import com.stg.utils.CardLabel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ListCardResponse {

    private String clientMessageId;
    private String errorCode;
    private List<String> errorDesc;
    private CardResp data;
    private List<ListCardResponse.DataCardResp> cardResps;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardResp {
        List<DataCardResp> data;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataCardResp {

        private String cardId; // ID thẻ
        private String cardNumber; // Số thẻ mask
        private String creditLimit; // Hạn mức được cấp
        private String amountAvailable; // Số dư khả dụng
        private String totalBlocked; // Số tiền mới chi tiêu gần nhất
        private String cardDateExpire; // Thời hạn thẻ hiệu lực

        private CardLabel label; // Label
    }
}
