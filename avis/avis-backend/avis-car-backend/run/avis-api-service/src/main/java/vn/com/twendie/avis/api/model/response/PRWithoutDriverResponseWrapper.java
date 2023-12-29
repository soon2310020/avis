package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRWithoutDriverResponseWrapper {

    @JsonProperty("diaries")
    List<PRWithoutDriverDiary> diaries;

    @JsonProperty("payment_request")
    List<PaymentRequestItemDTO> paymentRequestItemDTOS;
}
