package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestItemDTO {

    @JsonIgnore
    Long id;
    @JsonProperty("name")
    String name;
    @JsonProperty("price")
    Long price;
    @JsonProperty("count")
    BigDecimal count;
    @JsonProperty("unit")
    String unit;
    @JsonProperty("total_price")
    Long totalPrice;
    @JsonProperty("from_date")
    Timestamp fromDate;
    @JsonProperty("to_date")
    Timestamp toDate;
}
